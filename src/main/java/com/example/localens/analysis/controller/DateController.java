package com.example.localens.analysis.controller;

import com.example.localens.analysis.repository.CommercialDistrictRepository;
import com.example.localens.analysis.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/datecompare")
@RequiredArgsConstructor
public class DateController {

    private final DateAnalysisService dateAnalysisService;
    private final CommercialDistrictRepository commercialDistrictRepository;

    // 두 가지 날짜 형식을 모두 지원하는 DateTimeFormatter 목록
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"), // 두 자리 월/일
            DateTimeFormatter.ofPattern("yyyy년 M월 d일")   // 한 자리 월/일
    );

    /**
     * 한글 날짜를 LocalDateTime으로 변환하는 메서드
     */
    public LocalDateTime parseKoreanDate(String dateStr) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                // T00:00:00 제거, 날짜만 파싱
                LocalDate date = LocalDate.parse(dateStr, formatter);
                return LocalDateTime.of(date, LocalTime.MIDNIGHT);
            } catch (Exception e) {
                // Ignore and try the next formatter
            }
        }
        throw new IllegalArgumentException("Invalid date format: " + dateStr + ". Please use 'yyyy년 MM월 dd일' or 'yyyy년 M월 d일'.");
    }

    /**
     * 날짜 비교 API 엔드포인트
     */
    @GetMapping("/{districtUuid}")
    public ResponseEntity<Map<String, Object>> getPopulationResponse(
            @PathVariable Integer districtUuid,
            @RequestParam String date1,
            @RequestParam String date2
    ) {
        log.info("Received request for districtUuid: {}, date1: {}, date2: {}", districtUuid, date1, date2);
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            // 한글 형식 날짜 파싱
            LocalDateTime parsedDate1 = parseKoreanDate(date1);
            LocalDateTime parsedDate2 = parseKoreanDate(date2);

            log.info("Parsed dates: date1={}, date2={}", parsedDate1, parsedDate2);

            String place = commercialDistrictRepository.findDistrictNameByDistrictUuid(
                    districtUuid);

            // 서비스 호출
            Map<String, Object> result1 = dateAnalysisService.analyzeDateWithTopMetrics(place,
                    parsedDate1.toString());
            Map<String, Object> result2 = dateAnalysisService.analyzeDateWithTopMetrics(place,
                    parsedDate2.toString());

            // 응답 데이터 준비
            response.put("date1", result1);
            response.put("date2", result2);
            log.info("Compare result = {}", response);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid date format: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error in compareDates", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred."));
        }
    }
}
