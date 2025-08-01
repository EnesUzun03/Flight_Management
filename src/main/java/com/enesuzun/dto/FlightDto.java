package com.enesuzun.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightDto {
    
    private Long id;
    private String flightNumber;
    private LocalDateTime departureDateTime;
    private List<FlightCrewDto> flightCrews; // Nested DTO kullanarak circular reference'ı önlüyoruz
}
