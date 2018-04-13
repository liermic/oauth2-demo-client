package de.lierath.oauth2.client.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	private static Map<String, OauthFlowType> cache = new HashMap<>();

	static {
		for (OauthFlowType t : values()) {
			cache.put(t.getId(), t);
		}
	}

	public static List<OauthFlowType> getAll() {
		return Arrays.asList(values());
	}

	public static OauthFlowType forId(String id) {
		return cache.get(id);
	}

}
