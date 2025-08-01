package com.enesuzun.dto;

import com.enesuzun.Entity.CrewType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightCrewDto {
    
    private Long id;
    private String crewName;
    private CrewType crewType;
    private Long flightId; // Circular reference'ı önlemek için sadece Flight ID'sini tutuyoruz
    private String flightNumber; // Ek bilgi olarak flight number'ı da ekleyebiliriz
}
