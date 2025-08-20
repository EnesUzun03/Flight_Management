package com.enesuzun.camel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.enesuzun.Entity.CrewType;
import com.enesuzun.Entity.Flight;
import com.enesuzun.Entity.FlightCrew;
import com.enesuzun.Services.FlightCrewService;
import com.enesuzun.Services.FlightService;

/**
 * CrewMessageProcessor için birim testleri
 */
@ExtendWith(MockitoExtension.class)
class CrewMessageProcessorTest {
    
    @Mock
    private FlightService flightService;
    
    @Mock
    private FlightCrewService flightCrewService;
    
    @InjectMocks
    private CrewMessageProcessor crewMessageProcessor;
    
    private Map<String, Object> createMessageMap;
    private Map<String, Object> deleteMessageMap;
    
    @BeforeEach
    void setUp() {
        // CREATE mesajı için test verisi
        createMessageMap = new HashMap<>();
        createMessageMap.put("messageType", "CREATE");
        createMessageMap.put("crewName", "John Doe");
        createMessageMap.put("crewType", "PILOT");
        createMessageMap.put("flightId", 1L);
        
        // DELETE mesajı için test verisi
        deleteMessageMap = new HashMap<>();
        deleteMessageMap.put("messageType", "DELETE");
        deleteMessageMap.put("crewName", "Jane Smith");
    }
    
    @Test
    void testProcessCrewMessage_CreateMessage_Success() {
        // Given
        //setupda createMessageMap de fliightıd 1L olan uçuşa crew ekleniyor
        doNothing().when(flightService).addCrewToFlight(eq(1L), any(FlightCrew.class));
        
        // When
        //processCrewMessage metodu çağrılıyor ve createMessageMap ile birlikte çalışıyor.
        //bir messageType create ise handleCreateCrew metodu çağrılıyor.
        assertDoesNotThrow(() -> crewMessageProcessor.processCrewMessage(createMessageMap));
        
        // Then
        //flightService.addCrewToFlight(...) metodunun çağrılıp çağrılmadığını ve nasıl çağrıldığını kontrol ediyoruz.
        verify(flightService, times(1)).addCrewToFlight(
            eq(1L), 
            argThat(crew -> 
                crew != null &&
                "John Doe".equals(crew.getCrewName()) &&
                CrewType.PILOT.equals(crew.getCrewType())
            )
        );
    }
    //"crewType" = "PILOT" geldiğinde
    //FlightService.addCrewToFlight(...) metoduna gönderilen FlightCrew nesnesinin gerçekten PILOT tipinde olduğunu doğruluyor.
    @Test
    void testProcessCrewMessage_CreateMessage_PilotCrewType() {
        // Given
        createMessageMap.put("crewType", "PILOT");
        doNothing().when(flightService).addCrewToFlight(eq(1L), any(FlightCrew.class));
        
        // When
        assertDoesNotThrow(() -> crewMessageProcessor.processCrewMessage(createMessageMap));
        
        // Then
        verify(flightService, times(1)).addCrewToFlight(
            eq(1L),
            argThat(crew -> crew != null && CrewType.PILOT.equals(crew.getCrewType()))
        );
    }
    
    @Test
    void testProcessCrewMessage_CreateMessage_CabinCrewType() {
        // Given
        createMessageMap.put("crewType", "CABIN_CREW");
        doNothing().when(flightService).addCrewToFlight(eq(1L), any(FlightCrew.class));
        
        // When
        assertDoesNotThrow(() -> crewMessageProcessor.processCrewMessage(createMessageMap));
        
        // Then
        verify(flightService, times(1)).addCrewToFlight(
            eq(1L),
            argThat(crew -> crew != null && CrewType.CABIN_CREW.equals(crew.getCrewType()))
        );
    }
    
    @Test
    void testProcessCrewMessage_DeleteMessage_Success() {
        // Given
        when(flightCrewService.deleteCrewByName("Jane Smith")).thenReturn(true);
        
        // When
        assertDoesNotThrow(() -> crewMessageProcessor.processCrewMessage(deleteMessageMap));
        
        // Then
        verify(flightCrewService, times(1)).deleteCrewByName("Jane Smith");
    }
    
