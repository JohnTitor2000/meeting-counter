package ru.tinkoff.mappers;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.tinkoff.entity.AppUser;

@Component
public class AppUserMapper {
    public AppUser toAppUser(Update update) {
        if (update.hasMessage()) {
            User telegramUser = update.getMessage().getFrom();
            return AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .build();
        } else {
            User telegramUser = update.getCallbackQuery().getFrom();
            return AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .build();
        }
    }
}
