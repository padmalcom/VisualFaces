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

@FacesComponent("FlareChartComponent")
public class FlareChart extends UIComponentBase {

	private static Logger logger = Logger.getLogger(FlareChart.class
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
			strInput = UrlHelper.urlRequest(UrlHelper.getAbsoluteApplicationUrl(context)+ "/faces/dummydata/flarechart.json");
		}
		logger.log(Level.INFO, "Creating flarechart from '"+strInput+"'. Size: "+nWidth+"x"+nHeight);
		
		// Create unique id for image tag
		Random rand = new Random(1237);
		String strImageTagID = "id" + String.valueOf(rand.nextInt(100000));
		
		// Include D3.js
		IncludeResource.includeJavaScript(context, "../js/d3.v3.min.js");
		
		// Include flarepackages.js
		IncludeResource.includeJavaScript(context, "../js/flarepackages.js");		
		
		// Include flarechart css
		IncludeResource.includeCss(context, "../css/flarechart.css");		
		
		// Get response writer
		ResponseWriter writer = context.getResponseWriter();
		
		// Set image Tag
		writer.startElement("div", this);
		writer.writeAttribute("id", strImageTagID, null);
		writer.endElement("div");
		
		// Draw diagram
		writer.startElement("script", this);
		writer.writeAttribute("type", "text/javascript", null);
		
		writer.writeText("var w = "+nWidth+", h = "+nHeight+", rx = w / 2, ry = h / 2, m0, rotate = 0; var splines = [];\n\n", null);
		
		writer.writeText("var cluster = d3.layout.cluster().size([360, ry - 120]).sort(function(a, b) { return d3.ascending(a.key, b.key); });\n\n", null);
		
		writer.writeText("var bundle = d3.layout.bundle();\n\n", null);
		writer.writeText("var line = d3.svg.line.radial().interpolate(\"bundle\").tension(.85).radius(function(d) { return d.y; }).angle(function(d) { return d.x / 180 * Math.PI; });\n\n", null);
		//writer.writeText("var div = d3.select(\"#"+strImageTagID+"\").insert(\"div\", \"h2\").style(\"top\", \"0px\")\n", null);
		//writer.writeText(".style(\"left\", \"0px\").style(\"width\", w + \"px\").style(\"height\", w + \"px\")\n", null);
		
		writer.writeText("var div = d3.select(\"#"+strImageTagID+"\").insert(\"div\", \"h2\")\n", null);
		writer.writeText(".style(\"width\", w + \"px\").style(\"height\", w + \"px\")\n", null);		
		writer.writeText(".style(\"position\", \"absolute\").style(\"-webkit-backface-visibility\", \"hidden\");\n\n", null);
		writer.writeText("var svg = div.append(\"svg:svg\").attr(\"width\", w).attr(\"height\", w).append(\"svg:g\").attr(\"transform\", \"translate(\" + rx + \",\" + ry + \")\");\n\n", null);
		
		writer.writeText("svg.append(\"svg:path\").attr(\"class\", \"arc\").attr(\"d\", d3.svg.arc().outerRadius(ry - 120).innerRadius(0).startAngle(0).endAngle(2 * Math.PI))\n", null);
		writer.writeText(".on(\"mousedown\", mousedown);\n\n", null);
		
		writer.writeText("var classes = JSON.parse('"+strInput+"');\n\n", null);
		
		// Drawing diagram
		writer.writeText("var nodes = cluster.nodes(packages.root(classes)), links = packages.rellinks(nodes), splines = bundle(links);\n\n", null);
		writer.writeText("var path = svg.selectAll(\"path.link\").data(links).enter().append(\"svg:path\")\n", null);
		writer.writeText(".attr(\"class\", function(d) { return \"link source-\" + d.source.key + \" target-\" + d.target.key; })\n", null);
		writer.writeText(".attr(\"d\", function(d, i) { return line(splines[i]); });\n\n", null);
		
