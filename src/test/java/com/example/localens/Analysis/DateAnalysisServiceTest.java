package com.example.localens.Analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.localens.analysis.service.DateAnalysisService;
import com.example.localens.analysis.service.MetricStatsService;
import com.example.localens.influx.InfluxDBClientWrapper;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DateAnalysisServiceTest {

    @Mock
    private InfluxDBClientWrapper influxDBClientWrapper;

    @Mock
    private MetricStatsService metricStatsService;

    @InjectMocks
    private DateAnalysisService dateAnalysisService;

    private List<FluxTable> testTables;
    private List<FluxRecord> testRecords;

    @BeforeEach
    void setUp() {
        FluxRecord mockRecord = mock(FluxRecord.class);
        testRecords = List.of(mockRecord);

        FluxTable mockTable = mock(FluxTable.class);
        when(mockTable.getRecords()).thenReturn(testRecords);

        testTables = List.of(mockTable);

        when(influxDBClientWrapper.query(anyString())).thenReturn(testTables);

        when(metricStatsService.normalizeValue(anyString(), anyDouble())).thenReturn(0.5);
    }

    @Test
    void analyzeDate_ShouldReturnNormalizedValues_WhenDataExists() {
        Map<String, Integer> result = dateAnalysisService.analyzeDate("TestPlace", "2025-01-01");

        assertFalse(result.isEmpty());
        assertEquals(50, (int) result.get("population"));
    }

    @Test
    void analyDateWithTopMetrics_ShouldReturnTopTwo_WhenDataExists() {
        Map<String, Object> result = dateAnalysisService.analyzeDateWithTopMetrics("TestPlace", "2025-01-01");

        assertTrue(result.containsKey("values"));
        assertTrue(result.containsKey("topTwo"));

        Map<String, Integer> values = (Map<String, Integer>) result.get("values");
        assertEquals(50, (int) values.get("population"));

        Map<String, String> topTwo = (Map<String, String>) result.get("topTwo");
        assertTrue(topTwo.containsKey("first"));
    }
}
