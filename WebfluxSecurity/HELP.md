# start mongoDB in window
``` cd C:\software\mongodb-windows-x86_64-4.4.5\mongodb-win32-x86_64-windows-4.4.5\bin
mongod.exe

```
### launch the query shell
``` mongo --port 27017 ```

### execute mongo query to look into DB
```
db.user.find( {  } )
```
### invoke api
```
 npm i -g json (its for json formatting in curls)
 * curl -it -X POST http://localhost:8080/auth/token -H "Content-Type:application/json" -d "{\"username\":\"user\", \"password\":\"password\"}"
 * curl -i -X GET http://localhost:8080/users  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwicm9sZXMiOiJST0xFX1VTRVIiLCJpYXQiOjE2MTk2MzMzODMsImV4cCI6MTYxOTYzNjk4M30.xVYMJogCINbpYPpEqr9XcnWKw08jGH5je_qdnYCuvd0"   -H "Content-Type:application/json" | json
```
