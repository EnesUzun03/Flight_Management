package com.enesuzun.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "flight_crew")
public class FlightCrew extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flight_crew_seq")
    @SequenceGenerator(name = "flight_crew_seq", sequenceName = "flight_crew_seq", allocationSize = 50)
    public Long id;
    
    @Column(name = "crew_name", nullable = false)
    public String crewName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "crew_type")
    public CrewType crewType;  //PILOT,COPILOT,CABIN_CREW gibi personel tipi

    @ManyToOne
    @JoinColumn(name = "flight_id", nullable = false)
    @JsonBackReference
    public Flight flight;
}