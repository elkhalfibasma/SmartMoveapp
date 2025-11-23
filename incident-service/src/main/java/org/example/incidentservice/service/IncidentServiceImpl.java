package org.example.incidentservice.service;

import lombok.RequiredArgsConstructor;
import org.example.incidentservice.model.Incident;
import org.example.incidentservice.repository.IncidentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;

    @Override
    public List<Incident> getAll() {
        return incidentRepository.findAll();
    }

    @Override
    public Incident getById(Long id) {
        return incidentRepository.findById(id).orElse(null);
    }

    @Override
    public Incident createIncident(Incident incident) {
        incident.setTimestamp(LocalDateTime.now());
        return incidentRepository.save(incident);
    }

    @Override
    public void delete(Long id) {
        incidentRepository.deleteById(id);
    }
}

