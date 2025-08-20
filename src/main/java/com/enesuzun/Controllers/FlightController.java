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
import com.enesuzun.messaging.KafkaProducerService;
import com.enesuzun.messaging.FlightMessage;
import com.enesuzun.messaging.FlightCrewMessage;

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
    
    @Inject
    KafkaProducerService kafkaProducerService;

    // Tüm uçuşları getir
    @GET//URL : GET /flights
    public Response getAllFlights() {
        try {
            List<Flight> flights = flightService.getAllFlights();
            List<FlightDto> flightDtos = flightMapper.toDtoList(flights);//Entity'leri DTO'lara dönüştürür
            return Response.ok(flightDtos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Uçuşlar getirilirken bir hata oluştu: " + e.getMessage())
                           .build();
        }
    }

    // ID ile uçuş getir
    @GET
    @Path("/{id}")//URL path'inde id parametresi (/flights/123)
    public Response getFlightById(@PathParam("id") Long id) {//@PathParam("id"): URL'deki {id}'yi metod parametresine bind eder.(bind etmek bağlamak anlammında kullanılıyor)
        try {
            Flight flight = flightService.getFlightById(id);
            if (flight != null) {
                FlightDto flightDto = flightMapper.toDto(flight);
                return Response.ok(flightDto).build();//Response.ok http 200
            }
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Bu '" + id + "' ID'ye sahip uçuş bulunamadı")
                           .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Geçersiz ID değeri: " + e.getMessage())
                           .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Uçuş getirilirken bir hata oluştu: " + e.getMessage())
                           .build();
        }
    }

    // Yeni uçuş ekle - Artık Kafka üzerinden asenkron
    @POST
    public Response addFlight(@Valid FlightDto flightDto) {
        try {
            // Kafka'ya mesaj gönder (asenkron işlem)
            FlightMessage flightMessage = FlightMessage.createMessage(
                flightDto.getFlightNumber(), 
                flightDto.getDepartureDateTime(), 
                null // crew listesi ayrı mesajlarla gönderilecek
            );
            
            kafkaProducerService.sendFlightMessage(flightMessage);
            
            // Başarılı yanıt döndür (Kafka'ya gönderildi, DB'ye kayıt asenkron olarak yapılacak)
            return Response.status(Response.Status.ACCEPTED)
                           .entity("Uçuş ekleme isteği başarıyla alındı. İşlem asenkron olarak gerçekleştirilecek.")
                           .build();
                           
        } catch (ConstraintViolationException e) {
            // Bean Validation hataları
            StringBuilder errorMessages = new StringBuilder("Validasyon hataları:\n");
            //ConstraintViolation: Bean Validation'ın hatalarını temsil eder.
            for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
                errorMessages.append("- ").append(violation.getMessage()).append("\n");//violation.getMessage(): Hata mesajını döndürür.
            }
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(errorMessages.toString() + "\nGönderilen veri: " + flightDto.toString())
                           .build();
        } catch (IllegalArgumentException e) {
            //Validasyon hatası gibi durumlarda
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Geçersiz uçuş verisi: " + e.getMessage()+"\n"+flightDto.toString())
                           .build();
        } catch (Exception e) {
            // Beklenmeyen tüm hatalar
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Uçuş ekleme mesajı gönderilirken bir hata oluştu: " + e.getMessage())
                           .build();
        }
        }

    // Uçuş güncelle - Artık Kafka üzerinden asenkron
    @PUT
    @Path("/{id}")
    public Response updateFlight(@PathParam("id") Long id, @Valid FlightDto updatedFlightDto) {
        try {
            // Önce uçuşun var olup olmadığını kontrol et
            Flight existingFlight = flightService.getFlightById(id);
            if (existingFlight == null) {
                return Response.status(Response.Status.NOT_FOUND)
                   .entity("Bu '" + id + "' ye sahip uçuş bulunamadi")
                   .build();
            }
            
            // Kafka'ya güncelleme mesajı gönder
            FlightMessage updateMessage = FlightMessage.updateMessage(
                id, 
                updatedFlightDto.getFlightNumber(), 
                updatedFlightDto.getDepartureDateTime()
            );
            
            kafkaProducerService.sendFlightMessage(updateMessage);
            
            return Response.status(Response.Status.ACCEPTED)
                           .entity("Uçuş güncelleme isteği başarıyla alındı. İşlem asenkron olarak gerçekleştirilecek.")
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
            //Validasyon hatası gibi durumlarda
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Geçersiz uçuş verisi: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Uçuş güncelleme mesajı gönderilirken bir hata oluştu: " + e.getMessage())
                           .build();
        }
    }
    // Uçuş sil - Artık Kafka üzerinden asenkron
    @DELETE//Http delete isteği
    @Path("/{id}")
    public Response deleteFlight(@PathParam("id") Long id) {
        try {
            // Önce uçuşun var olup olmadığını kontrol et
            Flight existingFlight = flightService.getFlightById(id);
            if(existingFlight == null){
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Bu '" + id + "' ye sahip uçuş bulunamadi")
                        .build();
            }
            
            // Kafka'ya silme mesajı gönder
            FlightMessage deleteMessage = FlightMessage.deleteMessage(id);
            kafkaProducerService.sendFlightMessage(deleteMessage);
            
            return Response.status(Response.Status.ACCEPTED)
                    .entity("Uçuş silme isteği başarıyla alındı. İşlem asenkron olarak gerçekleştirilecek.")
                    .build();
                    
        }catch (IllegalArgumentException e) {
            //Validasyon hatası gibi durumlarda
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Geçersiz uçuş verisi: " + e.getMessage())
                    .build();
        }catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Calısma zamanı hatasi : " + e.getMessage())
                    .build();
        }catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Uçuş silme mesajı gönderilirken bir hata oluştu: " + e.getMessage())
                    .build();
        }
    }

    // Flight entity'sine crew ekle - Artık Kafka üzerinden asenkron
    @POST
    @Path("/{flightId}/add-crew")//URL path'inde flightId parametresi (/flights/123/add-crew)
    public Response addCrewToFlight(@PathParam("flightId") Long flightId, @Valid FlightCrewDto crewDto) {//@PathParam("flightId"): URL'deki {flightId}'yi metod parametresine bind eder.
        try {
            // Önce uçuşun var olup olmadığını kontrol et
            Flight existingFlight = flightService.getFlightById(flightId);
            if (existingFlight == null) {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("Bu '" + flightId + "' ID'ye sahip uçuş bulunamadı")
                               .build();
            }
            
            // Kafka'ya crew ekleme mesajı gönder
            FlightCrewMessage crewMessage = FlightCrewMessage.createMessage(
                crewDto.getCrewName(),
                crewDto.getCrewType(),
                flightId,
                existingFlight.getFlightNumber()
            );
            
            kafkaProducerService.sendCrewMessage(crewMessage);
            
            return Response.status(Response.Status.ACCEPTED)
                           .entity("Personel ekleme isteği başarıyla alındı. İşlem asenkron olarak gerçekleştirilecek.")
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
                           .entity("Personel ekleme mesajı gönderilirken bir hata oluştu: " + e.getMessage())
                           .build();
        }
    }
}
