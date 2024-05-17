cd /d "%~dp0"
docker run --env-file ./env_files/env-1.list -d -it --net=kafka_network --name weather-station-1 mustafafarrag/weather-station