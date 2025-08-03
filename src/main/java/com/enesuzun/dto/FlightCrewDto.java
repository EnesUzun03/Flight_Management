package com.enesuzun.dto;

import com.enesuzun.Entity.CrewType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightCrewDto {
    
    private Long id;
    
    @NotBlank(message = "Personel adı boş olamaz")
    @Size(min = 2, max = 50, message = "Personel adı 2-50 karakter arasında olmalıdır")
    private String crewName;
    
    @NotNull(message = "Personel tipi belirtilmelidir")
    private CrewType crewType;
    
    @NotNull(message = "Uçuş ID'si belirtilmelidir")
    @Positive(message = "Uçuş ID'si pozitif bir sayı olmalıdır")
    private Long flightId; // Circular reference'ı önlemek için sadece Flight ID'sini tutuyoruz
    
    private String flightNumber; // Ek bilgi olarak flight number'ı da ekleyebiliriz - validation gerekmez çünkü otomatik doldurulur
}
