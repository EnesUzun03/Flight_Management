package com.enesuzun.messaging;

import org.apache.camel.ProducerTemplate;//Apache Camel'in Kafka producer'ı
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Apache Camel ile Kafka'ya mesaj gönderen service sınıfı
 */
@ApplicationScoped
public class KafkaProducerService {
    
    private static final Logger LOG = Logger.getLogger(KafkaProducerService.class);
    
    @Inject
    ProducerTemplate producerTemplate;
    
    @ConfigProperty(name = "app.kafka.flight.topic", defaultValue = "flight-events")
    String flightTopic;
    
    @ConfigProperty(name = "app.kafka.crew.topic", defaultValue = "crew-events")
    String crewTopic;
    
    private final ObjectMapper objectMapper;
    
    public KafkaProducerService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * Flight mesajını Kafka'ya gönderir
     */
    public void sendFlightMessage(FlightMessage flightMessage) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(flightMessage);//mesajı json formatına çevirir
            String kafkaEndpoint = "kafka:" + flightTopic;//Mesajın yollanacağı kafka topic'i
            
            producerTemplate.sendBody(kafkaEndpoint, jsonMessage);
            LOG.infof("Flight mesajı kafka topic'e yollandı '%s': %s", flightTopic, jsonMessage);
        } catch (JsonProcessingException e) {
            LOG.errorf("Error serializing flight message: %s", e.getMessage());
            throw new RuntimeException("Failed to send flight message to Kafka", e);
        } catch (Exception e) {
            LOG.errorf("Error sending flight message to Kafka: %s", e.getMessage());
            throw new RuntimeException("Failed to send flight message to Kafka", e);
        }
    }
    
    /**
     * FlightCrew mesajını Kafka'ya gönderir
     */
    public void sendCrewMessage(FlightCrewMessage crewMessage) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(crewMessage);
            String kafkaEndpoint = "kafka:" + crewTopic;
            
            producerTemplate.sendBody(kafkaEndpoint, jsonMessage);
            LOG.infof("Crew message sent to Kafka topic '%s': %s", crewTopic, jsonMessage);
        } catch (JsonProcessingException e) {
            LOG.errorf("Error serializing crew message: %s", e.getMessage());
            throw new RuntimeException("Failed to send crew message to Kafka", e);
        } catch (Exception e) {
            LOG.errorf("Error sending crew message to Kafka: %s", e.getMessage());
            throw new RuntimeException("Failed to send crew message to Kafka", e);
        }
    }
}