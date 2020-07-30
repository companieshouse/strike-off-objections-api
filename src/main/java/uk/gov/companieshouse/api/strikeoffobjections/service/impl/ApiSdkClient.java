package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

/**
 * The {@code ApiSdkClient} class simply provides an abstraction that can be used when
 * testing {@code ApiSdkManager} static methods, without imposing the use of a test
 * framework that supports mocking of static methods.
 */
@Component
public class ApiSdkClient {

    public ApiClient getApiClient() {
        return ApiSdkManager.getSDK();
    }
}
