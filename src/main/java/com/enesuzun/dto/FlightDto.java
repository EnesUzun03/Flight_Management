package com.enesuzun.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightDto {
    
    private Long id;
    
    @NotBlank(message = "Uçuş numarası boş olamaz")
    @Size(min = 2, max = 10, message = "Uçuş numarası 2-10 karakter arasında olmalıdır")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{1,4}$", message = "Uçuş numarası format: 2 büyük harf + 1-4 rakam (ör: TK123)")
    private String flightNumber;
    
    @NotNull(message = "Kalkış tarihi ve saati belirtilmelidir")
    private LocalDateTime departureDateTime;
    
    @Valid // Nested DTO validation
    private List<FlightCrewDto> flightCrews; // Nested DTO kullanarak circular reference'ı önlüyoruz
}
