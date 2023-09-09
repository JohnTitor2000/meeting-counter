package ru.tinkoff.controller;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tinkoff.service.UpdateProducer;
import ru.tinkoff.utils.MessageUtils;

import static ru.tinkoff.RabbitQueue.CALLBACK_QUERY_UPDATE;
import static ru.tinkoff.RabbitQueue.TEXT_MESSAGE_UPDATE;

@Log4j
@Component
public class UpdateController {
    private TelegramBot telegramBot;
    private final MessageUtils messageUtils;
    private final UpdateProducer updateProducer;

    public UpdateController(MessageUtils messageUtils, UpdateProducer updateProducer) {
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;
    }

    public void registerBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Received update is null");
            return;
        }
        if (update.getMessage() != null || update.getCallbackQuery() != null) {
            distributeMessagesByType(update);
        } else {
            log.error("Received unsupported message type" + update);
        }
    }

    private void distributeMessagesByType(Update update) {
        Message message = update.getMessage();
        if (update.hasMessage() && update.getMessage().hasText()) {
            processTextMessage(update);
        } else if (update.hasCallbackQuery()) {
            processCallbackQuery(update);
        } else {
            setUnsupportedMessageTypeView(update);
        }
    }



    private void setUnsupportedMessageTypeView(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessageWithText(update, "Неподдерживаемый тип сообщения.");
        setView(sendMessage);
    }

    public void setView(SendMessage sendMessage) {
        telegramBot.sendAnswerMessage(sendMessage);
    }

    private void processTextMessage(Update update) {
        updateProducer.produce(TEXT_MESSAGE_UPDATE, update);
    }
    private void processCallbackQuery(Update update) {
        updateProducer.produce(CALLBACK_QUERY_UPDATE, update);
    }
}
