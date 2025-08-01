package com.enesuzun.Controllers;
/*
 * Bu dosyalar REST API Controller sınıflarıdır ve 
 * JAX-RS (Java API for RESTful Web Services) standardını kullanır.
 *  Presentation Layer'ın (Sunum Katmanı) bir parçası olarak HTTP isteklerini karşılar ve JSON formatında yanıt döner. 
 */
import java.util.List;

import com.enesuzun.Entity.Flight;
import com.enesuzun.Entity.FlightCrew;
import com.enesuzun.Services.FlightService;
import com.enesuzun.dto.FlightDto;
import com.enesuzun.dto.FlightCrewDto;
import com.enesuzun.mapper.FlightMapper;
import com.enesuzun.mapper.FlightCrewMapper;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/flights")//Bu controller'ın base URL'i /flights olacak
@Produces(MediaType.APPLICATION_JSON)//Tüm metodlar JSON response döneecek
@Consumes(MediaType.APPLICATION_JSON)//Tüm metodlar JSON input kabul eder
public class FlightController {
    @Inject//Controller → Service → Repository katman yapısı.Business logic'i service katmanına delege eder
    FlightService flightService;
    
    @Inject
    FlightMapper flightMapper;
    
    @Inject
    FlightCrewMapper flightCrewMapper;

    // Tüm uçuşları getir
    @GET//URL : GET /flights
    public List<FlightDto> getAllFlights() {
        List<Flight> flights = flightService.getAllFlights();
        return flightMapper.toDtoList(flights);//Entity'leri DTO'lara dönüştürür
    }

    // ID ile uçuş getir
    @GET
    @Path("/{id}")//URL path'inde id parametresi (/flights/123)
    public Response getFlightById(@PathParam("id") Long id) {//@PathParam("id"): URL'deki {id}'yi metod parametresine bind eder.(bind etmek bağlamak anlammında kullanılıyor)
        Flight flight = flightService.getFlightById(id);
        if (flight != null) {
            FlightDto flightDto = flightMapper.toDto(flight);
            return Response.ok(flightDto).build();//Response.ok http 200
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    // Yeni uçuş ekle
    @POST
    public Response addFlight(FlightDto flightDto) {
        Flight flight = flightMapper.toEntity(flightDto);
        flightService.addFlight(flight);
        FlightDto responseDto = flightMapper.toDto(flight);
        return Response.status(Response.Status.CREATED).entity(responseDto).build();//entity(flight): Response body'ye Flight objesini ekler 
    }

    // Uçuş güncelle
    @PUT
    @Path("/{id}")
    public Response updateFlight(@PathParam("id") Long id, FlightDto updatedFlightDto) {
        Flight existing = flightService.getFlightById(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();//Response nesne yoksa not_Found döndürür
        }
        flightService.updateFlight(id, updatedFlightDto.getFlightNumber(), updatedFlightDto.getDepartureDateTime());
        return Response.ok().build();
    }

    // Uçuş sil
    @DELETE//Http delete isteği
    @Path("/{id}")
    public Response deleteFlight(@PathParam("id") Long id) {
        Flight existing = flightService.getFlightById(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();//Response nesne yoksa not_Found döndrğür
        }
        flightService.deleteFlight(id);
        return Response.noContent().build();//içiboş body ve 200 status döner
    }

    // Flight entity'sine crew ekle
    @POST
    @Path("/{flightId}/add-crew")//URL path'inde flightId parametresi (/flights/123/add-crew)
    public Response addCrewToFlight(@PathParam("flightId") Long flightId, FlightCrewDto crewDto) {//@PathParam("flightId"): URL'deki {flightId}'yi metod parametresine bind eder.
        FlightCrew crew = flightCrewMapper.toEntity(crewDto);
        flightService.addCrewToFlight(flightId, crew);
        FlightCrewDto responseDto = flightCrewMapper.toDto(crew);
        return Response.status(Response.Status.CREATED).entity(responseDto).build();//entity(crew): Response body'ye FlightCrew objesini ekler
    }
}
