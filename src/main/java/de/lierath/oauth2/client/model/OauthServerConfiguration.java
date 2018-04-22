package de.lierath.oauth2.client.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "oauth.server")
public class OauthServerConfiguration {

	private String authorizeUrl;

	private String tokenUrl;

	private String jwkUrl;

	private String tenantId;

	private String organizationId;

	public String getAuthorizeUrl() {
		return replacePlaceholders(this.authorizeUrl);
	}

	public String getTokenUrl() {
		return replacePlaceholders(this.tokenUrl);
	}

	public String getJwkUrl() {
		return replacePlaceholders(this.jwkUrl);
	}

	private String replacePlaceholders(String url) {
		return url.replace("tenantId", this.tenantId).replace("organizationId", this.organizationId);
	}
}
