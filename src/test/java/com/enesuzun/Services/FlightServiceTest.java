package com.enesuzun.Services;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

import com.enesuzun.Entity.Flight;
import com.enesuzun.Entity.FlightCrew;
import com.enesuzun.Entity.CrewType;
import com.enesuzun.Repositories.FlightCrewRepository;
import com.enesuzun.Repositories.FlightRepository;

@QuarkusTest
@DisplayName("FlightService Kapsamlı Unit Testleri")
public class FlightServiceTest {

    // Mock Dependencies - Gerçek veritabanı kullanmadan test yapar
    @InjectMock
    FlightRepository flightRepository;

    @InjectMock
    FlightCrewRepository flightCrewRepository;

    @InjectMock
    FlightCrewService flightCrewService;

    // Test edilecek servis - Gerçek instance inject edilir
    @Inject
    FlightService flightService;

    // Test verileri
    private Flight testFlight;
    private FlightCrew testCrew;
    private List<FlightCrew> testCrewList;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setup() {
        // Test zamanı oluştur
        testDateTime = LocalDateTime.of(2024, 6, 15, 14, 30);
        
        // Test uçuşu oluştur
        testFlight = new Flight();
        testFlight.setFlightNumber("TK101");
        
        // Test crew üyesi oluştur
        testCrew = new FlightCrew();
        
        // Test crew listesi oluştur
        FlightCrew pilot = new FlightCrew();
        FlightCrew steward = new FlightCrew();
        testCrewList = Arrays.asList(pilot, steward);
    }

    @Nested
    @DisplayName("addFlight() Metodları Testleri")
    class AddFlightTests {

        @Test
        @DisplayName("Flight objesi ile uçuş ekleme - Başarılı")
        void testAddFlight_WithFlightObject_Success() {
            // Given
            Flight newFlight = new Flight();
            doNothing().when(flightRepository).persist(eq(newFlight));
            
            // When
            flightService.addFlight(newFlight);
            
            // Then
            verify(flightRepository, times(1)).persist(newFlight);
        }

        @Test
        @DisplayName("Flight objesi null ise persist çağrılmalı")
        void testAddFlight_WithNullFlight_ShouldStillCallPersist() {
            // Given
            Flight nullFlight = null;
            doNothing().when(flightRepository).persist(nullFlight);
            
            // When
            flightService.addFlight(nullFlight);
            
            // Then
            verify(flightRepository, times(1)).persist(nullFlight);
        }

        @Test
        @DisplayName("Parametrelerle uçuş ekleme - Başarılı")
        void testAddFlight_WithParameters_Success() {
            // Given
            String flightNumber = "TK123";
            LocalDateTime departureTime = testDateTime;
            List<FlightCrew> crews = testCrewList;
            
            doNothing().when(flightRepository).persist(any(Flight.class));
            
            // When
            flightService.addFlight(flightNumber, departureTime, crews);
            
            // Then
            verify(flightRepository, times(1)).persist(any(Flight.class));
        }

        @Test
        @DisplayName("Parametrelerle uçuş ekleme - Boş crew listesi")
        void testAddFlight_WithParameters_EmptyCrewList() {
            // Given
            String flightNumber = "TK456";
            LocalDateTime departureTime = testDateTime;
            List<FlightCrew> emptyCrews = Collections.emptyList();
            
            doNothing().when(flightRepository).persist(any(Flight.class));
            
            // When
            flightService.addFlight(flightNumber, departureTime, emptyCrews);
            
            // Then
            verify(flightRepository, times(1)).persist(any(Flight.class));
        }
    }

    @Nested
    @DisplayName("getAllFlights() Metodu Testleri")
    class GetAllFlightsTests {

        @Test
        @DisplayName("Tüm uçuşları başarıyla getir")
        void testGetAllFlights_Success() {
            // Given
            List<Flight> expectedFlights = Arrays.asList(testFlight, new Flight());
            when(flightRepository.listAll()).thenReturn(expectedFlights);
            
            // When
            List<Flight> result = flightService.getAllFlights();
            
            // Then
            assertNotNull(result, "Sonuç null olmamalıdır");
            assertEquals(2, result.size(), "2 uçuş dönmelidir");
            assertEquals(expectedFlights, result, "Dönen liste beklenen liste olmalıdır");
            verify(flightRepository).listAll();
        }

