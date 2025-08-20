package com.enesuzun.Repositories;

import java.time.LocalDateTime;
import java.util.List;

import com.enesuzun.Entity.Flight;
import com.enesuzun.Entity.FlightCrew;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped//CDI (Contexts and Dependency Injection) anotasyonu - Uygulama boyunca tek bir instance oluşturulur (Singleton pattern)
public class FlightRepository implements PanacheRepository<Flight> {
    //PanacheRepository ile basit CRUD işlemleri gerçekleşiyor zaten özel sorguları yazmak gerekli
    public Flight findByFlightNumber(String flightNumber) {
        return find("flightNumber", flightNumber).firstResult();//find() Panache'nin arama metodu flightnumber=? şeklinde sorgu oluşturur
    }

    //Flight departure datetime
    public LocalDateTime findByFlightDepartureDateTime(String flightNumber) {
        return find("flightNumber", flightNumber).firstResult().getDepartureDateTime();
    }
    //Flight crew
    public List<FlightCrew> findByFlightCrewList(String flightNumber) {
        return find("flightNumber", flightNumber).firstResult().getFlightCrews();
    }

    // Kaydet
    public void save(Flight flight) {
        persist(flight); //persist() JPA'dan gelen kayıt işlemi yapan bir metod.
    }

    // Tüm uçuşları döndüren metod
    public List<Flight> findAllFlights() {
        return listAll(); //panache'den gelen SELECT * FROM flight sorgusuna karşılık gelir
    }

    // Sil - flight number'a göre
    public boolean deleteByFlightNumber(String flightNumber) {
        return delete("flightNumber", flightNumber) > 0; //silinen kayıt sayısını geri döndürür.
    }

    // Belirli tarih aralığındaki uçuşları bul
    public List<Flight> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return list("departureDateTime >= ?1 and departureDateTime <= ?2", startDate, endDate);
    }

    // Belirli bir tarihten sonraki uçuşları bul
    public List<Flight> findFlightsAfterDate(LocalDateTime date) {
        return list("departureDateTime > ?1", date);
    }

    // Belirli bir tarihten önceki uçuşları bul
    public List<Flight> findFlightsBeforeDate(LocalDateTime date) {
        return list("departureDateTime < ?1", date);
    }

    // Bugünün uçuşlarını bul
    public List<Flight> findTodaysFlights(LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return list("departureDateTime >= ?1 and departureDateTime <= ?2", startOfDay, endOfDay);
    }

    // Flight number'da belirli bir metin içeren uçuşları bul (LIKE sorgusu)
    public List<Flight> findByFlightNumberContaining(String searchText) {
        return list("flightNumber LIKE ?1", "%" + searchText + "%");
    }

    // Flight number'ın belirli bir prefix ile başladığı uçuşları bul
    public List<Flight> findByFlightNumberStartingWith(String prefix) {
        return list("flightNumber LIKE ?1", prefix + "%");
    }

    // Crew sayısına göre uçuşları bul
    public List<Flight> findFlightsWithMinimumCrewCount(int minCrewCount) {
        return find("SELECT f FROM Flight f WHERE SIZE(f.flightCrews) >= ?1", minCrewCount).list();
    }

    // Crew'suz uçuşları bul
    public List<Flight> findFlightsWithoutCrew() {
        return find("SELECT f FROM Flight f WHERE SIZE(f.flightCrews) = 0").list();
    }

    // En son eklenen uçuşları bul (ID'ye göre DESC sıralama)
    public List<Flight> findLatestFlights(int limit) {
        return find("ORDER BY id DESC").page(0, limit).list();
    }

    // Uçuş sayısını döndür
    public long countAllFlights() {
        return count(); //SELECT COUNT(*) sorgusuna karşılık gelir
    }

    // Belirli tarih aralığındaki uçuş sayısını döndür
    public long countByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return count("departureDateTime >= ?1 and departureDateTime <= ?2", startDate, endDate);
    }

    // Flight number varlığını kontrol et
    public boolean existsByFlightNumber(String flightNumber) {
        return count("flightNumber", flightNumber) > 0;
    }

    // Aktif uçuşları bul (gelecekteki uçuşlar)
    public List<Flight> findActiveFlights() {
        return list("departureDateTime > ?1", LocalDateTime.now());
    }

    // Geçmiş uçuşları bul
    public List<Flight> findPastFlights() {
        return list("departureDateTime < ?1", LocalDateTime.now());
    }
}