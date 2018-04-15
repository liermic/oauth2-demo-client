package de.lierath.oauth2.client.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.lierath.oauth2.client.model.OauthFlowData;
import de.lierath.oauth2.client.model.OauthFlowResultData;
import lombok.Getter;
import lombok.Setter;

public class OauthSession {

	private static Map<UUID, OauthSession> sessionHolder = new HashMap<>();

	public static OauthSession open() {
		OauthSession s = new OauthSession(UUID.randomUUID());
		sessionHolder.put(s.getId(), s);
		return s;
	}

	public static OauthSession get(UUID uuid) {
		return sessionHolder.get(uuid);
	}

	public static OauthSession get(String uuid) {
		UUID id = UUID.fromString(uuid);
		return sessionHolder.get(id);
	}

	private OauthSession(UUID uuid) {
		this.id = uuid;
	}

	public void close() {
		sessionHolder.remove(this.id);
	}

	@Getter
	private final UUID id;

	@Getter
	@Setter
	private OauthFlowResultData result;

	@Getter
	@Setter
	private OauthFlowData inputData;

	@Getter
	@Setter
	private String nextPage;

}
