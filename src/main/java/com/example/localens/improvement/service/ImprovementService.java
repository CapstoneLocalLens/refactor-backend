package com.example.localens.improvement.service;

import com.example.localens.improvement.domain.Event;
import com.example.localens.improvement.domain.EventMetrics;
import com.example.localens.improvement.repository.EventMetricsRepository;
import com.example.localens.improvement.repository.EventRepository;
import com.example.localens.influx.InfluxDBService;
import com.example.localens.s3.service.S3Service;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImprovementService {

    private final EventRepository eventRepository;
    private final EventMetricsRepository eventMetricsRepository;
    private final InfluxDBService influxDBService;
    private final S3Service s3Service;

    @Autowired
    public ImprovementService(EventRepository eventRepository,
                              EventMetricsRepository eventMetricsRepository,
                              InfluxDBService influxDBService,
                              S3Service s3Service) {
        this.eventRepository = eventRepository;
        this.eventMetricsRepository = eventMetricsRepository;
        this.influxDBService = influxDBService;
        this.s3Service = s3Service;
    }

    public Map<String, Object> recommendEventsWithDistrictMetrics(String districtUuidNow, String districtUuidTarget) {
        Map<String, Double> metricsA = influxDBService.getLatestMetricsByDistrictUuid(districtUuidNow);
        Map<String, Double> metricsB = influxDBService.getLatestMetricsByDistrictUuid(districtUuidTarget);

        Map<String, Double> metricDifferences = calculateMetricDifferences(metricsA, metricsB);
        List<String> topTwoMetrics = getTopTwoMetrics(metricDifferences);
        List<Event> recommendedEvents = findEventByMetrics(topTwoMetrics);

        recommendedEvents.forEach(event -> {
            String imageUrl = s3Service.generatePresignedUrl("localens-image", event.getEventImg());
            event.setEventImg(imageUrl);
        });

        Map<String, List<Map<String, Double>>> eventMetricsData = new HashMap<>();
        for (Event event : recommendedEvents) {
            List<Map<String, Double>> metricsDuringEvent = influxDBService.getMetricsByDistrictUuidAndTimeRange(
                    districtUuidNow,
                    event.getEventStart(),
                    event.getEventEnd()
            );
            eventMetricsData.put(event.getEventUuid(), metricsDuringEvent);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("recommendedEvents", recommendedEvents);
        result.put("eventMetricsData", eventMetricsData);

        return result;
    }

    private Map<String, Double> calculateMetricDifferences(Map<String, Double> metricsA, Map<String, Double> metricsB) {
        Map<String, Double> differences = new HashMap<>();

        for (String metric : metricsA.keySet()) {
            if (metricsB.containsKey(metric)) {
                Double valueA = metricsA.get(metric);
                Double valueB = metricsB.get(metric);
                if (valueA > valueB) {
                    continue;
                }
                double difference = valueB - valueA;
                differences.put(metric, difference);
            }
        }
        return differences;
    }

    private List<String> getTopTwoMetrics(Map<String, Double> metricDifferences) {
        return metricDifferences.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(2)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<Event> findEventByMetrics(List<String> metricsUuids) {
        List<EventMetrics> eventMetricsList = eventMetricsRepository.findByMetricsUuidIn(metricsUuids);

        Set<String> eventUuids = eventMetricsList.stream()
                .map(EventMetrics::getEventUuid)
                .collect(Collectors.toSet());

        return eventRepository.findAllById(eventUuids);
    }
}
