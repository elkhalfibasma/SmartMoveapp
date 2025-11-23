package org.example.meteoservice.repository;

import org.example.meteoservice.entity.Meteo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeteoRepository extends JpaRepository<Meteo, Long> {
    
    Optional<Meteo> findTopByOrderByCreatedAtDesc();
    
    List<Meteo> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
    
    List<Meteo> findByConditionOrderByCreatedAtDesc(String condition);
    
    @Query("SELECT m FROM Meteo m WHERE m.createdAt >= :since ORDER BY m.createdAt DESC")
    List<Meteo> findRecentMeteo(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(m) FROM Meteo m WHERE m.condition = :condition AND m.createdAt >= :since")
    Long countByConditionSince(@Param("condition") String condition, @Param("since") LocalDateTime since);
}
