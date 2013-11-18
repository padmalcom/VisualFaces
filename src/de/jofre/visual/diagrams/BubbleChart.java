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

@FacesComponent("BubbleChartComponent")
public class BubbleChart extends UIComponentBase {

	private static Logger logger = Logger.getLogger(BubbleChart.class
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
			strInput = UrlHelper.urlRequest(UrlHelper.getAbsoluteApplicationUrl(context)+ "/faces/dummydata/bubblechart.json");
		}
		logger.log(Level.INFO, "Creating bubblechart from '"+strInput+"'. Size: "+nWidth+"x"+nHeight);
		
		// Create unique id for image tag
		Random rand = new Random(1234);
		String strImageTagID = "id" + String.valueOf(rand.nextInt(100000));
		
		// Include D3.js
		IncludeResource.includeJavaScript(context, "../js/d3.v3.min.js");
		
		// Get response writer
		ResponseWriter writer = context.getResponseWriter();
		
		// Set image Tag
		writer.startElement("div", this);
		writer.writeAttribute("id", strImageTagID, null);
		writer.endElement("div");
		
		// Draw diagram
		writer.startElement("script", this);
		writer.writeAttribute("type", "text/javascript", null);
				
		writer.writeText("var width="+nWidth+", height="+nHeight+", format = d3.format(\",d\"), color = d3.scale.category20c();\n", null);
		writer.writeText("var bubble = d3.layout.pack().sort(null).size([width, height]).padding(1.5);\n", null);
		writer.writeText("var svg = d3.select(\"#"+strImageTagID+"\").append(\"svg\").attr(\"width\", width).attr(\"height\", height).attr(\"class\", \"bubble\");\n", null);
		writer.writeText("var root = JSON.parse('"+strInput+"');\n\n", null);
		
		writer.writeText("\tvar node = svg.selectAll(\".node\").data(bubble.nodes(classes(root)).filter(function(d) { return !d.children; }))\n", null);
		writer.writeText("\t.enter().append(\"g\").attr(\"class\", \"node\").attr(\"transform\", function(d) { return \"translate(\" + d.x + \",\" + d.y + \")\"; });\n", null);
		writer.writeText("\tnode.append(\"title\").text(function(d) { return d.className + \": \" + format(d.value); });\n", null);
		writer.writeText("\tnode.append(\"circle\").attr(\"r\", function(d) { return d.r; }).style(\"fill\", function(d) { return color(d.packageName); });\n", null);
		writer.writeText("\tnode.append(\"text\").attr(\"dy\", \".3em\").style(\"text-anchor\", \"middle\").text(function(d) { return d.className.substring(0, d.r / 3); });\n", null);
		
		writer.writeText("function classes(root) {\n", null);
		writer.writeText("\tvar classes = [];\n", null);
		writer.writeText("\tfunction recurse(name, node) {\n", null);
		writer.writeText("\t\tif (node.children) node.children.forEach(function(child) { recurse(node.name, child); });\n", null);
		writer.writeText("\t\telse classes.push({packageName: name, className: node.name, value: node.size});\n", null);
		writer.writeText("}\n", null);
		
		writer.writeText("\trecurse(null, root);\n", null);
		writer.writeText("\treturn {children: classes};\n", null);
		writer.writeText("}\n\n", null);
		
		writer.writeText("d3.select(self.frameElement).style(\"height\", height + \"px\");\n", null);
		writer.endElement("script");
	}
}