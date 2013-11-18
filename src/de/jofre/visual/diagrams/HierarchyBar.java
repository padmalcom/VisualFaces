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

@FacesComponent("HierarchyBarComponent")
public class HierarchyBar extends UIComponentBase {

	private static Logger logger = Logger.getLogger(HierarchyBar.class
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
			strInput = UrlHelper.urlRequest(UrlHelper.getAbsoluteApplicationUrl(context)+ "/faces/dummydata/hierarchybar.json");
		}
		logger.log(Level.INFO, "Creating hierarchybar from '"+strInput+"'. Size: "+nWidth+"x"+nHeight);
		
		// Create unique id for image tag
		Random rand = new Random(1238);
		String strImageTagID = "id" + String.valueOf(rand.nextInt(100000));
		
		// Include D3.js
		IncludeResource.includeJavaScript(context, "../js/d3.v3.min.js");
		
		// Include hierarchy css
		IncludeResource.includeCss(context, "../css/hierarchybar.css");		
		
		// Get response writer
		ResponseWriter writer = context.getResponseWriter();
		
		// Set image Tag
		writer.startElement("div", this);
		writer.writeAttribute("id", strImageTagID, null);
		writer.endElement("div");
		
		// Draw diagram
		writer.startElement("script", this);
		writer.writeAttribute("type", "text/javascript", null);
		
		writer.writeText("var m = [80, 160, 0, 160], w = "+nWidth+" - m[1] - m[3], h = "+nHeight+" - m[0] - m[2], x = d3.scale.linear().range([0, w]), y = 25, z = d3.scale.ordinal().range([\"steelblue\", \"#aaa\"]);\n\n", null);

		writer.writeText("var hierarchy = d3.layout.partition().value(function(d) { return d.size; });\n\n", null);
		writer.writeText("var xAxis = d3.svg.axis().scale(x).orient(\"top\");\n\n", null);
		writer.writeText("var svg = d3.select(\"#"+strImageTagID+"\").append(\"svg:svg\").attr(\"width\", w + m[1] + m[3]).attr(\"height\", h + m[0] + m[2])\n", null);
		writer.writeText(".append(\"svg:g\").attr(\"transform\", \"translate(\" + m[3] + \",\" + m[0] + \")\");\n\n", null);

		writer.writeText("svg.append(\"svg:rect\").attr(\"class\", \"background\").attr(\"width\", w).attr(\"height\", h).on(\"click\", up);\n\n", null);
		
		writer.writeText("svg.append(\"svg:g\").attr(\"class\", \"x axis\");\n\n", null);
		
		writer.writeText("svg.append(\"svg:g\").attr(\"class\", \"y axis\").append(\"svg:line\").attr(\"y1\", \"100%\");\n\n", null);

		// Write data
		writer.writeText("var root = JSON.parse('"+strInput+"');\n\n", null);
		writer.writeText("hierarchy.nodes(root);\n\n", null);
		writer.writeText("x.domain([0, root.value]).nice();\n\n", null);
		writer.writeText("down(root, 0);\n\n", null);
		// Finished

		writer.writeText("function down(d, i) {\n", null);
		writer.writeText("if (!d.children || this.__transition__) return;\n", null);
		writer.writeText("var duration = d3.event && d3.event.altKey ? 7500 : 750, delay = duration / d.children.length;\n", null);
		
		writer.writeText("var exit = svg.selectAll(\".enter\").attr(\"class\", \"exit\");\n\n", null);

		writer.writeText("exit.selectAll(\"rect\").filter(function(p) { return p === d; }).style(\"fill-opacity\", 1e-6);\n\n", null);
		
		writer.writeText("var enter = bar(d).attr(\"transform\", stack(i)).style(\"opacity\", 1);\n", null);

		writer.writeText("enter.select(\"text\").style(\"fill-opacity\", 1e-6);\n", null);
		writer.writeText("enter.select(\"rect\").style(\"fill\", z(true));\n", null);
		writer.writeText("x.domain([0, d3.max(d.children, function(d) { return d.value; })]).nice();\n", null);
		
		writer.writeText("svg.selectAll(\".x.axis\").transition().duration(duration).call(xAxis);\n", null);

		writer.writeText("var enterTransition = enter.transition().duration(duration)", null);
		writer.writeText(".delay(function(d, i) { return i * delay; }).attr(\"transform\", function(d, i) { return \"translate(0,\" + y * i * 1.2 + \")\"; });\n", null);

