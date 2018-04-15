package de.lierath.oauth2.client.execution;

public class OauthExecutionException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public OauthExecutionException(String msg, Throwable e) {
		super(msg, e);
	}

}