    @Test
    void testProcessCrewMessage_UnknownMessageType() {
        // Given
        Map<String, Object> unknownMessageMap = new HashMap<>();
        unknownMessageMap.put("messageType", "UNKNOWN");
        unknownMessageMap.put("crewName", "Test Crew");
        
        // When & Then
        assertDoesNotThrow(() -> crewMessageProcessor.processCrewMessage(unknownMessageMap));
        
        // Hiçbir service metodu çağrılmamalı
        verify(flightService, never()).addCrewToFlight(anyLong(), any());
        verify(flightCrewService, never()).deleteCrewByName(anyString());
    }
    //crewMessageProcessor.processCrewMessage çalışırken service katmanında hata çıkarsa (örneğin veritabanı hatası),
    //Bu hatanın yakalanıp özel bir RuntimeException olarak fırlatıldığını test etmek.
    @Test
    void testProcessCrewMessage_CreateMessage_ServiceThrowsException() {
        // Given
        doThrow(new RuntimeException("Database error"))
            .when(flightService).addCrewToFlight(anyLong(), any(FlightCrew.class));
        
        // When & Then
        //Bu çağrı sırasında, içerde flightService.addCrewToFlight çalışacak → bu da bizim ayarladığımız gibi RuntimeException("Database error") fırlatacak.
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> crewMessageProcessor.processCrewMessage(createMessageMap)
        );
        //doğrulama
        assertEquals("Failed to process crew message", exception.getMessage());
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Database error", exception.getCause().getMessage());
    }
    
    @Test
    void testProcessCrewMessage_DeleteMessage_ServiceThrowsException() {
        // Given
        doThrow(new RuntimeException("Delete failed"))
            .when(flightCrewService).deleteCrewByName(anyString());
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> crewMessageProcessor.processCrewMessage(deleteMessageMap)
        );
        
        assertEquals("Failed to process crew message", exception.getMessage());
    }
    
    //geçersiz crewType geldiğinde
    //Bu kısmı anlamadım.
    @Test
    void testProcessCrewMessage_InvalidCrewType() {
        // Given
        createMessageMap.put("crewType", "INVALID_CREW_TYPE");
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> crewMessageProcessor.processCrewMessage(createMessageMap)
        );
        
        assertEquals("Failed to process crew message", exception.getMessage());
        verify(flightService, never()).addCrewToFlight(anyLong(), any());
    }
    //messageType olmadan gelen mesaj işlenmemeli
    @Test
    void testProcessCrewMessage_MissingMessageType() {
        // Given
        Map<String, Object> missingTypeMap = new HashMap<>();
        missingTypeMap.put("crewName", "Test Crew");
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> crewMessageProcessor.processCrewMessage(missingTypeMap)
        );
        
        assertEquals("Failed to process crew message", exception.getMessage());
    }
    
    @Test
    void testProcessCrewMessage_NullBody() {
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> crewMessageProcessor.processCrewMessage(null)
        );
        
        assertEquals("Failed to process crew message", exception.getMessage());
    }
    
    @Test
    void testProcessCrewMessage_CreateMessage_MissingCrewName() {
        // Given
        Map<String, Object> missingNameMap = new HashMap<>();
        missingNameMap.put("messageType", "CREATE");
        missingNameMap.put("crewType", "PILOT");
        missingNameMap.put("flightId", 1L);
        // crewName eksik - bu durumda null değer set edilir, exception atılmaz
        
        doNothing().when(flightService).addCrewToFlight(eq(1L), any(FlightCrew.class));
        
        // When
        assertDoesNotThrow(() -> crewMessageProcessor.processCrewMessage(missingNameMap));
        
        // Then
        verify(flightService, times(1)).addCrewToFlight(
            eq(1L), 
            argThat(crew -> 
                crew != null &&
                crew.getCrewName() == null && // crewName null olarak set edilir
                CrewType.PILOT.equals(crew.getCrewType())
            )
        );
    }
    
    @Test
    void testProcessCrewMessage_CreateMessage_MissingFlightId() {
        // Given
        Map<String, Object> missingIdMap = new HashMap<>();
        missingIdMap.put("messageType", "CREATE");
        missingIdMap.put("crewName", "Test Crew");
        missingIdMap.put("crewType", "PILOT");
        // flightId eksik
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> crewMessageProcessor.processCrewMessage(missingIdMap)
        );
        
        assertEquals("Failed to process crew message", exception.getMessage());
    }
    
    @Test
    void testProcessCrewMessage_DeleteMessage_MissingCrewName() {
        // Given
        Map<String, Object> missingNameMap = new HashMap<>();
        missingNameMap.put("messageType", "DELETE");
        // crewName eksik
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> crewMessageProcessor.processCrewMessage(missingNameMap)
        );
        
        assertEquals("Failed to process crew message", exception.getMessage());
    }
    //flightId alanının geçersiz bir türde
    @Test
    void testProcessCrewMessage_CreateMessage_InvalidFlightIdType() {
        // Given
        Map<String, Object> invalidIdMap = new HashMap<>();
        invalidIdMap.put("messageType", "CREATE");
        invalidIdMap.put("crewName", "Test Crew");
        invalidIdMap.put("crewType", "PILOT");
        invalidIdMap.put("flightId", "invalid-id"); // String instead of Long
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> crewMessageProcessor.processCrewMessage(invalidIdMap)
        );
        
        assertEquals("Failed to process crew message", exception.getMessage());
    }
}
