package de.jofre.visual.support;

import java.io.IOException;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

public class IncludeResource {

	private static Logger logger = Logger.getLogger(IncludeResource.class
			.getName());

	// Include a css resource
	public static void includeCss(FacesContext context, String resource)
			throws IOException {

		if (resourceIncluded(context, resource)) {
			logger.log(Level.INFO, "CSS Resource '" + resource
					+ "' already included!");
			return;
		}

		ResourceHandler rh = context.getApplication().getResourceHandler();
		Resource r = rh.createResource(resource);
		ResponseWriter w = context.getResponseWriter();
		w.write('\n');
		w.startElement("link", null);
		w.writeAttribute("type", "text/css", null);
		w.writeAttribute("rel", "stylesheet", null);
		w.writeAttribute("href", ((r != null) ? r.getRequestPath() : ""), null);
		w.endElement("link");
		w.append('\r');
		w.append('\n');
	}
	
	// Include a javascript resource
	public static void includeJavaScript(FacesContext context, String resource)
			throws IOException {

		if (resourceIncluded(context, resource)) {
			logger.log(Level.INFO, "JavaScript Resource '" + resource
					+ "' already included!");
			return;
		}

		ResourceHandler rh = context.getApplication().getResourceHandler();
		Resource r = rh.createResource(resource);
		ResponseWriter w = context.getResponseWriter();
		w.write('\n');
		w.startElement("script", null);
		w.writeAttribute("type", "text/javascript", null);
		w.writeAttribute("src", ((r != null) ? r.getRequestPath() : ""), null);
		w.endElement("script");
		/*w.write("<script type=\"text/javascript\" src=\"");
		w.write(((r != null) ? r.getRequestPath() : ""));
		w.write("\"></script>");*/
		w.append('\r');
		w.append('\n');
	}

	// Check if resource is already in context
	public static boolean resourceIncluded(FacesContext context, String resource)
			throws IOException {
		
		// Check head
		UIViewRoot viewRoot = context.getViewRoot();
		ListIterator<UIComponent> iter = (viewRoot.getComponentResources(context, "head"))
				.listIterator();
		while (iter.hasNext()) {
			UIComponent c = (UIComponent) iter.next();
			String rname = (String) c.getAttributes().get("name");
			if (resource.equals(rname)) {
				return true;
			}
		}
		
		// Check body
		iter = (viewRoot.getComponentResources(context, "body")).listIterator();
		while (iter.hasNext()) {
			UIComponent c = (UIComponent) iter.next();
			String rname = (String) c.getAttributes().get("name");
			if (resource.equals(rname)) {
				return true;
			}
		}
		
		// Check forms
		iter = (viewRoot.getComponentResources(context, "form")).listIterator();
		while (iter.hasNext()) {
			UIComponent c = (UIComponent) iter.next();
			String rname = (String) c.getAttributes().get("name");
			if (resource.equals(rname)) {
				return true;
			}
		}

		return false;
	}
}