        @Test
        @DisplayName("Boş uçuş listesi")
        void testGetAllFlights_EmptyList() {
            // Given
            when(flightRepository.listAll()).thenReturn(Collections.emptyList());
            
            // When
            List<Flight> result = flightService.getAllFlights();
            
            // Then
            assertNotNull(result, "Sonuç null olmamalıdır");
            assertTrue(result.isEmpty(), "Liste boş olmalıdır");
            verify(flightRepository).listAll();
        }
    }

    @Nested
    @DisplayName("getFlightById() Metodu Testleri")
    class GetFlightByIdTests {

        @Test
        @DisplayName("ID ile uçuş bulma - Başarılı")
        void testGetFlightById_Found() {
            // Given
            Long flightId = 1L;
            when(flightRepository.findById(flightId)).thenReturn(testFlight);
            
            // When
            Flight result = flightService.getFlightById(flightId);
            
            // Then
            assertNotNull(result, "Uçuş bulunmalıdır");
            assertEquals(testFlight, result, "Dönen uçuş test uçuşu olmalıdır");
            verify(flightRepository).findById(flightId);
        }

        @Test
        @DisplayName("ID ile uçuş bulma - Bulunamadı")
        void testGetFlightById_NotFound() {
            // Given
            Long flightId = 999L;
            when(flightRepository.findById(flightId)).thenReturn(null);
            
            // When
            Flight result = flightService.getFlightById(flightId);
            
            // Then
            assertNull(result, "Olmayan ID için null dönmelidir");
            verify(flightRepository).findById(flightId);
        }

        @Test
        @DisplayName("Null ID ile uçuş bulma")
        void testGetFlightById_NullId() {
            // Given
            Long nullId = null;
            when(flightRepository.findById(nullId)).thenReturn(null);
            
            // When
            Flight result = flightService.getFlightById(nullId);
            
            // Then
            assertNull(result, "Null ID için null dönmelidir");
            verify(flightRepository).findById(nullId);
        }
    }

    @Nested
    @DisplayName("updateFlight() Metodu Testleri")
    class UpdateFlightTests {

        @Test
        @DisplayName("Uçuş güncelleme - Başarılı")
        void testUpdateFlight_Success() {
            // Given
            Long flightId = 1L;
            String newFlightNumber = "TK999";
            LocalDateTime newDateTime = testDateTime.plusHours(2);
            
            when(flightRepository.findById(flightId)).thenReturn(testFlight);
            doNothing().when(flightRepository).persist(testFlight);
            
            // When
            flightService.updateFlight(flightId, newFlightNumber, newDateTime);
            
            // Then
            verify(flightRepository).findById(flightId);
            verify(flightRepository).persist(testFlight);
        }

        @Test
        @DisplayName("Uçuş güncelleme - Uçuş bulunamadı")
        void testUpdateFlight_FlightNotFound() {
            // Given
            Long flightId = 999L;
            String newFlightNumber = "TK999";
            LocalDateTime newDateTime = testDateTime;
            
            when(flightRepository.findById(flightId)).thenReturn(null);
            
            // When
            flightService.updateFlight(flightId, newFlightNumber, newDateTime);
            
            // Then
            verify(flightRepository).findById(flightId);
            // persist() çağrılmamalı çünkü flight null
            verify(flightRepository, never()).persist(any(Flight.class));
        }

        @Test
        @DisplayName("Uçuş güncelleme - Null parametreler")
        void testUpdateFlight_NullParameters() {
            // Given
            Long flightId = 1L;
            String nullFlightNumber = null;
            LocalDateTime nullDateTime = null;
            
            when(flightRepository.findById(flightId)).thenReturn(testFlight);
            doNothing().when(flightRepository).persist(testFlight);
            
            // When
            flightService.updateFlight(flightId, nullFlightNumber, nullDateTime);
            
            // Then
            verify(flightRepository).findById(flightId);
            verify(flightRepository).persist(testFlight);
        }
    }

