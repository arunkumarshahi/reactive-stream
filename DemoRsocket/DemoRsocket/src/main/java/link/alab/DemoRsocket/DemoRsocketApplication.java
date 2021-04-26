package link.alab.DemoRsocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

@SpringBootApplication
public class DemoRsocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoRsocketApplication.class, args);
    }

    @Bean
    RSocketRequester rSocketRequester(RSocketRequester.Builder builder) {
        return builder.connectTcp("localhost", 9000).block();
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

    Flux<GreetingResponse> greetMany(GreetingRequest request, int delay) {
        return Flux.fromStream(
                Stream.generate(() -> greet(request)))
                .delayElements(Duration.ofSeconds(delay)).subscribeOn(Schedulers.elastic());
    }

    Mono<GreetingResponse> greetOnce(GreetingRequest request) {
        return Mono.just(greet(request));

    }
}

@RequiredArgsConstructor
@Component
class JsonHelper {
    private final ObjectMapper objectMapper;

    @SneakyThrows
    <T> T read(String jsonString, Class<T> tClass) {
        return objectMapper.readValue(jsonString, tClass);
    }

    @SneakyThrows
    String write(Object o) {
        return objectMapper.writeValueAsString(o);
    }
}

@Controller
@RequiredArgsConstructor
class GreetingController {
    private final GreetingService greetingService;

    @MessageMapping("greetings.{timeInSeconds}")
    Flux<GreetingResponse> greet(@DestinationVariable int timeInSeconds, GreetingRequest request) {
        return greetingService.greetMany(request, timeInSeconds);
    }
}

@Component
@Log4j2
@RequiredArgsConstructor
class Consumer {
    private final RSocketRequester rSocketRequester;

    @EventListener(ApplicationEvent.class)
    public void consume() {
        this.rSocketRequester.route("greetings.{timeInSeconds}", 2)
                .data(new GreetingRequest("Happy Learning"))
                .retrieveFlux(GreetingResponse.class)
                .subscribe(log::info);
    }
}