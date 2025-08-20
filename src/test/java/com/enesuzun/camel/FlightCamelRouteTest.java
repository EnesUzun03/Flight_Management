package com.enesuzun.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FlightCamelRoute için birim testleri
 * Apache Camel Test Framework kullanarak route'ları test eder
 */
class FlightCamelRouteTest extends CamelTestSupport {
    
    private ObjectMapper objectMapper;
    private FlightMessageProcessor mockFlightMessageProcessor;
    
    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new FlightCamelRoute();
    }
    
    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();
        
        // Mock FlightMessageProcessor'ı context'e ekle
        mockFlightMessageProcessor = new FlightMessageProcessor() {
            @Override
            public void processFlightMessage(Object body) {
                // Test için boş implementation
            }
        };
        context.getRegistry().bind("flightMessageProcessor", mockFlightMessageProcessor);
        
        return context;
    }
    
    @Override
    protected void doPreSetup() throws Exception {
        super.doPreSetup();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Test
    void testFlightKafkaConsumerRoute_ProcessesMessage() throws Exception {
        // Given - Route'u test için configure et
        AdviceWith.adviceWith(context, "flight-kafka-consumer", routeBuilder -> {
            // Kafka consumer'ı mock endpoint ile değiştir
            routeBuilder.replaceFromWith("direct:kafka-input");
            // Kafka producer'ı mock endpoint ile değiştir
            routeBuilder.interceptSendToEndpoint("kafka:*")
                .skipSendToOriginalEndpoint()
                .to("mock:kafka-output");
            // Bean processor'ı mock ile değiştir
            routeBuilder.interceptSendToEndpoint("bean:flightMessageProcessor*")
                .skipSendToOriginalEndpoint()
                .to("mock:processor");
        });
        
        // Mock endpoint'leri al
        MockEndpoint kafkaOutputMock = getMockEndpoint("mock:kafka-output");
        MockEndpoint processorMock = getMockEndpoint("mock:processor");
        
        // Expectations
        kafkaOutputMock.expectedMessageCount(1);
        processorMock.expectedMessageCount(1);
        
        // Test mesajı oluştur
        Map<String, Object> testMessage = createTestFlightMessage();
        String jsonMessage = objectMapper.writeValueAsString(testMessage);
        
        // When - Mesajı route'a gönder
        ProducerTemplate template = context.createProducerTemplate();
        template.sendBody("direct:kafka-input", jsonMessage);
        
        // Then - Assertions
        kafkaOutputMock.assertIsSatisfied();
        processorMock.assertIsSatisfied();
        
        // Gelen mesajın JSON formatında olduğunu doğrula
        Object receivedBody = processorMock.getReceivedExchanges().get(0).getIn().getBody();
        assertNotNull(receivedBody);
        assertTrue(receivedBody instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> receivedMap = (Map<String, Object>) receivedBody;
        assertEquals("CREATE", receivedMap.get("messageType"));
        assertEquals("TK101", receivedMap.get("flightNumber"));
    }
    
    @Test
    void testFlightKafkaConsumerRoute_InvalidJsonMessage() throws Exception {
        // Given - Route'u test için configure et
        AdviceWith.adviceWith(context, "flight-kafka-consumer", routeBuilder -> {
            routeBuilder.replaceFromWith("direct:kafka-input");
            routeBuilder.interceptSendToEndpoint("kafka:*")
                .skipSendToOriginalEndpoint()
                .to("mock:kafka-output");
            routeBuilder.interceptSendToEndpoint("bean:flightMessageProcessor*")
                .skipSendToOriginalEndpoint()
                .to("mock:processor");
        });
        
        MockEndpoint kafkaOutputMock = getMockEndpoint("mock:kafka-output");
        MockEndpoint processorMock = getMockEndpoint("mock:processor");
        
        // Invalid JSON mesajı için hiç mesaj beklenmez
        kafkaOutputMock.expectedMessageCount(0);
        processorMock.expectedMessageCount(0);
        
        // When - Invalid JSON mesajı gönder
        ProducerTemplate template = context.createProducerTemplate();
        
        // Invalid JSON format
        String invalidJson = "{invalid-json-format}";
        
        // Then - Exception fırlatılır
        assertThrows(Exception.class, () -> {
            template.sendBody("direct:kafka-input", invalidJson);
        });
    }
    
    @Test
    void testFlightKafkaConsumerRoute_EmptyMessage() throws Exception {
        // Given
        AdviceWith.adviceWith(context, "flight-kafka-consumer", routeBuilder -> {
            routeBuilder.replaceFromWith("direct:kafka-input");
            routeBuilder.interceptSendToEndpoint("kafka:*")
                .skipSendToOriginalEndpoint()
                .to("mock:kafka-output");
            routeBuilder.interceptSendToEndpoint("bean:flightMessageProcessor*")
                .skipSendToOriginalEndpoint()
                .to("mock:processor");
        });
        
        MockEndpoint kafkaOutputMock = getMockEndpoint("mock:kafka-output");
        MockEndpoint processorMock = getMockEndpoint("mock:processor");
        
        kafkaOutputMock.expectedMessageCount(0);
        processorMock.expectedMessageCount(0);
        
        // When - Boş mesaj gönder
        ProducerTemplate template = context.createProducerTemplate();
        
        // Then - Exception fırlatılır
        assertThrows(Exception.class, () -> {
            template.sendBody("direct:kafka-input", "");
        });
    }
    
    @Test
    void testFlightKafkaConsumerRoute_MessageUnmarshalAndMarshal() throws Exception {
        // Given
        AdviceWith.adviceWith(context, "flight-kafka-consumer", routeBuilder -> {
            routeBuilder.replaceFromWith("direct:kafka-input");
            routeBuilder.interceptSendToEndpoint("kafka:*")
                .skipSendToOriginalEndpoint()
                .to("mock:kafka-output");
            routeBuilder.interceptSendToEndpoint("bean:flightMessageProcessor*")
                .skipSendToOriginalEndpoint()
                .to("mock:processor");
        });
        
        MockEndpoint kafkaOutputMock = getMockEndpoint("mock:kafka-output");
        MockEndpoint processorMock = getMockEndpoint("mock:processor");
        
        kafkaOutputMock.expectedMessageCount(1);
        processorMock.expectedMessageCount(1);
        
        // Test mesajı oluştur
        Map<String, Object> testMessage = createTestFlightMessage();
        String jsonMessage = objectMapper.writeValueAsString(testMessage);
        
        // When
        ProducerTemplate template = context.createProducerTemplate();
        template.sendBody("direct:kafka-input", jsonMessage);
        
        // Then
        kafkaOutputMock.assertIsSatisfied();
        processorMock.assertIsSatisfied();
        
        // Kafka output'a gönderilen mesajın JSON string formatında olduğunu doğrula
        Object outputBody = kafkaOutputMock.getReceivedExchanges().get(0).getIn().getBody();
        assertNotNull(outputBody);
        assertTrue(outputBody instanceof String);
        
        // JSON string'i tekrar parse edebilmemiz gerekiyor
        String outputJson = (String) outputBody;
        @SuppressWarnings("unchecked")
        Map<String, Object> parsedOutput = objectMapper.readValue(outputJson, Map.class);
        assertEquals("CREATE", parsedOutput.get("messageType"));
        assertEquals("TK101", parsedOutput.get("flightNumber"));
    }
    
    @Test
    void testFlightKafkaConsumerRoute_DifferentMessageTypes() throws Exception {
        // Given
        AdviceWith.adviceWith(context, "flight-kafka-consumer", routeBuilder -> {
            routeBuilder.replaceFromWith("direct:kafka-input");
            routeBuilder.interceptSendToEndpoint("kafka:*")
                .skipSendToOriginalEndpoint()
                .to("mock:kafka-output");
            routeBuilder.interceptSendToEndpoint("bean:flightMessageProcessor*")
                .skipSendToOriginalEndpoint()
                .to("mock:processor");
        });
        
        MockEndpoint kafkaOutputMock = getMockEndpoint("mock:kafka-output");
        MockEndpoint processorMock = getMockEndpoint("mock:processor");
        
        kafkaOutputMock.expectedMessageCount(3); // 3 farklı mesaj tipi
        processorMock.expectedMessageCount(3);
        
        ProducerTemplate template = context.createProducerTemplate();
        
        // CREATE mesajı
        Map<String, Object> createMessage = createTestFlightMessage();
        template.sendBody("direct:kafka-input", objectMapper.writeValueAsString(createMessage));
        
        // UPDATE mesajı
        Map<String, Object> updateMessage = createTestFlightMessage();
        updateMessage.put("messageType", "UPDATE");
        updateMessage.put("flightId", 1L);
        template.sendBody("direct:kafka-input", objectMapper.writeValueAsString(updateMessage));
        
        // DELETE mesajı
        Map<String, Object> deleteMessage = new HashMap<>();
        deleteMessage.put("messageType", "DELETE");
        deleteMessage.put("flightId", 1L);
        template.sendBody("direct:kafka-input", objectMapper.writeValueAsString(deleteMessage));
        
        // Then
        kafkaOutputMock.assertIsSatisfied();
        processorMock.assertIsSatisfied();
    }
    
    private Map<String, Object> createTestFlightMessage() {
        Map<String, Object> message = new HashMap<>();
        message.put("messageType", "CREATE");
        message.put("flightNumber", "TK101");
        message.put("departureDateTime", LocalDateTime.of(2024, 1, 15, 10, 30).toString());
        message.put("eventTimestamp", LocalDateTime.now().toString());
        return message;
    }
    

    protected boolean useAdviceWith() {
        return true; // AdviceWith kullanmak için true yapıyoruz
    }
}
