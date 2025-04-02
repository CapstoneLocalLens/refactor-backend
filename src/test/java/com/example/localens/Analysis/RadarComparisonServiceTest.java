package com.example.localens.Analysis;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.example.localens.analysis.dto.CompareTwoDistrictsDTO;
import com.example.localens.analysis.service.RadarAnalysisService;
import com.example.localens.analysis.service.RadarComparisonService;
import com.example.localens.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RadarComparisonServiceTest {

    @Mock
    private RadarAnalysisService radarAnalysisService;

    @InjectMocks
    private RadarComparisonService radarComparisonService;

    @BeforeEach
    void setUp() {
        var radarData1 = TestUtils.mockRadarDataDTO(60);
        var radarData2 = TestUtils.mockRadarDataDTO(30);

        when(radarAnalysisService.getRadarData(1)).thenReturn(radarData1);
        when(radarAnalysisService.getRadarData(2)).thenReturn(radarData2);
    }

    @Test
    void compareTwoDistricts_ShouldReturnDifferences_WhenValidDistricts() {
        CompareTwoDistrictsDTO result = radarComparisonService.compareTwoDistricts(1, 2);

        assertNotNull(result, "Comparison result should not be null");
        assertNotNull(result.getTopDifferences(), "Top differences should not be null");

        assertNotNull(result.getTopDifferences().getKey1());
    }
}
