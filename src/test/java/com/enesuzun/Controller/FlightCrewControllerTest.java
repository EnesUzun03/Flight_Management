package com.enesuzun.Controller;

import com.enesuzun.Controllers.FlightCrewController;
import com.enesuzun.Entity.CrewType;
import com.enesuzun.Entity.Flight;
import com.enesuzun.Entity.FlightCrew;
import com.enesuzun.Services.FlightCrewService;
import com.enesuzun.dto.FlightCrewDto;
import com.enesuzun.mapper.FlightCrewMapper;
import com.enesuzun.Controllers.FlightController;

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
import jakarta.validation.ConstraintViolation;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
import java.util.Set;

// import static io.restassured.RestAssured.when; ❌
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FlightCrewController Unit Tests")
public class FlightCrewControllerTest {
    
    @InjectMocks
    private FlightCrewController flightCrewController;

    @Mock
    private FlightCrewService flightCrewService;

    @Mock
    private FlightCrewMapper flightCrewMapper;

    // Test data
    private FlightCrew sampleCrew;
    private FlightCrewDto sampleCrewDto;
    private Flight sampleFlight;
    private List<FlightCrew> sampleCrewList;
    private List<FlightCrewDto> sampleCrewDtoList;

    @BeforeEach
    void setup() {
        // Sample Flight
        sampleFlight = new Flight();
        
        // Sample FlightCrew
        sampleCrew = new FlightCrew();
        sampleCrew.setId(1L);
        sampleCrew.setCrewName("Test Pilot");
        sampleCrew.setCrewType(CrewType.PILOT);
        sampleCrew.setFlight(sampleFlight);
        
        // Sample FlightCrewDto
        sampleCrewDto = new FlightCrewDto();
        sampleCrewDto.setId(1L);
        sampleCrewDto.setCrewName("Test Pilot");
        sampleCrewDto.setCrewType(CrewType.PILOT);
        sampleCrewDto.setFlightId(1L);
        
        // Sample Lists
        sampleCrewList = Arrays.asList(sampleCrew);
        sampleCrewDtoList = Arrays.asList(sampleCrewDto);
    }

    @Nested
    @DisplayName("getAllCrews() Tests")
    class GetAllCrewsTests {

        @Test
        @DisplayName("Tüm personelleri başarıyla getir")
        void getAllCrews_Success_Returns200AndDtoList() {
            // Arrange
            when(flightCrewService.getAllCrews()).thenReturn(sampleCrewList);
            when(flightCrewMapper.toDtoList(sampleCrewList)).thenReturn(sampleCrewDtoList);

            // Act
            Response response = flightCrewController.getAllCrews();

            // Assert
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity() instanceof List);
            
            // Verify
            verify(flightCrewService).getAllCrews();
            verify(flightCrewMapper).toDtoList(sampleCrewList);
        }

        @Test
        @DisplayName("Service exception durumunda 500 döner")
        void getAllCrews_ServiceThrowsException_Returns500() {
            // Arrange
            when(flightCrewService.getAllCrews()).thenThrow(new RuntimeException("Database error"));

            // Act
            Response response = flightCrewController.getAllCrews();

            // Assert
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("Personeller getirilirken bir hata oluştu"));
            
