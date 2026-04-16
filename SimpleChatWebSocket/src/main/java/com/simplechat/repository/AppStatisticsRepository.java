package com.simplechat.repository;

import com.simplechat.entity.AppStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AppStatisticsRepository extends JpaRepository<AppStatistics, Integer> {
    Optional<AppStatistics> findByStatDate(LocalDate statDate);
}
