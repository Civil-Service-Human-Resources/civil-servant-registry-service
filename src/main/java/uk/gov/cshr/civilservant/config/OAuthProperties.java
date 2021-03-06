package uk.gov.cshr.civilservant.config;

import java.io.Serializable;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties implements Serializable {

  private String serviceUrl;

  private String clientId;

  private String clientSecret;

  private String tokenUrl;

  private int maxTotalConnections;

  private int defaultMaxConnectionsPerRoute;

  private int maxPerServiceUrl;

  private String jwtKey;

  public String getServiceUrl() {
    return serviceUrl;
  }

  public void setServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public String getTokenUrl() {
    return tokenUrl;
  }

  public void setTokenUrl(String tokenUrl) {
    this.tokenUrl = tokenUrl;
  }

  public int getMaxTotalConnections() {
    return maxTotalConnections;
  }

  public void setMaxTotalConnections(int maxTotalConnections) {
    this.maxTotalConnections = maxTotalConnections;
  }

  public int getDefaultMaxConnectionsPerRoute() {
    return defaultMaxConnectionsPerRoute;
  }

  public void setDefaultMaxConnectionsPerRoute(int defaultMaxConnectionsPerRoute) {
    this.defaultMaxConnectionsPerRoute = defaultMaxConnectionsPerRoute;
  }

  public int getMaxPerServiceUrl() {
    return maxPerServiceUrl;
  }

  public void setMaxPerServiceUrl(int maxPerServiceUrl) {
    this.maxPerServiceUrl = maxPerServiceUrl;
  }

  public String getJwtKey() {
    return jwtKey;
  }

  public void setJwtKey(String jwtKey) {
    this.jwtKey = jwtKey;
  }
}
