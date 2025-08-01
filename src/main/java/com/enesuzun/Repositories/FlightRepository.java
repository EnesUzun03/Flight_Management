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
}