package com.example.localens.improvement.service;

import com.example.localens.improvement.domain.Event;
import com.example.localens.improvement.domain.EventMetrics;
import com.example.localens.improvement.repository.EventMetricsRepository;
import com.example.localens.improvement.repository.EventRepository;
import com.example.localens.influx.InfluxDBService;
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

    @Autowired
    public ImprovementService(EventRepository eventRepository,
                              EventMetricsRepository eventMetricsRepository,
                              InfluxDBService influxDBService) {
        this.eventRepository = eventRepository;
        this.eventMetricsRepository = eventMetricsRepository;
        this.influxDBService = influxDBService;
    }

    public List<Event> recommendEvents(String districtUuidNow, String districtUuidTarget) {
        influxDBService.getLatestMetricByDistrictUud(districtUuidNow);
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
}
