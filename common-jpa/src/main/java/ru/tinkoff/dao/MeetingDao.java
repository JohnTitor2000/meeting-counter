package ru.tinkoff.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.tinkoff.entity.AppUser;
import ru.tinkoff.entity.Meeting;

import java.util.Optional;

public interface MeetingDao extends JpaRepository<Meeting, String> {
    @Query("SELECT m FROM Meeting m WHERE m.user = :user ORDER BY m.wasHeld DESC")
    Meeting findLatestMeetingByTelegramUser(@Param("user") AppUser user);
}
