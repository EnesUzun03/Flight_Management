package com.enesuzun.Services;


import java.time.LocalDate;
import java.time.LocalTime;

import com.enesuzun.Entity.Flight;
import com.enesuzun.Entity.FlightCrew;


import com.enesuzun.Repositories.FlightRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class FlightService {
    @Inject
    FlightRepository flightRepository;

    //uçuş ekle
    public void addFlight(Flight flight){
        flightRepository.persist(flight);
    }

    //Özelliklerle uçuş nesnesi ekleme
    public void addFlight(String flightNumber,LocalTime departureTime,LocalDate departureDate,List<FlightCrew> flightCrews){
       Flight flight =new Flight();
       flight.setFlightNumber(flightNumber);
       flight.setDepartureTime(departureTime);
       flight.setDepartureDate(departureDate);
       flight.setflightCrews(flightCrews);

       flightRepository.persist(flight);

    }
    //UPDATE
    public void updateFlight(Long id, String newFlightNumber, LocalTime newDepartureTime, LocalDate newDepartureDate) {
        Flight flight = flightRepository.findById(id);
        if (flight != null) {
            flight.flightNumber = newFlightNumber;
            flight.departureTime = newDepartureTime;
            flight.departureDate = newDepartureDate;
            flightRepository.persist(flight); 
        }
    }

    //Uçuşları listele
    public List<Flight> getAllFlights(){
        return flightRepository.listAll();
    }

    //id ile uçuş bul
    public Flight getFlightById(Long id) {
        return flightRepository.findById(id);
    }

    //id ile uçuş sil
    public void deleteFlight(Long id) {
        flightRepository.deleteById(id);
    }

    //Uçuş ile uçuşun numarasunu bulma
    public Flight findByFlightNumber(Flight flight){
        return flightRepository.findByFlightNumber(flight.flightNumber);
    }

    //Uçuş ile uçuşun numarasunu bulma
    public LocalTime findByFlightDepartureTime(Flight flight){
        return flightRepository.findByFlightDepartureTime(flight.flightNumber);
    }
    //uçuş nesnesinin Dateini bulma
    public LocalDate findByFlightDepartureDate(Flight flight) {
        return flightRepository.findByFlightDepartureDate(flight.flightNumber);
    }
    //uçuştaki personel listesini bulma
    public List<FlightCrew> findByFlightCrewList(Flight flight){
        return flightRepository.findByFlightCrewList(flight.flightNumber);
    }
}
