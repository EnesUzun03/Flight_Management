package com.enesuzun.camel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.enesuzun.Entity.Flight;
import com.enesuzun.Services.FlightService;

/**
 * FlightMessageProcessor için birim testleri
 */
@ExtendWith(MockitoExtension.class)
class FlightMessageProcessorTest {
    
    @Mock
    private FlightService flightService;
    
    @InjectMocks
    private FlightMessageProcessor flightMessageProcessor;
    
    private Map<String, Object> createMessageMap;
    private Map<String, Object> updateMessageMap;
    private Map<String, Object> deleteMessageMap;
    
    @BeforeEach
    void setUp() {
        // CREATE mesajı için test verisi
        createMessageMap = new HashMap<>();
        createMessageMap.put("messageType", "CREATE");
        createMessageMap.put("flightNumber", "TK101");
        createMessageMap.put("departureDateTime", "2024-01-15T10:30:00");
        
        // UPDATE mesajı için test verisi
        updateMessageMap = new HashMap<>();
        updateMessageMap.put("messageType", "UPDATE");
        updateMessageMap.put("flightId", 1L);
        updateMessageMap.put("flightNumber", "TK102");
        updateMessageMap.put("departureDateTime", "2024-01-16T14:45:00");
        
        // DELETE mesajı için test verisi
        deleteMessageMap = new HashMap<>();
        deleteMessageMap.put("messageType", "DELETE");
        deleteMessageMap.put("flightId", 1L);
    }
    
    @Test
    void testProcessFlightMessage_CreateMessage_Success() {
        // Given
        doNothing().when(flightService).addFlight(any(Flight.class));
        
        // When
        assertDoesNotThrow(() -> flightMessageProcessor.processFlightMessage(createMessageMap));
        
        // Then
        verify(flightService, times(1)).addFlight(argThat(flight -> 
            flight != null &&
            "TK101".equals(flight.getFlightNumber()) &&
            LocalDateTime.of(2024, 1, 15, 10, 30).equals(flight.getDepartureDateTime())
        ));
    }
    
    @Test
    void testProcessFlightMessage_UpdateMessage_Success() {
        // Given
        Flight existingFlight = new Flight();
        doNothing().when(flightService).updateFlight(eq(1L), eq("TK102"), any(LocalDateTime.class));
        
        // When
        assertDoesNotThrow(() -> flightMessageProcessor.processFlightMessage(updateMessageMap));
        
        // Then
        verify(flightService, times(1)).updateFlight(
            eq(1L), 
            eq("TK102"), 
            eq(LocalDateTime.of(2024, 1, 16, 14, 45))
        );
    }
    
    @Test
    void testProcessFlightMessage_DeleteMessage_Success() {
        // Given
        doNothing().when(flightService).deleteFlight(1L);
        
        // When
        assertDoesNotThrow(() -> flightMessageProcessor.processFlightMessage(deleteMessageMap));
        
        // Then
        verify(flightService, times(1)).deleteFlight(1L);
    }
    
    @Test
    void testProcessFlightMessage_UnknownMessageType() {
        // Given
        Map<String, Object> unknownMessageMap = new HashMap<>();
        unknownMessageMap.put("messageType", "UNKNOWN");
        unknownMessageMap.put("flightNumber", "TK103");
        
        // When & Then
        assertDoesNotThrow(() -> flightMessageProcessor.processFlightMessage(unknownMessageMap));
        
        // Hiçbir service metodu çağrılmamalı
        verify(flightService, never()).addFlight(any());
        verify(flightService, never()).updateFlight(anyLong(), anyString(), any());
        verify(flightService, never()).deleteFlight(anyLong());
    }
    
    @Test
    void testProcessFlightMessage_CreateMessage_ServiceThrowsException() {
        // Given
        doThrow(new RuntimeException("Database error"))
            .when(flightService).addFlight(any(Flight.class));
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> flightMessageProcessor.processFlightMessage(createMessageMap)
        );
        
        assertEquals("Failed to process flight message", exception.getMessage());
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Database error", exception.getCause().getMessage());
    }
    
    @Test
    void testProcessFlightMessage_UpdateMessage_ServiceThrowsException() {
        // Given
        doThrow(new RuntimeException("Update failed"))
            .when(flightService).updateFlight(anyLong(), anyString(), any(LocalDateTime.class));
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> flightMessageProcessor.processFlightMessage(updateMessageMap)
        );
        
        assertEquals("Failed to process flight message", exception.getMessage());
    }
    
    @Test
    void testProcessFlightMessage_DeleteMessage_ServiceThrowsException() {
        // Given
        doThrow(new RuntimeException("Delete failed"))
            .when(flightService).deleteFlight(anyLong());
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> flightMessageProcessor.processFlightMessage(deleteMessageMap)
        );
        
        assertEquals("Failed to process flight message", exception.getMessage());
    }
    
    @Test
    void testProcessFlightMessage_InvalidDateFormat() {
        // Given
        Map<String, Object> invalidDateMap = new HashMap<>();
        invalidDateMap.put("messageType", "CREATE");
        invalidDateMap.put("flightNumber", "TK104");
        invalidDateMap.put("departureDateTime", "invalid-date-format");
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> flightMessageProcessor.processFlightMessage(invalidDateMap)
        );
        
        assertEquals("Failed to process flight message", exception.getMessage());
        verify(flightService, never()).addFlight(any());
    }
    
    @Test
    void testProcessFlightMessage_MissingMessageType() {
        // Given
        Map<String, Object> missingTypeMap = new HashMap<>();
        missingTypeMap.put("flightNumber", "TK105");
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> flightMessageProcessor.processFlightMessage(missingTypeMap)
        );
        
        assertEquals("Failed to process flight message", exception.getMessage());
    }
    
    @Test
    void testProcessFlightMessage_NullBody() {
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> flightMessageProcessor.processFlightMessage(null)
        );
        
        assertEquals("Failed to process flight message", exception.getMessage());
    }
    
    @Test
    void testProcessFlightMessage_CreateMessage_MissingFlightNumber() {
        // Given
        Map<String, Object> missingFlightNumberMap = new HashMap<>();
        missingFlightNumberMap.put("messageType", "CREATE");
        missingFlightNumberMap.put("departureDateTime", "2024-01-15T10:30:00");
        // flightNumber eksik - bu durumda null değer set edilir, exception atılmaz
        
        doNothing().when(flightService).addFlight(any(Flight.class));
        
        // When
        assertDoesNotThrow(() -> flightMessageProcessor.processFlightMessage(missingFlightNumberMap));
        
        // Then
        verify(flightService, times(1)).addFlight(argThat(flight -> 
            flight != null &&
            flight.getFlightNumber() == null && // flightNumber null olarak set edilir
            flight.getDepartureDateTime() != null
        ));
    }
    
    @Test
    void testProcessFlightMessage_UpdateMessage_MissingFlightId() {
        // Given
        Map<String, Object> missingIdMap = new HashMap<>();
        missingIdMap.put("messageType", "UPDATE");
        missingIdMap.put("flightNumber", "TK106");
        missingIdMap.put("departureDateTime", "2024-01-15T10:30:00");
        // flightId eksik
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> flightMessageProcessor.processFlightMessage(missingIdMap)
        );
        
        assertEquals("Failed to process flight message", exception.getMessage());
    }
}
