package com.example.localens.Analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.localens.analysis.controller.DateController;
import com.example.localens.analysis.repository.CommercialDistrictRepository;
import com.example.localens.analysis.service.DateAnalysisService;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DateControllerTest {

    @Mock
    private DateAnalysisService dateAnalysisService;

    @Mock
    private CommercialDistrictRepository commercialDistrictRepository;

    @InjectMocks
    private DateController dateController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetPopulationResponse_Success() {
        // Arrange
        Integer districtUuid = 1;
        String date1 = "2023년 12월 01일";
        String date2 = "2023년 12월 02일";
        String districtName = "Test District";

        // 첫 번째 날짜에 대한 결과
        Map<String, Object> result1 = new LinkedHashMap<>();
        Map<String, Integer> values1 = new LinkedHashMap<>();
        values1.put("population", 85);
        values1.put("stayVisit", 70);
        Map<String, String> topTwo1 = new LinkedHashMap<>();
        topTwo1.put("first", "population");
        topTwo1.put("second", "stayVisit");
        result1.put("values", values1);
        result1.put("topTwo", topTwo1);

        // 두 번째 날짜에 대한 결과 (여기서는 동일하게 설정)
        Map<String, Object> result2 = new LinkedHashMap<>();
        Map<String, Integer> values2 = new LinkedHashMap<>();
        values2.put("population", 85);
        values2.put("stayVisit", 70);
        Map<String, String> topTwo2 = new LinkedHashMap<>();
        topTwo2.put("first", "population");
        topTwo2.put("second", "stayVisit");
        result2.put("values", values2);
        result2.put("topTwo", topTwo2);

        // 모킹 설정
        when(commercialDistrictRepository.findDistrictNameByDistrictUuid(districtUuid))
                .thenReturn(districtName);

        // 연속 호출에 대한 응답을 체인 형태로 설정
        when(dateAnalysisService.analyzeDateWithTopMetrics(eq(districtName), any(String.class)))
                .thenReturn(result1, result2);

        // Act
        ResponseEntity<Map<String, Object>> response = dateController.getPopulationResponse(
                districtUuid, date1, date2);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("date1"));
        assertTrue(response.getBody().containsKey("date2"));

        // 위에서 모킹한 값이 응답에 포함되었는지 확인
        @SuppressWarnings("unchecked")
        Map<String, Object> date1Response = (Map<String, Object>) response.getBody().get("date1");
        @SuppressWarnings("unchecked")
        Map<String, Object> date2Response = (Map<String, Object>) response.getBody().get("date2");

        // 각 값이 처음에 설정한 모의 데이터와 동일한지 검증
        @SuppressWarnings("unchecked")
        Map<String, Integer> date1Values = (Map<String, Integer>) date1Response.get("values");
        @SuppressWarnings("unchecked")
        Map<String, String> date1TopTwo = (Map<String, String>) date1Response.get("topTwo");

        assertEquals(85, date1Values.get("population"));
        assertEquals(70, date1Values.get("stayVisit"));
        assertEquals("population", date1TopTwo.get("first"));
        assertEquals("stayVisit", date1TopTwo.get("second"));

        @SuppressWarnings("unchecked")
        Map<String, Integer> date2Values = (Map<String, Integer>) date2Response.get("values");
        @SuppressWarnings("unchecked")
        Map<String, String> date2TopTwo = (Map<String, String>) date2Response.get("topTwo");

        assertEquals(85, date2Values.get("population"));
        assertEquals(70, date2Values.get("stayVisit"));
        assertEquals("population", date2TopTwo.get("first"));
        assertEquals("stayVisit", date2TopTwo.get("second"));
    }
}
