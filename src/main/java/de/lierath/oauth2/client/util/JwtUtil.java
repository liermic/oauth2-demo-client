package de.lierath.oauth2.client.util;

import java.net.MalformedURLException;
import java.net.URL;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtUtil {

	public static boolean validateTokenSignature(String plainAccessToken, String jwkUrl,
			String expectedSignatureAlgorithm) {
		if("NONE".equals(expectedSignatureAlgorithm)) {
			return true;
		}
		// Set up a JWT processor to parse the tokens and then check their signature
		// and validity time window (bounded by the "iat", "nbf" and "exp" claims)
		ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor = new DefaultJWTProcessor<>();

		// The public RSA keys to validate the signatures will be sourced from the
		// OAuth 2.0 server's JWK set, published at a well-known URL. The RemoteJWKSet
		// object caches the retrieved keys to speed up subsequent look-ups and can
		// also gracefully handle key-rollover
		JWKSource<SimpleSecurityContext> keySource;
		try {
			ResourceRetriever resRetriever = new DefaultResourceRetriever(5000, 5000);
			keySource = new RemoteJWKSet<>(new URL(jwkUrl), resRetriever);
		} catch (MalformedURLException e1) {
			log.error("Unable to parse jwkUrl as URL!", e1);
			return false;
		}

		// The expected JWS algorithm of the access tokens (agreed out-of-band)
		JWSAlgorithm expectedJWSAlg = JWSAlgorithm.parse(expectedSignatureAlgorithm); // i.e. RS256 or EC256

		// Configure the JWT processor with a key selector to feed matching public
		// RSA keys sourced from the JWK set URL
		JWSKeySelector<SimpleSecurityContext> keySelector = new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);
		jwtProcessor.setJWSKeySelector(keySelector);

		// Process the token
		SimpleSecurityContext ctx = null; // optional context parameter, not required here
		try {
			JWTClaimsSet claimsSet = jwtProcessor.process(plainAccessToken, ctx);
			// Print out the token claims set
//			log.debug(claimsSet.toJSONObject().toJSONString());
			return true;
		} catch (BadJOSEException | JOSEException | java.text.ParseException e) {
			log.error("Invalid JWT!", e);
			return false;
		}

	}

}
