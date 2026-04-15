package com.ftgo.openapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for FTGO OpenAPI documentation.
 *
 * <p>Each microservice can customize its API documentation metadata via {@code application.yml}
 * under the {@code ftgo.openapi} prefix.
 *
 * <pre>{@code
 * ftgo:
 *   openapi:
 *     title: FTGO Consumer Service API
 *     description: Manages consumer registration and validation
 *     version: v1
 *     contact-name: FTGO Platform Team
 *     contact-email: platform@ftgo.com
 * }</pre>
 */
@ConfigurationProperties(prefix = "ftgo.openapi")
public class FtgoOpenApiProperties {

    private String title = "FTGO Service API";
    private String description = "FTGO Microservice REST API";
    private String version = "v1";
    private String contactName = "FTGO Platform Team";
    private String contactEmail = "platform@ftgo.com";
    private String contactUrl = "";
    private String licenseName = "Apache 2.0";
    private String licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0";

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

    public String getContactUrl() {
        return contactUrl;
    }

    public void setContactUrl(String contactUrl) {
        this.contactUrl = contactUrl;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }
}
