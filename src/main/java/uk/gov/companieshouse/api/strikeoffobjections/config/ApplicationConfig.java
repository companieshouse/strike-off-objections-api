package uk.gov.companieshouse.api.strikeoffobjections.config;

import java.time.LocalDateTime;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.api.strikeoffobjections.chips.ChipsKafkaClient;
import uk.gov.companieshouse.api.strikeoffobjections.chips.ChipsRestClient;
import uk.gov.companieshouse.api.strikeoffobjections.chips.ChipsSender;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.email.KafkaRestClient;

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

    @Bean("chips-sender")
    ChipsSender getChipsSender(
            ChipsKafkaClient chipsKafkaClient,
            ChipsRestClient chipsRestClient,
            @Value("${FEATURE_FLAG_USE_KAFKA_FOR_CHIPS_CALL_170121}") boolean isChipsKafkaFeatureFlagOn,
            ApiLogger logger) {
        logger.info("CHS ENV CONFIG - FEATURE_FLAG_USE_KAFKA_FOR_CHIPS_CALL_170121 = "
                + isChipsKafkaFeatureFlagOn);

        return (isChipsKafkaFeatureFlagOn) ? chipsKafkaClient : chipsRestClient;
    }
}
