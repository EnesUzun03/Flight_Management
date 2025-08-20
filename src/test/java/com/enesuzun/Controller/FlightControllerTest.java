package com.enesuzun.Controller;

import com.enesuzun.Controllers.FlightController;

import com.enesuzun.Entity.CrewType;
import com.enesuzun.Entity.Flight;
import com.enesuzun.Services.FlightService;
import com.enesuzun.dto.FlightCrewDto;
import com.enesuzun.dto.FlightDto;
import com.enesuzun.mapper.FlightCrewMapper;
import com.enesuzun.mapper.FlightMapper;
import com.enesuzun.messaging.FlightMessage;
import com.enesuzun.messaging.FlightCrewMessage;
import com.enesuzun.messaging.KafkaProducerService;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

// import static io.restassured.RestAssured.when; ❌
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("FlightController Unit Tests")
public class FlightControllerTest {

    @InjectMocks
    private FlightController flightController;// Mockito, @Inject alanlarına mock'ları basar

    @Mock private FlightService flightService;
    @Mock private FlightMapper flightMapper;
    @Mock private FlightCrewMapper flightCrewMapper;
    @Mock private KafkaProducerService kafkaProducerService;

    Flight sampleFlight;
    FlightDto sampleFlightDto;

    @BeforeEach
    void setup(){
        // Test nesnelerini her test öncesi hazırla
        sampleFlight = new Flight();
        sampleFlight.setFlightNumber("TK123");
        
        sampleFlightDto = new FlightDto();
        sampleFlightDto.setFlightNumber("TK123");
        sampleFlightDto.setDepartureDateTime(LocalDateTime.of(2024, 1, 15, 10, 30));
        // Mock stubbing'leri sadece gerektiğinde test metodlarının içinde yapacağız
    }

    //------Testler-----

    @Test
    void getallFlights_ok_returns200andDtoList(){
        //arrange-hazırlık evresi
        List<Flight> flights = List.of(new Flight(),new Flight());
        when(flightService.getAllFlights()).thenReturn(flights);
        when(flightMapper.toDtoList(flights)).thenReturn(List.of(new FlightDto(),new FlightDto()));

        //Act-eylem evresi (Controller'ın davranışı eylemdir.Bu test hangi donk için yazıldıysa o fonk eylemdir)
        Response resp = flightController.getAllFlights();
        //buradan bir sonuc döndü bu sonucu doğruluyoruz
        //assert - doğrulama evresi
        assertEquals(Response.Status.OK.getStatusCode(),resp.getStatus());
        //farklı yol assertEquals(200, resp.getStatus());
        assertTrue(resp.getEntity() instanceof List);

        //verify-doğrulama evresi(mock'ların çağrılıp çağrılmadığını kontrol eder)
        verify(flightService).getAllFlights();
        verify(flightMapper).toDtoList(flights);
    }

    @Test
    void getFlightById_found_returns200andDto(){
        //arrange-hazırlık evresi
        when(flightService.getFlightById(1L)).thenReturn(sampleFlight);
        when(flightMapper.toDto(sampleFlight)).thenReturn(sampleFlightDto);

        //act - eylem evresi
        Response resp = flightController.getFlightById(1L);
        
        //assert - doğrulama evresi
        assertEquals(Response.Status.OK.getStatusCode(),resp.getStatus());
        assertTrue(resp.getEntity() instanceof FlightDto);

        //verify - doğrulama evresi
        verify(flightService).getFlightById(1L);
        verify(flightMapper).toDto(sampleFlight);
    }

    @Test
    void getFlightById_notFound_returns404() {
        when(flightService.getFlightById(99L)).thenReturn(null);

        Response resp = flightController.getFlightById(99L);

        assertEquals(404, resp.getStatus());
        assertTrue(resp.getEntity().toString().contains("99"));
    }

    @Test
    void getFlightById_illegalArg_returns400() {
        when(flightService.getFlightById(-1L)).thenThrow(new IllegalArgumentException("negative"));

        Response resp = flightController.getFlightById(-1L);

        assertEquals(400, resp.getStatus());
    }

    @Test
    void addFlight_ok_accepted202_andKafkaSent() {
        // kafkaProducerService.sendFlightMessage(...) başarılı olur (default: doNothing)

        Response resp = flightController.addFlight(sampleFlightDto);

        assertEquals(202, resp.getStatus());

        ArgumentCaptor<FlightMessage> captor = ArgumentCaptor.forClass(FlightMessage.class);
        verify(kafkaProducerService).sendFlightMessage(captor.capture());
        assertEquals("TK123", captor.getValue().getFlightNumber());
    }

    @Test
    void addFlight_validationError_returns400() {
        doThrow(new ConstraintViolationException("invalid", Set.of()))
                .when(kafkaProducerService).sendFlightMessage(any());

        Response resp = flightController.addFlight(sampleFlightDto);

        assertEquals(400, resp.getStatus());
        assertTrue(resp.getEntity().toString().contains("Validasyon"));
    }

    @Test
    void updateFlight_notFound_returns404() {
        when(flightService.getFlightById(5L)).thenReturn(null);

        Response resp = flightController.updateFlight(5L, sampleFlightDto);

        assertEquals(404, resp.getStatus());
    }

    @Test
    void updateFlight_ok_accepted202_andKafkaSent() {
        when(flightService.getFlightById(7L)).thenReturn(sampleFlight);

        Response resp = flightController.updateFlight(7L, sampleFlightDto);

        assertEquals(202, resp.getStatus());
        verify(kafkaProducerService).sendFlightMessage(any(FlightMessage.class));
    }

    @Test
    void deleteFlight_notFound_returns404() {
        when(flightService.getFlightById(10L)).thenReturn(null);

        Response resp = flightController.deleteFlight(10L);

        assertEquals(404, resp.getStatus());
    }

    @Test
    void deleteFlight_ok_accepted202_andKafkaSent() {
        when(flightService.getFlightById(10L)).thenReturn(sampleFlight);

        Response resp = flightController.deleteFlight(10L);

        assertEquals(202, resp.getStatus());
        verify(kafkaProducerService).sendFlightMessage(any(FlightMessage.class));
    }

    @Test
    void addCrewToFlight_ok_accepted202() {
        when(flightService.getFlightById(1L)).thenReturn(sampleFlight);
        // existingFlight.getFlightNumber() kullanılıyor; kaptan numarası lazımsa mockla
        // burada sampleFlight.getFlightNumber() bilinmiyor ama FlightCrewMessage oluşturma yine de çalışır

        FlightCrewDto crewDto = mock(FlightCrewDto.class);
        when(crewDto.getCrewName()).thenReturn("Ali");
        when(crewDto.getCrewType()).thenReturn(CrewType.PILOT);

        Response resp = flightController.addCrewToFlight(1L, crewDto);

        assertEquals(202, resp.getStatus());
        verify(kafkaProducerService).sendCrewMessage(any(FlightCrewMessage.class));
    }

    @Test
    void addCrewToFlight_notFound_returns404() {
        when(flightService.getFlightById(2L)).thenReturn(null);

        FlightCrewDto crewDto = mock(FlightCrewDto.class);
        Response resp = flightController.addCrewToFlight(2L, crewDto);

        assertEquals(404, resp.getStatus());
    }
}


