package com.enesuzun.Controllers;

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

@Path("/flights")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FlightController {
    @Inject
    FlightService flightService;

    // Tüm uçuşları getir
    @GET
    public List<Flight> getAllFlights() {
        return flightService.getAllFlights();
    }

    // ID ile uçuş getir
    @GET
    @Path("/{id}")
    public Response getFlightById(@PathParam("id") Long id) {
        Flight flight = flightService.getFlightById(id);
        if (flight != null) {
            return Response.ok(flight).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    // Yeni uçuş ekle
    @POST
    public Response addFlight(Flight flight) {
        flightService.addFlight(flight);
        return Response.status(Response.Status.CREATED).entity(flight).build();
    }

    // Uçuş güncelle
    @PUT
    @Path("/{id}")
    public Response updateFlight(@PathParam("id") Long id, Flight updatedFlight) {
        Flight existing = flightService.getFlightById(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        flightService.updateFlight(id, updatedFlight.getFlightNumber(), updatedFlight.getDepartureTime(), updatedFlight.getDepartureDate());
        return Response.ok().build();
    }

    // Uçuş sil
    @DELETE
    @Path("/{id}")
    public Response deleteFlight(@PathParam("id") Long id) {
        Flight existing = flightService.getFlightById(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        flightService.deleteFlight(id);
        return Response.noContent().build();
    }

    // Flight entity'sine crew ekle
    @POST
    @Path("/{flightId}/add-crew")
    public Response addCrewToFlight(@PathParam("flightId") Long flightId, FlightCrew crew) {
        flightService.addCrewToFlight(flightId, crew);
        return Response.status(Response.Status.CREATED).entity(crew).build();
    }
}
