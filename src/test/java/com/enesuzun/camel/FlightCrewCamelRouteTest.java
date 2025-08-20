package com.enesuzun.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import com.enesuzun.Entity.CrewType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FlightCrewCamelRoute için birim testleri
 * Apache Camel Test Framework kullanarak route'ları test eder
 */
//CamelTestSupport: Apache Camel'in test framework base class'ı
class FlightCrewCamelRouteTest extends CamelTestSupport {
    
    private ObjectMapper objectMapper;
    private CrewMessageProcessor mockCrewMessageProcessor;
    
    //setup metodları
    
    //Bu metod CamelTestSupport’ın bir abstract metodunu override ediyor.
    //hangi Route’u test edeceğini bilmek ister.
    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new FlightCrewCamelRoute();//test etmek istediğimiz route’u tanımlar.
    }
    
    //Camel’in test sırasında kullanacağı CamelContext’i oluşturur.
    @Override
    protected CamelContext createCamelContext() throws Exception {
        //CamelTestSupport’ın sağladığı default CamelContext’i döndürür.
        CamelContext context = super.createCamelContext();
        
        // Mock CrewMessageProcessor'ı context'e ekle
        mockCrewMessageProcessor = new CrewMessageProcessor() {
            @Override
            public void processCrewMessage(Object body) {
                // Test için boş implementation
            }
        };
        //CamelContext’in bean registry’sine mock processor’ı ekliyoruz.
        //Böylece route içinde bean:crewMessageProcessor çağrıldığında bizim mock bean çalışır
        context.getRegistry().bind("crewMessageProcessor", mockCrewMessageProcessor);
        
        return context;
    }
    
    
    @Override
    protected void doPreSetup() throws Exception {
        super.doPreSetup();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    //--------TESTLER--------
    //crew-kafka-consumer route’unun CREATE mesajlarını alıp doğru şekilde işlediğini test eder.
    @Test
    void testCrewKafkaConsumerRoute_ProcessesMessage() throws Exception {
        // Given - Route'u test için configure et

        //AdviceWith → Camel route’unu runtime’da değiştirmemizi sağlar.
        AdviceWith.adviceWith(context, "crew-kafka-consumer", routeBuilder -> {
            // Kafka consumer'ı mock endpoint ile değiştir
            routeBuilder.replaceFromWith("direct:kafka-input");
            // Kafka producer'ı mock endpoint ile değiştir
            routeBuilder.interceptSendToEndpoint("kafka:*")
                .skipSendToOriginalEndpoint()
                .to("mock:kafka-output");
            // Bean processor'ı mock ile değiştir
            routeBuilder.interceptSendToEndpoint("bean:crewMessageProcessor*")
                .skipSendToOriginalEndpoint()
                .to("mock:processor");
        });
        
        // Mock endpoint'leri al
        //getMockEndpoint() → Route’daki mesajların test için yönlendirildiği mock endpoint’i alır.
        MockEndpoint kafkaOutputMock = getMockEndpoint("mock:kafka-output");
        MockEndpoint processorMock = getMockEndpoint("mock:processor");
        
        // Expectations - beklenen mesaj sayısını belirtiyoruz
        //Testte her endpoint’e sadece 1 mesaj gelmesini bekliyoruz.
        kafkaOutputMock.expectedMessageCount(1);
        processorMock.expectedMessageCount(1);
        
        // Test mesajı oluştur ve jsona çevir
        Map<String, Object> testMessage = createTestCrewMessage();
        String jsonMessage = objectMapper.writeValueAsString(testMessage);
        
        // When - Mesajı route'a gönder
        ProducerTemplate template = context.createProducerTemplate();
        //sendBody() → Mesajı belirttiğimiz endpoint’e gönderir (direct:kafka-input).
        //Mock topice gönderiyoruz 
        template.sendBody("direct:kafka-input", jsonMessage);
        
        // Then - Assertions
        //Mock endpoint’ler beklenen mesaj sayılarını aldı mı kontrol edilir.
        kafkaOutputMock.assertIsSatisfied();
        processorMock.assertIsSatisfied();
        
        // Gelen mesajın JSON formatında olduğunu doğrula
        Object receivedBody = processorMock.getReceivedExchanges().get(0).getIn().getBody();
        assertNotNull(receivedBody);
        assertTrue(receivedBody instanceof Map);
        
        //mesaj içeriği kontrol edilir
        @SuppressWarnings("unchecked")
        Map<String, Object> receivedMap = (Map<String, Object>) receivedBody;
        assertEquals("CREATE", receivedMap.get("messageType"));
        assertEquals("John Doe", receivedMap.get("crewName"));
        assertEquals("PILOT", receivedMap.get("crewType"));
    }
    
    @Test
    void testCrewKafkaConsumerRoute_ProcessesPilotMessage() throws Exception {
        // Given
        AdviceWith.adviceWith(context, "crew-kafka-consumer", routeBuilder -> {
            routeBuilder.replaceFromWith("direct:kafka-input");
            routeBuilder.interceptSendToEndpoint("kafka:*")
                .skipSendToOriginalEndpoint()
                .to("mock:kafka-output");
            routeBuilder.interceptSendToEndpoint("bean:crewMessageProcessor*")
                .skipSendToOriginalEndpoint()
                .to("mock:processor");
        });
        
        MockEndpoint kafkaOutputMock = getMockEndpoint("mock:kafka-output");
        MockEndpoint processorMock = getMockEndpoint("mock:processor");
        
        kafkaOutputMock.expectedMessageCount(1);
        processorMock.expectedMessageCount(1);
        
        // PILOT crew mesajı oluştur
        Map<String, Object> pilotMessage = createTestCrewMessage();
        pilotMessage.put("crewType", "PILOT");
        pilotMessage.put("crewName", "Captain Smith");
        String jsonMessage = objectMapper.writeValueAsString(pilotMessage);
        
        // When
        ProducerTemplate template = context.createProducerTemplate();
        template.sendBody("direct:kafka-input", jsonMessage);
        
        // Then / mesajın işlendiğini doğruluyoruz
        kafkaOutputMock.assertIsSatisfied();
        processorMock.assertIsSatisfied();
        
        // Gelen mesajın JSON formatında olduğunu doğrula
        @SuppressWarnings("unchecked")
        Map<String, Object> receivedMap = (Map<String, Object>) processorMock.getReceivedExchanges().get(0).getIn().getBody();
        assertEquals("PILOT", receivedMap.get("crewType"));
        assertEquals("Captain Smith", receivedMap.get("crewName"));
    }
    
    @Test
    void testCrewKafkaConsumerRoute_ProcessesCabinCrewMessage() throws Exception {
        // Given
        AdviceWith.adviceWith(context, "crew-kafka-consumer", routeBuilder -> {
            routeBuilder.replaceFromWith("direct:kafka-input");
            routeBuilder.interceptSendToEndpoint("kafka:*")
                .skipSendToOriginalEndpoint()
                .to("mock:kafka-output");
            routeBuilder.interceptSendToEndpoint("bean:crewMessageProcessor*")
                .skipSendToOriginalEndpoint()
                .to("mock:processor");
        });
        
        MockEndpoint kafkaOutputMock = getMockEndpoint("mock:kafka-output");
        MockEndpoint processorMock = getMockEndpoint("mock:processor");
        
        kafkaOutputMock.expectedMessageCount(1);
        processorMock.expectedMessageCount(1);
        
        // CABIN_CREW mesajı oluştur
        Map<String, Object> cabinCrewMessage = createTestCrewMessage();
        cabinCrewMessage.put("crewType", "CABIN_CREW");
        cabinCrewMessage.put("crewName", "Flight Attendant Jane");
        String jsonMessage = objectMapper.writeValueAsString(cabinCrewMessage);
        
        // When
        ProducerTemplate template = context.createProducerTemplate();
        template.sendBody("direct:kafka-input", jsonMessage);
        
        // Then
        kafkaOutputMock.assertIsSatisfied();
        processorMock.assertIsSatisfied();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> receivedMap = (Map<String, Object>) processorMock.getReceivedExchanges().get(0).getIn().getBody();
        assertEquals("CABIN_CREW", receivedMap.get("crewType"));
        assertEquals("Flight Attendant Jane", receivedMap.get("crewName"));
    }
    
    @Test
    void testCrewKafkaConsumerRoute_InvalidJsonMessage() throws Exception {
        // Given
        AdviceWith.adviceWith(context, "crew-kafka-consumer", routeBuilder -> {
            routeBuilder.replaceFromWith("direct:kafka-input");
            routeBuilder.interceptSendToEndpoint("kafka:*")
                .skipSendToOriginalEndpoint()
                .to("mock:kafka-output");
            routeBuilder.interceptSendToEndpoint("bean:crewMessageProcessor*")
                .skipSendToOriginalEndpoint()
                .to("mock:processor");
        });
        
        MockEndpoint kafkaOutputMock = getMockEndpoint("mock:kafka-output");
        MockEndpoint processorMock = getMockEndpoint("mock:processor");
        
        //Hatalı JSON mesajı geldiğinde route hiçbir endpoint’e mesaj göndermemeli.
        //Bu yüzden expectedMessageCount(0) olmalı.
        kafkaOutputMock.expectedMessageCount(0);
        processorMock.expectedMessageCount(0);
        
        // When - Invalid JSON mesajı gönder
        ProducerTemplate template = context.createProducerTemplate();
        String invalidJson = "{invalid-json-format}";
        
        // Then -Lambda içinde verilen kod exception fırlatıyorsa test geçer.
        assertThrows(Exception.class, () -> {
            template.sendBody("direct:kafka-input", invalidJson);
        });
    }
    
    @Test
    void testCrewKafkaConsumerRoute_DeleteMessage() throws Exception {
        // Given
        AdviceWith.adviceWith(context, "crew-kafka-consumer", routeBuilder -> {
            routeBuilder.replaceFromWith("direct:kafka-input");
            routeBuilder.interceptSendToEndpoint("kafka:*")
                .skipSendToOriginalEndpoint()
                .to("mock:kafka-output");
            routeBuilder.interceptSendToEndpoint("bean:crewMessageProcessor*")
                .skipSendToOriginalEndpoint()
                .to("mock:processor");
        });
        
        MockEndpoint kafkaOutputMock = getMockEndpoint("mock:kafka-output");
        MockEndpoint processorMock = getMockEndpoint("mock:processor");
        
        kafkaOutputMock.expectedMessageCount(1);
        processorMock.expectedMessageCount(1);
        
        // DELETE mesajı oluştur
        Map<String, Object> deleteMessage = new HashMap<>();
        deleteMessage.put("messageType", "DELETE");
        deleteMessage.put("crewName", "John Doe");
        deleteMessage.put("eventTimestamp", LocalDateTime.now().toString());
        String jsonMessage = objectMapper.writeValueAsString(deleteMessage);
        
        // When
        ProducerTemplate template = context.createProducerTemplate();
        template.sendBody("direct:kafka-input", jsonMessage);
        
        // Then
        kafkaOutputMock.assertIsSatisfied();
        processorMock.assertIsSatisfied();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> receivedMap = (Map<String, Object>) processorMock.getReceivedExchanges().get(0).getIn().getBody();
        assertEquals("DELETE", receivedMap.get("messageType"));
        assertEquals("John Doe", receivedMap.get("crewName"));
    }
    

    //Amaç: Route’un gelen JSON mesajını Java objesine çevirip (unmarshal) işleyip,Kafka output’a tekrar JSON string olarak göndermesini (marshal) doğrulamak.
    @Test
    void testCrewKafkaConsumerRoute_MessageUnmarshalAndMarshal() throws Exception {
        // Given
        AdviceWith.adviceWith(context, "crew-kafka-consumer", routeBuilder -> {
            routeBuilder.replaceFromWith("direct:kafka-input");
            routeBuilder.interceptSendToEndpoint("kafka:*")
                .skipSendToOriginalEndpoint()
                .to("mock:kafka-output");
            routeBuilder.interceptSendToEndpoint("bean:crewMessageProcessor*")
                .skipSendToOriginalEndpoint()
                .to("mock:processor");
        });
        
        MockEndpoint kafkaOutputMock = getMockEndpoint("mock:kafka-output");
        MockEndpoint processorMock = getMockEndpoint("mock:processor");
        
        kafkaOutputMock.expectedMessageCount(1);
        processorMock.expectedMessageCount(1);
        
        // Test mesajı oluştur
        Map<String, Object> testMessage = createTestCrewMessage();
        String jsonMessage = objectMapper.writeValueAsString(testMessage);
        
        // When
        ProducerTemplate template = context.createProducerTemplate();
        template.sendBody("direct:kafka-input", jsonMessage);
        
        // Then / Mesajların beklenen endpoint’lere ulaştığını kontrol ediyoruz.
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
        assertEquals("John Doe", parsedOutput.get("crewName"));
    }
    //birden fazla mesajı aynı anda doğru şekilde işleyip işlemediğini test ediyor
    @Test
    void testCrewKafkaConsumerRoute_MultipleMessages() throws Exception {
        // Given
        AdviceWith.adviceWith(context, "crew-kafka-consumer", routeBuilder -> {
            routeBuilder.replaceFromWith("direct:kafka-input");
            routeBuilder.interceptSendToEndpoint("kafka:*")
                .skipSendToOriginalEndpoint()
                .to("mock:kafka-output");
            routeBuilder.interceptSendToEndpoint("bean:crewMessageProcessor*")
                .skipSendToOriginalEndpoint()
                .to("mock:processor");
        });
        
        MockEndpoint kafkaOutputMock = getMockEndpoint("mock:kafka-output");
        MockEndpoint processorMock = getMockEndpoint("mock:processor");
        
        kafkaOutputMock.expectedMessageCount(2);
        processorMock.expectedMessageCount(2);
        
        ProducerTemplate template = context.createProducerTemplate();
        
        // İki farklı crew mesajı gönder
        Map<String, Object> pilotMessage = createTestCrewMessage();
        pilotMessage.put("crewType", "PILOT");
        pilotMessage.put("crewName", "Captain Smith");
        template.sendBody("direct:kafka-input", objectMapper.writeValueAsString(pilotMessage));
        
        Map<String, Object> cabinMessage = createTestCrewMessage();
        cabinMessage.put("crewType", "CABIN_CREW");
        cabinMessage.put("crewName", "Flight Attendant Jane");
        template.sendBody("direct:kafka-input", objectMapper.writeValueAsString(cabinMessage));
        
        // Then
        kafkaOutputMock.assertIsSatisfied();
        processorMock.assertIsSatisfied();
    }
    
    private Map<String, Object> createTestCrewMessage() {
        Map<String, Object> message = new HashMap<>();
        message.put("messageType", "CREATE");
        message.put("crewName", "John Doe");
        message.put("crewType", CrewType.PILOT.toString());
        message.put("flightId", 1L);
        message.put("flightNumber", "TK101");
        message.put("eventTimestamp", LocalDateTime.now().toString());
        return message;
    }
    
    protected boolean useAdviceWith() {
        return true; // AdviceWith kullanmak için true yapıyoruz
    }
}
