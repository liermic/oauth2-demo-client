package de.lierath.oauth2.client.util;

import java.util.Base64;

import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.AccessToken;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class OauthDisplayUtil {

	private static final String JWT_SEPERATOR = ".";
	private static final String JWT_SEPERATOR_REGEX = "\\.";
	private static final String NEW_LINE = System.getProperty("line.separator");

	public static String prettyPrint(AccessToken accessToken) {
		String token = accessToken.toJSONObject().getAsString("access_token");
		String[] splitToken = token.split(JWT_SEPERATOR_REGEX);
		StringBuilder sb = new StringBuilder();
		sb.append(splitToken[0]).append(NEW_LINE);
		sb.append(JWT_SEPERATOR).append(NEW_LINE);
		sb.append(splitToken[1]).append(NEW_LINE);
		sb.append(JWT_SEPERATOR).append(NEW_LINE);
		sb.append(splitToken[2]).append(NEW_LINE);
		return sb.toString();
	}

	public static String decodeClaimsSet(AccessToken accessToken) throws ParseException {
		String token = accessToken.toJSONObject().getAsString("access_token");
		String[] splitToken = token.split(JWT_SEPERATOR_REGEX);

		String jwsHeader = new String(Base64.getDecoder().decode(splitToken[0]));
		String claimsSet = new String(Base64.getDecoder().decode(splitToken[1]));

		JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
		JSONObject jwsHeaderJson = parser.parse(jwsHeader, JSONObject.class);
		JSONObject claimsSetJson = parser.parse(claimsSet, JSONObject.class);

		StringBuilder sb = new StringBuilder();
		sb.append(jwsHeaderJson.toJSONString(getStyle())).append(NEW_LINE);
		sb.append(JWT_SEPERATOR).append(NEW_LINE);
		sb.append(claimsSetJson.toJSONString(getStyle())).append(NEW_LINE);
		sb.append(JWT_SEPERATOR).append(NEW_LINE);
		sb.append(splitToken[2]).append(NEW_LINE);
		return sb.toString();
	}

	public static JSONStyle getStyle() {
		return PrettyJSONStyle.get();
	}

	public static String prettyPrint(HTTPRequest r) {
		StringBuilder sb = new StringBuilder();
		sb.append("Method: " + r.getMethod().toString() + NEW_LINE);
		sb.append("URL: " + r.getURL()).append(NEW_LINE);
		if (r.getAuthorization() != null) {
			sb.append("Authorization: " + r.getAuthorization()).append(NEW_LINE);
		}
		sb.append("Query: " + r.getQuery()).append(NEW_LINE);
		return sb.toString();
	}

	public static String prettyPrint(HTTPResponse r) {
		StringBuilder sb = new StringBuilder();
		sb.append("Headers:").append(NEW_LINE);
		sb.append(r.getHeaders()).append(NEW_LINE).append(NEW_LINE);
		sb.append("Content: " + NEW_LINE);
		sb.append(r.getContent());
		return sb.toString();
	}
}
