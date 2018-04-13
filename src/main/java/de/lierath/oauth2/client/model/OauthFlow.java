package de.lierath.oauth2.client.model;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;

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

	public TokenRequest toTokenRequest() throws URISyntaxException {
		OauthFlowType flowType = OauthFlowType.forId(this.type);
		switch (flowType) {
		case CLIENT:
			URI uri = new URI(this.tokenUrl);
			ClientAuthentication auth = new ClientSecretPost(new ClientID(this.key), new Secret(this.secret));
			return new TokenRequest(uri, auth, new ClientCredentialsGrant());
		default:
			break;
		}
		return null;
	}

}
