package com.example.localens.improvement.service.component;

import com.example.localens.improvement.constant.ImprovementConstants;
import com.example.localens.improvement.domain.Event;
import com.example.localens.improvement.domain.model.MetricDifference;
import com.example.localens.improvement.repository.EventMetricsRepository;
import com.example.localens.improvement.repository.EventRepository;
import com.example.localens.improvement.repository.MetricRepository;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventFinder {
    private final MetricRepository metricRepository;
    private final EventMetricsRepository eventMetricsRepository;
    private final EventRepository eventRepository;

    public List<Event> findRelevantEvents(List<MetricDifference> differences) {
        log.info("Finding events for differences: {}", differences);

        List<String> metricUuids = differences.stream()
                .map(diff -> {
                    String dbMetricName = convertMetricNameToDbName(diff.getMetricName());
                    String uuid = metricRepository.findMetricsUuidByMetricsName(dbMetricName);
                    log.info("Converting {} -> {} (UUID: {})",
                            diff.getMetricName(), dbMetricName, uuid);
                    return uuid;
                })
                .filter(Objects::nonNull)
                .toList();

        log.info("Found metric UUIDs: {}", metricUuids);

        if (metricUuids.isEmpty()) {
            log.warn("No matching metrics found");
            return Collections.emptyList();
        }

        List<String> eventUuidStrings = eventMetricsRepository.findEventUuidByMetricsUuidIn(metricUuids);
        log.info("Found event UUID strings (before conversion): {}", eventUuidStrings);

        List<String> convertedUuids = eventUuidStrings.stream()
                .map(this::convertAsciiToHexUuid)
                .distinct()
                .toList();
        log.info("Converted UUIDs: {}", convertedUuids);

        return eventRepository.findAllById(convertedUuids);
    }

    private String convertAsciiToHexUuid(String asciiUuid) {
        try {
            // 하이픈 제거
            String cleanAscii = asciiUuid.replace("-", "");

            // ASCII 문자열을 16진수로 변환
            StringBuilder hexUuid = new StringBuilder();
            for (int i = 0; i < cleanAscii.length(); i += 2) {
                String pair = cleanAscii.substring(i, i + 2);
                int asciiValue = Integer.parseInt(pair, 16);
                hexUuid.append(String.format("%02x", (byte) asciiValue));
            }

            // UUID 형식으로 하이픈 추가
            String result = hexUuid.toString();
            return String.format("%s-%s-%s-%s-%s",
                    result.substring(0, 8),
                    result.substring(8, 12),
                    result.substring(12, 16),
                    result.substring(16, 20),
                    result.substring(20));
        } catch (Exception e) {
            log.error("Error converting UUID: {} - {}", asciiUuid, e.getMessage());
            return asciiUuid;  // 변환 실패시 원래 값 반환
        }
    }

    private String convertMetricNameToDbName(String metricName) {
        return switch (metricName) {
            case "stayVisit" -> "STAY_VISIT_RATIO";
            case "stayPerVisitor" -> "STAY_PER_VISITOR";
            case "population" -> "TOTAL_POPULATION";
            case "congestion" -> "CONGESTION_RATE";
            case "visitConcentration" -> "VISIT_CONCENTRATION";
            case "stayTimeChange" -> "STAY_TIME_CHANGE";
            default -> metricName;
        };
    }
}
