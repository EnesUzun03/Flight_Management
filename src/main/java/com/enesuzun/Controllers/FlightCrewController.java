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
import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
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
    public Response getAllCrews() {
        try {
            List<FlightCrew> crews = flightCrewService.getAllCrews();
            List<FlightCrewDto> crewDtos = flightCrewMapper.toDtoList(crews);
            return Response.ok(crewDtos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Personeller getirilirken bir hata oluştu: " + e.getMessage())
                           .build();
        }
    }

    // ID ile personel getir
    @GET
    @Path("/{id}")
    public Response getCrewById(@PathParam("id") Long id) {
        try {
            FlightCrew crew = flightCrewService.getCrewById(id);
            if (crew != null) {
                FlightCrewDto crewDto = flightCrewMapper.toDto(crew);
                return Response.ok(crewDto).build();
            }
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Bu '" + id + "' ID'ye sahip personel bulunamadı")
                           .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Geçersiz ID değeri: " + e.getMessage())
                           .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Personel getirilirken bir hata oluştu: " + e.getMessage())
                           .build();
        }
    }

    // Yeni personel ekle
    @POST
    public Response addCrew(@Valid FlightCrewDto crewDto) {
        try {
            FlightCrew crew = flightCrewMapper.toEntity(crewDto);
            flightCrewService.addCrew(crew);
            FlightCrewDto responseDto = flightCrewMapper.toDto(crew);
            return Response.status(Response.Status.CREATED).entity(responseDto).build();
        } catch (ConstraintViolationException e) {
            // Bean Validation hataları
            StringBuilder errorMessages = new StringBuilder("Validasyon hataları:\n");
            for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
                errorMessages.append("- ").append(violation.getMessage()).append("\n");
            }
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(errorMessages.toString())
                           .build();
        } catch (IllegalArgumentException e) {
            //Validasyon hatası gibi durumlarda
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Geçersiz personel verisi: " + e.getMessage())
                           .build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Çalışma zamanı hatası: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Personel eklenirken bir hata oluştu: " + e.getMessage())
                           .build();
        }
    }

    // Personel güncelle
    @PUT
    @Path("/{id}")
    public Response updateCrew(@PathParam("id") Long id, @Valid FlightCrewDto updatedCrewDto) {
        try {
            FlightCrew existing = flightCrewService.getCrewById(id);
            if (existing == null) {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("Bu '" + id + "' ID'ye sahip personel bulunamadı")
                               .build();
            }
            // DTO'dan Flight entity'sini almak için service'den flight'ı çekmemiz gerekecek
            // Şimdilik basit approach kullanıyoruz - sadece temel alanları güncelliyoruz
            boolean updated = flightCrewService.updateCrew(id, updatedCrewDto.getCrewName(), updatedCrewDto.getCrewType(), existing.getFlight());
            if (updated) {
                return Response.ok().entity("ID: " + id + ", güncelleme işlemi başarılı").build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Personel güncellenirken bir hata oluştu")
                           .build();
        } catch (ConstraintViolationException e) {
            // Bean Validation hataları
            StringBuilder errorMessages = new StringBuilder("Validasyon hataları:\n");
            for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
                errorMessages.append("- ").append(violation.getMessage()).append("\n");
            }
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(errorMessages.toString())
                           .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Geçersiz personel verisi: " + e.getMessage())
                           .build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Çalışma zamanı hatası: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Personel güncellenirken bir hata oluştu: " + e.getMessage())
                           .build();
        }
    }

    // Personel sil (isim ile)
    @DELETE
    @Path("/by-name/{crewName}")
    public Response deleteCrewByName(@PathParam("crewName") String crewName) {
        try {
            boolean deleted = flightCrewService.deleteCrewByName(crewName);
            if (deleted) {
                return Response.status(Response.Status.ACCEPTED)
                               .entity("Personel '" + crewName + "' başarıyla silindi")
                               .build();
            }
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("'" + crewName + "' adında personel bulunamadı")
                           .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Geçersiz personel adı: " + e.getMessage())
                           .build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Çalışma zamanı hatası: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Personel silinirken bir hata oluştu: " + e.getMessage())
                           .build();
        }
    }

    // Uçuştaki tüm personelleri getir
    @GET
    @Path("/by-flight/{flightId}")
    public Response getCrewsByFlightId(@PathParam("flightId") Long flightId) {
        try {
            List<FlightCrew> crews = flightCrewService.getCrewsByFlightId(flightId);
            List<FlightCrewDto> crewDtos = flightCrewMapper.toDtoList(crews);
            return Response.ok(crewDtos).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Geçersiz uçuş ID değeri: " + e.getMessage())
                           .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Uçuş personelleri getirilirken bir hata oluştu: " + e.getMessage())
                           .build();
        }
    }

    // Uçuştaki belirli tipteki personelleri getir
    @GET
    @Path("/by-flight/{flightId}/by-type/{crewType}")//Multiple path parameters: İki parametre birden
    public Response getCrewsByFlightIdAndType(@PathParam("flightId") Long flightId, @PathParam("crewType") String crewType) {
        try {
            List<FlightCrew> crews = flightCrewService.getCrewsByFlightIdAndType(flightId, CrewType.valueOf(crewType));//CrewType.valueOf(): String'i enum'a dönüştürür
            List<FlightCrewDto> crewDtos = flightCrewMapper.toDtoList(crews);
            return Response.ok(crewDtos).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Geçersiz parametre değeri (FlightId: " + flightId + ", CrewType: " + crewType + "): " + e.getMessage())
                           .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Uçuş personelleri getirilirken bir hata oluştu: " + e.getMessage())
                           .build();
        }
    }

    // İsme göre personel ara
    @GET
    @Path("/by-name/{crewName}")
    public Response getCrewsByName(@PathParam("crewName") String crewName) {
        try {
            List<FlightCrew> crews = flightCrewService.getCrewsByName(crewName);
            List<FlightCrewDto> crewDtos = flightCrewMapper.toDtoList(crews);
            return Response.ok(crewDtos).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Geçersiz personel adı: " + e.getMessage())
                           .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Personel aranırken bir hata oluştu: " + e.getMessage())
                           .build();
        }
    }

    // Uçuştaki personel sayısı
    @GET
    @Path("/count/by-flight/{flightId}")
    public Response countCrewsByFlightId(@PathParam("flightId") Long flightId) {
        try {
            long count = flightCrewService.countCrewsByFlightId(flightId);
            return Response.ok(count).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Geçersiz uçuş ID değeri: " + e.getMessage())
                           .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Personel sayısı hesaplanırken bir hata oluştu: " + e.getMessage())
                           .build();
        }
    }
}
