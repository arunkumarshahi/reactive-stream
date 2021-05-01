call cls 
call docker-compose down
call mvn clean install -Dmaven.test.skip=true
call docker build -t arunkumarshahi/webfluxsecurity .  
call docker-compose up -d 
call docker ps