		writer.writeText("svg.selectAll(\"g.node\").data(nodes.filter(function(n) { return !n.children; }))\n", null);
		writer.writeText(".enter().append(\"svg:g\").attr(\"class\", \"node\").attr(\"id\", function(d) { return \"node-\" + d.key; })\n", null);
		writer.writeText(".attr(\"transform\", function(d) { return \"rotate(\" + (d.x - 90) + \")translate(\" + d.y + \")\"; })\n", null);
		writer.writeText(".append(\"svg:text\").attr(\"dx\", function(d) { return d.x < 180 ? 8 : -8; }).attr(\"dy\", \".31em\")\n", null);
		writer.writeText(".attr(\"text-anchor\", function(d) { return d.x < 180 ? \"start\" : \"end\"; })\n", null);
		writer.writeText(".attr(\"transform\", function(d) { return d.x < 180 ? null : \"rotate(180)\"; })\n", null);
		writer.writeText(".text(function(d) { return d.key; }).on(\"mouseover\", mouseover).on(\"mouseout\", mouseout);\n\n", null);

		writer.writeText("d3.select(\"input[type=range]\").on(\"change\", function() { line.tension(this.value / 100);\n", null);
		writer.writeText("path.attr(\"d\", function(d, i) { return line(splines[i]); }); \n});\n\n", null);
		// Finished

		writer.writeText("d3.select(window).on(\"mousemove\", mousemove).on(\"mouseup\", mouseup);\n\n", null);
		writer.writeText("function mouse(e) { return [e.pageX - rx, e.pageY - ry]; \n }\n", null);
		
		writer.writeText("function mousedown() { m0 = mouse(d3.event); d3.event.preventDefault();\n\n }\n\n", null);
		
		writer.writeText("function mousemove() { if (m0) { var m1 = mouse(d3.event),\n", null);
		writer.writeText("dm = Math.atan2(cross(m0, m1), dot(m0, m1)) * 180 / Math.PI;\n\n", null);
		writer.writeText("div.style(\"-webkit-transform\", \"translateY(\" + (ry - rx) + \"px)rotateZ(\" + dm + \"deg)translateY(\" + (rx - ry) + \"px)\"); \n\n }\n\n }\n\n", null);

		writer.writeText("function mouseup() { if (m0) { var m1 = mouse(d3.event), dm = Math.atan2(cross(m0, m1), dot(m0, m1)) * 180 / Math.PI;\n\n", null);
		writer.writeText(" rotate += dm; if (rotate > 360) rotate -= 360; else if (rotate < 0) rotate += 360; m0 = null;\n\n", null);
		
		writer.writeText("div.style(\"-webkit-transform\", null); svg.attr(\"transform\", \"translate(\" + rx + \",\" + ry + \")rotate(\" + rotate + \")\")\n", null);
		writer.writeText(".selectAll(\"g.node text\").attr(\"dx\", function(d) { return (d.x + rotate) % 360 < 180 ? 8 : -8; \n\n })\n\n", null);
		writer.writeText(".attr(\"text-anchor\", function(d) { return (d.x + rotate) % 360 < 180 ? \"start\" : \"end\"; \n\n })", null);
		writer.writeText(".attr(\"transform\", function(d) { return (d.x + rotate) % 360 < 180 ? null : \"rotate(180)\"; \n\n }); \n\n } }", null);

		writer.writeText("function mouseover(d) { svg.selectAll(\"path.link.target-\" + d.key).classed(\"target\", true)\n", null);
		writer.writeText(".each(updateNodes(\"source\", true));\n\n", null);
		
		writer.writeText("svg.selectAll(\"path.link.source-\" + d.key).classed(\"source\", true).each(updateNodes(\"target\", true)); \n\n }\n\n", null);
		writer.writeText("function mouseout(d) { svg.selectAll(\"path.link.source-\" + d.key).classed(\"source\", false).each(updateNodes(\"target\", false));\n\n", null);
		
		writer.writeText("svg.selectAll(\"path.link.target-\" + d.key).classed(\"target\", false).each(updateNodes(\"source\", false)); \n\n }\n\n", null);
		
		writer.writeText("function updateNodes(name, value) { return function(d) { if (value) this.parentNode.appendChild(this); svg.select(\"#node-\" + d[name].key).classed(name, value); \n\n  }; \n\n }\n\n", null);
		
		writer.writeText("function cross(a, b) { return a[0] * b[1] - a[1] * b[0]; }\n\n", null);
		
		writer.writeText("function dot(a, b) { return a[0] * b[0] + a[1] * b[1]; }\n\n", null);
		
		writer.endElement("script");
	}
}