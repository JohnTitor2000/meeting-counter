package ru.tinkoff.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tinkoff.entity.RawData;

public interface RawDataDao extends JpaRepository<RawData, Long> {
}