		writer.writeText("enterTransition.select(\"text\").style(\"fill-opacity\", 1);\n\n", null);
		
		writer.writeText("enterTransition.select(\"rect\").attr(\"width\", function(d) { return x(d.value); }).style(\"fill\", function(d) { return z(!!d.children); });\n\n", null);
		
		writer.writeText("var exitTransition = exit.transition().duration(duration).style(\"opacity\", 1e-6).remove();\n\n", null);
		
		writer.writeText("exitTransition.selectAll(\"rect\").attr(\"width\", function(d) { return x(d.value); });\n\n", null);
		
		writer.writeText("svg.select(\".background\").data([d]).transition().duration(duration * 2); d.index = i; }\n\n", null);

		writer.writeText("function up(d) { if (!d.parent || this.__transition__) return; var duration = d3.event && d3.event.altKey ? 7500 : 750,\n", null);
		writer.writeText("delay = duration / d.children.length;\n\n", null);
		
		writer.writeText("var exit = svg.selectAll(\".enter\").attr(\"class\", \"exit\");\n", null);
		
		writer.writeText("var enter = bar(d.parent).attr(\"transform\", function(d, i) { return \"translate(0,\" + y * i * 1.2 + \")\"; })\n", null);
		writer.writeText(".style(\"opacity\", 1e-6);\n", null);
		
		writer.writeText("enter.select(\"rect\").style(\"fill\", function(d) { return z(!!d.children); })\n", null);
		writer.writeText(".filter(function(p) { return p === d; }).style(\"fill-opacity\", 1e-6);\n\n", null);

		writer.writeText("x.domain([0, d3.max(d.parent.children, function(d) { return d.value; })]).nice();\n\n", null);
		
		
		writer.writeText("svg.selectAll(\".x.axis\").transition().duration(duration * 2).call(xAxis);\n\n", null);
		
		writer.writeText("var enterTransition = enter.transition().duration(duration * 2).style(\"opacity\", 1);\n\n", null);
		
		writer.writeText("enterTransition.select(\"rect\").attr(\"width\", function(d) { return x(d.value); })\n", null);
		writer.writeText(".each(\"end\", function(p) { if (p === d) d3.select(this).style(\"fill-opacity\", null); });\n\n", null);

		
		writer.writeText("var exitTransition = exit.selectAll(\"g\").transition().duration(duration)\n", null);
		writer.writeText(".delay(function(d, i) { return i * delay; }).attr(\"transform\", stack(d.index));\n\n", null);
		
		writer.writeText("exitTransition.select(\"text\").style(\"fill-opacity\", 1e-6);\n\n", null);
		
		writer.writeText("exitTransition.select(\"rect\").attr(\"width\", function(d) { return x(d.value); }).style(\"fill\", z(true));\n\n", null);

		writer.writeText("exit.transition().duration(duration * 2).remove();\n\n", null);
		
		writer.writeText("svg.select(\".background\").data([d.parent]).transition().duration(duration * 2);\n }\n\n", null);
		
		writer.writeText("function bar(d) { var bar = svg.insert(\"svg:g\", \".y.axis\").attr(\"class\", \"enter\").attr(\"transform\", \"translate(0,5)\")\n", null);
		writer.writeText(".selectAll(\"g\").data(d.children).enter().append(\"svg:g\").style(\"cursor\", function(d) { return !d.children ? null : \"pointer\"; }).on(\"click\", down);\n\n", null);

		writer.writeText("bar.append(\"svg:text\").attr(\"x\", -6).attr(\"y\", y / 2).attr(\"dy\", \".35em\")\n", null);
		writer.writeText(".attr(\"text-anchor\", \"end\").text(function(d) { return d.name; });\n\n", null);
		
		writer.writeText("bar.append(\"svg:rect\").attr(\"width\", function(d) { return x(d.value); }).attr(\"height\", y);\n\n", null);
		writer.writeText("return bar;\n	}\n\n", null);
		
		writer.writeText("function stack(i) { var x0 = 0; return function(d) { var tx = \"translate(\" + x0 + \",\" + y * i * 1.2 + \")\";\n", null);
		writer.writeText("x0 += x(d.value); return tx; }; }\n\n", null);
		
		writer.endElement("script");
	}
}