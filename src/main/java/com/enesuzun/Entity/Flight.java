package com.enesuzun.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

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
}