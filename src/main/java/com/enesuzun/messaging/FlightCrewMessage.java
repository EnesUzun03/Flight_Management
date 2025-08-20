package com.enesuzun.messaging;

import com.enesuzun.Entity.CrewType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kafka mesajları için FlightCrew verisini taşıyan model sınıfı
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightCrewMessage {
    
    private String messageType; // "CREATE", "UPDATE", "DELETE"
    private Long crewId;
    private String crewName;
    private CrewType crewType;
    private Long flightId;
    private String flightNumber;
    private String eventTimestamp;
    
    // Factory methods
    public static FlightCrewMessage createMessage(String crewName, CrewType crewType, Long flightId, String flightNumber) {
        FlightCrewMessage message = new FlightCrewMessage();
        message.setMessageType("CREATE");
        message.setCrewName(crewName);
        message.setCrewType(crewType);
        message.setFlightId(flightId);
        message.setFlightNumber(flightNumber);
        message.setEventTimestamp(java.time.LocalDateTime.now().toString());
        return message;
    }
    
    public static FlightCrewMessage deleteMessage(Long crewId) {
        FlightCrewMessage message = new FlightCrewMessage();
        message.setMessageType("DELETE");
        message.setCrewId(crewId);
        message.setEventTimestamp(java.time.LocalDateTime.now().toString());
        return message;
    }
}