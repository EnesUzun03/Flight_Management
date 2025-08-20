package com.enesuzun.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("FlightCrewService Unit Tests")
public class FlightCrewServiceTest {

    // Mock repository - Gerçek veritabanı kullanmadan test yapar
    @Mock
    FlightCrewRepository flightCrewRepository;

    // Test edilecek servis - Mockito ile mock'ları inject eder
    @InjectMocks
    FlightCrewService flightCrewService;

    // Test verileri
    private FlightCrew testCrew;
    private Flight testFlight;
    private List<FlightCrew> testCrewList;

    @BeforeEach
    void setup() {
        // Test uçuşu oluştur
        testFlight = new Flight();
        
        // Test crew üyesi oluştur
        testCrew = new FlightCrew();
        
        // Test crew listesi oluştur
        FlightCrew pilot = new FlightCrew();
        FlightCrew steward = new FlightCrew();
        
        testCrewList = Arrays.asList(pilot, steward, testCrew);
    }

    @Test
    @DisplayName("Yeni personel başarıyla eklenmeli")
    void testAddCrew_Success() {
        // Given
        FlightCrew newCrew = new FlightCrew();
        doNothing().when(flightCrewRepository).persist(newCrew);
        
        // When
        flightCrewService.addCrew(newCrew);
        
        // Then
        verify(flightCrewRepository, times(1)).persist(newCrew);
    }

    @Test
    @DisplayName("Tüm personeller başarıyla listelenmeli")
    void testGetAllCrews_Success() {
        // Given
        when(flightCrewRepository.findAllCrews()).thenReturn(testCrewList);
        
        // When
        List<FlightCrew> result = flightCrewService.getAllCrews();
        
        // Then
        assertNotNull(result, "Sonuç null olmamalıdır");
        assertEquals(3, result.size(), "3 personel dönmelidir");
        assertEquals(testCrewList, result, "Dönen liste test listesi olmalıdır");
        verify(flightCrewRepository).findAllCrews();
    }

    @Test
    @DisplayName("Boş personel listesi doğru işlenmeli")
    void testGetAllCrews_EmptyList() {
        // Given
        when(flightCrewRepository.findAllCrews()).thenReturn(Collections.emptyList());
        
        // When
        List<FlightCrew> result = flightCrewService.getAllCrews();
        
        // Then
        assertNotNull(result, "Sonuç null olmamalıdır");
        assertTrue(result.isEmpty(), "Liste boş olmalıdır");
        verify(flightCrewRepository).findAllCrews();
    }

    @Test
    @DisplayName("ID ile personel bulma - Başarılı")
    void testGetCrewById_Found() {
        // Given
        Long crewId = 1L;
        when(flightCrewRepository.findById(crewId)).thenReturn(testCrew);
        
        // When
        FlightCrew result = flightCrewService.getCrewById(crewId);
        
        // Then
        assertNotNull(result, "Personel bulunmalıdır");
        assertEquals(testCrew, result, "Dönen personel test personeli olmalıdır");
        verify(flightCrewRepository).findById(crewId);
    }

    @Test
    @DisplayName("ID ile personel bulma - Bulunamadı")
    void testGetCrewById_NotFound() {
        // Given
        Long crewId = 999L;
        when(flightCrewRepository.findById(crewId)).thenReturn(null);
        
        // When
        FlightCrew result = flightCrewService.getCrewById(crewId);
        
        // Then
        assertNull(result, "Olmayan ID için null dönmelidir");
        verify(flightCrewRepository).findById(crewId);
    }

    @Test
    @DisplayName("İsme göre personel arama - Başarılı")
    void testGetCrewsByName_Found() {
        // Given
        String crewName = "Ahmet Pilot";
        List<FlightCrew> expectedCrews = Arrays.asList(testCrew);
        when(flightCrewRepository.findByCrewName(crewName)).thenReturn(expectedCrews);
        
        // When
        List<FlightCrew> result = flightCrewService.getCrewsByName(crewName);
        
        // Then
        assertNotNull(result, "Sonuç null olmamalıdır");
        assertEquals(1, result.size(), "Bir personel bulunmalıdır");
        assertEquals(expectedCrews, result, "Dönen liste beklenen liste olmalıdır");
        verify(flightCrewRepository).findByCrewName(crewName);
    }

    @Test
    @DisplayName("İsme göre personel arama - Bulunamadı")
    void testGetCrewsByName_NotFound() {
        // Given
        String crewName = "Olmayan Personel";
        when(flightCrewRepository.findByCrewName(crewName)).thenReturn(Collections.emptyList());
        
        // When
        List<FlightCrew> result = flightCrewService.getCrewsByName(crewName);
        
        // Then
        assertNotNull(result, "Sonuç null olmamalıdır");
        assertTrue(result.isEmpty(), "Boş liste dönmelidir");
        verify(flightCrewRepository).findByCrewName(crewName);
    }

