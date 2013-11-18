package de.jofre.visual.support;

public class ColorGenerator {

	public static String generateRandomHtmlColor() {
	    String[] letters = "0123456789ABCDEF".split("");
	    StringBuilder color = new StringBuilder("#");
	    for (int i = 0; i < 6; i++ ) {
	    	color.append(letters[(int)Math.round(Math.random() * 15)]);
	    }
	    return color.toString();
	}
}
