set "CURRENT_DIR=%CD%"

cd /d "%CURRENT_DIR%"
call "Kafka\docker_compose.bat"

cd /d "%CURRENT_DIR%"
call "Elastic_Kibana\docker_compose.bat"

cd /d "%CURRENT_DIR%"
call "Weather_Station\build_image.bat"

cd /d "%CURRENT_DIR%"
call "Central_Station\build_image.bat"

cd /d "%CURRENT_DIR%"
call "Weather_Station\run_all_stations.bat"

cd /d "%CURRENT_DIR%"
call "Central_Station\run_central_station.bat"

pause