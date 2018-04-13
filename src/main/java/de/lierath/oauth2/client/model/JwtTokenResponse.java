package de.lierath.oauth2.client.model;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.token.Tokens;

import lombok.Data;

@Data
public class JwtTokenResponse {

	private String jsonResponse;

	private String jwt;

	private String claimsSet;

	public static JwtTokenResponse forResponse(AccessTokenResponse response) {
		JwtTokenResponse jwt = new JwtTokenResponse();
		jwt.setJsonResponse(response.toJSONObject().toJSONString());
		if (response.indicatesSuccess()) {
			AccessTokenResponse success = response.toSuccessResponse();
			Tokens tokens = success.getTokens();
			jwt.setJwt(tokens.getAccessToken().toJSONString());
			jwt.setClaimsSet(tokens.getAccessToken().toJSONString());
		}
		return jwt;
	}

}
