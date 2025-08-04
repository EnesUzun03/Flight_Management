package com.enesuzun.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.enesuzun.Entity.Flight;
import com.enesuzun.dto.FlightDto;
//CDI anotasyonlarını kullanırız.Böylelikler Controller'larda otomatik inject olabiliyor.
//FlightCrew -> flightcrewDto için FlightCrewMapper kullanmasını belirtiyoruz.
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI, uses = FlightCrewMapper.class)
public interface FlightMapper {
    
    /**
     * Entity'den DTO'ya dönüştürme
     * FlightCrew listesi otomatik olarak FlightCrewMapper kullanılarak dönüştürülür
     * Çünkü beradaki flight içerisinde FlightCrew listesi var DTO'da da flightCrew listesi var.Otomatik olarak dönüşüyor.
     * Manuel bir referanslama yapmamıza gerek yok.
     */
    FlightDto toDto(Flight flight);
    
    /**
     * DTO'dan Entity'ye dönüştürme
     * flightCrews alanı manuel olarak set edilmeli çünkü circular reference var
     */
    @Mapping(target = "flightCrews", ignore = true)
    Flight toEntity(FlightDto flightDto);
    
    /**
     * Liste dönüştürmeleri
     */
    List<FlightDto> toDtoList(List<Flight> flights);
    
    List<Flight> toEntityList(List<FlightDto> flightDtos);
}