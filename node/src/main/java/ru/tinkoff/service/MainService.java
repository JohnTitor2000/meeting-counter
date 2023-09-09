package ru.tinkoff.service;


import org.telegram.telegrambots.meta.api.objects.Update;

public interface MainService {
    void processTextMessage(Update update);

    void processCallbackQuery(Update update);
}
