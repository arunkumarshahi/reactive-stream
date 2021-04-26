package link.alab.ReactiveClient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class ReactiveClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveClientApplication.class, args);
    }

    @Bean
    WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:8080")
                .filter(ExchangeFilterFunctions.basicAuthentication("admin", "admin"))
                .build();
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class GreetingResponse {
    private String message;
}

@Component
@RequiredArgsConstructor
@Log4j2
class Consumer {
    private final WebClient client;

    @EventListener(ApplicationEvent.class)
    private void ready() {
        this.client.get().uri("/greetings").retrieve().bodyToFlux(GreetingResponse.class)
                .subscribe(log::info);
    }
}
