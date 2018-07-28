package de.lierath.oauth2.client.execution;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
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
public class ClientCredentialsFlowExecution implements OauthFlowExecution {

	private OauthFlowResultData result;

	public ClientCredentialsFlowExecution() {
		this.result = new OauthFlowResultData();
		this.result.setOauthFlowType(OauthFlowType.CLIENT.getId());
	}

	@Override
	public OauthFlowResultData executeInitialRequest(OauthFlowData inputData, OauthSession session) {
		this.result.setExpectedSignatureAlgorithm(inputData.getExpectedSignatureAlgorithm());

		HTTPResponse httpResponse;
		TokenResponse response;
		// request token
		try {
			httpResponse = requestToken(new URI(inputData.getTokenUrl()), inputData.getKey(), inputData.getSecret(),
					Scope.parse(inputData.getScope()));
		} catch (IOException e) {
			log.error("Unable to parse token response", e);
			throw new OauthExecutionException("Unable to parse token response", e);
		} catch (URISyntaxException e) {
			log.error("Invalid URI as Token URI!", e);
			throw new OauthExecutionException("Invalid URI as Token URI!", e);
		}
		// parse response
		try {
			response = TokenResponse.parse(httpResponse);
			this.result.addAccessTokenResponse(response, inputData.getJwkUrl());
		} catch (ParseException e) {
			OauthDisplayUtil.prettyPrint(httpResponse);
		}

		// set session variables
		session.setResult(this.result);
		session.setNextPage(this.result.getOauthFlowType());
		return this.result;
	}

	private HTTPResponse requestToken(URI uri, String clientID, String clientSecret, Scope scope) throws IOException {
		// prepare client authentication (Http Basic)
		ClientAuthentication auth = new ClientSecretBasic(new ClientID(clientID), new Secret(clientSecret));
		// create request object and transform to HttpRequest
		TokenRequest request = scope == null ? new TokenRequest(uri, auth, new ClientCredentialsGrant())
				: new TokenRequest(uri, auth, new ClientCredentialsGrant(), scope);
		HTTPRequest httpRequest = request.toHTTPRequest();
		// save request for display purposes
		this.result.setTokenRequest(OauthDisplayUtil.prettyPrint(httpRequest));
		// send request and return response
		return httpRequest.send();
	}

}
