package com.air.quality.prediction.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "air_quality_data")
public class AirQualityData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private LocalTime time;
    private Double pm25;
    private Double pm10;
    private Double iaq;
    private Double hcho;
    private Double co2;
    private Double p;
    private Double tin;
    private Double tout;
    private Double rhin;
    private Double rhout;
    private Double lat;
    private Double lon;
    private Double hdg;
    private Double amsl;
    private Double agl;
    private Double mil;
    private Double no2;
    private Double no;
    private Double so2;
    private Double h2s;
    private Double co;
    private Double hcn;
    private Double hcl;
    private Double nh3;
    private Double ec;
    private String mrk;

    private String sourceFile;

    public AirQualityData() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public Double getPm25() {
        return pm25;
    }

    public void setPm25(Double pm25) {
        this.pm25 = pm25;
    }

    public Double getPm10() {
        return pm10;
    }

    public void setPm10(Double pm10) {
        this.pm10 = pm10;
    }

    public Double getIaq() {
        return iaq;
    }

    public void setIaq(Double iaq) {
        this.iaq = iaq;
    }

    public Double getHcho() {
        return hcho;
    }

    public void setHcho(Double hcho) {
        this.hcho = hcho;
    }

    public Double getCo2() {
        return co2;
    }

    public void setCo2(Double co2) {
        this.co2 = co2;
    }

    public Double getP() {
        return p;
    }

    public void setP(Double p) {
        this.p = p;
    }

    public Double getTin() {
        return tin;
    }

    public void setTin(Double tin) {
        this.tin = tin;
    }

    public Double getTout() {
        return tout;
    }

    public void setTout(Double tout) {
        this.tout = tout;
    }

    public Double getRhin() {
        return rhin;
    }

    public void setRhin(Double rhin) {
        this.rhin = rhin;
    }

    public Double getRhout() {
        return rhout;
    }

    public void setRhout(Double rhout) {
        this.rhout = rhout;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getHdg() {
        return hdg;
    }

    public void setHdg(Double hdg) {
        this.hdg = hdg;
    }

    public Double getAmsl() {
        return amsl;
    }

    public void setAmsl(Double amsl) {
        this.amsl = amsl;
    }

    public Double getAgl() {
        return agl;
    }

    public void setAgl(Double agl) {
        this.agl = agl;
    }

    public Double getMil() {
        return mil;
    }

    public void setMil(Double mil) {
        this.mil = mil;
    }

    public Double getNo2() {
        return no2;
    }

    public void setNo2(Double no2) {
        this.no2 = no2;
    }

    public Double getNo() {
        return no;
    }

    public void setNo(Double no) {
        this.no = no;
    }

    public Double getSo2() {
        return so2;
    }

    public void setSo2(Double so2) {
        this.so2 = so2;
    }

    public Double getH2s() {
        return h2s;
    }

    public void setH2s(Double h2s) {
        this.h2s = h2s;
    }

    public Double getCo() {
        return co;
    }

    public void setCo(Double co) {
        this.co = co;
    }

    public Double getHcn() {
        return hcn;
    }

    public void setHcn(Double hcn) {
        this.hcn = hcn;
    }

    public Double getHcl() {
        return hcl;
    }

    public void setHcl(Double hcl) {
        this.hcl = hcl;
    }

    public Double getNh3() {
        return nh3;
    }

    public void setNh3(Double nh3) {
        this.nh3 = nh3;
    }

    public Double getEc() {
        return ec;
    }

    public void setEc(Double ec) {
        this.ec = ec;
    }

    public String getMrk() {
        return mrk;
    }

    public void setMrk(String mrk) {
        this.mrk = mrk;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }
}