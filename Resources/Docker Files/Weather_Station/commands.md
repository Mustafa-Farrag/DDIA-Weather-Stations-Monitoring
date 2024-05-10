## build docker image
`docker build -t weather-station-image .`

## create a container from image
`docker run -e STATION_ID=1 -d -it -p 6660:6666 --name weather-station-1 weather-station-image`
