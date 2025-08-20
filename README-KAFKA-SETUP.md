# Apache Camel + Kafka Entegrasyonu - Mevcut Docker Desktop Setup

## Mimarinin Açıklaması

### Önceki Mimari
```
REST API → Service → Repository → PostgreSQL
```

### Yeni Mimari (Asenkron)
```
REST API → Kafka Producer → Kafka Topic → Apache Camel Consumer → Service → Repository → PostgreSQL
```

## Mevcut Docker Desktop Setup'ınız

Zaten Docker Desktop'ta çalışan servisleriniz:
- ✅ **PostgreSQL** (port 5432)
- ✅ **Zookeeper** (port 2181) 
- ✅ **Kafka Brokers** (kafka0:9092, kafka1:9093)
- ✅ **Kafka UI** (port 8080) - Topic'leri görüntülemek için
- ✅ **Schema Registry** (port 8085)

**🎉 Ek kuruluma gerek YOK!** Mevcut Docker Desktop setup'ınız yeterli.

## Kafka Topic'lerini Kontrol Etme

### 1. Kafka UI kullanarak (Tavsiye edilen)
- http://localhost:8080 adresini açın
- `flight-events` ve `crew-events` topic'lerini görebilirsiniz
- Topic'ler otomatik oluşacak ilk mesaj gönderildiğinde

### 2. Komut satırı ile
```bash
# Kafka container'ına bağlan (kafka0 veya kafka1 kullanabilirsiniz)
docker exec -it kafka0 bash

# Topic'leri listele
kafka-topics --bootstrap-server localhost:9092 --list

# Belirli bir topic'i dinle (mesajları görmek için)
kafka-console-consumer --bootstrap-server localhost:9092,localhost:9093 --topic flight-events --from-beginning
```

## Quarkus Uygulamasını Başlatma

```bash
./mvnw compile quarkus:dev
```

## API Endpoint'lerini Test Etme

### 1. Uçuş Ekleme (Asenkron)
```bash
curl -X POST http://localhost:8087/flights \
  -H "Content-Type: application/json" \
  -d '{
    "flightNumber": "TK123",
    "departureDateTime": "2024-01-15T10:00:00"
  }'
```

**Yanıt:** `202 Accepted` - İşlem asenkron olarak gerçekleştirilecek

### 2. Uçuş Güncelleme (Asenkron)
```bash
curl -X PUT http://localhost:8087/flights/1 \
  -H "Content-Type: application/json" \
  -d '{
    "flightNumber": "TK124",
    "departureDateTime": "2024-01-15T11:00:00"
  }'
```

### 3. Uçuş Silme (Asenkron)
```bash
curl -X DELETE http://localhost:8087/flights/1
```

### 4. Personel Ekleme (Asenkron)
```bash
curl -X POST http://localhost:8087/flights/1/add-crew \
  -H "Content-Type: application/json" \
  -d '{
    "crewName": "Ahmet Yılmaz",
    "crewType": "PILOT",
    "flightId": 1
  }'
```

### 5. Uçuşları Listeleme (Senkron)
```bash
curl http://localhost:8087/flights
```

## Mesaj Akışını İzleme

1. **Kafka UI'da mesajları görüntüleyin:**
   - http://localhost:8080 → Topics → flight-events veya crew-events

2. **Uygulama loglarını takip edin:**
```bash
# Quarkus dev modunda loglar otomatik görünür
```

## İlk Test

1. **Uygulamayı başlatın:**
```bash
./mvnw compile quarkus:dev
```

2. **Bir uçuş ekleyin:**
```bash
curl -X POST http://localhost:8087/flights \
  -H "Content-Type: application/json" \
  -d '{
    "flightNumber": "TK456",
    "departureDateTime": "2024-01-20T14:30:00"
  }'
```

3. **Kafka UI'da mesajı kontrol edin:**
   - http://localhost:8080 → Topics → flight-events

4. **Veritabanında kaydı kontrol edin:**
```bash
curl http://localhost:8087/flights
```

## Troubleshooting

### Kafka Bağlantı Sorunları
- Docker Desktop'ta kafka0 ve kafka1 container'larının çalıştığını kontrol edin
- Port'ların açık olduğunu kontrol edin: 9092, 9093

### Veritabanı Bağlantı Sorunları
- PostgreSQL container'ının çalıştığını kontrol edin
- Port 5432'nin açık olduğunu kontrol edin

### Topic Oluşmuyorsa
- `kafka.auto.create.topics.enable=true` Kafka'da aktif olmalı (genelde varsayılan)
- İlk mesaj gönderildiğinde topic'ler otomatik oluşur

## Önemli Notlar

1. **Asenkron İşlemler:** POST, PUT, DELETE işlemleri artık `202 Accepted` döner
2. **GET İşlemleri:** Okuma işlemleri hala senkron olarak çalışır
3. **Çoklu Broker:** kafka0:9092 ve kafka1:9093 kullanıyor (yüksek availability)
4. **Schema Registry:** Varsa gelişmiş veri şeması yönetimi için kullanılabilir

## Kafka Cluster Bilgileri

Mevcut setup'ınızda:
- **Broker 1:** kafka0 (localhost:9092)
- **Broker 2:** kafka1 (localhost:9093) 
- **Replication Factor:** 2 (yüksek güvenilirlik)
- **Kafka UI:** http://localhost:8080
- **Schema Registry:** http://localhost:8085

Bu setup **production-ready** bir Kafka cluster'ıdır! 🚀