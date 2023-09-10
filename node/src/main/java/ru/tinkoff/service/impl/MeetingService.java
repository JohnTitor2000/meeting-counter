package ru.tinkoff.service.impl;

import org.springframework.data.domain.PageRequest;
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
            String regex = "Id активности - (\\w+)";
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
                                                                                    new String[]{"DK_FIRST", "DK_REISSUE", "DK_ADDITIONAL",
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
        Meeting meeting = meetingDao.findLatestMeetingByTelegramUser(appUser, PageRequest.of(0, 1)).get(0);
        switch (callback) {
            case "DK_FIRST":
                meeting.setProduct(Product.DK_FIRST);
                meetingDao.save(meeting);
                break;
            case "KK_FIRST":
                meeting.setProduct(Product.KK_FIRST);
                meetingDao.save(meeting);
                break;
            case "DK_REISSUE":
                meeting.setProduct(Product.DK_REISSUE);
                meetingDao.save(meeting);
                break;
            case "DK_ADDITIONAL":
                meeting.setProduct(Product.DK_ADDITIONAL);
                meetingDao.save(meeting);
                break;
            case "KK_REISSUE":
                meeting.setProduct(Product.KK_REISSUE);
                meetingDao.save(meeting);
                break;
            case "KK_ADDITIONAL":
                meeting.setProduct(Product.KK_ADDITIONAL);
                meetingDao.save(meeting);
                break;
            default:
                System.out.println("А че такое");
        }
        appUser.setState(UserState.PRODUCE_OFFERS);
        appUserDao.save(appUser);

        SendMessage sendMessage = SendMessage.builder()
                .chatId(update.getCallbackQuery().getMessage().getChatId())
                .text("Записал. А что ты продал? Когда выберешь нажми \"Все\"")
                .replyMarkup(keyBoardUtils.createInlineKeyboardButtons(1,4, new String[] {"КК", "МНП", "Инвестиции", "Все"},
                                                                                             new String[] {"KK_OFFER", "MNT_OFFER", "INVESTMENT", "END_OFFERS"}))
                .build();
        producerService.producerAnswer(sendMessage);
    }

    public void noteOffers(Update update) {
        AppUser appUser = appUserDao.findAppUserByTelegramUserId(update.getCallbackQuery().getFrom().getId());
        String callback = update.getCallbackQuery().getData();
        Meeting meeting = meetingDao.findLatestMeetingByTelegramUser(appUser, PageRequest.of(0, 1)).get(0);
        if (meeting.getOffers() == null || meeting.getOffers().isBlank()) {
            meeting.setOffers("");
        }
        String offers = meeting.getOffers();
        switch (callback) {
            case "KK_OFFER":
                if (!isUniqueOffer(offers, "KK_OFFER")) {
                    sendNotUniqueMessage(update);
                    return;
                }
                offers += "KK_OFFER;";
                meeting.setOffers(offers);
                meetingDao.save(meeting);
                break;
            case "MNT_OFFER":
                if (!isUniqueOffer(offers, "MNT_OFFER")) {
                    sendNotUniqueMessage(update);
                    return;
                }
                offers += "MNT_OFFER;";
                meeting.setOffers(offers);
                meetingDao.save(meeting);
                break;
            case "INVESTMENT":
                if (!isUniqueOffer(offers, "INVESTMENT")) {
                    sendNotUniqueMessage(update);
                    return;
                }
                offers += "INVESTMENT;";
                meeting.setOffers(offers);
                meetingDao.save(meeting);
                break;
            case "END_OFFERS":
                SendMessage sendMessage = SendMessage.builder()
                        .text("Отлично, я все записал! Возвращайся, когда захочешь записать еще.")
                        .chatId(update.getCallbackQuery().getMessage().getChatId()).build();
                appUser.setState(UserState.WAITING);
                appUserDao.save(appUser);
                producerService.producerAnswer(sendMessage);
                break;
        }
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

    private boolean isUniqueOffer(String offers, String additionalOffer) {
        if (!offers.isBlank()) {
            String[] offersArray = offers.split(";");
            for (String offer : offersArray) {
                if (offer.equals(additionalOffer)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void sendNotUniqueMessage(Update update) {
        SendMessage sendMessage = SendMessage.builder()
                .text("Этот оффер я уже записал, выбери другой или нажми \"Все\".")
                .chatId(update.getCallbackQuery().getMessage().getChatId()).build();
        producerService.producerAnswer(sendMessage);
    }
}
