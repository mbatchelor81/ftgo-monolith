package net.chrisrichardson.ftgo.openapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Per-service OpenAPI metadata, bound from the {@code ftgo.openapi.*} keys in
 * {@code application.yml}.
 *
 * <p>Every FTGO microservice must populate at least {@link #title} and
 * {@link #description}. The other fields (version, contact, licence) fall back
 * to FTGO-wide defaults so a new service gets a reasonable Swagger UI out of
 * the box.
 *
 * <p>Example:
 *
 * <pre>
 * ftgo:
 *   openapi:
 *     title: "FTGO Order Service API"
 *     description: "REST API for placing, revising, and tracking food orders."
 *     version: "1.0.0"
 *     contact:
 *       name: "FTGO Platform Team"
 *       email: "platform@ftgo.example.com"
 *       url: "https://github.com/mbatchelor81/ftgo-monolith"
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo.openapi")
public class OpenApiProperties {

  /** Human-readable title shown at the top of Swagger UI. */
  private String title = "FTGO Service API";

  /** Short paragraph describing what this service does. */
  private String description = "REST API exposed by an FTGO microservice.";

  /** API contract version (independent of the service build version). */
  private String version = "1.0.0";

  /** Optional terms-of-service URL. */
  private String termsOfService;

  /** Contact block rendered in Swagger UI. */
  private Contact contact = new Contact();

  /** Licence block rendered in Swagger UI. */
  private License license = new License();

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

  public String getTermsOfService() {
    return termsOfService;
  }

  public void setTermsOfService(String termsOfService) {
    this.termsOfService = termsOfService;
  }

  public Contact getContact() {
    return contact;
  }

  public void setContact(Contact contact) {
    this.contact = contact;
  }

  public License getLicense() {
    return license;
  }

  public void setLicense(License license) {
    this.license = license;
  }

  /** Contact metadata for the team that owns the service. */
  public static class Contact {
    private String name = "FTGO Platform Team";
    private String email = "platform@ftgo.example.com";
    private String url = "https://github.com/mbatchelor81/ftgo-monolith";

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }
  }

  /** Licence metadata rendered under the API title in Swagger UI. */
  public static class License {
    private String name = "Apache 2.0";
    private String url = "https://www.apache.org/licenses/LICENSE-2.0";

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }
  }
}
