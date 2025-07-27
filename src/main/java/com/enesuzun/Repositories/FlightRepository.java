package com.enesuzun.Repositories;

import java.time.LocalDate;
import java.time.LocalTime;

import com.enesuzun.Entity.Flight;
import com.enesuzun.Entity.FlightCrew;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class FlightRepository implements PanacheRepository<Flight> {
    //PanacheRepository ile basit CRUD işlemleri gerçekleşiyor zaten özel sorguları yazmak gerekli
    public Flight findByFlightNumber(String flightNumber) {
        return find("flightNumber", flightNumber).firstResult();
    }

    //Flight deperture_time
    public LocalTime findByFlightDepartureTime(String flightNumber) {
        return find("flightNumber", flightNumber).firstResult().departureTime;
    }

    //Flight deperture_date
    public LocalDate findByFlightDepartureDate(String flightNumber) {
        return find("flightNumber", flightNumber).firstResult().departureDate;
    }
    //Flight crew
    public List<FlightCrew> findByFlightCrewList(String flightNumber) {
        return find("flightNumber", flightNumber).firstResult().flightCrews;
    }
}