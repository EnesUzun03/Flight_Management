package com.enesuzun.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.enesuzun.Entity.Flight;
import com.enesuzun.dto.FlightDto;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, uses = FlightCrewMapper.class)
public interface FlightMapper {
    
    /**
     * Entity'den DTO'ya dönüştürme
     * FlightCrew listesi otomatik olarak FlightCrewMapper kullanılarak dönüştürülür
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