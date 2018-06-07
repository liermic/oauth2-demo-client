package de.lierath.oauth2.client.execution;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;

import de.lierath.oauth2.client.controller.OauthSession;
import de.lierath.oauth2.client.model.OauthFlowData;
import de.lierath.oauth2.client.model.OauthFlowResultData;
import de.lierath.oauth2.client.model.OauthFlowType;
import de.lierath.oauth2.client.util.OauthDisplayUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PasswordGrantFlowExecution implements OauthFlowExecution {

	@Override
	public OauthFlowResultData execute(OauthFlowData inputData, OauthSession session) {
		URI uri;
		try {
			uri = new URI(inputData.getTokenUrl());
		} catch (URISyntaxException e) {
			log.error("Invalid URI as Token URI!", e);
			throw new OauthExecutionException("Invalid URI as Token URI!", e);
		}

		AuthorizationGrant passwordGrant = new ResourceOwnerPasswordCredentialsGrant(inputData.getUsername(),
				new Secret(inputData.getUserpassword()));
		Scope scope = Scope.parse(inputData.getScope());

		TokenRequest request;
		if (inputData.getSecret() != null && !inputData.getSecret().isEmpty()) {
			// trusted client
			ClientAuthentication auth = new ClientSecretPost(new ClientID(inputData.getKey()),
					new Secret(inputData.getSecret()));
			request = new TokenRequest(uri, auth, passwordGrant, scope);
		} else {
			// public client
			request = new TokenRequest(uri, new ClientID(inputData.getKey()), passwordGrant, scope);
		}

		OauthFlowResultData result = new OauthFlowResultData();
		result.setOauthFlowType(OauthFlowType.PASSWORD.getId());

		HTTPRequest httpRequest;
		HTTPResponse httpResponse;
		TokenResponse response;
		try {
			httpRequest = request.toHTTPRequest();
			result.setTokenRequest(OauthDisplayUtil.prettyPrint(httpRequest));
			httpResponse = httpRequest.send();
		} catch (IOException e) {
			log.error("Unable to parse token response", e);
			throw new OauthExecutionException("Unable to parse token response", e);
		}
		try {
			response = TokenResponse.parse(httpResponse);
			result.addAccessTokenResponse(response, inputData.getJwkUrl());
		} catch (ParseException e) {
			OauthDisplayUtil.prettyPrint(httpResponse);
		}

		session.setResult(result);
		session.setNextPage(result.getOauthFlowType());
		return result;
	}

}
