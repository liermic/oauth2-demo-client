package de.lierath.oauth2.client.ui;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.View;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.token.AccessToken;

import de.lierath.oauth2.client.model.JwtTokenResponse;
import de.lierath.oauth2.client.model.OauthClientConfiguration;
import de.lierath.oauth2.client.model.OauthFlow;
import de.lierath.oauth2.client.model.OauthFlowType;
import de.lierath.oauth2.client.model.OauthServerConfiguration;
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
		OauthFlow oauthFlow = OauthFlow.forConf(this.serverConf, this.clientConf);
		model.addAttribute("oauthFlow", oauthFlow);
		model.addAttribute("oauthFlowTypes", OauthFlowType.getAll());
		return "home";
	}

	@PostMapping("/startFlow")
	public String startFlow(HttpServletRequest httpRequest, @ModelAttribute OauthFlow oauthFlow,
			BindingResult bindingResult, Model model) throws URISyntaxException, ParseException, IOException {
		log.debug(oauthFlow.getType());
		bindingResult.getModel();
		// TODO execute flow
		if (OauthFlowType.CLIENT.getId().equals(oauthFlow.getType())) {
			TokenRequest request = oauthFlow.toTokenRequest();
			TokenResponse response = TokenResponse.parse(request.toHTTPRequest().send());

			if (!response.indicatesSuccess()) {
				// We got an error response...
				TokenErrorResponse errorResponse = response.toErrorResponse();
				log.error(errorResponse.toJSONObject().toJSONString());
			}

			AccessTokenResponse successResponse = response.toSuccessResponse();

			// Get the access token
			AccessToken accessToken = successResponse.getTokens().getAccessToken();
			log.info(accessToken.getValue());
			model.addAttribute("tokenResponse", JwtTokenResponse.forResponse(successResponse));
			httpRequest.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
			return "redirect:/clientCredentials";
		}
		return "redirect:/home";
	}

	@PostMapping("/clientCredentials")
	public String clientCredentialFlow(@ModelAttribute OauthFlow oauthFlow,
			@ModelAttribute JwtTokenResponse tokenResponse, Model model) {
		return "clientCredentials";
	}

}