            // Verify
            verify(flightCrewService).getAllCrews();
            verify(flightCrewMapper, never()).toDtoList(any());
        }
    }

    @Nested
    @DisplayName("getCrewById() Tests")
    class GetCrewByIdTests {

        @Test
        @DisplayName("Geçerli ID ile personel getir - Başarılı")
        void getCrewById_ValidId_Returns200AndDto() {
            // Arrange
            Long crewId = 1L;
            when(flightCrewService.getCrewById(crewId)).thenReturn(sampleCrew);
            when(flightCrewMapper.toDto(sampleCrew)).thenReturn(sampleCrewDto);

            // Act
            Response response = flightCrewController.getCrewById(crewId);

            // Assert
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity() instanceof FlightCrewDto);
            
            // Verify
            verify(flightCrewService).getCrewById(crewId);
            verify(flightCrewMapper).toDto(sampleCrew);
        }

        @Test
        @DisplayName("Olmayan ID ile personel getir - 404 döner")
        void getCrewById_NotFound_Returns404() {
            // Arrange
            Long crewId = 999L;
            when(flightCrewService.getCrewById(crewId)).thenReturn(null);

            // Act
            Response response = flightCrewController.getCrewById(crewId);

            // Assert
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("999"));
            
            // Verify
            verify(flightCrewService).getCrewById(crewId);
            verify(flightCrewMapper, never()).toDto(any());
        }

        @Test
        @DisplayName("Geçersiz ID ile personel getir - 400 döner")
        void getCrewById_IllegalArgument_Returns400() {
            // Arrange
            Long invalidId = -1L;
            when(flightCrewService.getCrewById(invalidId))
                .thenThrow(new IllegalArgumentException("Invalid ID"));

            // Act
            Response response = flightCrewController.getCrewById(invalidId);

            // Assert
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("Geçersiz ID değeri"));
            
            // Verify
            verify(flightCrewService).getCrewById(invalidId);
        }

        @Test
        @DisplayName("Service exception durumunda 500 döner")
        void getCrewById_ServiceException_Returns500() {
            // Arrange
            Long crewId = 1L;
            when(flightCrewService.getCrewById(crewId))
                .thenThrow(new RuntimeException("Database connection failed"));

            // Act
            Response response = flightCrewController.getCrewById(crewId);

            // Assert
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("Personel getirilirken bir hata oluştu"));
        }
    }

    @Nested
    @DisplayName("addCrew() Tests")
    class AddCrewTests {

        @Test
        @DisplayName("Geçerli veri ile personel ekleme - Başarılı")
        void addCrew_ValidData_Returns201AndDto() {
            // Arrange
            when(flightCrewMapper.toEntity(sampleCrewDto)).thenReturn(sampleCrew);
            doNothing().when(flightCrewService).addCrew(sampleCrew);
            when(flightCrewMapper.toDto(sampleCrew)).thenReturn(sampleCrewDto);

            // Act
            Response response = flightCrewController.addCrew(sampleCrewDto);

            // Assert
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity() instanceof FlightCrewDto);
            
            // Verify
            verify(flightCrewMapper).toEntity(sampleCrewDto);
            verify(flightCrewService).addCrew(sampleCrew);
            verify(flightCrewMapper).toDto(sampleCrew);
        }

        @Test
        @DisplayName("Validation hatası durumunda 400 döner")
        void addCrew_ValidationError_Returns400() {
            // Arrange
            Set<ConstraintViolation<?>> violations = Set.of();
            ConstraintViolationException exception = new ConstraintViolationException("Validation failed", violations);
            
            when(flightCrewMapper.toEntity(sampleCrewDto)).thenReturn(sampleCrew);
            doThrow(exception).when(flightCrewService).addCrew(sampleCrew);

            // Act
            Response response = flightCrewController.addCrew(sampleCrewDto);

            // Assert
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("Validasyon hataları"));
        }

        @Test
        @DisplayName("IllegalArgument exception durumunda 400 döner")
        void addCrew_IllegalArgument_Returns400() {
            // Arrange
            when(flightCrewMapper.toEntity(sampleCrewDto)).thenReturn(sampleCrew);
            doThrow(new IllegalArgumentException("Invalid crew data"))
                .when(flightCrewService).addCrew(sampleCrew);

            // Act
            Response response = flightCrewController.addCrew(sampleCrewDto);

            // Assert
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("Geçersiz personel verisi"));
        }

        @Test
        @DisplayName("Runtime exception durumunda 400 döner")
        void addCrew_RuntimeException_Returns400() {
            // Arrange
            when(flightCrewMapper.toEntity(sampleCrewDto)).thenReturn(sampleCrew);
            doThrow(new RuntimeException("Runtime error"))
                .when(flightCrewService).addCrew(sampleCrew);

            // Act
            Response response = flightCrewController.addCrew(sampleCrewDto);

            // Assert
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("Çalışma zamanı hatası"));
        }

        @Test
        @DisplayName("Generic exception durumunda 500 döner")
        void addCrew_GenericException_Returns500() {
            // Arrange
            when(flightCrewMapper.toEntity(sampleCrewDto))
                .thenThrow(new RuntimeException("Unexpected error"));

            // Act
            Response response = flightCrewController.addCrew(sampleCrewDto);

            // Assert
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("Çalışma zamanı hatası"));
        }
    }

    @Nested
    @DisplayName("updateCrew() Tests")
    class UpdateCrewTests {

        @Test
        @DisplayName("Geçerli veri ile personel güncelleme - Başarılı")
        void updateCrew_ValidData_Returns200() {
            // Arrange
            Long crewId = 1L;
            when(flightCrewService.getCrewById(crewId)).thenReturn(sampleCrew);
            when(flightCrewService.updateCrew(eq(crewId), eq(sampleCrewDto.getCrewName()), 
                eq(sampleCrewDto.getCrewType()), any(Flight.class))).thenReturn(true);

            // Act
            Response response = flightCrewController.updateCrew(crewId, sampleCrewDto);

            // Assert
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("güncelleme işlemi başarılı"));
            
            // Verify
            verify(flightCrewService).getCrewById(crewId);
            verify(flightCrewService).updateCrew(eq(crewId), eq(sampleCrewDto.getCrewName()), 
                eq(sampleCrewDto.getCrewType()), any(Flight.class));
        }

        @Test
        @DisplayName("Olmayan ID ile personel güncelleme - 404 döner")
        void updateCrew_NotFound_Returns404() {
            // Arrange
            Long crewId = 999L;
            when(flightCrewService.getCrewById(crewId)).thenReturn(null);

            // Act
            Response response = flightCrewController.updateCrew(crewId, sampleCrewDto);

            // Assert
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("999"));
            
            // Verify
            verify(flightCrewService).getCrewById(crewId);
            verify(flightCrewService, never()).updateCrew(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Update başarısız durumunda 500 döner")
        void updateCrew_UpdateFailed_Returns500() {
            // Arrange
            Long crewId = 1L;
            when(flightCrewService.getCrewById(crewId)).thenReturn(sampleCrew);
            when(flightCrewService.updateCrew(eq(crewId), eq(sampleCrewDto.getCrewName()), 
                eq(sampleCrewDto.getCrewType()), any(Flight.class))).thenReturn(false);

            // Act
            Response response = flightCrewController.updateCrew(crewId, sampleCrewDto);

            // Assert
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("Personel güncellenirken bir hata oluştu"));
        }
    }

    @Nested
    @DisplayName("deleteCrewByName() Tests")
    class DeleteCrewTests {

        @Test
        @DisplayName("Geçerli isim ile personel silme - Başarılı")
        void deleteCrewByName_ValidName_Returns202() {
            // Arrange
            String crewName = "Test Personel";
            when(flightCrewService.deleteCrewByName(crewName)).thenReturn(true);

            // Act
            Response response = flightCrewController.deleteCrewByName(crewName);

            // Assert
            assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("başarıyla silindi"));
            
            // Verify
            verify(flightCrewService).deleteCrewByName(crewName);
        }

        @Test
        @DisplayName("Olmayan isim ile personel silme - 404 döner")
        void deleteCrewByName_NotFound_Returns404() {
            // Arrange
            String crewName = "Olmayan Personel";
            when(flightCrewService.deleteCrewByName(crewName)).thenReturn(false);

            // Act
            Response response = flightCrewController.deleteCrewByName(crewName);

            // Assert
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("bulunamadı"));
            
            // Verify
            verify(flightCrewService).deleteCrewByName(crewName);
        }

        @Test
        @DisplayName("IllegalArgument exception durumunda 400 döner")
        void deleteCrewByName_IllegalArgument_Returns400() {
            // Arrange
            String invalidName = "";
            when(flightCrewService.deleteCrewByName(invalidName))
                .thenThrow(new IllegalArgumentException("Invalid name"));

            // Act
            Response response = flightCrewController.deleteCrewByName(invalidName);

            // Assert
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("Geçersiz personel adı"));
        }
    }

    @Nested
    @DisplayName("getCrewsByFlightId() Tests")
    class GetCrewsByFlightIdTests {

        @Test
        @DisplayName("Geçerli flight ID ile personelleri getir")
        void getCrewsByFlightId_ValidId_Returns200AndList() {
            // Arrange
            Long flightId = 1L;
            when(flightCrewService.getCrewsByFlightId(flightId)).thenReturn(sampleCrewList);
            when(flightCrewMapper.toDtoList(sampleCrewList)).thenReturn(sampleCrewDtoList);

            // Act
            Response response = flightCrewController.getCrewsByFlightId(flightId);

            // Assert
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity() instanceof List);
            
            // Verify
            verify(flightCrewService).getCrewsByFlightId(flightId);
            verify(flightCrewMapper).toDtoList(sampleCrewList);
        }

        @Test
        @DisplayName("Geçersiz flight ID ile 400 döner")
        void getCrewsByFlightId_InvalidId_Returns400() {
            // Arrange
            Long invalidId = -1L;
            when(flightCrewService.getCrewsByFlightId(invalidId))
                .thenThrow(new IllegalArgumentException("Invalid flight ID"));

            // Act
            Response response = flightCrewController.getCrewsByFlightId(invalidId);

            // Assert
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("Geçersiz uçuş ID değeri"));
        }
    }

    @Nested
    @DisplayName("getCrewsByFlightIdAndType() Tests")
    class GetCrewsByFlightIdAndTypeTests {

        @Test
        @DisplayName("Geçerli parametreler ile personelleri getir")
        void getCrewsByFlightIdAndType_ValidParams_Returns200AndList() {
            // Arrange
            Long flightId = 1L;
            String crewType = "PILOT";
            when(flightCrewService.getCrewsByFlightIdAndType(flightId, CrewType.PILOT))
                .thenReturn(sampleCrewList);
            when(flightCrewMapper.toDtoList(sampleCrewList)).thenReturn(sampleCrewDtoList);

            // Act
            Response response = flightCrewController.getCrewsByFlightIdAndType(flightId, crewType);

            // Assert
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity() instanceof List);
            
            // Verify
            verify(flightCrewService).getCrewsByFlightIdAndType(flightId, CrewType.PILOT);
            verify(flightCrewMapper).toDtoList(sampleCrewList);
        }

        @Test
        @DisplayName("Geçersiz crew type ile 400 döner")
        void getCrewsByFlightIdAndType_InvalidCrewType_Returns400() {
            // Arrange
            Long flightId = 1L;
            String invalidCrewType = "INVALID_TYPE";

            // Act
            Response response = flightCrewController.getCrewsByFlightIdAndType(flightId, invalidCrewType);

            // Assert
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("Geçersiz parametre değeri"));
        }
    }

    @Nested
    @DisplayName("getCrewsByName() Tests")
    class GetCrewsByNameTests {

        @Test
        @DisplayName("Geçerli isim ile personel arama")
        void getCrewsByName_ValidName_Returns200AndList() {
            // Arrange
            String crewName = "Test";
            when(flightCrewService.getCrewsByName(crewName)).thenReturn(sampleCrewList);
            when(flightCrewMapper.toDtoList(sampleCrewList)).thenReturn(sampleCrewDtoList);

            // Act
            Response response = flightCrewController.getCrewsByName(crewName);

            // Assert
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity() instanceof List);
            
            // Verify
            verify(flightCrewService).getCrewsByName(crewName);
            verify(flightCrewMapper).toDtoList(sampleCrewList);
        }

        @Test
        @DisplayName("Geçersiz isim ile 400 döner")
        void getCrewsByName_InvalidName_Returns400() {
            // Arrange
            String invalidName = "";
            when(flightCrewService.getCrewsByName(invalidName))
                .thenThrow(new IllegalArgumentException("Invalid name"));

            // Act
            Response response = flightCrewController.getCrewsByName(invalidName);

            // Assert
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("Geçersiz personel adı"));
        }
    }

    @Nested
    @DisplayName("countCrewsByFlightId() Tests")
    class CountCrewsByFlightIdTests {

        @Test
        @DisplayName("Geçerli flight ID ile personel sayısı")
        void countCrewsByFlightId_ValidId_Returns200AndCount() {
            // Arrange
            Long flightId = 1L;
            long expectedCount = 5L;
            when(flightCrewService.countCrewsByFlightId(flightId)).thenReturn(expectedCount);

            // Act
            Response response = flightCrewController.countCrewsByFlightId(flightId);

            // Assert
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals(expectedCount, response.getEntity());
            
            // Verify
            verify(flightCrewService).countCrewsByFlightId(flightId);
        }

        @Test
        @DisplayName("Geçersiz flight ID ile 400 döner")
        void countCrewsByFlightId_InvalidId_Returns400() {
            // Arrange
            Long invalidId = -1L;
            when(flightCrewService.countCrewsByFlightId(invalidId))
                .thenThrow(new IllegalArgumentException("Invalid flight ID"));

            // Act
            Response response = flightCrewController.countCrewsByFlightId(invalidId);

            // Assert
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("Geçersiz uçuş ID değeri"));
        }

        @Test
        @DisplayName("Service exception durumunda 500 döner")
        void countCrewsByFlightId_ServiceException_Returns500() {
            // Arrange
            Long flightId = 1L;
            when(flightCrewService.countCrewsByFlightId(flightId))
                .thenThrow(new RuntimeException("Database error"));

            // Act
            Response response = flightCrewController.countCrewsByFlightId(flightId);

            // Assert
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity().toString().contains("Personel sayısı hesaplanırken bir hata oluştu"));
        }
    }
}