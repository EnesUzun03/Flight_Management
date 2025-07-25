package com.enesuzun.Repositories;

import com.enesuzun.Entity.FlightCrew;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class FlightCrewRepository implements PanacheRepository<FlightCrew> {
    // Özel sorgular ekleyebilirsin, örnek:
    public List<FlightCrew> findByCrewType(String crewType) {
        return list("crewType", crewType);
    }

    //Uçuştaki tüm personelleri çağırmak
    public List<FlightCrew> findByFlightId(Long flightId) {
        return list("flight.id", flightId);
    }

    //Uçuştaki aranan tipteki personelleri çağırmak
    public List<FlightCrew> findByFlightIdAndCrewType(Long flightId, String crewType) {
        return list("flight.id = ?1 and crewType = ?2", flightId, crewType);
    }

    //personel ismine göre arama
    public List<FlightCrew> findByCrewName(String crewName) {
        return list("crewName", crewName);
    }

    //uçuştaki personel sayısını döndürmek için
    public long countByFlightId(Long flightId) {
        return count("flight.id", flightId);
    }

}