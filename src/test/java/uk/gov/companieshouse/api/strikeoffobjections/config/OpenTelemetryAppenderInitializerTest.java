package uk.gov.companieshouse.api.strikeoffobjections.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@Unit
class OpenTelemetryAppenderInitializerTest {

    @Test
    void shouldInstallOpenTelemetryAppender() {
        OpenTelemetry openTelemetry = mock(OpenTelemetry.class);
        OpenTelemetryAppenderInitializer initializer =
                new OpenTelemetryAppenderInitializer(openTelemetry);

        try (MockedStatic<OpenTelemetryAppender> appender = mockStatic(OpenTelemetryAppender.class)) {
            initializer.afterPropertiesSet();

            appender.verify(() -> OpenTelemetryAppender.install(openTelemetry));
        }
    }
}
