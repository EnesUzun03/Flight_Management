package com.enesuzun.Services;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.enesuzun.Entity.Flight;
import com.enesuzun.Entity.FlightCrew;
import com.enesuzun.Repositories.FlightCrewRepository;
import com.enesuzun.Repositories.FlightRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class FlightService {
    @Inject
    FlightRepository flightRepository;

    @Inject
    FlightCrewRepository flightCrewRepository;
    
    @Inject
    FlightCrewService flightCrewService;

    //uçuş ekle
    @Transactional
    public void addFlight(Flight flight){
        flightRepository.persist(flight);
    }

    //Özelliklerle uçuş nesnesi ekleme
    @Transactional
    public void addFlight(String flightNumber,LocalTime departureTime,LocalDate departureDate,List<FlightCrew> flightCrews){
       Flight flight =new Flight();
       flight.setFlightNumber(flightNumber);
       flight.setDepartureTime(departureTime);
       flight.setDepartureDate(departureDate);
       flight.setFlightCrews(flightCrews);

       flightRepository.persist(flight);

    }
    //UPDATE
    @Transactional
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
    @Transactional
    public void deleteFlight(Long id) {
        // Önce bu Flight'e bağlı tüm FlightCrew'ları sil
        List<FlightCrew> crews = flightCrewRepository.findByFlightId(id);
        for (FlightCrew crew : crews) {
            flightCrewRepository.deleteById(crew.id);
        }
        
        // Sonra Flight'i sil
        flightRepository.deleteById(id);
    }

    //Uçuş no ile uçuşu bulma
    public Flight findByFlightNumber(String flightNumber){
        return flightRepository.findByFlightNumber(flightNumber);
    }

    //Uçuş numarası ile uçuşun LocalTime bulma
    public LocalTime findByFlightDepartureTime(String flightNumber){
        return flightRepository.findByFlightDepartureTime(flightNumber);
    }
    //uçuş numarası ile uçuşun LocalDateini bulma
    public LocalDate findByFlightDepartureDate(String flightNumber) {
        return flightRepository.findByFlightDepartureDate(flightNumber);
    }
    
    //uçuştaki personel listesini bulma
    public List<FlightCrew> findByFlightCrewList(Flight flight){
        return flightRepository.findByFlightCrewList(flight.flightNumber);
    }

    // Flight entity'sine crew ekle
    @Transactional
    public void addCrewToFlight(Long flightId, FlightCrew crew) {
        Flight flight = flightRepository.findById(flightId);
        /*if (flight != null) {
            crew.flight = flight;
            flightCrewRepository.persist(crew);
        }
        */
        if (flight != null) {
            // Yeni bir FlightCrew nesnesi oluştur
            FlightCrew newCrew = new FlightCrew();
            newCrew.crewName = crew.crewName;
            newCrew.crewType = crew.crewType;
            newCrew.flight = flight;
            
            flightCrewRepository.persist(newCrew);
        }
    }
}
