package de.lierath.oauth2.client.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import de.lierath.oauth2.client.execution.OauthFlowExecution;
import de.lierath.oauth2.client.model.OauthFlowData;
import de.lierath.oauth2.client.model.OauthFlowResultData;
import de.lierath.oauth2.client.model.OauthFlowType;
import de.lierath.oauth2.client.model.OauthServerConfiguration;
import de.lierath.oauth2.client.model.OauthTrustedClientConfiguration;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This controller serves as starting point for the client application. It
 * prepares the input data from configured properties. It then starts a chosen
 * OAuth-Flow with given input data.
 *
 * @author Michael Lierath
 *
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class Oauth2ClientUIController {

	private static final String PAGE_HOME = "home";

	@NonNull
	OauthTrustedClientConfiguration clientConf;

	@NonNull
	OauthServerConfiguration serverConf;

	/**
	 * Initializes the model for input data and move on to the starting page.
	 *
	 * @param model
	 *            the generic UI model that will be injected into the page
	 * @return the starting page
	 */
	@GetMapping("/")
	public String home(Model model) {
		OauthFlowData oauthFlow = OauthFlowData.forConf(this.serverConf, this.clientConf);
		model.addAttribute("oauthFlow", oauthFlow);
		model.addAttribute("oauthFlowTypes", OauthFlowType.getAll());
		return PAGE_HOME;
	}

	/**
	 * This is called by clicking the "Start Flow" button on the starting page. The
	 * method will parse the input data, prepare a session and execute the chosen
	 * grant type. It will return either a redirect to the authorization server or
	 * the result page.
	 *
	 * @param oauthFlow
	 *            the input data from the form
	 * @param model
	 *            the generic UI model will be initialized with result data for
	 *            client credentials flow
	 * @return the next page to show
	 */
	@PostMapping("/startFlow")
	public String startFlow(@ModelAttribute OauthFlowData oauthFlow, Model model) {
		log.debug(oauthFlow.getType());
		// start session for result handling
		OauthSession session = OauthSession.open();
		session.setInputData(oauthFlow);
		// execute flow
		OauthFlowType flowType = OauthFlowType.forId(oauthFlow.getType());
		OauthFlowExecution execution = flowType.getExecution();
		OauthFlowResultData result = execution.executeInitialRequest(oauthFlow, session);
		result.setExpectedSignatureAlgorithm(oauthFlow.getExpectedSignatureAlgorithm());
		model.addAttribute("result", result);
		// redirect to auth server or result page
		return session.getNextPage();
	}

}