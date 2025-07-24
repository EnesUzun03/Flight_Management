package com.enesuzun.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "flight_crew")
public class FlightCrew extends PanacheEntity {
    
    @Column(name = "crew_name")
    public String crewName;
    
    @Column(name = "crew_type")
    public String crewType;  // Cabin/Cockpit personnel type
    
    @ManyToOne
    @JoinColumn(name = "flight_id")
    public Flight flight;
}