package com.enesuzun.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;

import com.enesuzun.Entity.Flight;
import com.enesuzun.Entity.FlightCrew;
import com.enesuzun.Services.FlightService;
import com.enesuzun.dto.FlightCrewDto;

import jakarta.inject.Inject;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public abstract class FlightCrewMapper {
    
    @Inject
    protected FlightService flightService;
    
    /**
     * Entity'den DTO'ya dönüştürme
     * flight.id -> flightId ve flight.flightNumber -> flightNumber mapping'i
     */
    @Mapping(target = "flightId", source = "flight.id")
    @Mapping(target = "flightNumber", source = "flight.flightNumber")
    public abstract FlightCrewDto toDto(FlightCrew flightCrew);
    
    /**
     * DTO'dan Entity'ye dönüştürme
     * flight alanı @AfterMapping ile otomatik set edilir
     */
    @Mapping(target = "flight", ignore = true)
    public abstract FlightCrew toEntity(FlightCrewDto flightCrewDto);
    
    /**
     * DTO → Entity dönüştürmeden sonra flight relationship'ini set et
     * Bu method her toEntity() çağrısından sonra otomatik çalışır
     */
    @AfterMapping
    protected void setFlightRelationship(@MappingTarget FlightCrew flightCrew, FlightCrewDto dto) {
        if (dto.getFlightId() != null) {
            Flight flight = flightService.getFlightById(dto.getFlightId());
            if (flight != null) {
                flightCrew.setFlight(flight);
            }
        }
    }
    
    /**
     * Liste dönüştürmeleri
     */
    public abstract List<FlightCrewDto> toDtoList(List<FlightCrew> flightCrews);
    
    public abstract List<FlightCrew> toEntityList(List<FlightCrewDto> flightCrewDtos);
}