package com.enesuzun.Services;
/*
 * Bu dosyalar Service Layer (İş Mantığı Katmanı) sınıflarıdır.
 *  Repository'lerden farklı olarak, business logic'i içerir ve transaction yönetimi yapar. 
 * Service Layer Pattern uygulayarak, veri erişim katmanı ile presentation katmanı arasında köprü görevi görürler.
 * 
 */
import java.util.List;

import com.enesuzun.Entity.CrewType;
import com.enesuzun.Entity.Flight;
import com.enesuzun.Entity.FlightCrew;
import com.enesuzun.Repositories.FlightCrewRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped//CDI ile yönetilen singleton bean
public class FlightCrewService {
    //Dependency Injection ile FlightCrewRepository'yi otomatik enjekte eder
    @Inject
    FlightCrewRepository flightCrewRepository;

    // Yeni personel ekle
    @Transactional// Bu metod bir transaction içinde çalışır.Transactional başarısız olursa rollback işlemi yapılır
    public void addCrew(FlightCrew crew) {
        flightCrewRepository.persist(crew);
    }

    // Tüm personelleri getir
    //Burada transactional kullanmama sebebimiz ise sadece veritabanından okuma işlemi olduğu için
    public List<FlightCrew> getAllCrews() {
        return flightCrewRepository.findAllCrews();
    }

    // ID ile personel getir
    public FlightCrew getCrewById(Long id) {
        return flightCrewRepository.findById(id);//findById() Panache'nin arama metodu id=? şeklinde sorgu oluşturur
    }

    // İsme göre personel ara
    public List<FlightCrew> getCrewsByName(String crewName) {
        return flightCrewRepository.findByCrewName(crewName);
    }

    // Uçuştaki tüm personelleri getir
    public List<FlightCrew> getCrewsByFlightId(Long flightId) {
        return flightCrewRepository.findByFlightId(flightId);
    }

    // Uçuştaki belirli tipteki personelleri getir
    public List<FlightCrew> getCrewsByFlightIdAndType(Long flightId, CrewType crewType) {
        return flightCrewRepository.findByFlightIdAndCrewType(flightId, crewType);
    }

    // Personel sil
    @Transactional
    public boolean deleteCrewByName(String crewName) {//Silme işleminde transaction gereklidir.Veri tabaındaki herdegişikilikte transaction gereklidir
        return flightCrewRepository.deleteByCrewName(crewName);
    }

    // Personel güncelle (tüm alanlar)
    @Transactional
    public boolean updateCrew(Long id, String crewName, CrewType crewType, Flight flight) {
        return flightCrewRepository.updateFlightCrew(id, crewName, crewType, flight);
    }

    // Uçuştaki personel sayısı
    public long countCrewsByFlightId(Long flightId) {
        return flightCrewRepository.countByFlightId(flightId);
    }
}
