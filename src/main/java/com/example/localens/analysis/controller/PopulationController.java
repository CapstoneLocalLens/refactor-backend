package com.example.localens.analysis.controller;

import com.example.localens.analysis.dto.TimeZonePopulationRatioResponse;
import com.example.localens.analysis.service.PopulationRatioService;
import com.example.localens.analysis.service.StayVisitRatioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/population")
@RequiredArgsConstructor
public class PopulationController {

    private final PopulationRatioService populationRatioService;
    private final StayVisitRatioService stayVisitRatioService;

    @GetMapping("/ratio/{districtUuid}")
    public ResponseEntity<TimeZonePopulationRatioResponse> getPopulationRatioByDistrictUuid(
            @PathVariable Integer districtUuid) {
        TimeZonePopulationRatioResponse result = populationRatioService.getPopulationRatioByDistrictUuid(districtUuid);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stay-visit-ratio/{districtUuid}")
    public ResponseEntity<TimeZonePopulationRatioResponse> getStayVisitRatioByDistrictUuid(
            @PathVariable Integer districtUuid) {
        TimeZonePopulationRatioResponse result = stayVisitRatioService.getStayVisitRatioByDistrictUuid(districtUuid);
        return ResponseEntity.ok(result);
    }
}
