package uk.gov.companieshouse.api.strikeoffobjections.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.api.strikeoffobjections.email.KafkaRestClient;

import java.time.LocalDateTime;
import java.util.function.Supplier;

@Configuration
public class ApplicationConfig {

    @Bean
    public Supplier<LocalDateTime> dateTimeNow() {
        return LocalDateTime::now;
    }

    @Bean
    public KafkaRestClient restClient(RestTemplate restTemplate) {
        return new KafkaRestClient(restTemplate);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

}
