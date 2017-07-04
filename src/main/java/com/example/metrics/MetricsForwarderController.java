package com.example.metrics;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetricsForwarderController {
	private final ConcurrentMap<String /* metrics name */, ConcurrentMap<String /* instance_id */, String /* metrics */ >> metricsMap = new ConcurrentHashMap<>();

	@PostMapping("/")
	public void metrics(@RequestBody Payload payload) {
		payload.getApplications().forEach(application -> {
			String applicationId = application.getId();
			List<Instance> instances = application.getInstances();
			instances.forEach(instance -> {
				String instanceId = instance.getId();
				String instanceIndex = instance.getIndex();
				instance.getMetrics().forEach(metric -> {
					StringBuilder builder = new StringBuilder();
					String metricName = "spring_"
							+ metric.getName().replace("-", "_").replace(".", "_");
					builder.append(metricName).append("{").append("application_index=\"")
							.append(applicationId).append("\",")
							.append("application_instance_index=\"").append(instanceIndex)
							.append("\",").append("application_instance_id=\"")
							.append(instanceId).append("\",").append("} ")
							.append(metric.getValue()).append(" ")
							.append(metric.getTimestamp()).append(System.lineSeparator());
					metricsMap.computeIfAbsent(metricName, k -> new ConcurrentHashMap<>())
							.put(instanceId, builder.toString());

				});
			});
		});
	}

	@GetMapping(path = "/prometheus", produces = "text/plain; version=0.0.4")
	public String prometheus() {
		StringBuilder builder = new StringBuilder();
		metricsMap.forEach((metricName, map) -> {
			String type = metricName.startsWith("spring_counter") ? "counter" : "gauge";
			builder.append("# HELP ").append(metricName).append(System.lineSeparator());
			builder.append("# TYPE ").append(metricName).append(" ").append(type)
					.append(System.lineSeparator());
			map.values().forEach(builder::append);
			builder.append(System.lineSeparator());
		});
		return builder.toString();
	}
}
