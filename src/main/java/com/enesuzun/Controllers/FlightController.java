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

    // Tüm uçuşları getir
    @GET//URL : GET /flights
    public List<Flight> getAllFlights() {
        return flightService.getAllFlights();//Cevao json formatındaki tüm uçuşlardır
    }

    // ID ile uçuş getir
    @GET
    @Path("/{id}")//URL path'inde id parametresi (/flights/123)
    public Response getFlightById(@PathParam("id") Long id) {//@PathParam("id"): URL'deki {id}'yi metod parametresine bind eder.(bind etmek bağlamak anlammında kullanılıyor)
        Flight flight = flightService.getFlightById(id);
        if (flight != null) {
            return Response.ok(flight).build();//Response.ok http 200
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    // Yeni uçuş ekle
    @POST
    public Response addFlight(Flight flight) {
        flightService.addFlight(flight);
        return Response.status(Response.Status.CREATED).entity(flight).build();//entity(flight): Response body'ye Flight objesini ekler 
    }

    // Uçuş güncelle
    @PUT
    @Path("/{id}")
    public Response updateFlight(@PathParam("id") Long id, Flight updatedFlight) {
        Flight existing = flightService.getFlightById(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();//Response nesne yoksa not_Found döndürür
        }
        flightService.updateFlight(id, updatedFlight.getFlightNumber(), updatedFlight.getDepartureTime(), updatedFlight.getDepartureDate());
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
    public Response addCrewToFlight(@PathParam("flightId") Long flightId, FlightCrew crew) {//@PathParam("flightId"): URL'deki {flightId}'yi metod parametresine bind eder.
        flightService.addCrewToFlight(flightId, crew);
        return Response.status(Response.Status.CREATED).entity(crew).build();//entity(crew): Response body'ye FlightCrew objesini ekler
    }
}
