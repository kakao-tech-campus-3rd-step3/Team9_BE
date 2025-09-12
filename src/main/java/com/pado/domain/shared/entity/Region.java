package com.pado.domain.shared.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum Region {
    ONLINE("온라인"),
    SEOUL("서울"),
    GYEONGGI("경기"),
    INCHEON("인천"),
    GANGWON("강원"),
    DAEJEON("대전"),
    SEJONG("세종"),
    CHUNGNAM("충남"),
    CHUNGBUK("충북"),
    GWANGJU("광주"),
    JEONNAM("전남"),
    JEONBUK("전북"),
    DAEGU("대구"),
    GYEONGBUK("경북"),
    BUSAN("부산"),
    ULSAN("울산"),
    GYEONGNAM("경남"),
    JEJU("제주");

    @JsonValue
    private final String krName;

    @JsonCreator
    public static Region from(String krName) {
        return Stream.of(Region.values())
                .filter(region -> region.getKrName().equals(krName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("region: 존재하지 않는 지역입니다: " + krName));
    }
}