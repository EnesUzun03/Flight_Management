package com.enesuzun.Controllers;

import java.util.List;

import com.enesuzun.Entity.CrewType;
import com.enesuzun.Entity.FlightCrew;
import com.enesuzun.Services.FlightCrewService;

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

@Path("/flight-crews")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FlightCrewController {
    @Inject
    FlightCrewService flightCrewService;

    // Tüm personelleri getir
    @GET
    public List<FlightCrew> getAllCrews() {
        return flightCrewService.getAllCrews();
    }

    // ID ile personel getir
    @GET
    @Path("/{id}")
    public Response getCrewById(@PathParam("id") Long id) {
        FlightCrew crew = flightCrewService.getCrewById(id);
        if (crew != null) {
            return Response.ok(crew).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    // Yeni personel ekle
    @POST
    public Response addCrew(FlightCrew crew) {
        flightCrewService.addCrew(crew);
        return Response.status(Response.Status.CREATED).entity(crew).build();
    }

    // Personel güncelle
    @PUT
    @Path("/{id}")
    public Response updateCrew(@PathParam("id") Long id, FlightCrew updatedCrew) {
        FlightCrew existing = flightCrewService.getCrewById(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        boolean updated = flightCrewService.updateCrew(id, updatedCrew.crewName, updatedCrew.crewType, updatedCrew.flight);
        if (updated) {
            return Response.ok().build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    // Personel sil (isim ile)
    @DELETE
    @Path("/by-name/{crewName}")
    public Response deleteCrewByName(@PathParam("crewName") String crewName) {
        boolean deleted = flightCrewService.deleteCrewByName(crewName);
        if (deleted) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    // Uçuştaki tüm personelleri getir
    @GET
    @Path("/by-flight/{flightId}")
    public List<FlightCrew> getCrewsByFlightId(@PathParam("flightId") Long flightId) {
        return flightCrewService.getCrewsByFlightId(flightId);
    }

    // Uçuştaki belirli tipteki personelleri getir
    @GET
    @Path("/by-flight/{flightId}/by-type/{crewType}")
    public List<FlightCrew> getCrewsByFlightIdAndType(@PathParam("flightId") Long flightId, @PathParam("crewType") String crewType) {
        return flightCrewService.getCrewsByFlightIdAndType(flightId, CrewType.valueOf(crewType));
    }

    // İsme göre personel ara
    @GET
    @Path("/by-name/{crewName}")
    public List<FlightCrew> getCrewsByName(@PathParam("crewName") String crewName) {
        return flightCrewService.getCrewsByName(crewName);
    }

    // Uçuştaki personel sayısı
    @GET
    @Path("/count/by-flight/{flightId}")
    public long countCrewsByFlightId(@PathParam("flightId") Long flightId) {
        return flightCrewService.countCrewsByFlightId(flightId);
    }
}
