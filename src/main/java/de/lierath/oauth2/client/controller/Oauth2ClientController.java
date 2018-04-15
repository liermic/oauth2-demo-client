package de.lierath.oauth2.client.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

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

import de.lierath.oauth2.client.execution.OauthFlowExecution;
import de.lierath.oauth2.client.model.OauthClientConfiguration;
import de.lierath.oauth2.client.model.OauthFlowData;
import de.lierath.oauth2.client.model.OauthFlowResultData;
import de.lierath.oauth2.client.model.OauthFlowType;
import de.lierath.oauth2.client.model.OauthServerConfiguration;
import de.lierath.oauth2.client.util.OauthDisplayUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class Oauth2ClientController {

	@NonNull
	OauthClientConfiguration clientConf;

	@NonNull
	OauthServerConfiguration serverConf;

	@GetMapping("/")
	public String home(HttpServletRequest request, Model model) {
		OauthFlowData oauthFlow = OauthFlowData.forConf(this.serverConf, this.clientConf);
		model.addAttribute("oauthFlow", oauthFlow);
		model.addAttribute("oauthFlowTypes", OauthFlowType.getAll());
		return "home";
	}

	@PostMapping("/startFlow")
	public String startFlow(HttpServletRequest httpRequest, @ModelAttribute OauthFlowData oauthFlow,
			BindingResult bindingResult, Model model) throws URISyntaxException, ParseException, IOException {
		log.debug(oauthFlow.getType());
		bindingResult.getModel();
		// start session for result handling
		OauthSession session = OauthSession.open();
		// execute flow
		OauthFlowType flowType = OauthFlowType.forId(oauthFlow.getType());
		OauthFlowExecution execution = flowType.getExecution();
		OauthFlowResultData result = execution.execute(oauthFlow, session);
		model.addAttribute("result", result);
		// redirect to result page (i.e. "clientCredentials")
		return session.getNextPage(); // return "redirect:/home";
	}

	@GetMapping("/api/decodeToken")
	public String receiveAuthorizeResponse(HttpServletRequest httpServletRequest, Model model) {
		return "decodeFragment";
	}

	@GetMapping("/implicitGrant")
	public String implicitGrant(HttpServletRequest httpServletRequest, Model model) {
		try {
			HTTPRequest httpRequest = ServletUtils.createHTTPRequest(httpServletRequest);
			log.debug(httpRequest.getURL().toString());
			log.debug("Fragment:" + httpRequest.getQuery());
			AuthorizationResponse tokenResponse = AuthorizationResponse.parse(httpRequest);
			State state = tokenResponse.getState();
			OauthSession session = OauthSession.get(state.getValue());
			session.getResult().addAuthorizeTokenResponse(tokenResponse);
			model.addAttribute("result", session.getResult());
		} catch (IOException | ParseException e) {
			log.error("Unable to parse incoming http request.", e);
		}
		return "implicitFlow";
	}

	@GetMapping("/api/authcode")
	public String authorizationCode(HttpServletRequest httpServletRequest, Model model) {
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
		return OauthFlowType.AUTH_CODE.getId();
	}

	private void getTokenForCode(OauthFlowData inputData, AuthorizationCode code, OauthFlowResultData result)
			throws URISyntaxException, ParseException, IOException {
		URI tokenURI = new URI(inputData.getTokenUrl());
		URI redirectURI = new URI(inputData.getRedirectUri());
		ClientAuthentication auth = new ClientSecretBasic(new ClientID(inputData.getKey()),
				new Secret(inputData.getSecret()));
		AuthorizationCodeGrant codeGrant = new AuthorizationCodeGrant(code, redirectURI);

		// Make the token request
		TokenRequest request = new TokenRequest(tokenURI, auth, codeGrant);
		HTTPRequest httpRequest = request.toHTTPRequest();
		result.setTokenRequest(OauthDisplayUtil.prettyPrint(httpRequest));
		TokenResponse response = TokenResponse.parse(httpRequest.send());

		// add token to result
		result.addAccessTokenResponse(response);
	}

}
