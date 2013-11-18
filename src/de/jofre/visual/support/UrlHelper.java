package de.jofre.visual.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;



public class UrlHelper {

	private static Logger logger = Logger.getLogger(UrlHelper.class.getName());

	// Get the absolute URL of the FacesContext
	public static String getAbsoluteApplicationUrl(FacesContext context) {

		ExternalContext extContext = context.getExternalContext();
		HttpServletRequest request = (HttpServletRequest) extContext
				.getRequest();
		URL url = null;
		URL newUrl = null;
		try {
			url = new URL(request.getRequestURL().toString());
			newUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(),
					request.getContextPath());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		logger.log(Level.INFO, "URL for JSF context is: " + newUrl.toString());
		return newUrl.toString();
	}

	// Get content of a webpage
	public static String urlRequest(String url) {
		StringBuilder result = new StringBuilder();	
		logger.log(Level.INFO, "HTTP request to: "+url);
		URL newUrl = null;
		try {
			newUrl = new URL(url);
			URLConnection yc = newUrl.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					yc.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				result.append(inputLine);
			}
			in.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result.toString();
	}
}
