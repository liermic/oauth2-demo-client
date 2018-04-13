package de.lierath.oauth2.client.ui;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

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
	public String startFlow(@ModelAttribute OauthFlow oauthFlow, BindingResult bindingResult, Model model) {
		log.debug(oauthFlow.getType());
		bindingResult.getModel();
		// TODO execute flow
		return oauthFlow.getType();
	}

	@PostMapping("/clientCredentials")
	public String clientCredentialFlow(@ModelAttribute OauthFlow oauthFlow, Model model) {
		// TODO execute command
		return "clientCredentials";
	}

}
