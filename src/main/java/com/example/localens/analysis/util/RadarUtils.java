package com.example.localens.analysis.util;

import com.example.localens.analysis.domain.Pair;
import com.example.localens.analysis.dto.*;

import java.util.ArrayList;
import java.util.List;

public class RadarUtils {

    public static List<Pair<String, Double>> findTopTwo(List<Object> overallData) {
        List<Pair<String, Double>> valueList = new ArrayList<>();

        for (Object obj : overallData) {
            if (obj instanceof RadarFloatingPopulationResponse) {
                RadarFloatingPopulationResponse dto = (RadarFloatingPopulationResponse) obj;
                valueList.add(new Pair<>("유동인구 수", dto.get유동인구_수()));
            } else if (obj instanceof RadarStayVisitRatioResponse) {
                RadarStayVisitRatioResponse dto = (RadarStayVisitRatioResponse) obj;
                valueList.add(new Pair<>("체류/방문 비율", dto.get체류_방문_비율()));
            } else if (obj instanceof RadarCongestionRateResponse) {
                RadarCongestionRateResponse dto = (RadarCongestionRateResponse) obj;
                valueList.add(new Pair<>("혼잡도 변화율", dto.get혼잡도_변화율()));
            } else if (obj instanceof RadarStayPerVisitorResponse) {
                RadarStayPerVisitorResponse dto = (RadarStayPerVisitorResponse) obj;
                valueList.add(new Pair<>("체류시간 대비 방문자 수", dto.get체류시간_대비_방문자_수()));
            } else if (obj instanceof RadarVisitConcentrationResponse) {
                RadarVisitConcentrationResponse dto = (RadarVisitConcentrationResponse) obj;
                valueList.add(new Pair<>("방문 집중도", dto.get방문_집중도()));
            } else if (obj instanceof RadarStayDurationChangeResponse) {
                RadarStayDurationChangeResponse dto = (RadarStayDurationChangeResponse) obj;
                valueList.add(new Pair<>("평균 체류시간 변화율", dto.get평균_체류시간_변화율()));
            }
        }

        valueList.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return valueList.subList(0, 2); // 가장 큰 두 개의 요소 반환
    }

}

