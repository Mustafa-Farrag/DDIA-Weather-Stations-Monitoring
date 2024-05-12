## build docker image
`docker build -t rain-detector-image .`

## create a container from image
`docker run -d -it -p 7777:7777 --name rain-detector rain-detector-image`