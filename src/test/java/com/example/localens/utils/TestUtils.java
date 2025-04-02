package com.example.localens.utils;

import com.example.localens.analysis.dto.AnalysisRadarDistrictInfoDTO;
import com.example.localens.analysis.dto.RadarDataDTO;
import java.util.Map;

public class TestUtils {
    public static RadarDataDTO<AnalysisRadarDistrictInfoDTO> mockRadarDataDTO(int populationValue) {
        RadarDataDTO<AnalysisRadarDistrictInfoDTO> radarData = new RadarDataDTO<>();

        radarData.setOverallData(
                Map.of("population", populationValue)
        );

        return radarData;
    }
}
