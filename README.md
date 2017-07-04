This Metric Forwarder service converts payload to Prometheus format and works as Prometheus Exporter.


### deploy service and create user provided service

```
./mvnw clean package -DskipTests=true && cf push
cf create-user-provided-service metrics-forwarder -p '{"endpoint": "https://demo-metrics-forwarder-service.cfapps.io", "access_key":""}'
```

# send metrics to the metrics forwarder service

```
cf push my-boot-app --no-start
cf bind-service my-boot-app metrics-forwarder
```


See [https://demo-metrics-forwarder-service.cfapps.io/prometheus].

> Java Buildpack must be 3.18+ or 4.2+.
