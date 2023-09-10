package ru.tinkoff.service.impl;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.tinkoff.dao.AppUserDao;
import ru.tinkoff.dao.RawDataDao;
import ru.tinkoff.entity.AppUser;
import ru.tinkoff.entity.RawData;
import ru.tinkoff.entity.enums.UserState;
import ru.tinkoff.service.MainService;
import ru.tinkoff.service.ProducerService;
import ru.tinkoff.service.utils.KeyBoardUtils;

import java.util.ArrayList;
import java.util.List;


@Service
public class MainServiceImpl implements MainService {

    private final RawDataDao rawDataDao;
    private final ProducerService producerService;
    private final AppUserDao appUserDao;
    private final MeetingService meetingService;
    private final KeyBoardUtils keyBoardUtils;

    public MainServiceImpl(RawDataDao rawDataDao, ProducerService producerService, AppUserDao appUserDao, MeetingService meetingService, KeyBoardUtils keyBoardUtils) {
        this.rawDataDao = rawDataDao;
        this.producerService = producerService;
        this.appUserDao = appUserDao;
        this.meetingService = meetingService;
        this.keyBoardUtils = keyBoardUtils;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        Message message = update.getMessage();
        if (message.getText().equals("/start")) {
            findOrSaveAppUser(update);
        } else if (message.getText().equals("Записать встречу.")) {
            meetingService.startNoteMeetingProcess(update);
        } else if (!appUserDao.findAppUserByTelegramUserId(update.getMessage().getFrom().getId()).getState().equals(UserState.WAITING)) {
            meetingService.noteMeeting(update);
        }
    }

    @Override
    public void processCallbackQuery(Update update) {
        meetingService.noteProduct(update);
    }

    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        AppUser persistentAppUser = appUserDao.findAppUserByTelegramUserId(telegramUser.getId());
        if (persistentAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .build();
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId());
            sendMessage.setText("Привет! Я помогу тебе эффективно вести учет твоих встреч. \n\n" +
                    "Тинькофф, без сомнения, самый технологичный банк, но даже самые технологичные иногда ошибаются. \n\n" +
                    "Вот некоторые ключевые функции бота, которые ты можешь использовать: \n\n" +
                    "• Удобный учет встреч: Ты можете легко записывать и хранить информацию о каждой встрече, которую провел, включая дату, время и офферы, которые продал на ней.\n\n" +
                    "• Создание отчетов: Ты можешь получить статистику по своей работе за определенный отрезок времени или за конкретный день, включая процент успешно проведенных встреч и геймификацию. \n\n" +
                    "• Хранение id встреч: Я также храню id встреч, которые ты провел, у тебя всегда будет возможность быстро получить id встречи, которыю нужно оспорить в поддержке.\n\n" +
                    "К сожелению я не могу помочь тебе расчитать вознаграждение, так как эта информация является корпоративной тайной. Но ты можешь посчитать его сам, используя отчет.");
            sendMessage.setReplyMarkup(keyBoardUtils.createReplyKeyboardMarkup(2, new String[]{"Записать встречу.", "Получить статистику."}));
            producerService.producerAnswer(sendMessage);
            return appUserDao.save(transientAppUser);
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("А мы уже знакомы.");
        producerService.producerAnswer(sendMessage);
        return persistentAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder().event(update).build();
        rawDataDao.save(rawData);
    }
}
