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

	private String expectedSignatureAlgorithm;

}
