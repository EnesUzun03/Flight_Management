package com.enesuzun.Repositories;

import java.util.List;

import com.enesuzun.Entity.CrewType;
import com.enesuzun.Entity.Flight;
import com.enesuzun.Entity.FlightCrew;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FlightCrewRepository implements PanacheRepository<FlightCrew> {
    // PanacheRepository den extend ederek temel crud isşlemleri otomatik yapıldı
    public List<FlightCrew> findByCrewType(String crewType) {
        return list("crewType", crewType);
    }

    // Kaydet
    public void save(FlightCrew crew) {
        persist(crew);
    }

    //tüm kayıtları döndüren metot
    public List<FlightCrew> findAllCrews(){
        return listAll();
    }

    // Sil
    public boolean deleteByCrewName(String crewName) {
        return delete("crewName", crewName) > 0;
    }

    //Uçuştaki tüm personelleri çağırmak
    public List<FlightCrew> findByFlightId(Long flightId) {
        return list("flight.id", flightId);
    }

    //Uçuştaki aranan tipteki personelleri çağırmak
    public List<FlightCrew> findByFlightIdAndCrewType(Long flightId, CrewType crewType) {
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

    // FlightCrew entity'sinin tüm alanlarını güncelleyen metot
    public boolean updateFlightCrew(Long id, String crewName, CrewType crewType, Flight flight) {
        FlightCrew crew = findById(id);
        if (crew != null) {
            crew.crewName = crewName;
            crew.crewType = crewType;
            crew.flight = flight;
            return true;
        }
        return false;
    }
}