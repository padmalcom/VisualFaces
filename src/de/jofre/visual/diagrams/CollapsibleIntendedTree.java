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

@FacesComponent("CollapsibleIntendedTreeComponent")
public class CollapsibleIntendedTree extends UIComponentBase {

	private static Logger logger = Logger.getLogger(CollapsibleIntendedTree.class
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
		int nBarHeight = (getAttributes().get("barheight") != null) ? Integer.parseInt(getAttributes().get("barheight").toString()) : 20;
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
			strInput = UrlHelper.urlRequest(UrlHelper.getAbsoluteApplicationUrl(context)+ "/faces/dummydata/collapsibleintendedtree.json");
		}
		logger.log(Level.INFO, "Creating collapsibleintendedtree from '"+strInput+"'. Size: "+nWidth+"x"+nHeight);
		
		// Create unique id for image tag
		Random rand = new Random(1236);
		String strImageTagID = "id" + String.valueOf(rand.nextInt(100000));
		
		// Include D3.js
		IncludeResource.includeJavaScript(context, "../js/d3.v3.min.js");
		
		// Include collapsibletree css
		IncludeResource.includeCss(context, "../css/collapsibleintendedtree.css");		
		
		// Get response writer
		ResponseWriter writer = context.getResponseWriter();
		
		// Set image Tag
		writer.startElement("div", this);
		writer.writeAttribute("id", strImageTagID, null);
		writer.endElement("div");
		
		// Draw diagram
		writer.startElement("script", this);
		writer.writeAttribute("type", "text/javascript", null);
		
		writer.writeText("var w = "+nWidth+",h = "+nHeight+",i = 0,barHeight = "+nBarHeight+",barWidth = w * .8,duration = 400,root;\n", null);
		writer.writeText("var tree = d3.layout.tree().size([h, 100]);\n", null);
		writer.writeText("var diagonal = d3.svg.diagonal().projection(function(d) { return [d.y, d.x]; });\n", null);
		writer.writeText("var vis = d3.select(\"#"+strImageTagID+"\").append(\"svg:svg\").attr(\"width\", w)\n", null);
		writer.writeText("\t.attr(\"height\", h).append(\"svg:g\").attr(\"transform\", \"translate(20,30)\");\n", null);
		
		writer.writeText("var json = JSON.parse('"+strInput+"');\n\n", null);
		
		writer.writeText("json.x0 = 0; json.y0 = 0; update(root = json);\n\n", null);
		
		writer.writeText("function update(source) {\n\tvar nodes = tree.nodes(root);\n\tnodes.forEach(function(n, i) {\n", null);
		writer.writeText("\t\tn.x = i * barHeight;\n", null);
		writer.writeText("\t});\n\n", null);
		
		writer.writeText("var node = vis.selectAll(\"g.node\").data(nodes, function(d) { return d.id || (d.id = ++i); });\n\n", null);
		
		writer.writeText("var nodeEnter = node.enter().append(\"svg:g\").attr(\"class\", \"node\")\n", null);
		writer.writeText("\t.attr(\"transform\", function(d) { return \"translate(\" + source.y0 + \",\" + source.x0 + \")\"; })\n", null);
		writer.writeText("\t.style(\"opacity\", 1e-6);\n\n", null);
		
		writer.writeText("nodeEnter.append(\"svg:rect\").attr(\"y\", -barHeight / 2).attr(\"height\", barHeight)\n", null);
		writer.writeText("\t.attr(\"width\", barWidth).style(\"fill\", color).on(\"click\", click);\n\n", null);
		
		writer.writeText("nodeEnter.append(\"svg:text\").attr(\"dy\", 3.5).attr(\"dx\", 5.5).text(function(d) { return d.name; });\n\n", null);
		
		writer.writeText("nodeEnter.transition().duration(duration).attr(\"transform\", function(d) { return \"translate(\" + d.y + \",\" + d.x + \")\"; })\n", null);
		writer.writeText("\t.style(\"opacity\", 1);\n\n", null);
		
		writer.writeText("node.transition().duration(duration).attr(\"transform\", function(d) { return \"translate(\" + d.y + \",\" + d.x + \")\"; })\n", null);
		writer.writeText("\t.style(\"opacity\", 1).select(\"rect\").style(\"fill\", color);\n\n", null);
		
		writer.writeText("node.exit().transition().duration(duration).attr(\"transform\", function(d) { return \"translate(\" + source.y + \",\" + source.x + \")\"; })\n", null);
		writer.writeText("\t.style(\"opacity\", 1e-6).remove();\n\n", null);
		
		writer.writeText("var link = vis.selectAll(\"path.link\").data(tree.links(nodes), function(d) { return d.target.id; });\n\n", null);
		
		writer.writeText("link.enter().insert(\"svg:path\", \"g\").attr(\"class\", \"link\").attr(\"d\", function(d) {\n", null);
		writer.writeText("\tvar o = {x: source.x0, y: source.y0};\n\treturn diagonal({source: o, target: o});\n", null);
		writer.writeText("}).transition().duration(duration).attr(\"d\", diagonal);\n\n", null);
		
		writer.writeText("link.transition().duration(duration).attr(\"d\", diagonal);\n\n", null);
		
		writer.writeText("link.exit().transition().duration(duration).attr(\"d\", function(d) {\n", null);
		writer.writeText("\tvar o = {x: source.x, y: source.y};\n\treturn diagonal({source: o, target: o});\n", null);
		writer.writeText("}).remove();\n\n", null);
		
		writer.writeText("nodes.forEach(function(d) {\n\td.x0 = d.x;\td.y0 = d.y;\n});\n}\n\n", null);
		
		writer.writeText("function click(d) {\n\tif (d.children) {\n\t\td._children = d.children;\n\td.children = null;\n", null);
		writer.writeText("\t} else {\n\t\td.children = d._children;\n\t\td._children = null;\n\t}\n\tupdate(d);\n}\n\n", null);
		
		writer.writeText("function color(d) {\n\treturn d._children ? \"#3182bd\" : d.children ? \"#c6dbef\" : \"#fd8d3c\";\n}\n\n", null);
		
		writer.endElement("script");
	}
}