package ru.tinkoff.service.impl;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.tinkoff.dao.AppUserDao;
import ru.tinkoff.dao.MeetingDao;
import ru.tinkoff.entity.AppUser;
import ru.tinkoff.entity.Meeting;
import ru.tinkoff.entity.enums.Product;
import ru.tinkoff.entity.enums.UserState;
import ru.tinkoff.service.ProducerService;
import ru.tinkoff.service.utils.KeyBoardUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MeetingService {

    private final AppUserDao appUserDao;
    private final ProducerService producerService;
    private final MeetingDao meetingDao;
    private final KeyBoardUtils keyBoardUtils;

    public MeetingService(AppUserDao appUserDao, ProducerService producerService, MeetingDao meetingDao, KeyBoardUtils keyBoardUtils) {
        this.appUserDao = appUserDao;
        this.producerService = producerService;
        this.meetingDao = meetingDao;
        this.keyBoardUtils = keyBoardUtils;
    }

    public void noteMeeting(Update update) {
        AppUser appUser = appUserDao.findAppUserByTelegramUserId(update.getMessage().getFrom().getId());
        if (appUser.getState().equals(UserState.PRODUCE_MEETING_ID)) {
            String regex = "Id активности - (\\w+-\\d+)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(update.getMessage().getText());
            if (matcher.find()) {
                String idАctivity = matcher.group(1);
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(update.getMessage().getChatId());
                Meeting meeting = new Meeting();
                meeting.setId(idАctivity);
                meeting.setUser(appUser);
                meetingDao.save(meeting);
                sendMessage.setText("Записал id - " + idАctivity + "\nТеперь выбери продукт по которому была встреча.");

                sendMessage.setReplyMarkup(keyBoardUtils.createInlineKeyboardButtons(2, 3,
                                                                                    new String[]{"ДК Первичка", "ДК Перевыпуск", "ДК доп",
                                                                                                 "КК Первичка", "КК перевыпуск", "KK доп"},
                                                                                    new String[]{"DK_FIRST_BUTTON", "DK_REISSUE", "DK_ADDITIONAL",
                                                                                                 "KK_FIRST", "KK_REISSUE", "KK_ADDITIONAL"}));
                appUser.setState(UserState.PRODUCE_MEETING_PRODUCT);
                appUserDao.save(appUser);
                producerService.producerAnswer(sendMessage);
            } else {
                System.out.println("Id активности не найден");
            }
        }
    }

    public void noteProduct(Update update) {
        AppUser appUser = appUserDao.findAppUserByTelegramUserId(update.getCallbackQuery().getFrom().getId());
        String callback = update.getCallbackQuery().getData();
        Meeting meeting = meetingDao.findLatestMeetingByTelegramUser(appUser);
        switch (callback) {
            case "DK_FIRST_BUTTON":
                meeting.setProduct(Product.FIRST_DEBET_CARD);
                meetingDao.save(meeting);
                break;
            case "KK_FIRST_BUTTON":
                meeting.setProduct(Product.CREDIT_CARD);
                meetingDao.save(meeting);
                break;
            default:
                System.out.println("А че такое");
        }
        SendMessage sendMessage = SendMessage.builder()
                .chatId(update.getCallbackQuery().getMessage().getChatId())
                .text("Записал. А что ты продал?").build();
        producerService.producerAnswer(sendMessage);
    }

    public void startNoteMeetingProcess(Update update) {
        AppUser appUser = appUserDao.findAppUserByTelegramUserId(update.getMessage().getFrom().getId());
        appUser.setState(UserState.PRODUCE_MEETING_ID);
        appUserDao.save(appUser);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Хорошо, отправь мне встречу, как ты делаешь это обычно из мп.");
        sendMessage.setChatId(update.getMessage().getChatId());
        producerService.producerAnswer(sendMessage);
    }
}
