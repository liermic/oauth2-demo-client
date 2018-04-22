package de.lierath.oauth2.client.model;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationErrorResponse;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.oauth2.sdk.AuthorizationSuccessResponse;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.token.AccessToken;

import de.lierath.oauth2.client.execution.OauthExecutionException;
import de.lierath.oauth2.client.util.JwtUtil;
import de.lierath.oauth2.client.util.OauthDisplayUtil;
import lombok.Data;

@Data
public class OauthFlowResultData {

	private String oauthFlowType;

	private String authorizeRequest;

	private String authorizeResponse; // Authorization Grant and Implicit Grant

	private String authorizationCode; // Authorization Grant flow only

	private String tokenRequest; // Not for Implicit Grant

	private String tokenResponse;

	private String accessToken;

	private String jwtClaimsSet;

	private Boolean isValidSignature;

	public void addAccessTokenResponse(TokenResponse response, String jwkUrl) {
		if (response.indicatesSuccess()) {
			// token response
			AccessTokenResponse successResponse = response.toSuccessResponse();
			this.tokenResponse = successResponse.toJSONObject().toJSONString(OauthDisplayUtil.getStyle());
			// access token (signed JWT)
			AccessToken accessToken = successResponse.getTokens().getAccessToken();
			String tokenString = OauthDisplayUtil.prettyPrint(accessToken);
			this.accessToken = tokenString;
			this.isValidSignature = JwtUtil.validateTokenSignature(accessToken.getValue(), jwkUrl);
			// decoded claims set
			try {
				String claimsSet = OauthDisplayUtil.decodeClaimsSet(accessToken);
				this.jwtClaimsSet = claimsSet;
			} catch (net.minidev.json.parser.ParseException e) {
				throw new OauthExecutionException("Unable to parse jwt to JSON.", e);
			}
		} else {
			// We got an error response...
			TokenErrorResponse errorResponse = response.toErrorResponse();
			this.tokenResponse = errorResponse.toJSONObject().toJSONString(OauthDisplayUtil.getStyle());
		}
	}

	public void addAuthorizeTokenResponse(AuthorizationResponse response, String jwkUrl) {
		if (response.indicatesSuccess()) {
			AuthorizationSuccessResponse successResponse = response.toSuccessResponse();
			successResponse.getAccessToken();
			this.authorizeResponse = successResponse.toURI().toString();

			// access token (signed JWT)
			AccessToken accessToken = successResponse.getAccessToken();
			String tokenString = OauthDisplayUtil.prettyPrint(accessToken);
			this.accessToken = tokenString;
			this.isValidSignature = JwtUtil.validateTokenSignature(accessToken.getValue(), jwkUrl);
			// decoded claims set
			try {
				String claimsSet = OauthDisplayUtil.decodeClaimsSet(accessToken);
				this.jwtClaimsSet = claimsSet;
			} catch (net.minidev.json.parser.ParseException e) {
				throw new OauthExecutionException("Unable to parse jwt to JSON.", e);
			}
		} else {
			// We got an error response...
			AuthorizationErrorResponse errorResponse = response.toErrorResponse();
			this.authorizeResponse = errorResponse.toURI().toString();
		}

	}

}
