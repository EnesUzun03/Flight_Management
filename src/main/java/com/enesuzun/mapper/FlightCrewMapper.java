package com.enesuzun.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.enesuzun.Entity.FlightCrew;
import com.enesuzun.dto.FlightCrewDto;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface FlightCrewMapper {
    
    /**
     * Entity'den DTO'ya dönüştürme
     * flight.id -> flightId ve flight.flightNumber -> flightNumber mapping'i
     */
    @Mapping(target = "flightId", source = "flight.id")
    @Mapping(target = "flightNumber", source = "flight.flightNumber")
    FlightCrewDto toDto(FlightCrew flightCrew);
    
    /**
     * DTO'dan Entity'ye dönüştürme
     * flight alanı manuel olarak set edilmeli
     */
    @Mapping(target = "flight", ignore = true)
    FlightCrew toEntity(FlightCrewDto flightCrewDto);
    
    /**
     * Liste dönüştürmeleri
     */
    List<FlightCrewDto> toDtoList(List<FlightCrew> flightCrews);
    
    List<FlightCrew> toEntityList(List<FlightCrewDto> flightCrewDtos);
}