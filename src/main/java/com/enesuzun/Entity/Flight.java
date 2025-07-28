package com.enesuzun.Entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "flight")
public class Flight extends PanacheEntity {
    
    
    @Column(name = "flight_number")
    public String flightNumber;
    
    @Column(name = "departure_time")
    public LocalTime departureTime;
    
    @Column(name = "departure_date")
    public LocalDate departureDate;
    
    // Relationships
    @OneToMany(mappedBy = "flight")
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