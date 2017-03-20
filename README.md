# Twitter Wall #

A Twitter Wall project for GSOC 2017 Vert.x submission

## Build and run ##

Clone the repository and build with maven:

    git clone https://github.com/slinkydeveloper/twitter-wall-GSOC.git
    cd twitter-wall-GSOC
    mvn package
    
Create a configuration file with twitter `api_key` and `api_secret` (Also if you want you can configure `http_port`) like following:

    {
      "api_key": "your_api_key",
      "api_secret": "your_api_secret",
    }
    
Now you can run the Twitter Wall

    vertx run Server -cp target/server-1.0-SNAPSHOT.jar -conf conf.json
    
Open the url on your browser [localhost:8080](localhost:8080/)

## Author ##
Francesco Guardiani