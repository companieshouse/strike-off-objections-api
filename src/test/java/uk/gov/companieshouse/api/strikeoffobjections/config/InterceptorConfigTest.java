package uk.gov.companieshouse.api.strikeoffobjections.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.interceptor.CompanyNumberInterceptor;
import uk.gov.companieshouse.api.strikeoffobjections.interceptor.ObjectionInterceptor;
import uk.gov.companieshouse.api.strikeoffobjections.interceptor.ObjectionStatusInterceptor;
import uk.gov.companieshouse.api.strikeoffobjections.interceptor.authorization.AttachmentDownloadAuthorizationInterceptor;
import uk.gov.companieshouse.api.strikeoffobjections.interceptor.authorization.UserAuthorizationInterceptor;
import uk.gov.companieshouse.api.strikeoffobjections.service.IObjectionService;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ERICHeaderParser;

@Unit
@ExtendWith(MockitoExtension.class)
class InterceptorConfigTest {

    @Mock private ApiLogger apiLogger;

    @Mock private IObjectionService objectionService;

    @Mock private ERICHeaderParser ericHeaderParser;

    @InjectMocks private InterceptorConfig interceptorConfig;

    @Test
    void testAttachmentDownloadAuthInterceptorCreation() {
        AttachmentDownloadAuthorizationInterceptor interceptor =
                interceptorConfig.attachmentDownloadAuthorizationInterceptor(apiLogger);

        assertNotNull(interceptor);
    }

    @Test
    void testUserAuthorizationInterceptorCreation() {
        UserAuthorizationInterceptor interceptor =
                interceptorConfig.userAuthorizationInterceptor(apiLogger, ericHeaderParser);

        assertNotNull(interceptor);
    }

    @Test
    void testCompanyNumberInterceptorCreation() {
        CompanyNumberInterceptor interceptor = interceptorConfig.companyNumberInterceptor(apiLogger);

        assertNotNull(interceptor);
    }

    @Test
    void testObjectionInterceptorCreation() {
        ObjectionInterceptor interceptor =
                interceptorConfig.objectionInterceptor(objectionService, apiLogger);

        assertNotNull(interceptor);
    }

    @Test
    void testObjectionStatusInterceptorCreation() {
        ObjectionStatusInterceptor interceptor =
                interceptorConfig.objectionStatusInterceptor(apiLogger);

        assertNotNull(interceptor);
    }

    @Test
    void testAddInterceptors() {
        InterceptorRegistry interceptorRegistry = new InterceptorRegistry();
        InterceptorRegistry spyRegistry = spy(interceptorRegistry);

        interceptorConfig.addInterceptors(spyRegistry);

        InOrder interceptorOrder = inOrder(spyRegistry);
        interceptorOrder
                .verify(spyRegistry)
                .addInterceptor(any(AttachmentDownloadAuthorizationInterceptor.class));
        interceptorOrder.verify(spyRegistry).addInterceptor(any(ObjectionInterceptor.class));
        interceptorOrder.verify(spyRegistry).addInterceptor(any(ObjectionStatusInterceptor.class));
        interceptorOrder.verify(spyRegistry).addInterceptor(any(CompanyNumberInterceptor.class));
        interceptorOrder.verify(spyRegistry).addInterceptor(any(UserAuthorizationInterceptor.class));
    }
}
