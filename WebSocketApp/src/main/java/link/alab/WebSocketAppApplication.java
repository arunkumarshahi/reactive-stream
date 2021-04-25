package link.alab;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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

@Service
class GreetingService {
	private GreetingResponse greet(GreetingRequest request) {
		return new GreetingResponse("Hello " + request.getName() + " @ " + Instant.now());
	}

	Flux<GreetingResponse> greetMany(GreetingRequest request) {
		return Flux.fromStream(Stream.generate(() -> greet(request))).delayElements(Duration.ofSeconds(5))
				.subscribeOn(Schedulers.elastic());
	}
}

@Configuration
@RequiredArgsConstructor
@Slf4j
class webSocketHandlerApp {
	private final GreetingService greetingService;

	@Bean
	SimpleUrlHandlerMapping simpleUrlHandlerMapping(WebSocketHandler wsh) {
		return new SimpleUrlHandlerMapping(Map.of("/ws/greeting", wsh), 10);
	}

	@Bean
	WebSocketHandler webSocketHandler1(GreetingService gs) {
		// TODO Auto-generated constructor stub
		return session -> {
			// TODO Auto-generated method stub
			Flux<WebSocketMessage> receive = session.receive();
			Flux<String> names = receive.map(WebSocketMessage::getPayloadAsText);
			//names.subscribe(System.out::println);
			Flux<GreetingRequest> reqFlux = names.map(name -> new GreetingRequest(name));
			Flux<GreetingResponse> greetingResFlux = reqFlux.flatMap(req -> greetingService.greetMany(req));
			Flux<String> map = greetingResFlux.map(GreetingResponse::getMessage);
			Flux<WebSocketMessage> webSocketMsgFlux = map.map(session::textMessage);
			webSocketMsgFlux.doFinally(signal->log.info("session cancellled"));
			return session.send(webSocketMsgFlux);

		};
	}

	@Bean
	WebSocketHandlerAdapter webSocketHandlerAdapter() {
		return new WebSocketHandlerAdapter();
	}

}

@Configuration
class GreetingController {

	@Bean
	RouterFunction<ServerResponse> greetingRoute(GreetingService gs) {
		return route()
				.GET("/greetings/{name}",
						r -> ok().contentType(MediaType.TEXT_EVENT_STREAM).body(
								gs.greetMany(new GreetingRequest(r.pathVariable("name"))), GreetingResponse.class))
				.GET("/testgreetings/{name}",
						r -> ok().contentType(MediaType.TEXT_EVENT_STREAM).body(
								gs.greetMany(new GreetingRequest(r.pathVariable("name"))), GreetingResponse.class))
				.build();

	}
}
