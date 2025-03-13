package com.example.localens.Analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.example.localens.analysis.controller.PopulationController;
import com.example.localens.analysis.dto.AgeGroupStayPatternDTO;
import com.example.localens.analysis.dto.GenderDataDTO;
import com.example.localens.analysis.dto.NationalityPatternDTO;
import com.example.localens.analysis.dto.PopulationDetailsTransformedDTO;
import com.example.localens.analysis.dto.PopulationHourlyDataDTO;
import com.example.localens.analysis.service.PopulationDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class PopulationControllerTest {

    @Mock
    private PopulationDetailsService populationDetailsService;

    @InjectMocks
    private PopulationController populationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetTransformedDetails() {
        Integer districtUuid = 1;
        PopulationDetailsTransformedDTO mockDTO = createMockPopulationDetailsDTO();

        when(populationDetailsService.getTransformedDetailsByDistrictUuid(districtUuid))
                .thenReturn(mockDTO);

        ResponseEntity<PopulationDetailsTransformedDTO> response =
                populationController.getTransformedDetails(districtUuid);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        PopulationDetailsTransformedDTO resultDTO = response.getBody();

        assertHourlyData(mockDTO.getHourlyFloatingPopulation(), resultDTO.getHourlyFloatingPopulation());
        assertHourlyData(mockDTO.getHourlyStayVisitRatio(), resultDTO.getHourlyStayVisitRatio());
        assertHourlyData(mockDTO.getHourlyCongestionRateChange(), resultDTO.getHourlyCongestionRateChange());
        assertHourlyData(mockDTO.getStayPerVisitorDuration(), resultDTO.getStayPerVisitorDuration());
        assertHourlyData(mockDTO.getHourlyAvgStayDurationChange(), resultDTO.getHourlyAvgStayDurationChange());

        // 연령별 데이터 검증
        AgeGroupStayPatternDTO expectedAge = mockDTO.getAgeGroupStayPattern();
        AgeGroupStayPatternDTO actualAge = resultDTO.getAgeGroupStayPattern();

        assertEquals(expectedAge.getTeenagers().getFEMALE(), actualAge.getTeenagers().getFEMALE());
        assertEquals(expectedAge.getTeenagers().getMALE(), actualAge.getTeenagers().getMALE());
        assertEquals(expectedAge.getTwenties().getFEMALE(), actualAge.getTwenties().getFEMALE());
        assertEquals(expectedAge.getTwenties().getMALE(), actualAge.getTwenties().getMALE());

        // 국적별 데이터 검증
        NationalityPatternDTO expectedNat = mockDTO.getNationalityStayPattern();
        NationalityPatternDTO actualNat = resultDTO.getNationalityStayPattern();

        assertEquals(expectedNat.getForeigner(), actualNat.getForeigner());
        assertEquals(expectedNat.getLocal(), actualNat.getLocal());
    }

    private void assertHourlyData(PopulationHourlyDataDTO expected, PopulationHourlyDataDTO actual) {
        assertEquals(expected.getZero(), actual.getZero());
        assertEquals(expected.getOne(), actual.getOne());
        assertEquals(expected.getTwo(), actual.getTwo());
    }

    private PopulationDetailsTransformedDTO createMockPopulationDetailsDTO() {
        PopulationDetailsTransformedDTO dto = new PopulationDetailsTransformedDTO();

        PopulationHourlyDataDTO hourlyFloatingPopulation = new PopulationHourlyDataDTO();
        hourlyFloatingPopulation.setZero(100.0);
        hourlyFloatingPopulation.setOne(120.0);
        hourlyFloatingPopulation.setTwo(90.0);
        dto.setHourlyFloatingPopulation(hourlyFloatingPopulation);

        PopulationHourlyDataDTO hourlyStayVisitRatio = new PopulationHourlyDataDTO();
        hourlyStayVisitRatio.setZero(0.6);
        hourlyStayVisitRatio.setOne(0.7);
        hourlyStayVisitRatio.setTwo(0.5);
        dto.setHourlyStayVisitRatio(hourlyStayVisitRatio);

        PopulationHourlyDataDTO hourlyCongestionRateChange = new PopulationHourlyDataDTO();
        hourlyCongestionRateChange.setZero(0.1);
        hourlyCongestionRateChange.setOne(0.2);
        hourlyCongestionRateChange.setTwo(0.0);
        dto.setHourlyCongestionRateChange(hourlyCongestionRateChange);

        // 체류시간 대비 방문자 수
        PopulationHourlyDataDTO stayPerVisitorDuration = new PopulationHourlyDataDTO();
        stayPerVisitorDuration.setZero(2.5);
        stayPerVisitorDuration.setOne(3.0);
        stayPerVisitorDuration.setTwo(2.0);
        dto.setStayPerVisitorDuration(stayPerVisitorDuration);

        PopulationHourlyDataDTO hourlyAvgStayDurationChange = new PopulationHourlyDataDTO();
        hourlyAvgStayDurationChange.setZero(0.05);
        hourlyAvgStayDurationChange.setOne(0.1);
        hourlyAvgStayDurationChange.setTwo(-0.05);
        dto.setHourlyAvgStayDurationChange(hourlyAvgStayDurationChange);

        AgeGroupStayPatternDTO ageGroupStayPattern = new AgeGroupStayPatternDTO();

        GenderDataDTO teenagers = new GenderDataDTO();
        teenagers.setFEMALE(15.0);
        teenagers.setMALE(18.0);
        ageGroupStayPattern.setTeenagers(teenagers);

        GenderDataDTO twenties = new GenderDataDTO();
        twenties.setFEMALE(25.0);
        twenties.setMALE(23.0);
        ageGroupStayPattern.setTwenties(twenties);

        GenderDataDTO thirties = new GenderDataDTO();
        thirties.setFEMALE(20.0);
        thirties.setMALE(22.0);
        ageGroupStayPattern.setThirties(thirties);

        dto.setAgeGroupStayPattern(ageGroupStayPattern);

        NationalityPatternDTO nationalityStayPattern = new NationalityPatternDTO();
        nationalityStayPattern.setForeigner(25.0);
        nationalityStayPattern.setLocal(75.0);
        dto.setNationalityStayPattern(nationalityStayPattern);

        return dto;
    }
}
