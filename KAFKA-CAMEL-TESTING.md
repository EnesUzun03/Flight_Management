# Kafka ve Camel Birim Testleri Kılavuzu

Bu döküman, Quarkus projesindeki Kafka ve Apache Camel bileşenleri için yazılmış birim testlerinin kullanımını açıklar.

## Test Yapısı

### 1. Kafka Producer Service Testleri
**Dosya:** `src/test/java/com/enesuzun/messaging/KafkaProducerServiceTest.java`

**Test Edilen Özellikler:**
- Flight mesajlarının Kafka'ya başarılı gönderilmesi
- Crew mesajlarının Kafka'ya başarılı gönderilmesi
- JSON serializasyon işlemleri
- Exception handling (hata durumları)
- Null mesaj kontrolü

**Örnek Test Senaryoları:**
```java
@Test
void testSendFlightMessage_Success() {
    // Başarılı mesaj gönderimi testi
}

@Test
void testSendFlightMessage_ProducerTemplateThrowsException() {
    // Kafka bağlantı hatası testi
}
```

### 2. Message Processor Testleri

#### Flight Message Processor
**Dosya:** `src/test/java/com/enesuzun/camel/FlightMessageProcessorTest.java`

**Test Edilen Özellikler:**
- CREATE, UPDATE, DELETE mesaj tiplerinin işlenmesi
- Geçersiz mesaj formatları
- Service katmanı entegrasyonu
- Hata durumları ve exception handling

#### Crew Message Processor  
**Dosya:** `src/test/java/com/enesuzun/camel/CrewMessageProcessorTest.java`

**Test Edilen Özellikler:**
- Crew oluşturma ve silme işlemleri
- Farklı crew tipleri (PILOT, CABIN_CREW)
- Geçersiz crew tip kontrolü
- Service katmanı entegrasyonu

### 3. Camel Route Testleri

#### Flight Camel Route
**Dosya:** `src/test/java/com/enesuzun/camel/FlightCamelRouteTest.java`

**Test Edilen Özellikler:**
- Kafka mesajlarının route üzerinden işlenmesi
- JSON unmarshal/marshal işlemleri
- Route'ların doğru çalışması
- Mock endpoint'ler ile mesaj akışı kontrolü

#### Flight Crew Camel Route
**Dosya:** `src/test/java/com/enesuzun/camel/FlightCrewCamelRouteTest.java`

**Test Edilen Özellikler:**
- Crew mesajlarının route üzerinden işlenmesi
- Farklı mesaj tiplerinin route'da işlenmesi
- JSON processing
- Route performansı

## Test Dependency'leri

Aşağıdaki dependency'ler `pom.xml` dosyasına eklenmiştir:

```xml
<!-- Test Dependencies for Kafka and Camel -->
<dependency>
    <groupId>org.apache.camel</groupId>
    <artifactId>camel-test-junit5</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>kafka</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.awaitility</groupId>
    <artifactId>awaitility</artifactId>
    <scope>test</scope>
</dependency>
```

## Test Konfigürasyonu

**Dosya:** `src/test/resources/application.properties`

Test ortamı için özel konfigürasyonlar:
- H2 in-memory database
- Test-specific Kafka topics
- Test logging seviyesi
- Camel test konfigürasyonu

## Testleri Çalıştırma

### Tüm Testleri Çalıştırma
```bash
./mvnw test
```

### Sadece Kafka ve Camel Testlerini Çalıştırma
```bash
# Sadece messaging paketindeki testler
./mvnw test -Dtest="com.enesuzun.messaging.*Test"

# Sadece camel paketindeki testler  
./mvnw test -Dtest="com.enesuzun.camel.*Test"
```

### Specific Test Class Çalıştırma
```bash
# Kafka Producer Service testleri
./mvnw test -Dtest="KafkaProducerServiceTest"

# Flight Message Processor testleri
./mvnw test -Dtest="FlightMessageProcessorTest"

# Camel Route testleri
./mvnw test -Dtest="FlightCamelRouteTest"
```

## Test Coverage Raporu

Test coverage raporu almak için:
```bash
./mvnw clean test jacoco:report
```

Rapor `target/site/jacoco/index.html` dosyasında oluşturulur.

## Mockito Kullanımı

Testlerde Mockito framework kullanılmıştır:

```java
@Mock
private FlightService flightService;

@InjectMocks
private FlightMessageProcessor flightMessageProcessor;

// Behavior verification
verify(flightService, times(1)).addFlight(any(Flight.class));

// Stubbing
when(flightService.addFlight(any())).thenReturn(new Flight());
```

## Apache Camel Test Framework

Camel route testlerinde Apache Camel Test Framework kullanılmıştır:

```java
public class FlightCamelRouteTest extends CamelTestSupport {
    
    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new FlightCamelRoute();
    }
    
    @Test
    void testRoute() throws Exception {
        // AdviceWith ile route'u test için configure et
        AdviceWith.adviceWith(context, "route-id", routeBuilder -> {
            routeBuilder.replaceFromWith("direct:test-input");
            routeBuilder.interceptSendToEndpoint("kafka:*")
                .skipSendToOriginalEndpoint()
                .to("mock:output");
        });
        
        MockEndpoint mockOutput = getMockEndpoint("mock:output");
        mockOutput.expectedMessageCount(1);
        
        template.sendBody("direct:test-input", testMessage);
        
        mockOutput.assertIsSatisfied();
    }
}
```

## Test Best Practices

1. **Isolation**: Her test bağımsız çalışmalı
2. **Mocking**: External dependencies mock'lanmalı
3. **Data Setup**: Test data'ları setUp() metodunda hazırlanmalı
4. **Assertions**: Net ve anlaşılır assertion'lar yazılmalı
5. **Exception Testing**: Negative case'ler test edilmeli
6. **Coverage**: Tüm code path'ler test edilmeli

## Troubleshooting

### Test Hatası: Kafka Connection
Eğer Kafka connection hatası alıyorsanız, test properties dosyasındaki Kafka konfigürasyonunu kontrol edin.

### Test Hatası: Camel Context
Camel context başlatma problemi yaşıyorsanız, `@EnableAutoConfiguration` annotation'ını kontrol edin.

### Test Hatası: H2 Database
H2 database sorunları için test application.properties'deki database URL'ini kontrol edin.

## Sonuç

Bu test suite'i ile:
- ✅ Kafka Producer Service tamamen test edildi
- ✅ Message Processor'lar test edildi
- ✅ Camel Route'ları test edildi  
- ✅ Error handling test edildi
- ✅ JSON serialization/deserialization test edildi
- ✅ Mock framework entegrasyonu sağlandı

Testler düzenli olarak çalıştırılarak kod kalitesi sağlanmalıdır.



