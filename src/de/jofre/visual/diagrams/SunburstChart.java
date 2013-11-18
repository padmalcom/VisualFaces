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

@FacesComponent("SunburstChartComponent")
public class SunburstChart extends UIComponentBase {

	private static Logger logger = Logger.getLogger(SunburstChart.class
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
			strInput = UrlHelper.urlRequest(UrlHelper.getAbsoluteApplicationUrl(context)+ "/faces/dummydata/sunburstchart.json");
		}
		logger.log(Level.INFO, "Creating sunburstchart from '"+strInput+"'. Size: "+nWidth+"x"+nHeight);
		
		// Create unique id for image tag
		Random rand = new Random(1237);
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
		
		writer.writeText("var width = "+nWidth+",height = "+nHeight+",radius = width / 2,x = d3.scale.linear().range([0, 2 * Math.PI]),\n", null);
		writer.writeText("\ty = d3.scale.pow().exponent(1.3).domain([0, 1]).range([0, radius]),padding = 5,duration = 1000;\n\n", null);
		
		writer.writeText("var div = d3.select(\"#"+strImageTagID+"\")\n", null);

		writer.writeText("var vis = div.append(\"svg\").attr(\"width\", width + padding * 2).attr(\"height\", height + padding * 2)\n", null);
		writer.writeText("\t.append(\"g\").attr(\"transform\", \"translate(\" + [radius + padding, radius + padding] + \")\");\n\n", null);
		
		writer.writeText("var partition = d3.layout.partition().sort(null).value(function(d) { return 5.8 - d.depth; });\n\n", null);
		
		writer.writeText("var arc = d3.svg.arc().startAngle(function(d) { return Math.max(0, Math.min(2 * Math.PI, x(d.x))); })\n", null);
		writer.writeText("\t.endAngle(function(d) { return Math.max(0, Math.min(2 * Math.PI, x(d.x + d.dx))); })\n", null);
		writer.writeText("\t.innerRadius(function(d) { return Math.max(0, d.y ? y(d.y) : d.y); })\n", null);
		writer.writeText("\t.outerRadius(function(d) { return Math.max(0, y(d.y + d.dy)); });\n\n", null);
		
		writer.writeText("var json = JSON.parse('"+strInput+"');\n\n", null);

			//d3.json(\"wheel.json\", function(json) {
		writer.writeText("var nodes = partition.nodes({children: json});\n", null);
		writer.writeText("var path = vis.selectAll(\"path\").data(nodes);\n", null);
		writer.writeText("path.enter().append(\"path\").attr(\"id\", function(d, i) { return \"path-\" + i; }).attr(\"d\", arc)\n", null);
		writer.writeText("\t.attr(\"fill-rule\", \"evenodd\").style(\"fill\", colour).on(\"click\", click);\n\n", null);
		
		writer.writeText("var text = vis.selectAll(\"text\").data(nodes);\n", null);
		writer.writeText("var textEnter = text.enter().append(\"text\").style(\"fill-opacity\", 1).style(\"fill\", function(d) {\n", null);
		writer.writeText("\treturn brightness(d3.rgb(colour(d))) < 125 ? \"#eee\" : \"#000\";\n", null);
		writer.writeText("}).attr(\"text-anchor\", function(d) {\n", null);
		writer.writeText("\treturn x(d.x + d.dx / 2) > Math.PI ? \"end\" : \"start\";\n", null);
		writer.writeText("}).attr(\"dy\", \".2em\").attr(\"transform\", function(d) {\n", null);
		writer.writeText("\tvar multiline = (d.name || \"\").split(\" \").length > 1,\n", null);
		writer.writeText("\tangle = x(d.x + d.dx / 2) * 180 / Math.PI - 90, rotate = angle + (multiline ? -.5 : 0);\n", null);
		writer.writeText("\treturn \"rotate(\" + rotate + \")translate(\" + (y(d.y) + padding) + \")rotate(\" + (angle > 90 ? -180 : 0) + \")\";\n", null);
		writer.writeText("}).on(\"click\", click);\n\n", null);
		