    @Nested
    @DisplayName("deleteFlight() Metodu Testleri")
    class DeleteFlightTests {

        @Test
        @DisplayName("Uçuş silme - Crew'lar ile birlikte başarılı")
        void testDeleteFlight_WithCrews_Success() {
            // Given
            Long flightId = 1L;
            FlightCrew crew1 = new FlightCrew();
            FlightCrew crew2 = new FlightCrew();
            List<FlightCrew> crews = Arrays.asList(crew1, crew2);
            
            when(flightCrewRepository.findByFlightId(flightId)).thenReturn(crews);
            when(flightCrewRepository.deleteById(any())).thenReturn(true);
            when(flightRepository.deleteById(flightId)).thenReturn(true);
            
            // When
            flightService.deleteFlight(flightId);
            
            // Then
            verify(flightCrewRepository).findByFlightId(flightId);
            verify(flightCrewRepository, times(2)).deleteById(any());
            verify(flightRepository).deleteById(flightId);
        }

        @Test
        @DisplayName("Uçuş silme - Crew'sız uçuş")
        void testDeleteFlight_NoCrews() {
            // Given
            Long flightId = 2L;
            when(flightCrewRepository.findByFlightId(flightId)).thenReturn(Collections.emptyList());
            when(flightRepository.deleteById(flightId)).thenReturn(true);
            
            // When
            flightService.deleteFlight(flightId);
            
            // Then
            verify(flightCrewRepository).findByFlightId(flightId);
            verify(flightCrewRepository, never()).deleteById(any());
            verify(flightRepository).deleteById(flightId);
        }
    }

    @Nested
    @DisplayName("findByFlightNumber() Metodu Testleri")
    class FindByFlightNumberTests {

        @Test
        @DisplayName("Uçuş numarası ile bulma - Başarılı")
        void testFindByFlightNumber_Found() {
            // Given
            String flightNumber = "TK123";
            when(flightRepository.findByFlightNumber(flightNumber)).thenReturn(testFlight);
            
            // When
            Flight result = flightService.findByFlightNumber(flightNumber);
            
            // Then
            assertNotNull(result, "Uçuş bulunmalıdır");
            assertEquals(testFlight, result, "Dönen uçuş test uçuşu olmalıdır");
            verify(flightRepository).findByFlightNumber(flightNumber);
        }

        @Test
        @DisplayName("Uçuş numarası ile bulma - Bulunamadı")
        void testFindByFlightNumber_NotFound() {
            // Given
            String flightNumber = "OLMAYAN123";
            when(flightRepository.findByFlightNumber(flightNumber)).thenReturn(null);
            
            // When
            Flight result = flightService.findByFlightNumber(flightNumber);
            
            // Then
            assertNull(result, "Olmayan uçuş numarası için null dönmelidir");
            verify(flightRepository).findByFlightNumber(flightNumber);
        }
    }

    @Nested
    @DisplayName("findByFlightDepartureDateTime() Metodu Testleri")
    class FindByFlightDepartureDateTimeTests {

        @Test
        @DisplayName("Uçuş numarası ile kalkış zamanı bulma - Başarılı")
        void testFindByFlightDepartureDateTime_Found() {
            // Given
            String flightNumber = "TK123";
            LocalDateTime expectedDateTime = testDateTime;
            when(flightRepository.findByFlightDepartureDateTime(flightNumber)).thenReturn(expectedDateTime);
            
            // When
            LocalDateTime result = flightService.findByFlightDepartureDateTime(flightNumber);
            
            // Then
            assertNotNull(result, "Kalkış zamanı bulunmalıdır");
            assertEquals(expectedDateTime, result, "Dönen zaman beklenen zaman olmalıdır");
            verify(flightRepository).findByFlightDepartureDateTime(flightNumber);
        }

