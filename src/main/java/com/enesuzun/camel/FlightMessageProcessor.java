package com.enesuzun.camel;

import java.util.Map;

import com.enesuzun.Entity.Flight;
import com.enesuzun.Services.FlightService;
import com.fasterxml.jackson.databind.ObjectMapper;//JSON verisini Java objesine dönüştürmek için Jackson kütüphanesi.
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.jboss.logging.Logger;

import java.time.LocalDateTime;

/**
 * Kafka'dan gelen Flight mesajlarını işleyen processor sınıfı
 */
@ApplicationScoped
@Named("flightMessageProcessor")
public class FlightMessageProcessor {
    //Loglama için static final logger tanımlandı.
    private static final Logger LOG = Logger.getLogger(FlightMessageProcessor.class);
    
    @Inject
    FlightService flightService;
    //JSON verisini Java nesnesine çevirmek için ObjectMapper.
    private final ObjectMapper objectMapper;
    
    public FlightMessageProcessor() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    @SuppressWarnings("unchecked")
    public void processFlightMessage(Object body) {
        try {
            LOG.infof("Processing flight message: %s", body);
            //Gelen veri map'e çevriliyor
            Map<String, Object> messageMap = (Map<String, Object>) body;
            String messageType = (String) messageMap.get("messageType");
            
            switch (messageType) {
                case "CREATE":
                    handleCreateFlight(messageMap);
                    break;
                case "UPDATE":
                    handleUpdateFlight(messageMap);
                    break;
                case "DELETE":
                    handleDeleteFlight(messageMap);
                    break;
                default:
                    LOG.warnf("Unknown message type: %s", messageType);
            }
            
        } catch (Exception e) {
            LOG.errorf("Error processing flight message: %s", e.getMessage());
            throw new RuntimeException("Failed to process flight message", e);
        }
    }
    
    private void handleCreateFlight(Map<String, Object> messageMap) {
        String flightNumber = (String) messageMap.get("flightNumber");
        String departureTimeStr = (String) messageMap.get("departureDateTime");
        LocalDateTime departureDateTime = LocalDateTime.parse(departureTimeStr);
        
        Flight flight = new Flight();//MessageMap içindeki veriler ile flight nesnesi oluşturuyoruz.
        flight.setFlightNumber(flightNumber);
        flight.setDepartureDateTime(departureDateTime);
        
        flightService.addFlight(flight);
        LOG.infof("Flight created: %s", flightNumber);
    }
    
    private void handleUpdateFlight(Map<String, Object> messageMap) {
        Long flightId = Long.valueOf(messageMap.get("flightId").toString());
        String flightNumber = (String) messageMap.get("flightNumber");
        String departureTimeStr = (String) messageMap.get("departureDateTime");
        LocalDateTime departureDateTime = LocalDateTime.parse(departureTimeStr);
        
        flightService.updateFlight(flightId, flightNumber, departureDateTime);
        LOG.infof("Flight updated: %s", flightNumber);
    }
    
    private void handleDeleteFlight(Map<String, Object> messageMap) {
        Long flightId = Long.valueOf(messageMap.get("flightId").toString());
        
        flightService.deleteFlight(flightId);
        LOG.infof("Flight deleted: %s", flightId);
    }
}