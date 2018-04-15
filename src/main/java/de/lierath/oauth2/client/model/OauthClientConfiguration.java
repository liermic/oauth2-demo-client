package de.lierath.oauth2.client.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "oauth.client")
public class OauthClientConfiguration {

	private String key;

	private String secret;

	private String redirectUri;

	private String scope;

	private String state;

}
