package com.enesuzun.messaging;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kafka mesajları için Flight verisini taşıyan model sınıfı
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightMessage {
    
    private String messageType; // "CREATE", "UPDATE", "DELETE"
    private Long flightId;
    private String flightNumber;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime departureDateTime;
    
    private List<FlightCrewMessage> flightCrews;
    private String eventTimestamp;
    
    // Factory methods
    public static FlightMessage createMessage(String flightNumber, LocalDateTime departureDateTime, List<FlightCrewMessage> crews) {
        FlightMessage message = new FlightMessage();
        message.setMessageType("CREATE");
        message.setFlightNumber(flightNumber);
        message.setDepartureDateTime(departureDateTime);
        message.setFlightCrews(crews);
        message.setEventTimestamp(LocalDateTime.now().toString());
        return message;
    }
    
    public static FlightMessage updateMessage(Long flightId, String flightNumber, LocalDateTime departureDateTime) {
        FlightMessage message = new FlightMessage();
        message.setMessageType("UPDATE");
        message.setFlightId(flightId);
        message.setFlightNumber(flightNumber);
        message.setDepartureDateTime(departureDateTime);
        message.setEventTimestamp(LocalDateTime.now().toString());
        return message;
    }
    
    public static FlightMessage deleteMessage(Long flightId) {
        FlightMessage message = new FlightMessage();
        message.setMessageType("DELETE");
        message.setFlightId(flightId);
        message.setEventTimestamp(LocalDateTime.now().toString());
        return message;
    }
}