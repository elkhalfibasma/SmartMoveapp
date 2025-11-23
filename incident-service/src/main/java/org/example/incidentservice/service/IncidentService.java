package org.example.incidentservice.service;

import org.example.incidentservice.model.Incident;

import java.util.List;

public interface IncidentService {
    List<Incident> getAll();
    Incident getById(Long id);
    Incident createIncident(Incident incident);
    void delete(Long id);
}
