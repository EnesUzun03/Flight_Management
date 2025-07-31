package com.enesuzun.Entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

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

//Jpa entity classı olduğunu belitmek için kullanılır.
@Entity
@Table(name = "flight")//Veri tabanında flight tablosu olacak.
//PanacheEntity'den extend etmek yerine PanacheEntityBase'den extend etme sebebimiz id anotasyonunu kullanabilmek için böylelikle özel bir id alanı oluşturduk.
public class Flight extends PanacheEntityBase {//PanacheEntityBase ile temel CRUD işlemleri hazır metodlar içeren bir sınıf
    //liquibase ile oluşturulan sequence'i kendi id alanımız'a bağladık.
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flight_seq")//GenerationType.SEQUENCE Veritabanı sequence kullanarak ID ürettik.
    @SequenceGenerator(name = "flight_seq", sequenceName = "flight_seq", allocationSize = 50)
    public Long id;


    @Column(name = "flight_number", nullable = false)
    public String flightNumber;
    
    @Column(name = "departure_time")
    public LocalTime departureTime;
    
    @Column(name = "departure_date")
    public LocalDate departureDate;
    
    // Relationships
    //bir uçuşun birden fazla personeli olabilir bu yüzden one to many ilişkisi kurduk.
    //Flight'a göre maplendi yani flightCrew tablosunu  "flight" alanı bu ilişkiyi yönetir
    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, orphanRemoval = true)//cascade = CascadeType.ALL: Flight silindiğinde tüm crew kayıtları da silinir
    @JsonManagedReference
    public List<FlightCrew> flightCrews;

    //getter-setter
    public String getFlightNumber(){
        return this.flightNumber;
    }
    public LocalTime getDepartureTime(){
        return this.departureTime;
    }//localdatetime lacak ayrı ayrı olmayacak
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