package de.lierath.oauth2.client.model;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OauthFlowType {

	AUTH_CODE("authCodeFlow", "Authorization Code Grant"),

	IMPLICIT("implicitFlow", "Implicit Grant"),

	PASSWORD("passwordFlow", "Resource Owner Password Grant"),

	CLIENT("clientCredentialFlow", "Client Credentials Grant");

	@Getter
	private final String id;

	@Getter
	private final String name;

	public static List<OauthFlowType> getAll() {
		return Arrays.asList(values());
	}

}
