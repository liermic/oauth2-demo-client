package de.lierath.oauth2.client.model;

import java.io.Serializable;

import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;

import lombok.Data;

@Data
public class OauthFlowData implements Serializable {
	private static final long serialVersionUID = 1L;

	public static OauthFlowData forConf(OauthServerConfiguration serverConf,
			OauthTrustedClientConfiguration clientConf) {
		OauthFlowData flow = new OauthFlowData();
		flow.authorizeUrl = serverConf.getAuthorizeUrl();
		flow.tokenUrl = serverConf.getTokenUrl();
		flow.jwkUrl = serverConf.getJwkUrl();
		flow.expectedSignatureAlgorithm = serverConf.getExpectedSignatureAlgorithm();
		flow.key = clientConf.getKey();
		flow.secret = clientConf.getSecret();
		flow.redirectUri = clientConf.getRedirectUri();
		flow.scope = clientConf.getScope();
		flow.state = clientConf.getState();
		return flow;
	}

	private String type = OauthFlowType.AUTH_CODE.getId();

	private String authorizeUrl;

	private String tokenUrl;

	private String jwkUrl;

	private String expectedSignatureAlgorithm;

	private String key;

	private String secret;

	private String redirectUri;

	private String scope;

	private String state;

	private String username;

	private String userpassword;

	private CodeVerifier pkceVerifier;

}
