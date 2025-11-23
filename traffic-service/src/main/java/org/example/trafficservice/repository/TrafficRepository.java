package org.example.trafficservice.repository;
import org.example.trafficservice.model.Traffic;
import org.springframework.data.jpa.repository.JpaRepository;

    public interface TrafficRepository extends JpaRepository<Traffic, Long> {
    }

