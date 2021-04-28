package link.alab.WebfluxSecurity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "jwt")
@Data
@Configuration
public class JwtProperties {
    private String secretKey = "flzxsqcysyhljt";
    //validity in milliseconds
    private long validityInMs = 3600000; // 1h
}