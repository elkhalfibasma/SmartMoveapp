package org.example.trafficservice.service;

import lombok.RequiredArgsConstructor;
import org.example.trafficservice.Client.TrafficTomTomClient;
import org.example.trafficservice.model.Traffic;
import org.example.trafficservice.repository.TrafficRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrafficServiceImpl implements TrafficService {

    private final TrafficRepository trafficRepository;
    private final TrafficTomTomClient tomTomClient;

    @Override
    public List<Traffic> getAllTraffic() {
        return trafficRepository.findAll();
    }

    @Override
    public Traffic saveTraffic(Traffic traffic) {
        return trafficRepository.save(traffic);
    }

    @Override
    public Traffic getTrafficFromTomTom(double latitude, double longitude) {
        // On récupère le JSON de TomTom
        String json = tomTomClient.getTomTomTraffic(latitude, longitude);

        // TODO : un jour on convertira le JSON → Traffic
        Traffic t = new Traffic();
        t.setSource("TomTom API");
        t.setRawResponse(json);   // si tu veux stocker brut
        return t;
    }
}
