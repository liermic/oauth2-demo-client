package de.lierath.oauth2.client.execution;

import java.net.URI;
import java.net.URISyntaxException;

import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.ResponseMode;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPRequest.Method;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;

import de.lierath.oauth2.client.controller.OauthSession;
import de.lierath.oauth2.client.model.OauthFlowData;
import de.lierath.oauth2.client.model.OauthFlowResultData;
import de.lierath.oauth2.client.model.OauthFlowType;
import de.lierath.oauth2.client.util.OauthDisplayUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImplicitGrantFlowExecution implements OauthFlowExecution {

	@Override
	public OauthFlowResultData execute(OauthFlowData inputData, OauthSession session) {
		URI uri;
		try {
			uri = new URI(inputData.getAuthorizeUrl());
		} catch (URISyntaxException e) {
			log.error("Invalid URI as Authorize URI!", e);
			throw new OauthExecutionException("Invalid URI as Authorize URI!", e);
		}
		URI redirectURI;
		try {
			redirectURI = new URI(inputData.getRedirectUri());
		} catch (URISyntaxException e) {
			log.error("Invalid URI as Redirect URI!", e);
			throw new OauthExecutionException("Invalid URI as Redirect URI!", e);
		}

		ClientID clientId = new ClientID(inputData.getKey());
		ResponseType rt = new ResponseType("token");
		Scope scope = Scope.parse(inputData.getScope());
		State state = new State(session.getId().toString());
		AuthorizationRequest request = new AuthorizationRequest(uri, rt, ResponseMode.FRAGMENT, clientId, redirectURI,
				scope, state);

		OauthFlowResultData result = new OauthFlowResultData();
		result.setOauthFlowType(OauthFlowType.IMPLICIT.getId());
		session.setResult(result);
		HTTPRequest req = request.toHTTPRequest(Method.GET);
		result.setAuthorizeRequest(OauthDisplayUtil.prettyPrint(req));
		String rd = req.getURL() + "?" + req.getQuery();
		session.setNextPage("redirect:" + rd);

		return result;
	}

}
