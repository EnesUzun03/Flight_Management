package com.enesuzun.Entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
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
@Data
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
//PanacheEntity'den extend etmek yerine PanacheEntityBase'den extend etme sebebimiz id anotasyonunu kullanabilmek için böylelikle özel bir id alanı oluşturduk.
public class Flight extends PanacheEntityBase {//PanacheEntityBase ile temel CRUD işlemleri hazır metodlar içeren bir sınıf
    //liquibase ile oluşturulan sequence'i kendi id alanımız'a bağladık.
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flight_seq")//GenerationType.SEQUENCE Veritabanı sequence kullanarak ID ürettik.
    @SequenceGenerator(name = "flight_seq", sequenceName = "flight_seq", allocationSize = 50)
    private Long id;

    @Column(name = "flight_number", nullable = false)
    private String flightNumber;
    
    @Column(name = "departure_datetime")
    private LocalDateTime departureDateTime;
    
    // Relationships
    //bir uçuşun birden fazla personeli olabilir bu yüzden one to many ilişkisi kurduk.
    //Flight'a göre maplendi yani flightCrew tablosunu  "flight" alanı bu ilişkiyi yönetir
    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, orphanRemoval = true)//cascade = CascadeType.ALL: Flight silindiğinde tüm crew kayıtları da silinir
    @JsonManagedReference
    private List<FlightCrew> flightCrews;
}