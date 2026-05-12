package com.ftgo.openapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for customizing OpenAPI documentation.
 *
 * <pre>
 * ftgo.openapi.title=Order Service
 * ftgo.openapi.description=Manages the order lifecycle
 * ftgo.openapi.version=v1
 * ftgo.openapi.contact-name=FTGO Team
 * ftgo.openapi.contact-email=team@ftgo.example.com
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo.openapi")
public class OpenApiProperties {

    private String title = "FTGO Service";
    private String description = "FTGO Platform API";
    private String version = "v1";
    private String contactName = "FTGO Team";
    private String contactEmail = "";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
}
