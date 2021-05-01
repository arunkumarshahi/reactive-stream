### flow description - token generation
-----------------------------------------
* user passes credentials to the controller
* controller intercept credential and call Authentication manager with UsernamePasswordAuthenticationToken
* upon succesful verification of authenticity controller invoke create token with authntication object.
* This authentication object is used to generate jwt token

### flow description - token interception
-----------------------------------------
* SecurityWebFilterChain bean is defined with path matcher to apply ath based authorization.
* A filter jwtauthentication is added in  SecurityWebFilterChain.addFilterAt.
* JwtTokenAuthenticationFilter implements WebFilter which verify and validate token with help of token provider bean

### start mongoDB in window
---------------------------------------
``` cd C:\software\mongodb-windows-x86_64-4.4.5\mongodb-win32-x86_64-windows-4.4.5\bin 
    mongod.exe
```
### launch the query shell
``` mongo --port 27017 ```

### execute mongo query to look into DB
```
db.user.find( {  } )
```
### start mongoDB in docker compose
---------------------------------------
``` docker exec -it fc825fc14b70 bash
    
```
### launch the query shell
``` mongo --port 27017 ```

### execute mongo query to look into DB
```
use blog;
db.user.find( {  } ).pretty()
```

### invoke api
```
 npm i -g json (its for json formatting in curls)
 * curl -it -X POST http://localhost:8080/auth/token -H "Content-Type:application/json" -d "{\"username\":\"user\", \"password\":\"password\"}"
 * curl -i -X GET http://localhost:8080/users  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwicm9sZXMiOiJST0xFX1VTRVIiLCJpYXQiOjE2MTk2OTI2MTgsImV4cCI6MTYxOTY5NjIxOH0.gFHBhSyQ5AkQ6oUPfZbVyi4xG3WoHiaDNVO5BPtkchA"   -H "Content-Type:application/json" | json
```
