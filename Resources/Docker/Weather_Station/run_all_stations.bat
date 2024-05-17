cd /d "%~dp0"
for /l %%i in (1, 1, 10) do (
    docker run --env-file ./env_files/env-%%i.list -d -it --net=kafka_network --name weather-station-%%i mustafafarrag/weather-station
)