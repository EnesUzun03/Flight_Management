package com.enesuzun.camel;

import java.util.Map;

import com.enesuzun.Entity.CrewType;
import com.enesuzun.Entity.FlightCrew;
import com.enesuzun.Services.FlightService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.jboss.logging.Logger;

import com.enesuzun.Services.FlightCrewService;

/**
 * Kafka'dan gelen FlightCrew mesajlarını işleyen processor sınıfı
 */
@ApplicationScoped
@Named("crewMessageProcessor")
public class CrewMessageProcessor {
    
    private static final Logger LOG = Logger.getLogger(CrewMessageProcessor.class);
    
    @Inject
    FlightService flightService;

    @Inject
    FlightCrewService flightCrewService;
    
    @SuppressWarnings("unchecked")
    public void processCrewMessage(Object body) {
        try {
            LOG.infof("Processing crew message: %s", body);
            
            Map<String, Object> messageMap = (Map<String, Object>) body;
            String messageType = (String) messageMap.get("messageType");
            
            switch (messageType) {
                case "CREATE":
                    handleCreateCrew(messageMap);
                    break;
                case "DELETE":
                    handleDeleteCrew(messageMap);
                    break;
                default:
                    LOG.warnf("Unknown message type: %s", messageType);
            }
            
        } catch (Exception e) {
            LOG.errorf("Error processing crew message: %s", e.getMessage());
            throw new RuntimeException("Failed to process crew message", e);
        }
    }
    
    private void handleCreateCrew(Map<String, Object> messageMap) {
        String crewName = (String) messageMap.get("crewName");
        String crewTypeStr = (String) messageMap.get("crewType");
        CrewType crewType = CrewType.valueOf(crewTypeStr);
        Long flightId = Long.valueOf(messageMap.get("flightId").toString());
        
        FlightCrew crew = new FlightCrew();
        crew.setCrewName(crewName);
        crew.setCrewType(crewType);
        
        flightService.addCrewToFlight(flightId, crew);
        LOG.infof("Crew added to flight: %s - %s", crewName, flightId);
    }
    
    private void handleDeleteCrew(Map<String, Object> messageMap) {
        //Long crewId = Long.valueOf(messageMap.get("crewId").toString());
        String crewName = (String) messageMap.get("crewName").toString();
        // FlightCrewService'e bir deleteById methodu eklenmesi gerekebilir
        LOG.infof("Crew delete requested: %s", crewName);
        // flightCrewService.deleteById(crewId); // Bu metodu eklemeniz gerekebilir
        flightCrewService.deleteCrewByName(crewName);
    }
}