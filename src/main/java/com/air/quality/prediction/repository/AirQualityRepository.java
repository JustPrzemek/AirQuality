package com.air.quality.prediction.repository;

import com.air.quality.prediction.model.AirQualityData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface AirQualityRepository extends JpaRepository<AirQualityData, Long> {

    boolean existsBySourceFile(String sourceFile);

    List<AirQualityData> findByDate(LocalDate date);

    @Query("SELECT a FROM AirQualityData a WHERE a.date BETWEEN :startDate AND :endDate")
    List<AirQualityData> findByDateRange(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT " +
            "AVG(pm25) as avgPm25, MIN(pm25) as minPm25, MAX(pm25) as maxPm25, " +
            "AVG(pm10) as avgPm10, MIN(pm10) as minPm10, MAX(pm10) as maxPm10, " +
            "AVG(iaq) as avgIaq, MIN(iaq) as minIaq, MAX(iaq) as maxIaq, " +
            "AVG(hcho) as avgHcho, MIN(hcho) as minHcho, MAX(hcho) as maxHcho, " +
            "AVG(co2) as avgCo2, MIN(co2) as minCo2, MAX(co2) as maxCo2, " +
            "AVG(p) as avgP, MIN(p) as minP, MAX(p) as maxP, " +
            "AVG(tin) as avgTin, MIN(tin) as minTin, MAX(tin) as maxTin, " +
            "AVG(tout) as avgTout, MIN(tout) as minTout, MAX(tout) as maxTout, " +
            "AVG(rhin) as avgRhin, MIN(rhin) as minRhin, MAX(rhin) as maxRhin, " +
            "AVG(rhout) as avgRhout, MIN(rhout) as minRhout, MAX(rhout) as maxRhout, " +
            "AVG(no2) as avgNo2, MIN(no2) as minNo2, MAX(no2) as maxNo2, " +
            "AVG(no) as avgNo, MIN(no) as minNo, MAX(no) as maxNo, " +
            "AVG(so2) as avgSo2, MIN(so2) as minSo2, MAX(so2) as maxSo2, " +
            "AVG(h2s) as avgH2s, MIN(h2s) as minH2s, MAX(h2s) as maxH2s, " +
            "AVG(co) as avgCo, MIN(co) as minCo, MAX(co) as maxCo, " +
            "AVG(hcn) as avgHcn, MIN(hcn) as minHcn, MAX(hcn) as maxHcn, " +
            "AVG(hcl) as avgHcl, MIN(hcl) as minHcl, MAX(hcl) as maxHcl, " +
            "AVG(nh3) as avgNh3, MIN(nh3) as minNh3, MAX(nh3) as maxNh3, " +
            "AVG(ec) as avgEc, MIN(ec) as minEc, MAX(ec) as maxEc " +
            "FROM air_quality_data " +
            "WHERE date = :date",
            nativeQuery = true)
    Map<String, Object> getDailyStatsByDateNative(@Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM AirQualityData a WHERE a.date = :date")
    long countByDate(@Param("date") LocalDate date);

    @Query("SELECT DISTINCT a.date FROM AirQualityData a ORDER BY a.date")
    List<LocalDate> findAllDistinctDates();
}
