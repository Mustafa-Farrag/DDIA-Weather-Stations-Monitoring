import yaml

# Define the base template for a weather station deployment
base_template = """
apiVersion: apps/v1
kind: Deployment
metadata:
  name: weather-station-{station_id}-deployment
spec:
  replicas: 1
  selector:
    matchLabels:
      app: weather-station-{station_id}
  template:
    metadata:
      labels:
        app: weather-station-{station_id}
    spec:
      containers:
        - name: weather-station-{station_id}
          image: mustafafarrag/weather-station:latest
          ports:
            - containerPort: 6666
          env:
            - name: KAFKA_BOOTSTRAP
              value: "kafka-service:9092"
            - name: STATION_ID
              value: "{station_id}"
"""

# Generate YAML files for stations 1 to 10
for station_id in range(1, 11):
    yaml_content = base_template.format(station_id=station_id)
    with open(f"weather-station-{station_id}.yaml", "w") as yaml_file:
        yaml_file.write(yaml_content)
