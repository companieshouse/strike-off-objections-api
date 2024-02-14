package uk.gov.companieshouse.api.strikeoffobjections.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.chips.ChipsKafkaClient;
import uk.gov.companieshouse.api.strikeoffobjections.chips.ChipsRestClient;
import uk.gov.companieshouse.api.strikeoffobjections.chips.ChipsSender;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;

@Unit
@ExtendWith(MockitoExtension.class)
class ApplicationConfigTest {

    private static final String CHIPS_KAFKA_CONFIG_MESSAGE =
            "CHS ENV CONFIG - FEATURE_FLAG_USE_KAFKA_FOR_CHIPS_CALL_170121 = %s";

    @Mock
    private ChipsRestClient chipsRestClient;

    @Mock
    private ChipsKafkaClient chipsKafkaClient;

    @Mock
    private ApiLogger apiLogger;

    private ApplicationConfig applicationConfig;

    @BeforeEach
    void setup() {
        applicationConfig = new ApplicationConfig();
    }

    @Test
    void testGetChipsSenderReturnsChipsKafkaClient() {
        ChipsSender chipsSender =
                applicationConfig.getChipsSender(chipsKafkaClient, chipsRestClient, true, apiLogger);

        assertInstanceOf(ChipsKafkaClient.class, chipsSender);
        verify(apiLogger, times(1)).info(String.format(CHIPS_KAFKA_CONFIG_MESSAGE, true));
    }

    @Test
    void testGetChipsSenderReturnsChipsRestClient() {
        ChipsSender chipsSender =
                applicationConfig.getChipsSender(chipsKafkaClient, chipsRestClient, false, apiLogger);

        assertInstanceOf(ChipsRestClient.class, chipsSender);
        verify(apiLogger, times(1)).info(String.format(CHIPS_KAFKA_CONFIG_MESSAGE, false));
    }
}
