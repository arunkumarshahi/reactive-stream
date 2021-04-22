package link.alab.reactiver2dbc.serversideevent;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import static org.springframework.web.reactive.function.server.RequestPredicates.method;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
public class GreetingApp {
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

class GreetingService {
	private GreetingResponse greet(GreetingRequest request) {
		return new GreetingResponse("Hello " + request.getName() + " @ " + Instant.now());
	}
	Flux<GreetingResponse> greetMany(GreetingRequest request){
		return Flux.fromStream(
				Stream.generate(()->greet(request)))
				.delayElements(Duration.ofSeconds(5)).subscribeOn(Schedulers.elastic());
	}
}

@Configuration
 class GreetingController {
		
	 @Bean
	    RouterFunction<ServerResponse> greetingRoute(GreetingService gs) {
		 return route().GET("/greetings/{name}",r->ok().contentType(MediaType.TEXT_EVENT_STREAM).
				 body(gs.greetMany(new GreetingRequest(r.pathVariable("name"))),GreetingResponse.class))
				 .GET("/testgreetings/{name}",r->ok().contentType(MediaType.TEXT_EVENT_STREAM).
						 body(gs.greetMany(new GreetingRequest(r.pathVariable("name"))),GreetingResponse.class))
				 .build();

	 }
}
