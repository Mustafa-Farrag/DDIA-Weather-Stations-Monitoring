kubectl apply -f Central_Station\central-station.yaml
pause
kubectl apply -f Kafka\kafka.yaml
pause

for /l %%i in (1, 1, 10) do (
    kubectl apply -f Weather_Station\weather-station-%%i.yaml
)
pause