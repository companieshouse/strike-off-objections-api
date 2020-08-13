package uk.gov.companieshouse.api.strikeoffobjections.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import uk.gov.companieshouse.api.strikeoffobjections.authorization.AttachmentDownloadAuthorizationInterceptor;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private ApiLogger logger;

    @Bean
    public AttachmentDownloadAuthorizationInterceptor attachmentDownloadAuthorizationInterceptor(ApiLogger logger) {
        return new AttachmentDownloadAuthorizationInterceptor(logger);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(attachmentDownloadAuthorizationInterceptor(logger))
            .addPathPatterns("/**/attachments/**/download");
    }
}
