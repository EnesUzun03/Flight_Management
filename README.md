# Quarkus Flight Management System

Bu proje, **Quarkus framework** kullanılarak geliştirilmiş bir **uçuş ve personel yönetim sistemi**dir. PostgreSQL veritabanı, Liquibase migration, Hibernate ORM ve RESTful API'ler kullanılarak oluşturulmuştur.

## 📋 Proje Genel Bakış

Bu sistem, havayolu şirketlerinin uçuş ve personel yönetimini kolaylaştırmak için tasarlanmıştır. Temel özellikler:

- ✈️ Uçuş yönetimi (ekleme, güncelleme, silme, listeleme)
- 👥 Personel yönetimi (pilot, yardımcı pilot, kabin ekibi)
- 🔗 Uçuş-personel ilişki yönetimi
- 🌐 RESTful API ile kolay entegrasyon
- 📊 Swagger UI ile API dokümantasyonu

## 🏗️ Proje Mimarisi

### Katmanlı Mimari (Layered Architecture)
```
┌─────────────────┐
│   Controller    │ ← REST API Endpoints
├─────────────────┤
│    Service      │ ← Business Logic
├─────────────────┤
│   Repository    │ ← Data Access
├─────────────────┤
│    Entity       │ ← Data Models
├─────────────────┤
│   Database      │ ← PostgreSQL
└─────────────────┘
```

## 🛠️ Teknolojiler

- **Framework:** Quarkus 3.24.5
- **Database:** PostgreSQL
- **ORM:** Hibernate with Panache
- **Migration:** Liquibase
- **API Documentation:** Swagger/OpenAPI
- **Build Tool:** Maven
- **Java Version:** 21

## 📁 Proje Yapısı

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── enesuzun/
│   │           ├── Entity/
│   │           │   ├── Flight.java
│   │           │   ├── FlightCrew.java
│   │           │   └── CrewType.java
│   │           ├── Repositories/
│   │           │   ├── FlightRepository.java
│   │           │   └── FlightCrewRepository.java
│   │           ├── Services/
│   │           │   ├── FlightService.java
│   │           │   └── FlightCrewService.java
│   │           └── Controllers/
│   │               ├── FlightController.java
│   │               └── FlightCrewController.java
│   └── resources/
│       ├── application.properties
│       └── db/
│           └── changelog/
│               ├── db.changelog-master.xml
│               ├── flight.xml
│               └── flightcrew.xml
```

## 🚀 Kurulum ve Çalıştırma

### Gereksinimler
- Java 21
- Maven 3.8+
- Docker (PostgreSQL için)
- Docker Compose

### 1. Veritabanı Kurulumu

Docker Compose ile PostgreSQL'i başlatın:

```yaml
services:
  postgres:
    image: postgres
    container_name: postgres
    environment:
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: secret
      POSTGRES_DB: mydb
    ports:
      - "5432:5432"
```

```bash
docker-compose up -d
```

### 2. Uygulama Kurulumu

```bash
# Projeyi klonlayın
git clone <repository-url>
cd quarkus_crud

# Bağımlılıkları yükleyin
./mvnw clean install

# Uygulamayı başlatın
./mvnw compile quarkus:dev
```

### 3. Erişim

- **Uygulama:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/q/swagger-ui
- **OpenAPI:** http://localhost:8080/q/openapi

## 🗄️ Veritabanı Şeması

### Flight Tablosu
```sql
CREATE TABLE flight (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    flight_number VARCHAR(255) NOT NULL,
    departure_time TIME,
    departure_date DATE
);
```

### Flight_Crew Tablosu
```sql
CREATE TABLE flight_crew (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    crew_name VARCHAR(255) NOT NULL,
    crew_type VARCHAR(255),
    flight_id BIGINT NOT NULL,
    FOREIGN KEY (flight_id) REFERENCES flight(id)
);
```

### CrewType Enum
```java
public enum CrewType {
    PILOT,
    COPILOT,
    CABIN_CREW
}
```

## 🌐 API Dokümantasyonu

### Flight Endpoint'leri

#### Tüm Uçuşları Listele
```http
GET /flights
```

#### ID ile Uçuş Getir
```http
GET /flights/{id}
```

#### Yeni Uçuş Ekle
```http
POST /flights
Content-Type: application/json

{
  "flightNumber": "TK123",
  "departureTime": "14:30:00",
  "departureDate": "2024-07-28"
}
```

#### Uçuş Güncelle
```http
PUT /flights/{id}
Content-Type: application/json

{
  "flightNumber": "TK124",
  "departureTime": "15:30:00",
  "departureDate": "2024-07-28"
}
```

#### Uçuş Sil
```http
DELETE /flights/{id}
```

#### Uçuşa Personel Ekle
```http
POST /flights/{flightId}/add-crew
Content-Type: application/json

{
  "crewName": "John Doe",
  "crewType": "PILOT"
}
```

### FlightCrew Endpoint'leri

#### Tüm Personelleri Listele
```http
GET /flight-crews
```

#### ID ile Personel Getir
```http
GET /flight-crews/{id}
```

#### Yeni Personel Ekle
```http
POST /flight-crews
Content-Type: application/json

