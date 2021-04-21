call cls 
call mvn clean install 
call docker build -t arunkumarshahi/reactive-r2dbc .  
call docker-compose up -d 
call docker ps
