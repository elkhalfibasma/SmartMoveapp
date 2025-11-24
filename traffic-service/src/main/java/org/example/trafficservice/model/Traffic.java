package org.example.trafficservice.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

    @Entity
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class Traffic {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private double latitude;
        private double longitude;

        private int congestionLevel;
        // ➕ Champs ajoutés
        private String source;

        @Column(columnDefinition = "TEXT")  // le JSON peut être long
        private String rawResponse;
    }
