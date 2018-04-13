package de.lierath.oauth2.client.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class OauthFlow implements Serializable {
	private static final long serialVersionUID = 1L;

	public static OauthFlow forConf(OauthServerConfiguration serverConf, OauthClientConfiguration clientConf) {
		OauthFlow flow = new OauthFlow();
		flow.authorizeUrl = serverConf.getAuthorizeUrl();
		flow.tokenUrl = serverConf.getTokenUrl();
		flow.key = clientConf.getKey();
		flow.secret = clientConf.getSecret();
		flow.redirectUri = clientConf.getRedirectUri();
		flow.state = clientConf.getState();
		return flow;
	}

	private String type = OauthFlowType.AUTH_CODE.getId();

	private String authorizeUrl;

	private String tokenUrl;

	private String key;

	private String secret;

	private String redirectUri;

	private String state;

}
