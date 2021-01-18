package uk.gov.companieshouse.api.strikeoffobjections.config;

import org.apache.avro.Schema;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.api.strikeoffobjections.email.KafkaRestClient;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.kafka.producer.ProducerConfigHelper;

@Configuration
public class KafkaConfiguration {

    @Value("${SCHEMA_REGISTRY_URL}")
    private String schemaRegistryUrl;

    @Value("${EMAIL_SCHEMA_URI}")
    private String emailSchemaUri;

    @Value("${KAFKA_PRODUCER_MAXIMUM_RETRY_ATTEMPTS}")
    private String maximumRetryAttempts;

    @Bean
    public Schema fetchSchema(KafkaRestClient restClient) throws JSONException {
       return getSchema(restClient, emailSchemaUri);
    }

    private Schema getSchema(KafkaRestClient restClient, String schemaUri) throws JSONException {
        byte[] bytes = restClient.getSchema(schemaRegistryUrl, schemaUri);
        String schemaJson = new JSONObject(new String(bytes)).getString("schema");
        return new Schema.Parser().parse(schemaJson);
    }

    @Bean
    public CHKafkaProducer buildKafkaProducer() {
        ProducerConfig config = new ProducerConfig();
        ProducerConfigHelper.assignBrokerAddresses(config);

        config.setRoundRobinPartitioner(true);
        config.setAcks(Acks.WAIT_FOR_ALL);
        config.setRetries(Integer.parseInt(maximumRetryAttempts));
        return new CHKafkaProducer(config);
    }
}
