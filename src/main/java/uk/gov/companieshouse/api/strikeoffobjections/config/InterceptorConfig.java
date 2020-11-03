package uk.gov.companieshouse.api.strikeoffobjections.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import uk.gov.companieshouse.api.strikeoffobjections.interceptor.CompanyNumberInterceptor;
import uk.gov.companieshouse.api.strikeoffobjections.interceptor.ObjectionInterceptor;
import uk.gov.companieshouse.api.strikeoffobjections.interceptor.ObjectionStatusInterceptor;
import uk.gov.companieshouse.api.strikeoffobjections.interceptor.authorization.AttachmentDownloadAuthorizationInterceptor;
import uk.gov.companieshouse.api.strikeoffobjections.interceptor.authorization.UserAuthorizationInterceptor;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.service.IObjectionService;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ERICHeaderParser;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private static final String ATTACHMENTS_DOWNLOAD_PATH = "/**/attachments/**/download";
    private static final String STRIKE_OFF_OBJECTIONS_OBJECTION_ID = "/**/strike-off-objections/?**/**";
    private static final String ELIGIBILITY_CHECK_PATH = "/**/strike-off-objections/eligibility";

    @Autowired
    private ApiLogger logger;

    @Autowired
    private IObjectionService objectionService;

    @Autowired
    private ERICHeaderParser ericHeaderParser;

    @Bean
    public AttachmentDownloadAuthorizationInterceptor attachmentDownloadAuthorizationInterceptor(ApiLogger logger) {
        return new AttachmentDownloadAuthorizationInterceptor(logger);
    }

    @Bean
    public UserAuthorizationInterceptor userAuthorizationInterceptor(
            ApiLogger logger,
            ERICHeaderParser ericHeaderParser) {
        return new UserAuthorizationInterceptor(logger, ericHeaderParser);
    }

    @Bean
    public CompanyNumberInterceptor companyNumberInterceptor(ApiLogger apiLogger) {
        return new CompanyNumberInterceptor(apiLogger);
    }

    @Bean
    public ObjectionInterceptor objectionInterceptor(IObjectionService objectionService, ApiLogger apiLogger) {
        return new ObjectionInterceptor(objectionService, apiLogger);
    }

    @Bean
    public ObjectionStatusInterceptor objectionStatusInterceptor(ApiLogger apiLogger) {
        return new ObjectionStatusInterceptor(apiLogger);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(attachmentDownloadAuthorizationInterceptor(logger))
                .addPathPatterns(ATTACHMENTS_DOWNLOAD_PATH)
                .order(0);

        registry.addInterceptor(objectionInterceptor(objectionService, logger))
                .addPathPatterns(STRIKE_OFF_OBJECTIONS_OBJECTION_ID)
                .excludePathPatterns(ELIGIBILITY_CHECK_PATH)
                .order(1);

        registry.addInterceptor(objectionStatusInterceptor(logger))
                .addPathPatterns(STRIKE_OFF_OBJECTIONS_OBJECTION_ID)
                .excludePathPatterns(ELIGIBILITY_CHECK_PATH, ATTACHMENTS_DOWNLOAD_PATH)
                .order(2);

        registry.addInterceptor(companyNumberInterceptor(logger))
                .addPathPatterns(STRIKE_OFF_OBJECTIONS_OBJECTION_ID)
                .excludePathPatterns(ELIGIBILITY_CHECK_PATH)
                .order(3);

        registry.addInterceptor(userAuthorizationInterceptor(logger, ericHeaderParser))
                .addPathPatterns(STRIKE_OFF_OBJECTIONS_OBJECTION_ID)
                .excludePathPatterns(ATTACHMENTS_DOWNLOAD_PATH, ELIGIBILITY_CHECK_PATH)
                .order(4);
    }
}
