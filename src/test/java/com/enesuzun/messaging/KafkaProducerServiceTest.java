package com.enesuzun.messaging;

import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.enesuzun.Entity.CrewType;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

/**
 * KafkaProducerService için birim testleri
 */
@ExtendWith(MockitoExtension.class)//JUnit 5 için Mockito’nun test uzantısını kullanır.
class KafkaProducerServiceTest {
    
    @Mock
    private ProducerTemplate producerTemplate;//ProducerTemplate mock nesnesi
    
    @InjectMocks
    private KafkaProducerService kafkaProducerService;
    
    private FlightMessage testFlightMessage;
    private FlightCrewMessage testCrewMessage;
    
    @BeforeEach
    void setUp() {
        // Test verilerini hazırla
        testFlightMessage = FlightMessage.createMessage(
            "TK101", 
            LocalDateTime.of(2024, 1, 15, 10, 30), 
            null
        );
        
        testCrewMessage = FlightCrewMessage.createMessage(
            "John Doe", 
            CrewType.PILOT, 
            1L, 
            "TK101"
        );
        
        // KafkaProducerService'i manuel olarak initialize et
        kafkaProducerService = new KafkaProducerService();
        //@Inject yetersiz kaldığı için reflection kullanıyoruz.Quarkus'ta @Inject ile quarkus context'i (Config değerleri vb.) ekleyemiyoruz.
        //@InjectMocks configuration property'leri inject edemez Bu yüzden manuel olarak field'ları set etmek gerekiyor.
        // Reflection kullanarak mock'ları inject et
        try {
            java.lang.reflect.Field producerField = KafkaProducerService.class.getDeclaredField("producerTemplate");
            producerField.setAccessible(true);//private field'ları erişilebilir yapıyoruz
            producerField.set(kafkaProducerService, producerTemplate);
            
            java.lang.reflect.Field flightTopicField = KafkaProducerService.class.getDeclaredField("flightTopic");
            flightTopicField.setAccessible(true);
            flightTopicField.set(kafkaProducerService, "flight-events-test");
            
            java.lang.reflect.Field crewTopicField = KafkaProducerService.class.getDeclaredField("crewTopic");
            crewTopicField.setAccessible(true);
            crewTopicField.set(kafkaProducerService, "crew-events-test");
        } catch (Exception e) {
            throw new RuntimeException("Test setup failed", e);
        }
    }
    //Kafka mesajının başarılı bir şekilde gönderildiğini test eder
    @Test
    void testSendFlightMessage_Success() {
        // Given (Veri)
        doNothing().when(producerTemplate).sendBody(anyString(), anyString());//çağrıldığında gerçekte bir şey yapma (Kafka’ya gitmesin).
        
        // When (Eylem)
        //assertDoesNotThrow - Metodun hata fırlatmaması bekleniyor.
        assertDoesNotThrow(() -> kafkaProducerService.sendFlightMessage(testFlightMessage));
        
        // Then (Doğrulama)
        verify(producerTemplate, times(1)).sendBody(
            eq("kafka:flight-events-test"), //ilk parametre kafka endpoint'i
            contains("TK101") //ikinci parametre mesajın içeriği
        );
    }
    //SendFlightMessage Servis hata alırsa, beklediğimiz şekilde mi tepki veriyor?
    @Test
    void testSendFlightMessage_ProducerTemplateThrowsException() {
        // Given 
        //doThrow - Metodun hata fırlatması bekleniyor.
        doThrow(new RuntimeException("Kafka connection error"))
            .when(producerTemplate).sendBody(anyString(), anyString());
        
        // When & Then
        //assertThrows - Metodun hata fırlatması bekleniyor.
        RuntimeException exception = assertThrows(
            RuntimeException.class, 
            () -> kafkaProducerService.sendFlightMessage(testFlightMessage)
        );
        //Servisin bizim beklediğimiz custom mesajla (Failed to send flight message to Kafka) exception fırlattığı doğrulanıyor.
        assertEquals("Failed to send flight message to Kafka", exception.getMessage());
        verify(producerTemplate, times(1)).sendBody(anyString(), anyString());
    }
    
    @Test
    void testSendCrewMessage_Success() {
        // Given
        //doNothing - Metodun hiçbir şey yapmaması bekleniyor.
        doNothing().when(producerTemplate).sendBody(anyString(), anyString());
        
        // When
        assertDoesNotThrow(() -> kafkaProducerService.sendCrewMessage(testCrewMessage));
        
        // Then
        //verify - Metodun belirtilen sayıda çağrılması bekleniyor.
        verify(producerTemplate, times(1)).sendBody(
            eq("kafka:crew-events-test"), 
            contains("John Doe")
        );
    }
    
    @Test
    void testSendCrewMessage_ProducerTemplateThrowsException() {
        // Given
        doThrow(new RuntimeException("Kafka connection error"))
            .when(producerTemplate).sendBody(anyString(), anyString());
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class, 
            () -> kafkaProducerService.sendCrewMessage(testCrewMessage)
        );
        
        assertEquals("Failed to send crew message to Kafka", exception.getMessage());
        verify(producerTemplate, times(1)).sendBody(anyString(), anyString());
    }
    
    @Test
    void testSendFlightMessage_NullMessage() {
        // Given - null message, ObjectMapper "null" string döndürür
        
        // When
        assertDoesNotThrow(() -> kafkaProducerService.sendFlightMessage(null));
        
        // ProducerTemplate'e hiç çağrı yapılmamalı
        // Then - "null" string gönderilir
        verify(producerTemplate, times(1)).sendBody(anyString(), eq("null"));
    }
    
    @Test
    void testSendCrewMessage_NullMessage() {
        // Given - null message, ObjectMapper "null" string döndürür
        
        // When
        assertDoesNotThrow(() -> kafkaProducerService.sendCrewMessage(null));
        
        // Then - "null" string gönderilir
        verify(producerTemplate, times(1)).sendBody(anyString(), eq("null"));
    }
    
    @Test
    void testFlightMessageSerialization() {
        // Given
        doNothing().when(producerTemplate).sendBody(anyString(), anyString());
        
        // When
        kafkaProducerService.sendFlightMessage(testFlightMessage);
        
        // Then - JSON formatında serialize edilmiş mesaj gönderildiğini doğrula
        verify(producerTemplate).sendBody(
            anyString(),
            argThat(message -> {
                String jsonMessage = (String) message;
                return jsonMessage.contains("\"messageType\":\"CREATE\"") &&
                       jsonMessage.contains("\"flightNumber\":\"TK101\"");
            })
        );
    }
    
    @Test
    void testCrewMessageSerialization() {
        // Given
        doNothing().when(producerTemplate).sendBody(anyString(), anyString());
        
        // When
        kafkaProducerService.sendCrewMessage(testCrewMessage);
        
        // Then - JSON formatında serialize edilmiş mesaj gönderildiğini doğrula
        verify(producerTemplate).sendBody(
            anyString(),
            argThat(message -> {
                String jsonMessage = (String) message;
                return jsonMessage.contains("\"messageType\":\"CREATE\"") &&
                       jsonMessage.contains("\"crewName\":\"John Doe\"") &&
                       jsonMessage.contains("\"crewType\":\"PILOT\"");
            })
        );
    }
}
