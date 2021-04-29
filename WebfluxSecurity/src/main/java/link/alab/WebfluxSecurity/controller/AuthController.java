package link.alab.WebfluxSecurity.controller;

import link.alab.WebfluxSecurity.model.AuthenticationRequest;
import link.alab.WebfluxSecurity.model.User;
import link.alab.WebfluxSecurity.repository.UserRepository;
import link.alab.WebfluxSecurity.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtTokenProvider tokenProvider;
    private final ReactiveAuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @PostMapping("/token")
    public Mono<ResponseEntity> login(@RequestBody Mono<AuthenticationRequest> authRequest) {
        return authRequest
                .flatMap(login -> authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()))
                        .map(tokenProvider::createToken)
                )
                .map(jwt -> {
                            HttpHeaders httpHeaders = new HttpHeaders();
                            httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
                            var tokenBody = Map.of("id_token", jwt);
                            return new ResponseEntity<>(tokenBody, httpHeaders, HttpStatus.OK);
                        }
                );
    }
}
@Configuration
@Log4j2
class GreetingController {
    @Bean
    RouterFunction<ServerResponse> greetingRoute(UserRepository userRepository,
                                                 ReactiveAuthenticationManager authenticationManager,
                                                 JwtTokenProvider tokenProvider) {
        return route().GET("/users", r -> ok().contentType(MediaType.APPLICATION_JSON).body(userRepository.findAll(), User.class))
                .POST("/auth/token1", request -> {
                    return (request.bodyToMono(AuthenticationRequest.class))
                            .switchIfEmpty(Mono.error(new ServerWebInputException("Request body cannot be empty.")))
                            .flatMap(login -> authenticationManager
                                    .authenticate(new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword())))
                            .map(tokenProvider::createToken)
                             .flatMap(jwt->{
                                 HttpHeaders httpHeaders = new HttpHeaders();
                                        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
                                        var tokenBody = Map.of("id_token", jwt);
                                        return ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt).body(Mono.just(jwt), String.class);
                             });
                }).build();


    }
}