        @Test
        @DisplayName("Uçuş numarası ile kalkış zamanı bulma - Bulunamadı")
        void testFindByFlightDepartureDateTime_NotFound() {
            // Given
            String flightNumber = "OLMAYAN123";
            when(flightRepository.findByFlightDepartureDateTime(flightNumber)).thenReturn(null);
            
            // When
            LocalDateTime result = flightService.findByFlightDepartureDateTime(flightNumber);
            
            // Then
            assertNull(result, "Olmayan uçuş için null dönmelidir");
            verify(flightRepository).findByFlightDepartureDateTime(flightNumber);
        }
    }

    @Nested
    @DisplayName("findByFlightCrewList() Metodu Testleri")
    class FindByFlightCrewListTests {

        @Test
        @DisplayName("Uçuştaki personel listesini bulma - Başarılı")
        void testFindByFlightCrewList_Found() {
            // Given
            when(flightRepository.findByFlightCrewList(any(String.class))).thenReturn(testCrewList);
            
            // When
            List<FlightCrew> result = flightService.findByFlightCrewList(testFlight);
            
            // Then
            assertNotNull(result, "Crew listesi bulunmalıdır");
            assertEquals(testCrewList.size(), result.size(), "Crew sayısı eşleşmelidir");
            assertEquals(testCrewList, result, "Dönen liste test listesi olmalıdır");
            verify(flightRepository).findByFlightCrewList(any(String.class));
        }

        @Test
        @DisplayName("Uçuştaki personel listesini bulma - Boş liste")
        void testFindByFlightCrewList_EmptyList() {
            // Given
            when(flightRepository.findByFlightCrewList(any(String.class))).thenReturn(Collections.emptyList());
            
            // When
            List<FlightCrew> result = flightService.findByFlightCrewList(testFlight);
            
            // Then
            assertNotNull(result, "Sonuç null olmamalıdır");
            assertTrue(result.isEmpty(), "Liste boş olmalıdır");
            verify(flightRepository).findByFlightCrewList(any(String.class));
        }
    }

    @Nested
    @DisplayName("addCrewToFlight() Metodu Testleri")
    class AddCrewToFlightTests {

        @Test
        @DisplayName("Uçuşa personel ekleme - Başarılı")
        void testAddCrewToFlight_Success() {
            // Given
            Long flightId = 1L;
            FlightCrew crew = new FlightCrew();
            
            when(flightRepository.findById(flightId)).thenReturn(testFlight);
            doNothing().when(flightCrewRepository).persist(any(FlightCrew.class));
            
            // When
            flightService.addCrewToFlight(flightId, crew);
            
            // Then
            verify(flightRepository).findById(flightId);
            verify(flightCrewRepository).persist(any(FlightCrew.class));
        }

        @Test
        @DisplayName("Uçuşa personel ekleme - Uçuş bulunamadı")
        void testAddCrewToFlight_FlightNotFound() {
            // Given
            Long flightId = 999L;
            FlightCrew crew = new FlightCrew();
            
            when(flightRepository.findById(flightId)).thenReturn(null);
            
            // When
            flightService.addCrewToFlight(flightId, crew);
            
            // Then
            verify(flightRepository).findById(flightId);
            // persist() çağrılmamalı çünkü flight null
            verify(flightCrewRepository, never()).persist(any(FlightCrew.class));
        }

        @Test
        @DisplayName("Uçuşa personel ekleme - Null crew")
        void testAddCrewToFlight_NullCrew() {
            // Given
            Long flightId = 1L;
            FlightCrew nullCrew = null;
            
            when(flightRepository.findById(flightId)).thenReturn(testFlight);
            doNothing().when(flightCrewRepository).persist(any(FlightCrew.class));
            
            // When & Then
            // Bu durumda NullPointerException beklenir
            assertThrows(NullPointerException.class, () -> {
                flightService.addCrewToFlight(flightId, nullCrew);
            });
            
            verify(flightRepository).findById(flightId);
        }
    }
}