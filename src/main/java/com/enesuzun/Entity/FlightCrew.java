package com.enesuzun.Entity;


import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;


@Entity
@Table(name = "flight_crew")
public class FlightCrew extends PanacheEntity {
    
    @Column(name = "crew_name")
    public String crewName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "crew_type")
    public CrewType crewType;  // Cabin/Cockpit gibi personel tipi
    
    @ManyToOne
    @JoinColumn(name = "flight_id")
    public Flight flight;
}