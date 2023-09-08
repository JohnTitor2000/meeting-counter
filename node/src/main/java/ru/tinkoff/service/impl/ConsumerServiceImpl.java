package ru.tinkoff.service.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tinkoff.service.ConsumerService;
import ru.tinkoff.service.MainService;
import ru.tinkoff.service.ProducerService;

import static ru.tinkoff.RabbitQueue.TEXT_MESSAGE_UPDATE;

@Log4j
@Service
public class ConsumerServiceImpl implements ConsumerService {

    private final MainService mainService;
    private final ProducerService producerService;



    public ConsumerServiceImpl(MainService mainService, ProducerService producerService) {
        this.mainService = mainService;
        this.producerService = producerService;
    }

    @Override
    @RabbitListener(queues = TEXT_MESSAGE_UPDATE)
    public void consumeTextMessageUpdates(Update update) {
        log.debug("NODE: Text message is received.");
        mainService.processTextMessage(update);
    }
}
