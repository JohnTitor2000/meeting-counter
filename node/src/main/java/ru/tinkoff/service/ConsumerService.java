package ru.tinkoff.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.telegram.telegrambots.meta.api.objects.Update;

import static ru.tinkoff.RabbitQueue.CALLBACK_QUERY_UPDATE;

public interface ConsumerService {
    void consumeTextMessageUpdates(Update update);

    @RabbitListener(queues = CALLBACK_QUERY_UPDATE)
    void consumeCallBackQueryUpdates(Update update);
}
