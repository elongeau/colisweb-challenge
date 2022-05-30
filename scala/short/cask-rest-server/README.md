    sbt ~reStart
    
    curl localhost:8080
    curl -X POST -d azerty localhost:8080/reverse   
    curl -X POST -d '{"person":{"name": "hao", "age":29}}' localhost:8080/api/person/aze
