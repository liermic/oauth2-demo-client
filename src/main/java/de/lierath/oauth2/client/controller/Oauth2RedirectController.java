package de.lierath.oauth2.client.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationErrorResponse;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.oauth2.sdk.AuthorizationSuccessResponse;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.ServletUtils;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;

import de.lierath.oauth2.client.model.OauthFlowData;
import de.lierath.oauth2.client.model.OauthFlowResultData;
import de.lierath.oauth2.client.model.OauthServerConfiguration;
import de.lierath.oauth2.client.model.OauthTrustedClientConfiguration;
import de.lierath.oauth2.client.util.OauthDisplayUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This controller is called when a resource owner authorizes this client.
 * Depending on the current OAuth-Flow (iImplicit or authorization code), the
 * token is read from the URL fragment or the query.
 *
 * @author Michael Lierath
 *
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class Oauth2RedirectController {

	private static final String PAGE_RESULT_IMPLICIT_FLOW = "implicitFlow";

	private static final String PAGE_RESULT_AUTHCODE_FLOW = "authCodeFlow";

	private static final String PAGE_DECODE_FRAGMENT = "decodeFragment";

	@NonNull
	OauthTrustedClientConfiguration clientConf;

	@NonNull
	OauthServerConfiguration serverConf;

	/**
	 * This serves as endpoint for the redirect from authorization server to this
	 * client. It is used by both the implicit grant flow and the authorization
	 * grant flow. The distinction between these flows is done inside the method.
	 *
	 * @param httpServletRequest
	 *            the incoming request with token or error
	 * @param model
	 *            the generic UI model that will be used to prepare the result page
	 * @return either a redirect to the fragment decoding page or to the result page
	 *         for authorization code flow.
	 */
	@GetMapping("/api/oauth/redirect")
	public String receiveAuthorizeResponse(HttpServletRequest httpServletRequest, Model model) {
		String queryString = httpServletRequest.getQueryString();
		if (queryString == null || queryString.isEmpty()) {
			// IMPLICIT GRANT with token in fragment, decode and continue with implicitGrant
			return PAGE_DECODE_FRAGMENT;
		} else {
			// AUTHORIZATION CODE GRANT with code in query
			receiveAuthCodeRedirect(httpServletRequest, model);
			return PAGE_RESULT_AUTHCODE_FLOW;
		}
	}

	/**
	 * This is called after decoding the token from URL fragment. The decoding page
	 * will have transferred the fragment into the query.
	 *
	 * @param httpServletRequest
	 *            the request now contains the token response as query
	 * @param model
	 *            the generic UI model that will hold the data for the result page
	 * @return the result page for implicit flow
	 */
	@GetMapping("/implicitGrant")
	public String implicitGrant(HttpServletRequest httpServletRequest, Model model) {
		try {
			HTTPRequest httpRequest = ServletUtils.createHTTPRequest(httpServletRequest);
			log.debug(httpRequest.getURL().toString());
			log.debug("Fragment:" + httpRequest.getQuery());
			AuthorizationResponse tokenResponse = AuthorizationResponse.parse(httpRequest);
			State state = tokenResponse.getState();
			OauthSession session = OauthSession.get(state.getValue());
			session.getResult().addAuthorizeTokenResponse(tokenResponse, session.getInputData().getJwkUrl());
			model.addAttribute("result", session.getResult());
		} catch (IOException | ParseException e) {
			log.error("Unable to parse incoming http request.", e);
		}
		return PAGE_RESULT_IMPLICIT_FLOW;
	}

	private void receiveAuthCodeRedirect(HttpServletRequest httpServletRequest, Model model) {
		HTTPRequest httpRequest;
		try {
			httpRequest = ServletUtils.createHTTPRequest(httpServletRequest);
			log.info(httpRequest.getURL().toString());
			log.info("Query:" + httpRequest.getQuery());
			AuthorizationResponse codeResponse = AuthorizationResponse.parse(httpRequest);
			OauthSession session = OauthSession.get(codeResponse.getState().getValue());
			OauthFlowResultData result = session.getResult();
			model.addAttribute("result", result);
			if (codeResponse.indicatesSuccess()) {
				// set response in result
				AuthorizationSuccessResponse successResponse = codeResponse.toSuccessResponse();
				result.setAuthorizeResponse(successResponse.toURI().toString());
				// set code in result
				AuthorizationCode code = successResponse.getAuthorizationCode();
				result.setAuthorizationCode(code.getValue());
				getTokenForCode(session.getInputData(), code, session.getResult());
			} else {
				AuthorizationErrorResponse errorResponse = codeResponse.toErrorResponse();
				result.setAuthorizeResponse(errorResponse.toURI().toString());
			}
		} catch (IOException | ParseException e) {
			log.error("Unable to parse incoming authorization code request.", e);
		} catch (URISyntaxException e) {
			log.error("Invalid URI of token endpoint.", e);
		}
	}

	private void getTokenForCode(OauthFlowData inputData, AuthorizationCode code, OauthFlowResultData result)
			throws URISyntaxException, ParseException, IOException {
		URI tokenURI = new URI(inputData.getTokenUrl());
		URI redirectURI = inputData.getRedirectUri() == null ? null : new URI(inputData.getRedirectUri());
		ClientAuthentication auth = new ClientSecretBasic(new ClientID(inputData.getKey()),
				new Secret(inputData.getSecret()));
		AuthorizationCodeGrant codeGrant = new AuthorizationCodeGrant(code, redirectURI, inputData.getPkceVerifier());

		// Make the token request
		TokenRequest request = new TokenRequest(tokenURI, auth, codeGrant);
		HTTPRequest httpRequest = request.toHTTPRequest();
		result.setTokenRequest(OauthDisplayUtil.prettyPrint(httpRequest));
		TokenResponse response = TokenResponse.parse(httpRequest.send());

		// add token to result
		result.addAccessTokenResponse(response, inputData.getJwkUrl());
	}
}
