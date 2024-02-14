package uk.gov.companieshouse.api.strikeoffobjections;

import java.util.TimeZone;
import javax.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static final String APP_NAMESPACE = "strike-off-objections-api";

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @PostConstruct
    public void init() {
        // This is to prevent times being out of time by an hour during British Summer Time
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}
