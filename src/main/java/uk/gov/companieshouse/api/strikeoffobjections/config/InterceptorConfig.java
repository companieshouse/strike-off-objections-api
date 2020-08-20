package uk.gov.companieshouse.api.strikeoffobjections.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import uk.gov.companieshouse.api.strikeoffobjections.authorization.AttachmentDownloadAuthorizationInterceptor;
import uk.gov.companieshouse.api.strikeoffobjections.authorization.UserAuthorizationInterceptor;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.service.IObjectionService;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ERICHeaderParser;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

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
            IObjectionService objectionService,
            ERICHeaderParser ericHeaderParser
    ) {
        return new UserAuthorizationInterceptor(logger, objectionService, ericHeaderParser);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(attachmentDownloadAuthorizationInterceptor(logger))
            .addPathPatterns("/**/attachments/**/download");
        registry.addInterceptor(userAuthorizationInterceptor(logger, objectionService, ericHeaderParser))
                .addPathPatterns("/**/strike-off-objections/?**");
    }
}
