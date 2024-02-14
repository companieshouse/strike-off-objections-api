package uk.gov.companieshouse.api.strikeoffobjections.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

@Configuration
public class ResponseEntityConfig {

    @Bean
    public PluggableResponseEntityFactory createResponseFactory() {
        return PluggableResponseEntityFactory.builder()
                .addStandardFactories()
                .add(new ObjectionsResponseFactory())
                .build();
    }
}
