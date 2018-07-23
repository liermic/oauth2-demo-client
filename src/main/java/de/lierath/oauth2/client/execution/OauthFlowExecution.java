package de.lierath.oauth2.client.execution;

import de.lierath.oauth2.client.controller.OauthSession;
import de.lierath.oauth2.client.model.OauthFlowData;
import de.lierath.oauth2.client.model.OauthFlowResultData;

public interface OauthFlowExecution {

	OauthFlowResultData executeInitialRequest(OauthFlowData inputData, OauthSession session);

}
