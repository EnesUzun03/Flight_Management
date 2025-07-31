package com.enesuzun.Repositories;

import java.util.List;

import com.enesuzun.Entity.CrewType;
import com.enesuzun.Entity.Flight;
import com.enesuzun.Entity.FlightCrew;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped//CDI (Contexts and Dependency Injection) anotasyonu - Uygulama boyunca tek bir instance oluşturulur (Singleton pattern)
public class FlightCrewRepository implements PanacheRepository<FlightCrew> {//PanacheRepository Quarkusun sağladığı temel crud işlemlerini yapan bir sınıf
    
    public List<FlightCrew> findByCrewType(String crewType) {
        return list("crewType", crewType);//list() panache'den gelen bir metod."crewType", crewType: WHERE crewType = ? şeklinde sorgu oluşturur
    }


    // Kaydet
    public void save(FlightCrew crew) {
        persist(crew);//persist() JPA'dan gelen kayıt ışlemi yapan bir metod.
    }

    //tüm kayıtları döndüren metot
    public List<FlightCrew> findAllCrews(){
        return listAll();//panache'den gelen SELECT * FROM flight_crew sorgusuna karşılık gelir
    }

    // Sil
    public boolean deleteByCrewName(String crewName) {
        return delete("crewName", crewName) > 0;//silinen kayıt sayısını geri döndürür.
    }

    //Uçuştaki tüm personelleri çağırmak
    public List<FlightCrew> findByFlightId(Long flightId) {
        return list("flight.id", flightId); //Join yaparak flight tablosunun id'sine erişir
    }

    //Uçuştaki aranan tipteki personelleri çağırmak
    public List<FlightCrew> findByFlightIdAndCrewType(Long flightId, CrewType crewType) {
        return list("flight.id = ?1 and crewType = ?2", flightId, crewType);//iki sorgu beraber çalışır.
    }

    //personel ismine göre arama
    public List<FlightCrew> findByCrewName(String crewName) {
        return list("crewName", crewName);
    }

    //uçuştaki personel sayısını döndürmek için
    public long countByFlightId(Long flightId) {
        return count("flight.id", flightId);//SELECT COUNT(*) sorgusuna karşılık gelir
    }

    // FlightCrew entity'sinin tüm alanlarını güncelleyen metot
    //Entity'nin alanlarını direkt değiştirme (JPA dirty checking)
    //Transaction commit edildiğinde değişiklikler otomatik kaydedilir
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