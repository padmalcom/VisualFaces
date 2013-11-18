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

@FacesComponent("ZoomableTreemapComponent")
public class ZoomableTreemap extends UIComponentBase {

	private static Logger logger = Logger.getLogger(ZoomableTreemap.class
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
		
		boolean nSortBySize = (getAttributes().get("sortbysize") != null) ? Boolean.parseBoolean(getAttributes().get("sortbysize").toString()) : true;
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
			strInput = UrlHelper.urlRequest(UrlHelper.getAbsoluteApplicationUrl(context)+ "/faces/dummydata/zoomabletreemap.json");
		}
		logger.log(Level.INFO, "Creating zoomabletreemap from '"+strInput+"'. Size: "+nWidth+"x"+nHeight);
		
		// Create unique id for image tag
		Random rand = new Random(1240);
		String strImageTagID = "id" + String.valueOf(rand.nextInt(100000));
		
		// Include D3.js
		IncludeResource.includeJavaScript(context, "../js/d3.v3.min.js");
		
		// Include collapsibletree css
		IncludeResource.includeCss(context, "../css/zoomabletreemap.css");		
		
		// Get response writer
		ResponseWriter writer = context.getResponseWriter();
		
		// Set image Tag
		writer.startElement("div", this);
		writer.writeAttribute("id", strImageTagID, null);
		writer.endElement("div");
		
		// Draw diagram
		writer.startElement("script", this);
		writer.writeAttribute("type", "text/javascript", null);
		
		writer.writeText("var w = "+nWidth+" - 80, h = "+nHeight+" - 180, x = d3.scale.linear().range([0, w]), y = d3.scale.linear().range([0, h]), color = d3.scale.category20c(), root, node;\n\n", null);

		writer.writeText("var treemap = d3.layout.treemap().round(false).size([w, h]).sticky(true).value(function(d) { return d.size; });\n\n", null);
		
		writer.writeText("var svg = d3.select(\"#"+strImageTagID+"\").append(\"div\").attr(\"class\", \"chart\").style(\"width\", w + \"px\").style(\"height\", h + \"px\")\n", null);
		
		writer.writeText("\t.append(\"svg:svg\").attr(\"width\", w).attr(\"height\", h).append(\"svg:g\").attr(\"transform\", \"translate(.5,.5)\");\n\n", null);
		
		writer.writeText("var data = JSON.parse('"+strInput+"');\n\n", null);

			//d3.json("flare.json", function(data) {
		writer.writeText("node = root = data;\nvar nodes = treemap.nodes(root).filter(function(d) { return !d.children; });\n\n", null);
		
		writer.writeText("var cell = svg.selectAll(\"g\").data(nodes).enter().append(\"svg:g\").attr(\"class\", \"cell\")\n", null);
		writer.writeText("\t.attr(\"transform\", function(d) { return \"translate(\" + d.x + \",\" + d.y + \")\"; })\n", null);
		writer.writeText("\t.on(\"click\", function(d) { return zoom(node == d.parent ? root : d.parent); });\n\n", null);
		
		writer.writeText("cell.append(\"svg:rect\").attr(\"width\", function(d) { return d.dx - 1; })\n", null);
		writer.writeText("\t.attr(\"height\", function(d) { return d.dy - 1; }).style(\"fill\", function(d) { return color(d.parent.name); });\n\n", null);
		
		writer.writeText("cell.append(\"svg:text\").attr(\"x\", function(d) { return d.dx / 2; }).attr(\"y\", function(d) { return d.dy / 2; })\n", null);
		writer.writeText("\t.attr(\"dy\", \".35em\").attr(\"text-anchor\", \"middle\").text(function(d) { return d.name; })\n", null);
		writer.writeText("\t.style(\"opacity\", function(d) { d.w = this.getComputedTextLength(); return d.dx > d.w ? 1 : 0; });\n\n", null);
		
		writer.writeText("d3.select(window).on(\"click\", function() { zoom(root); });\n\n", null);

		writer.writeText("d3.select(\"select\").on(\"change\", function() {\n", null);
		//writer.writeText("treemap.value(this.value == \"size\" ? size : count).nodes(root);\n", null);
		writer.writeText("\ttreemap.value("+nSortBySize+" ? size : count).nodes(root);\n", null);
		writer.writeText("\tzoom(node);\n});\n\n", null);
		
		writer.writeText("function size(d) { return d.size;} \n\n function count(d) { return 1; }\n\n", null);
		
		writer.writeText("function zoom(d) { var kx = w / d.dx, ky = h / d.dy; x.domain([d.x, d.x + d.dx]); y.domain([d.y, d.y + d.dy]);\n\n", null);
		
		writer.writeText("var t = svg.selectAll(\"g.cell\").transition().duration(d3.event.altKey ? 7500 : 750)\n", null);
		writer.writeText("\t.attr(\"transform\", function(d) { return \"translate(\" + x(d.x) + \",\" + y(d.y) + \")\"; });\n\n", null);
		
		writer.writeText("t.select(\"rect\").attr(\"width\", function(d) { return kx * d.dx - 1; })\n", null);
		writer.writeText("\t.attr(\"height\", function(d) { return ky * d.dy - 1; });\nt.select(\"text\")", null);
		writer.writeText(".attr(\"x\", function(d) { return kx * d.dx / 2; }).attr(\"y\", function(d) { return ky * d.dy / 2; })\n", null);
		writer.writeText("\t.style(\"opacity\", function(d) { return kx * d.dx > d.w ? 1 : 0; });\n\n", null);
		writer.writeText("node = d; d3.event.stopPropagation();\n}\n", null);		
		writer.endElement("script");
	}
}