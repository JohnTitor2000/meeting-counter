package ru.tinkoff.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tinkoff.entity.AppUser;

public interface AppUserDao extends JpaRepository<AppUser, Long> {
    AppUser findAppUserByTelegramUserId(Long id);

    boolean findIsGettingDataByTelegramUserId(Long telegramUserId);
}
