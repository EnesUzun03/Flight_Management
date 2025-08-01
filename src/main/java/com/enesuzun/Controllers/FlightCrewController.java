package com.enesuzun.Controllers;
/*
 * Bu dosyalar REST API Controller sınıflarıdır ve 
 * JAX-RS (Java API for RESTful Web Services) standardını kullanır.
 *  Presentation Layer'ın (Sunum Katmanı) bir parçası olarak HTTP isteklerini karşılar ve JSON formatında yanıt döner. 
 */
import java.util.List;

import com.enesuzun.Entity.CrewType;
import com.enesuzun.Entity.FlightCrew;
import com.enesuzun.Services.FlightCrewService;
import com.enesuzun.dto.FlightCrewDto;
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

@Path("/flight-crews")//Base URL: /flight-crews
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FlightCrewController {
    @Inject
    FlightCrewService flightCrewService;
    
    @Inject
    FlightCrewMapper flightCrewMapper;

    // Tüm personelleri getir
    @GET
    public List<FlightCrewDto> getAllCrews() {
        List<FlightCrew> crews = flightCrewService.getAllCrews();
        return flightCrewMapper.toDtoList(crews);
    }

    // ID ile personel getir
    @GET
    @Path("/{id}")
    public Response getCrewById(@PathParam("id") Long id) {
        FlightCrew crew = flightCrewService.getCrewById(id);
        if (crew != null) {
            FlightCrewDto crewDto = flightCrewMapper.toDto(crew);
            return Response.ok(crewDto).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    // Yeni personel ekle
    @POST
    public Response addCrew(FlightCrewDto crewDto) {
        FlightCrew crew = flightCrewMapper.toEntity(crewDto);
        flightCrewService.addCrew(crew);
        FlightCrewDto responseDto = flightCrewMapper.toDto(crew);
        return Response.status(Response.Status.CREATED).entity(responseDto).build();
    }

    // Personel güncelle
    @PUT
    @Path("/{id}")
    public Response updateCrew(@PathParam("id") Long id, FlightCrewDto updatedCrewDto) {
        FlightCrew existing = flightCrewService.getCrewById(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        // DTO'dan Flight entity'sini almak için service'den flight'ı çekmemiz gerekecek
        // Şimdilik basit approach kullanıyoruz - sadece temel alanları güncelliyoruz
        boolean updated = flightCrewService.updateCrew(id, updatedCrewDto.getCrewName(), updatedCrewDto.getCrewType(), existing.getFlight());
        if (updated) {
            return Response.ok().build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();//Beklenmeyen hata durumunda 500 status döner
    }

    // Personel sil (isim ile)
    @DELETE
    @Path("/by-name/{crewName}")
    public Response deleteCrewByName(@PathParam("crewName") String crewName) {
        boolean deleted = flightCrewService.deleteCrewByName(crewName);
        if (deleted) {
            //return Response.noContent().build();
            return Response.ok(crewName + " silindi").build();//Silineniin verisini göstermek için deneme amaçlı yapıldı
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    // Uçuştaki tüm personelleri getir
    @GET
    @Path("/by-flight/{flightId}")
    public List<FlightCrewDto> getCrewsByFlightId(@PathParam("flightId") Long flightId) {
        List<FlightCrew> crews = flightCrewService.getCrewsByFlightId(flightId);
        return flightCrewMapper.toDtoList(crews);
    }

    // Uçuştaki belirli tipteki personelleri getir
    @GET
    @Path("/by-flight/{flightId}/by-type/{crewType}")//Multiple path parameters: İki parametre birden
    public List<FlightCrewDto> getCrewsByFlightIdAndType(@PathParam("flightId") Long flightId, @PathParam("crewType") String crewType) {
        List<FlightCrew> crews = flightCrewService.getCrewsByFlightIdAndType(flightId, CrewType.valueOf(crewType));//CrewType.valueOf(): String'i enum'a dönüştürür
        return flightCrewMapper.toDtoList(crews);
    }

    // İsme göre personel ara
    @GET
    @Path("/by-name/{crewName}")
    public List<FlightCrewDto> getCrewsByName(@PathParam("crewName") String crewName) {
        List<FlightCrew> crews = flightCrewService.getCrewsByName(crewName);
        return flightCrewMapper.toDtoList(crews);
    }

    // Uçuştaki personel sayısı
    @GET
    @Path("/count/by-flight/{flightId}")
    public long countCrewsByFlightId(@PathParam("flightId") Long flightId) {
        return flightCrewService.countCrewsByFlightId(flightId);
    }
}
