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

	AUTH_CODE("authCodeFlow", "Authorization Code Grant") {
		@Override
		public OauthFlowExecution getExecution() {
			return new AuthorizationCodeFlowExecution();
		}
	},

	IMPLICIT("implicitFlow", "Implicit Grant") {
		@Override
		public OauthFlowExecution getExecution() {
			return new ImplicitGrantFlowExecution();
		}
	},

	CLIENT("clientCredentialsFlow", "Client Credentials Grant") {
		@Override
		public OauthFlowExecution getExecution() {
			return new ClientCredentialsFlowExecution();
		}
	};

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

	public abstract OauthFlowExecution getExecution();

}
