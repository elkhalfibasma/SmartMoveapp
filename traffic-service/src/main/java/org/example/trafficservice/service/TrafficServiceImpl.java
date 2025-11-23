package org.example.trafficservice.service;


import lombok.RequiredArgsConstructor;
import org.example.trafficservice.model.Traffic;
import org.example.trafficservice.repository.TrafficRepository;
import org.springframework.stereotype.Service;
import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class TrafficServiceImpl implements TrafficService {

        private final TrafficRepository trafficRepository;

        @Override
        public List<Traffic>> getAllTraffic() {
            return trafficRepository.findAll();
        }

        @Override
        public Traffic saveTraffic(Traffic traffic) {
            return trafficRepository.save(traffic);
        }
    }
