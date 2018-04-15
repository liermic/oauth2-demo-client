package de.lierath.oauth2.client.util;

import java.io.IOException;

import net.minidev.json.JSONStyle;

public class PrettyJSONStyle extends JSONStyle {

	private static final String NEW_LINE = System.getProperty("line.separator");

	private int currentLevel = 0;

	private String INDENT_STRING = "    ";

	public static PrettyJSONStyle get() {
		return new PrettyJSONStyle();
	}

	@Override
	public void objectStart(Appendable out) throws IOException {
		super.objectStart(out);
		out.append(NEW_LINE);
		this.currentLevel++;
		doIndent(out);
	}

	@Override
	public void objectStop(Appendable out) throws IOException {
		out.append(NEW_LINE);
		super.objectStop(out);
		this.currentLevel--;
	}

	/**
	 * Start a new Object element
	 */
	@Override
	public void objectNext(Appendable out) throws IOException {
		super.objectNext(out); // out.append(',');
		out.append(NEW_LINE);
		doIndent(out);
	}

	private void doIndent(Appendable out) throws IOException {
		for (int i = 0; i < this.currentLevel; i++) {
			out.append(this.INDENT_STRING);
		}
	}

	@Override
	public void objectEndOfKey(Appendable out) throws IOException {
		super.objectEndOfKey(out); // out.append(':');
		out.append(" ");
	}

	@Override
	public boolean indent() {
		return true;
	}

}
