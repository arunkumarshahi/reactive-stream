package link.alab;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import link.alab.reactiver2dbc.serversideevent.GreetingRequest;
import link.alab.reactiver2dbc.serversideevent.GreetingResponse;
import link.alab.reactiver2dbc.serversideevent.GreetingService;
import lombok.AllArgsConstructor;
import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
public class WebSocketAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebSocketAppApplication.class, args);
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

