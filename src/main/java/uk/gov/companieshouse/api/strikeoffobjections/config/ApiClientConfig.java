package uk.gov.companieshouse.api.strikeoffobjections.config;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;

@Configuration
public class ApiClientConfig {

    @Bean
    public Supplier<InternalApiClient> internalApiClientSupplier(@Value("${chs.api.key}") String apiKey, @Value("${chs.kafka.api.url}") String apiUrl) {
        return () -> {
            var client = new InternalApiClient(new ApiKeyHttpClient(apiKey));
            client.setBasePath(apiUrl);

            return client;
        };
    }
}