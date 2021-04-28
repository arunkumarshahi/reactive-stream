package link.alab.WebfluxSecurity.DataInitializer;

import link.alab.WebfluxSecurity.model.User;
import link.alab.WebfluxSecurity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Log4j2
public class DataInitializer {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    @EventListener(value = ApplicationReadyEvent.class)
    public void init() {
        log.info("start data initialization...");
        var initUsers = users.deleteAll()
                .thenMany(Flux.just("user", "admin")).flatMap(userName -> {
                    List<String> roles = "user".equals(userName) ?
                            Arrays.asList("ROLE_USER") : Arrays.asList("ROLE_USER", "ROLE_ADMIN");
                    User user = User.builder().roles(roles).username(userName).password(passwordEncoder.encode("password")).email(userName + "alink.lab").build();
                    return users.save(user);
                });
        initUsers.subscribe(log::info);
    }
}