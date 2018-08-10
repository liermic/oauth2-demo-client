package de.lierath.oauth2.client.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.lierath.oauth2.client.execution.AuthorizationCodeFlowExecution;
import de.lierath.oauth2.client.execution.ClientCredentialsFlowExecution;
import de.lierath.oauth2.client.execution.ImplicitGrantFlowExecution;
import de.lierath.oauth2.client.execution.OauthFlowExecution;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OauthFlowType {

	AUTH_CODE("authCodeFlow", "Authorization Code Grant",
			"Authorization request will return code to redirect URL; code is used to request access token. Requires trusted client (with secret).") {
		@Override
		public OauthFlowExecution getExecution() {
			return new AuthorizationCodeFlowExecution();
		}
	},

	IMPLICIT("implicitFlow", "Implicit Grant",
			"Authorization request will return access token to redirect URL. Denied to trusted clients.") {
		@Override
		public OauthFlowExecution getExecution() {
			return new ImplicitGrantFlowExecution();
		}
	},

	CLIENT("clientCredentialsFlow", "Client Credentials Grant",
			"No authorization by resource owner, server will return token in response body. Requires trusted client (with secret).") {
		@Override
		public OauthFlowExecution getExecution() {
			return new ClientCredentialsFlowExecution();
		}
	};

	@Getter
	private final String id;

	@Getter
	private final String name;

	@Getter
	private final String description;

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

	public abstract OauthFlowExecution getExecution();

}
