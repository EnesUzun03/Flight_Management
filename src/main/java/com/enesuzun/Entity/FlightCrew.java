package com.enesuzun.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;
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
@Getter
@Setter
public class FlightCrew extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flight_crew_seq")
    @SequenceGenerator(name = "flight_crew_seq", sequenceName = "flight_crew_seq", allocationSize = 50)
    private Long id;
    
    @Column(name = "crew_name", nullable = false)
    private String crewName;
    
    @Enumerated(EnumType.STRING)//Enum değerleri string olarak vt de tutulur
    @Column(name = "crew_type")
    private CrewType crewType;  //PILOT,COPILOT,CABIN_CREW gibi personel tipi

    @ManyToOne// Birden fazla crew üyesi aynı uçuşa atanabilir
    @JoinColumn(name = "flight_id", nullable = false)//@JoinColumn(name = "flight_id"): Foreign key sütunu Flight tablosundaki id sutunu ile bağlanır
    @JsonBackReference
    private Flight flight;
}