call cls 
call mvn clean install 
call docker build -t arunkumarshahi/WebfluxSecurity .  
call docker-compose up -d 
call docker ps
