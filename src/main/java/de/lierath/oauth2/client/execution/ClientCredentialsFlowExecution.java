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

	@Override
	public OauthFlowResultData execute(OauthFlowData inputData, OauthSession session) {
		URI uri;
		try {
			uri = new URI(inputData.getTokenUrl());
		} catch (URISyntaxException e) {
			log.error("Invalid URI as Token URI!", e);
			throw new OauthExecutionException("Invalid URI as Token URI!", e);
		}

		ClientAuthentication auth = new ClientSecretBasic(new ClientID(inputData.getKey()),
				new Secret(inputData.getSecret()));

		Scope scope = Scope.parse(inputData.getScope());
		TokenRequest request = new TokenRequest(uri, auth, new ClientCredentialsGrant(), scope);

		OauthFlowResultData result = new OauthFlowResultData();
		result.setOauthFlowType(OauthFlowType.CLIENT.getId());
		result.setExpectedSignatureAlgorithm(inputData.getExpectedSignatureAlgorithm());
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
