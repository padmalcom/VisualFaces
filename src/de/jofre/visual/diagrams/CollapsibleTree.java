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

@FacesComponent("CollapsibleTreeComponent")
public class CollapsibleTree extends UIComponentBase {

	private static Logger logger = Logger.getLogger(CollapsibleTree.class
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
			strInput = UrlHelper.urlRequest(UrlHelper.getAbsoluteApplicationUrl(context)+ "/faces/dummydata/collapsibletree.json");
		}
		logger.log(Level.INFO, "Creating collapsibletree from '"+strInput+"'. Size: "+nWidth+"x"+nHeight);
		
		// Create unique id for image tag
		Random rand = new Random(1236);
		String strImageTagID = "id" + String.valueOf(rand.nextInt(100000));
		
		// Include D3.js
		IncludeResource.includeJavaScript(context, "../js/d3.v3.min.js");
		
		// Include collapsibletree css
		IncludeResource.includeCss(context, "../css/collapsibletree.css");		
		
		// Get response writer
		ResponseWriter writer = context.getResponseWriter();
		
		// Set image Tag
		writer.startElement("div", this);
		writer.writeAttribute("id", strImageTagID, null);
		writer.endElement("div");
		
		// Draw diagram
		writer.startElement("script", this);
		writer.writeAttribute("type", "text/javascript", null);
		
		//writer.writeText("var m = [20, 120, 20, 120], w = "+nWidth+" - m[1] - m[3], h = "+nHeight+" - m[0] - m[2], i = 0, root;\n", null);
		writer.writeText("var m = [0, 0, 0, 0], w = "+nWidth+" - m[1] - m[3], h = "+nHeight+" - m[0] - m[2], i = 0, root;\n", null);
		writer.writeText("var tree = d3.layout.tree().size([h, w]);\n", null);
		writer.writeText("var diagonal = d3.svg.diagonal().projection(function(d) { return [d.y, d.x]; });\n", null);
		
		writer.writeText("var vis = d3.select(\"#"+strImageTagID+"\").append(\"svg:svg\").attr(\"width\", w + m[1] + m[3])\n", null);
		writer.writeText(".attr(\"height\", h + m[0] + m[2]).append(\"svg:g\").attr(\"transform\", \"translate(\" + m[3] + \",\" + m[0] + \")\");\n\n", null);
		
		writer.writeText("var json = JSON.parse('"+strInput+"');\n\n", null);
		writer.writeText("root = json; root.x0 = h / 2; root.y0 = 0;\n", null);
		
		writer.writeText("function toggleAll(d) { if (d.children) { d.children.forEach(toggleAll); toggle(d); } }\n", null);
		
		writer.writeText("root.children.forEach(toggleAll); toggle(root.children[1]); toggle(root.children[1].children[2]);\n", null);
		writer.writeText("toggle(root.children[9]); toggle(root.children[9].children[0]); update(root);\n", null);
			//});

		writer.writeText("function update(source) { var duration = d3.event && d3.event.altKey ? 5000 : 500;\n", null);

		writer.writeText("var nodes = tree.nodes(root).reverse(); nodes.forEach(function(d) { d.y = d.depth * 180; });\n", null);
		
		writer.writeText("var node = vis.selectAll(\"g.node\").data(nodes, function(d) { return d.id || (d.id = ++i); });\n", null);
		
		writer.writeText("var nodeEnter = node.enter().append(\"svg:g\").attr(\"class\", \"node\")\n", null);
		writer.writeText(".attr(\"transform\", function(d) { return \"translate(\" + source.y0 + \",\" + source.x0 + \")\"; })\n", null);
		writer.writeText(".on(\"click\", function(d) { toggle(d); update(d); });\n", null);
		
		writer.writeText("nodeEnter.append(\"svg:circle\").attr(\"r\", 1e-6)\n", null);
		writer.writeText(".style(\"fill\", function(d) { return d._children ? \"lightsteelblue\" : \"#fff\"; });\n", null);

		writer.writeText("nodeEnter.append(\"svg:text\").attr(\"x\", function(d) { return d.children || d._children ? -10 : 10; })\n", null);
		writer.writeText(".attr(\"dy\", \".35em\").attr(\"text-anchor\", function(d) { return d.children || d._children ? \"end\" : \"start\"; })\n", null);
		writer.writeText(".text(function(d) { return d.name; }).style(\"fill-opacity\", 1e-6);\n", null);
		
		writer.writeText("var nodeUpdate = node.transition().duration(duration).attr(\"transform\", function(d) { return \"translate(\" + d.y + \",\" + d.x + \")\"; });\n", null);
		
		writer.writeText("nodeUpdate.select(\"circle\").attr(\"r\", 4.5).style(\"fill\", function(d) { return d._children ? \"lightsteelblue\" : \"#fff\"; });\n", null);
		
		writer.writeText("nodeUpdate.select(\"text\").style(\"fill-opacity\", 1);\n", null);
		
		writer.writeText("var nodeExit = node.exit().transition().duration(duration).attr(\"transform\", function(d) { return \"translate(\" + source.y + \",\" + source.x + \")\"; })\n", null);
		
		writer.writeText(".remove();\n\n", null);
		
		writer.writeText("nodeExit.select(\"circle\").attr(\"r\", 1e-6);\n\n", null);
		
		writer.writeText("nodeExit.select(\"text\").style(\"fill-opacity\", 1e-6);\n\n", null);
		
		writer.writeText("var link = vis.selectAll(\"path.link\").data(tree.links(nodes), function(d) { return d.target.id; });\n\n", null);
		
		writer.writeText("link.enter().insert(\"svg:path\", \"g\").attr(\"class\", \"link\").attr(\"d\", function(d) {\n", null);
		writer.writeText("var o = {x: source.x0, y: source.y0}; return diagonal({source: o, target: o}); })\n", null);
		writer.writeText(".transition().duration(duration).attr(\"d\", diagonal);\n\n", null);
		
		writer.writeText("link.transition().duration(duration).attr(\"d\", diagonal);\n\n", null);
		
		writer.writeText("link.exit().transition().duration(duration).attr(\"d\", function(d) {var o = {x: source.x, y: source.y}; \n", null);
		writer.writeText("return diagonal({source: o, target: o}); }).remove();\n\n", null);
		
		writer.writeText("nodes.forEach(function(d) { d.x0 = d.x; d.y0 = d.y; }); }\n", null);
		
		writer.writeText("function toggle(d) { if (d.children) { d._children = d.children; d.children = null;\n", null);
		writer.writeText("} else {d.children = d._children; d._children = null; } }\n\n", null);
		writer.endElement("script");
	}
}