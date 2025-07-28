package com.enesuzun.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "flight_crew")
public class FlightCrew extends PanacheEntity {
    
    @Column(name = "crew_name")
    public String crewName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "crew_type")
    public CrewType crewType;  //PILOT,COPILOT,CABIN_CREW gibi personel tipi

    @ManyToOne
    @JoinColumn(name = "flight_id")
    public Flight flight;
}