    @Test
    @DisplayName("Uçuş ID'sine göre personel bulma - Başarılı")
    void testGetCrewsByFlightId_Success() {
        // Given
        Long flightId = 1L;
        when(flightCrewRepository.findByFlightId(flightId)).thenReturn(testCrewList);
        
        // When
        List<FlightCrew> result = flightCrewService.getCrewsByFlightId(flightId);
        
        // Then
        assertNotNull(result, "Sonuç null olmamalıdır");
        assertEquals(3, result.size(), "3 personel bulunmalıdır");
        assertEquals(testCrewList, result, "Dönen liste test listesi olmalıdır");
        verify(flightCrewRepository).findByFlightId(flightId);
    }

    @Test
    @DisplayName("Uçuş ID ve crew tipine göre personel bulma")
    void testGetCrewsByFlightIdAndType_Success() {
        // Given
        Long flightId = 1L;
        CrewType crewType = CrewType.PILOT;
        List<FlightCrew> pilots = Arrays.asList(testCrew);
        when(flightCrewRepository.findByFlightIdAndCrewType(flightId, crewType))
            .thenReturn(pilots);
        
        // When
        List<FlightCrew> result = flightCrewService.getCrewsByFlightIdAndType(flightId, crewType);
        
        // Then
        assertNotNull(result, "Sonuç null olmamalıdır");
        assertEquals(1, result.size(), "Bir pilot bulunmalıdır");
        assertEquals(pilots, result, "Dönen liste pilot listesi olmalıdır");
        verify(flightCrewRepository).findByFlightIdAndCrewType(flightId, crewType);
    }

    @Test
    @DisplayName("Personel silme - Başarılı")
    void testDeleteCrewByName_Success() {
        // Given
        String crewName = "Silinecek Personel";
        when(flightCrewRepository.deleteByCrewName(crewName)).thenReturn(true);
        
        // When
        boolean result = flightCrewService.deleteCrewByName(crewName);
        
        // Then
        assertTrue(result, "Silme işlemi başarılı olmalıdır");
        verify(flightCrewRepository).deleteByCrewName(crewName);
    }

    @Test
    @DisplayName("Personel silme - Bulunamadı")
    void testDeleteCrewByName_NotFound() {
        // Given
        String crewName = "Olmayan Personel";
        when(flightCrewRepository.deleteByCrewName(crewName)).thenReturn(false);
        
        // When
        boolean result = flightCrewService.deleteCrewByName(crewName);
        
        // Then
        assertFalse(result, "Silme işlemi başarısız olmalıdır");
        verify(flightCrewRepository).deleteByCrewName(crewName);
    }

    @Test
    @DisplayName("Personel güncelleme - Başarılı")
    void testUpdateCrew_Success() {
        // Given
        Long crewId = 1L;
        String newCrewName = "Güncellenmiş İsim";
        CrewType newCrewType = CrewType.CABIN_CREW;
        Flight newFlight = new Flight();
        
        when(flightCrewRepository.updateFlightCrew(crewId, newCrewName, newCrewType, newFlight))
            .thenReturn(true);
        
        // When
        boolean result = flightCrewService.updateCrew(crewId, newCrewName, newCrewType, newFlight);
        
        // Then
        assertTrue(result, "Güncelleme işlemi başarılı olmalıdır");
        verify(flightCrewRepository).updateFlightCrew(crewId, newCrewName, newCrewType, newFlight);
    }

    @Test
    @DisplayName("Personel güncelleme - Bulunamadı")
    void testUpdateCrew_NotFound() {
        // Given
        Long crewId = 999L;
        String newCrewName = "Güncellenmiş İsim";
        CrewType newCrewType = CrewType.CABIN_CREW;
        Flight newFlight = new Flight();
        
        when(flightCrewRepository.updateFlightCrew(crewId, newCrewName, newCrewType, newFlight))
            .thenReturn(false);
        
        // When
        boolean result = flightCrewService.updateCrew(crewId, newCrewName, newCrewType, newFlight);
        
        // Then
        assertFalse(result, "Güncelleme işlemi başarısız olmalıdır");
        verify(flightCrewRepository).updateFlightCrew(crewId, newCrewName, newCrewType, newFlight);
    }

    @Test
    @DisplayName("Uçuştaki personel sayısını sayma")
    void testCountCrewsByFlightId_Success() {
        // Given
        Long flightId = 1L;
        long expectedCount = 5L;
        when(flightCrewRepository.countByFlightId(flightId)).thenReturn(expectedCount);
        
        // When
        long result = flightCrewService.countCrewsByFlightId(flightId);
        
        // Then
        assertEquals(expectedCount, result, "Personel sayısı doğru olmalıdır");
        verify(flightCrewRepository).countByFlightId(flightId);
    }

    @Test
    @DisplayName("Boş uçuştaki personel sayısını sayma")
    void testCountCrewsByFlightId_EmptyFlight() {
        // Given
        Long flightId = 2L;
        long expectedCount = 0L;
        when(flightCrewRepository.countByFlightId(flightId)).thenReturn(expectedCount);
        
        // When
        long result = flightCrewService.countCrewsByFlightId(flightId);
        
        // Then
        assertEquals(0, result, "Boş uçuş için 0 dönmelidir");
        verify(flightCrewRepository).countByFlightId(flightId);
    }
}
