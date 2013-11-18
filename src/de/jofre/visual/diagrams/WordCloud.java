package de.jofre.visual.diagrams;

import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import de.jofre.visual.support.IncludeResource;
import de.jofre.visual.support.UrlHelper;

@FacesComponent("WordCloudComponent")
public class WordCloud extends UIComponentBase {

	private static Logger logger = Logger.getLogger(WordCloud.class
			.getName());
	
	@Override
	public String getFamily() {
		return "jofre.visual.diagrams";
	}
	

	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		if (context == null) {
			logger.log(Level.SEVERE, "No context defined!");
			throw new NullPointerException();
		}
				
		// Get JSF attributes
		int nWidth = (getAttributes().get("width") != null) ? Integer.parseInt(getAttributes().get("width").toString()) : 400;
		int nHeight = (getAttributes().get("height") != null) ? Integer.parseInt(getAttributes().get("height").toString()) : 400;
		String strInput = null;
		
		// Get input as Data
		strInput = (getAttributes().get("input") != null) ? (String)getAttributes().get("input") : null;
		
		// Is input data a URL?
		if (strInput != null) {
			if (strInput.startsWith("http")) {
				strInput = UrlHelper.urlRequest(strInput);
			}
		}
		// No input found? Take dummy data
		else {
			strInput = UrlHelper.urlRequest(UrlHelper.getAbsoluteApplicationUrl(context)+ "/faces/dummydata/wordcloud.txt");
		}
		logger.log(Level.INFO, "Creating wordcloud from '"+strInput+"'. Size: "+nWidth+"x"+nHeight);
		
		// Create unique id for image tag
		Random rand = new Random(1239);
		String strImageTagID = "id" + String.valueOf(rand.nextInt(100000));
		
		// Include D3.js
		IncludeResource.includeJavaScript(context, "../js/d3.v3.min.js");
		IncludeResource.includeJavaScript(context, "../js/d3.js.layout.clouds.js");
		
		// Get response writer
		ResponseWriter writer = context.getResponseWriter();
		
		// Set image Tag
		writer.startElement("div", this);
		writer.writeAttribute("id", strImageTagID, null);
		writer.endElement("div");
		
		// Draw diagram
		writer.startElement("script", this);
		writer.writeAttribute("type", "text/javascript", null);
		
		writer.writeText("var fill = d3.scale.category20();", null);
		
		writer.writeText("var width="+nWidth+", height="+nHeight+";", null);
		
		writer.writeText("d3.layout.cloud().size([width, height]).words([", null);
		
		// Format "This", "is", "a", "test"
		writer.writeText(strInput, null);
		
		writer.writeText("].map(function(d) { return {text: d, size: 10 + Math.random() * 90}; }))", null);
		writer.writeText(".rotate(function() { return ~~(Math.random() * 2) * 90; }).font(\"Impact\")", null);
		writer.writeText(".fontSize(function(d) { return d.size; }).on(\"end\", draw).start();", null);
		
		writer.writeText("function draw(words) { d3.select(\"#"+strImageTagID+"\").append(\"svg\")", null);
		writer.writeText(".attr(\"width\", width).attr(\"height\", height).append(\"g\")", null);
		writer.writeText(".attr(\"transform\", \"translate(\"+(width/2)+\", \"+(height/2)+\")\")", null);
		writer.writeText(".selectAll(\"text\").data(words).enter().append(\"text\")", null);
		writer.writeText(".style(\"font-size\", function(d) { return d.size + \"px\"; })", null);
		writer.writeText(".style(\"font-family\", \"Impact\").style(\"fill\", function(d, i) { return fill(i); })", null);
		writer.writeText(".attr(\"text-anchor\", \"middle\").attr(\"transform\", function(d) {", null);
		writer.writeText("return \"translate(\" + [d.x, d.y] + \")rotate(\" + d.rotate + \")\";", null);
		writer.writeText("}).text(function(d) { return d.text; }); }", null);
		
		writer.endElement("script");
	}
}
