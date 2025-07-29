package com.enesuzun.Entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "flight")
public class Flight extends PanacheEntityBase {
    
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flight_seq")
    @SequenceGenerator(name = "flight_seq", sequenceName = "flight_seq", allocationSize = 50)
    public Long id;


    @Column(name = "flight_number")
    public String flightNumber;
    
    @Column(name = "departure_time")
    public LocalTime departureTime;
    
    @Column(name = "departure_date")
    public LocalDate departureDate;
    
    // Relationships
    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<FlightCrew> flightCrews;

    //getter-setter
    public String getFlightNumber(){
        return this.flightNumber;
    }
    public LocalTime getDepartureTime(){
        return this.departureTime;
    }
    public LocalDate getDepartureDate(){
        return this.departureDate;
    }

    public List<FlightCrew> getFlightCrews(){
        return this.flightCrews;
    }

    public void setFlightNumber(String a){
        this.flightNumber=a;
    }
    public void setDepartureTime(LocalTime newTime){
        this.departureTime=newTime;
    }
    public void setDepartureDate(LocalDate newDate){
        this.departureDate=newDate;
    }

    public void setFlightCrews(List<FlightCrew> newflightCrews){
        this.flightCrews=newflightCrews;
    }
}