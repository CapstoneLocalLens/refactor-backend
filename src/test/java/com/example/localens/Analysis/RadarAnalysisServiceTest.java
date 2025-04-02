package com.example.localens.Analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.localens.analysis.domain.CommercialDistrict;
import com.example.localens.analysis.dto.AnalysisRadarDistrictInfoDTO;
import com.example.localens.analysis.dto.RadarDataDTO;
import com.example.localens.analysis.repository.CommercialDistrictRepository;
import com.example.localens.analysis.service.MetricStatsService;
import com.example.localens.analysis.service.RadarAnalysisService;
import com.example.localens.influx.InfluxDBClientWrapper;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RadarAnalysisServiceTest {

    @Mock
    private CommercialDistrictRepository commercialDistrictRepository;

    @Mock
    private MetricStatsService metricStatsService;

    @Mock
    private InfluxDBClientWrapper influxDBClientWrapper;

    @InjectMocks
    private RadarAnalysisService radarAnalysisService;

    private CommercialDistrict mockDistrict;
    private List<FluxTable> testTables;

    @BeforeEach
    void setUp() {
        mockDistrict = mock(CommercialDistrict.class);
        when(mockDistrict.getDistrictName()).thenReturn("TestDistrict");
        when(commercialDistrictRepository.findById(anyInt())).thenReturn(Optional.of(mockDistrict));

        FluxRecord mockRecord = mock(FluxRecord.class);
        when(mockRecord.getValueByKey("_value")).thenReturn(100.0);

        FluxTable mockTable = mock(FluxTable.class);
        when(mockTable.getRecords()).thenReturn(List.of(mockRecord));
        testTables = List.of(mockTable);

        when(influxDBClientWrapper.query(anyString())).thenReturn(testTables);

        when(metricStatsService.normalizeValue(anyString(), anyDouble())).thenReturn(0.5);
    }

    @Test
    void getRadarData_ShouldReturnData_WhenValidDistrictUuid() {
        RadarDataDTO<AnalysisRadarDistrictInfoDTO> result = radarAnalysisService.getRadarData(1);

        assertNotNull(result, "RadarDataDTO should not be null");
        assertNotNull(result.getOverallData(), "OverallData should not be null");
        assertFalse(result.getOverallData().isEmpty(), "OverallData should not be empty");

        Integer population = result.getOverallData().get("population");
        assertEquals(1, population,"정규화된 population이 1이어야 함");

    }
}