		writer.writeText("textEnter.append(\"tspan\").attr(\"x\", 0).text(function(d) { return d.depth ? d.name.split(\" \")[0] : \"\";\n});\n\n", null);
		
		writer.writeText("textEnter.append(\"tspan\").attr(\"x\", 0).attr(\"dy\", \"1em\")\n", null);
		writer.writeText("\t.text(function(d) { return d.depth ? d.name.split(\" \")[1] || \"\" : \"\";\n});\n\n", null);

		writer.writeText("function click(d) {\n", null);
		writer.writeText("\tpath.transition().duration(duration).attrTween(\"d\", arcTween(d));\n", null);
		writer.writeText("text.style(\"visibility\", function(e) {\n", null);
		writer.writeText("\treturn isParentOf(d, e) ? null : d3.select(this).style(\"visibility\");\n", null);
		writer.writeText("}).transition().duration(duration).attrTween(\"text-anchor\", function(d) {\n", null);
		writer.writeText("\treturn function() {\n", null);
		writer.writeText("\t\treturn x(d.x + d.dx / 2) > Math.PI ? \"end\" : \"start\";\n\t};\n})", null);
		writer.writeText(".attrTween(\"transform\", function(d) {\n", null);
		writer.writeText("\tvar multiline = (d.name || \"\").split(\" \").length > 1;\n", null);
		writer.writeText("\treturn function() {\n", null);
		writer.writeText("\t\tvar angle = x(d.x + d.dx / 2) * 180 / Math.PI - 90, rotate = angle + (multiline ? -.5 : 0);\n", null);
		writer.writeText("\t\treturn \"rotate(\" + rotate + \")translate(\" + (y(d.y) + padding) + \")rotate(\" + (angle > 90 ? -180 : 0) + \")\";\n", null);
		writer.writeText("\t};\n}).style(\"fill-opacity\", function(e) { return isParentOf(d, e) ? 1 : 1e-6; }).each(\"end\", function(e) {\n", null);
		writer.writeText("\td3.select(this).style(\"visibility\", isParentOf(d, e) ? null : \"hidden\");\n", null);
		writer.writeText("});\n}\n", null);
			//});

		writer.writeText("function isParentOf(p, c) {\n\tif (p === c) return true;\n\tif (p.children) {\n", null);
		writer.writeText("\t\treturn p.children.some(function(d) {\n\t\t\treturn isParentOf(d, c);\n\t\t});\n\t}\n\treturn false;\n}\n\n", null);
		
		writer.writeText("function colour(d) {\n\tif (d.children) {\n\t\tvar colours = d.children.map(colour),a = d3.hsl(colours[0]),b = d3.hsl(colours[1]);\n", null);
		writer.writeText("\t\treturn d3.hsl((a.h + b.h) / 2, a.s * 1.2, a.l / 1.2);\n", null);
		writer.writeText("\t}\n\treturn d.colour || \"#fff\";\n}\n\n", null);
		
		writer.writeText("function arcTween(d) {\n\tvar my = maxY(d),xd = d3.interpolate(x.domain(), [d.x, d.x + d.dx]),\n", null);
		writer.writeText("\t\tyd = d3.interpolate(y.domain(), [d.y, my]),yr = d3.interpolate(y.range(), [d.y ? 20 : 0, radius]);\n", null);
		writer.writeText("\treturn function(d) {\n\t\treturn function(t) { x.domain(xd(t)); y.domain(yd(t)).range(yr(t)); return arc(d);};\n", null);
		writer.writeText("\t};\n}\n\n", null);
		
		writer.writeText("function maxY(d) {\n\treturn d.children ? Math.max.apply(Math, d.children.map(maxY)) : d.y + d.dy;\n}\n\n", null);

		writer.writeText("function brightness(rgb) {\n\treturn rgb.r * .299 + rgb.g * .587 + rgb.b * .114;\n}\n", null);
		
		writer.endElement("script");
	}
}