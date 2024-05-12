for /l %%i in (1, 1, 10) do (
    docker run -e KAFKA_BOOTSTRAP="kafka:9092" -e STATION_ID=%%i -d -it --net=kafka_network --name weather-station-%%i mustafafarrag/weather-station
)