package link.alab.ReactiveSecurity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class ReactiveSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveSecurityApplication.class, args);
    }

    @Bean
    MapReactiveUserDetailsService userDetailsService() {
        UserDetails userDetails = User.withDefaultPasswordEncoder().username("admin").password("admin").roles("admin").build();
        UserDetails userDetails1 = User.withDefaultPasswordEncoder().username("user").password("user").roles("user").build();
        return new MapReactiveUserDetailsService(userDetails, userDetails1);
    }

    @Bean
    SecurityWebFilterChain authChain(ServerHttpSecurity httpSecurity) {
        return httpSecurity
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(Customizer.withDefaults())
                .authorizeExchange(ae -> ae.pathMatchers("/greeting*").authenticated()
                        .anyExchange().permitAll())
                .build();
    }

    @Bean
    RouterFunction<ServerResponse> routers(GreetingService gs) {
        return route().GET("/greetings", ((ServerRequest request) -> {
            Flux<GreetingResponse> gsResponse = request.principal().map(Principal::getName).map(GreetingRequest::new)
                    .flatMapMany(gs::greetMany);
            return ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM).body(gsResponse, GreetingResponse.class);
        })).GET("/greeting", ((ServerRequest request) -> {
            Mono<GreetingResponse> gsResponse = request.principal().map(Principal::getName).map(GreetingRequest::new)
                    .flatMap(gs::greetOnce);
            return ServerResponse.ok().body(gsResponse, GreetingResponse.class);
        }))
                .build();
    }
}

@Data
@AllArgsConstructor
class GreetingRequest {
    private String name;
}

@Data
@AllArgsConstructor
class GreetingResponse {
    private String message;
}

@Service
class GreetingService {
    private GreetingResponse greet(GreetingRequest request) {
        return new GreetingResponse("Hello " + request.getName() + " @ " + Instant.now());
    }

    Flux<GreetingResponse> greetMany(GreetingRequest request) {
        return Flux.fromStream(
                Stream.generate(() -> greet(request)))
                .delayElements(Duration.ofSeconds(5)).subscribeOn(Schedulers.elastic());
    }

    Mono<GreetingResponse> greetOnce(GreetingRequest request) {
        return Mono.just(greet(request));

    }
}

//	@Configuration
//	class GreetingController {
//
//		@Bean
//		RouterFunction<ServerResponse> greetingRoute(GreetingService gs) {
//			return route().GET("/greetings/{name}",r->ok().contentType(MediaType.TEXT_EVENT_STREAM).
//					body(gs.greetMany(new GreetingRequest(r.pathVariable("name"))),GreetingResponse.class))
//					.GET("/testgreetings/{name}",r->ok().contentType(MediaType.TEXT_EVENT_STREAM).
//							body(gs.greetMany(new GreetingRequest(r.pathVariable("name"))),GreetingResponse.class))
//					.build();
//
//		}
//	}
//




