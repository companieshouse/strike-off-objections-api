package uk.gov.companieshouse.api.strikeoffobjections.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Unit
class KafkaRestClientUnitTest {

    private String schemaRegistryUrl = "http://testSchema:1000";
    private String emailSchemaUri = "/subjects/test-email-send";
    private ResponseEntity response;
    private String schemaUrl;

    private KafkaRestClient restClient;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = Mockito.mock(RestTemplate.class);
        restClient = new KafkaRestClient(restTemplate);
        schemaUrl = String.format("%s%s", schemaRegistryUrl, emailSchemaUri);
        String body = "abc";
        response = new ResponseEntity<>(body.getBytes(), HttpStatus.OK);
        when(restTemplate.exchange(eq(schemaUrl), eq(HttpMethod.GET), any(), eq(byte[].class))).thenReturn(response);
    }

    @Test
    void testExchangeHasBeenCalled() {
        byte[] schema = restClient.getSchema(schemaRegistryUrl, emailSchemaUri);
        verify(restTemplate, times(1)).exchange(eq(schemaRegistryUrl + emailSchemaUri), eq(HttpMethod.GET), any(),
                eq(byte[].class));
        assertEquals(response.getBody(), schema);
    }
}