{
  "crewName": "Jane Smith",
  "crewType": "CABIN_CREW",
  "flight": {
    "id": 1
  }
}
```

#### Personel Güncelle
```http
PUT /flight-crews/{id}
Content-Type: application/json

{
  "crewName": "Jane Smith",
  "crewType": "PILOT",
  "flight": {
    "id": 1
  }
}
```

#### İsme Göre Personel Sil
```http
DELETE /flight-crews/by-name/{crewName}
```

#### Uçuştaki Personelleri Getir
```http
GET /flight-crews/by-flight/{flightId}
```

#### Uçuştaki Belirli Tipteki Personelleri Getir
```http
GET /flight-crews/by-flight/{flightId}/by-type/{crewType}
```

#### İsme Göre Personel Ara
```http
GET /flight-crews/by-name/{crewName}
```

#### Uçuştaki Personel Sayısı
```http
GET /flight-crews/count/by-flight/{flightId}
```

## 🧪 Test Senaryoları

### Swagger UI ile Test

1. **Swagger UI'ya erişin:** http://localhost:8080/q/swagger-ui

2. **Uçuş Ekleme Testi:**
   ```json
   {
     "flightNumber": "TK123",
     "departureTime": "14:30:00",
     "departureDate": "2024-07-28"
   }
   ```

3. **Personel Ekleme Testi:**
   ```json
   {
     "crewName": "John Doe",
     "crewType": "PILOT"
   }
   ```

4. **Uçuşa Personel Ekleme Testi:**
   ```json
   {
     "crewName": "Jane Smith",
     "crewType": "CABIN_CREW"
   }
   ```

### cURL ile Test

#### Uçuş Ekleme
```bash
curl -X 'POST' \
  'http://localhost:8080/flights' \
  -H 'Content-Type: application/json' \
  -d '{
    "flightNumber": "TK123",
    "departureTime": "14:30:00",
    "departureDate": "2024-07-28"
  }'
```

#### Personel Ekleme
```bash
curl -X 'POST' \
  'http://localhost:8080/flight-crews' \
  -H 'Content-Type: application/json' \
  -d '{
    "crewName": "John Doe",
    "crewType": "PILOT",
    "flight": {
      "id": 1
    }
  }'
```

## 🔧 Konfigürasyon

### application.properties
```properties
# Database Configuration
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=admin
quarkus.datasource.password=secret
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/mydb

# Hibernate Configuration
quarkus.hibernate-orm.database.generation=none

# Liquibase Configuration
quarkus.liquibase.migrate-at-start=true
quarkus.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml

# HTTP Configuration
quarkus.http.port=8080
```

## 🐛 Bilinen Sorunlar ve Çözümler

### 1. Sequence Hatası
**Sorun:** `relation "flight_seq" does not exist`
**Çözüm:** Veritabanını sıfırlayın:
```sql
DROP TABLE IF EXISTS flight_crew CASCADE;
DROP TABLE IF EXISTS flight CASCADE;
DROP TABLE IF EXISTS databasechangelog CASCADE;
DROP TABLE IF EXISTS databasechangeloglock CASCADE;
```

### 2. Transaction Hatası
**Sorun:** `Transaction is not active`
**Çözüm:** Service metodlarına `@Transactional` anotasyonu eklenmiştir.

### 3. Detached Entity Hatası
**Sorun:** `detached entity passed to persist`
**Çözüm:** Service'te yeni nesne oluşturma yaklaşımı benimsenmiştir.

## 📊 Proje Durumu

- ✅ **Temel CRUD işlemleri** (Create, Read, Update, Delete)
- ✅ **İlişkisel veri yönetimi** (Flight ↔ FlightCrew)
- ✅ **RESTful API tasarımı**
- ✅ **Swagger/OpenAPI entegrasyonu**
- ✅ **Liquibase migration yönetimi**
- ✅ **Transaction yönetimi**
- ✅ **Enum kullanımı** (CrewType)
- ✅ **Özel sorgular** (repository katmanında)
- ✅ **Katmanlı mimari** (Controller → Service → Repository → Entity)
- ✅ **Docker PostgreSQL entegrasyonu**

**Proje %95 tamamlanmış durumda** ve temel işlevsellik çalışır durumda.

## 🤝 Katkıda Bulunma

1. Projeyi fork edin
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Değişikliklerinizi commit edin (`git commit -m 'Add some amazing feature'`)
4. Branch'inizi push edin (`git push origin feature/amazing-feature`)
5. Pull Request oluşturun

## 📝 Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## 👨‍💻 Geliştirici

**Enes Uzun**

- GitHub: [github-EnesUzun03]

---

**Not:** Bu proje eğitim amaçlı geliştirilmiştir ve production ortamında kullanmadan önce güvenlik, performans ve hata yönetimi konularında iyileştirmeler yapılması önerilir.
