package com.enesuzun.Services;


import java.time.LocalDateTime;
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

    //Birden fazla repository ve service inject edilir
    //Bunun sebebi bir uçuş silineceği zamen ona bağlı olan personellerde silinir
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
    public void addFlight(String flightNumber, LocalDateTime departureDateTime, List<FlightCrew> flightCrews){
       Flight flight =new Flight();
       flight.setFlightNumber(flightNumber);
       flight.setDepartureDateTime(departureDateTime);
       flight.setFlightCrews(flightCrews);

       flightRepository.persist(flight);

    }
    //UPDATE
    @Transactional
    public void updateFlight(Long id, String newFlightNumber, LocalDateTime newDepartureDateTime) {
        Flight flight = flightRepository.findById(id);
        if (flight != null) {
            flight.setFlightNumber(newFlightNumber);
            flight.setDepartureDateTime(newDepartureDateTime);
            flightRepository.persist(flight); //Burada persist gereksizmiş JPA Dirty Checking ile otomatik güncelleme gerçekleşiyormuş
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

    //NOT:  Entity cascade kullanarak sadece flight silinebilir
    //id ile uçuş sil
    @Transactional
    public void deleteFlight(Long id) {
        // Önce bu Flight'e bağlı tüm FlightCrew'ları sil bunun sebebi Foreign key constraint'i önlemek için sıralı silme yapıyoruz
        List<FlightCrew> crews = flightCrewRepository.findByFlightId(id);
        for (FlightCrew crew : crews) {
            flightCrewRepository.deleteById(crew.getId());
        }
        
        // Sonra Flight'i sil
        flightRepository.deleteById(id);
    }

    //Uçuş no ile uçuşu bulma
    public Flight findByFlightNumber(String flightNumber){
        return flightRepository.findByFlightNumber(flightNumber);
    }

    //Uçuş numarası ile uçuşun LocalDateTime bulma
    public LocalDateTime findByFlightDepartureDateTime(String flightNumber){
        return flightRepository.findByFlightDepartureDateTime(flightNumber);
    }
    
    //uçuştaki personel listesini bulma
    public List<FlightCrew> findByFlightCrewList(Flight flight){
        return flightRepository.findByFlightCrewList(flight.getFlightNumber());
    }

    // Flight entity'sine crew ekle
    @Transactional
    public void addCrewToFlight(Long flightId, FlightCrew crew) {
        Flight flight = flightRepository.findById(flightId);
        //Burada bir hata var var olan crew'i değiştirirken 500 hatası alıyorum
        //Burada parametre olan crewi direkt kullanmak yerine flightcrew repodaki findbyıd ile mi onun adresini mi kullanmam gerekli
        /*
        if (flight != null) {
            crew.flight = flight;
            flightCrewRepository.persist(crew);
        }
        */
        if (flight != null) {
            // Yeni bir FlightCrew nesnesi oluştur
            FlightCrew newCrew = new FlightCrew();
            newCrew.setCrewName(crew.getCrewName());
            newCrew.setCrewType(crew.getCrewType());
            newCrew.setFlight(flight);
            
            flightCrewRepository.persist(newCrew);
        }
    }
}
