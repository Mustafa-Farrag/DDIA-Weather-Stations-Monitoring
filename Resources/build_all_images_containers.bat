set "CURRENT_DIR=%CD%"

call docker_compose.bat

cd /d "%CURRENT_DIR%"
call "Docker Files\Weather_Station\build_image.bat"

cd /d "%CURRENT_DIR%"
call "Docker Files\Central_Station\build_image.bat"

cd /d "%CURRENT_DIR%"
call "Docker Files\Weather_Station\run_all_stations.bat"

cd /d "%CURRENT_DIR%"
call "Docker Files\Central_Station\run_central_station.bat"

pause