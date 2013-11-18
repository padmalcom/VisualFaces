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

@FacesComponent("ParallelSetsComponent")
public class ParallelSets extends UIComponentBase {

	private static Logger logger = Logger.getLogger(ParallelSets.class
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
			strInput = UrlHelper.urlRequest(UrlHelper.getAbsoluteApplicationUrl(context)+ "/faces/dummydata/parallelset.json");
		}
		logger.log(Level.INFO, "Creating parallelsets from '"+strInput+"'. Size: "+nWidth+"x"+nHeight);
		
		// Create unique id for image tag
		Random rand = new Random(1237);
		String strImageTagID = "id" + String.valueOf(rand.nextInt(100000));
		
		// Include D3.js
		IncludeResource.includeJavaScript(context, "../js/d3.v3.min.js");
		
		// Include d3.js.layout.parsets.js
		IncludeResource.includeJavaScript(context, "../js/d3.js.layout.parsets.js");		
		
		// Include parallelset css
		IncludeResource.includeCss(context, "../css/parallelset.css");		
		
		// Get response writer
		ResponseWriter writer = context.getResponseWriter();
		
		// Set image Tag
		writer.startElement("div", this);
		writer.writeAttribute("id", strImageTagID, null);
		writer.endElement("div");
		
		// Draw diagram
		writer.startElement("script", this);
		writer.writeAttribute("type", "text/javascript", null);
		
		writer.writeText("var chart = d3.parsets().dimensions([\"Survived\", \"Sex\", \"Age\", \"Class\"]);\n\n", null);

		writer.writeText("var vis = d3.select(\"#"+strImageTagID+"\").append(\"svg\").attr(\"width\", chart.width()).attr(\"height\", chart.height());\n\n", null);
		
		writer.writeText("var partition = d3.layout.partition().sort(null).size([chart.width(), chart.height() * 5 / 4])\n", null);
		writer.writeText("\t.children(function(d) { return d.children ? d3.values(d.children) : null; }).value(function(d) { return d.count; });\n\n", null);
		
		writer.writeText("var ice = false;\n\nfunction curves() {\tvar t = vis.transition().duration(500);\n\tif (ice) {\n\t\tt.delay(1000);\n", null);
		writer.writeText("\t\ticicle();\n\t}\n\tt.call(chart.tension(this.checked ? .5 : 1));\n}\n\n", null);
		
		writer.writeText("var csv = JSON.parse('"+strInput+"');\n\n", null);

			//d3.csv(\"titanic.csv\", function(csv) {
		writer.writeText("vis.datum(csv).call(chart);\n\n", null);
		
		writer.writeText("window.icicle = function() {\n\tvar newIce = this.checked,tension = chart.tension();\n\t", null);
		writer.writeText("\tif (newIce === ice) return;\n\tif (ice = newIce) {\n", null);
		writer.writeText("\t\tvar dimensions = [];\n\t\tvis.selectAll(\"g.dimension\").each(function(d) { dimensions.push(d); });\n", null);
		writer.writeText("\t\tdimensions.sort(function(a, b) { return a.y - b.y; });\n", null);
		writer.writeText("\t\tvar root = d3.parsets.tree({children: {}}, csv, dimensions.map(function(d) { return d.name; }), function() { return 1; }),\n", null);
		writer.writeText("\t\t\tnodes = partition(root),nodesByPath = {};\n", null);
		writer.writeText("\t\tnodes.forEach(function(d) {\n", null);
		writer.writeText("\t\t\tvar path = d.data.name,p = d;\n", null);
		writer.writeText("\t\t\twhile ((p = p.parent) && p.data.name) { path = p.data.name + \"\0\" + path; }\n", null);
		writer.writeText("\t\t\tif (path) nodesByPath[path] = d;\n\t\t});\n", null);
		writer.writeText("\tvar data = [];\n\tvis.on(\"mousedown.icicle\", stopClick, true).select(\".ribbon\").selectAll(\"path\")\n", null);
		writer.writeText("\t\t.each(function(d) {\n\t\t\tvar node = nodesByPath[d.path],s = d.source,t = d.target;\n", null);
		writer.writeText("\t\ts.node.x0 = t.node.x0 = 0;\n\t\ts.x0 = t.x0 = node.x;\n\t\ts.dx0 = s.dx;\n\t\tt.dx0 = t.dx;\n", null);
		writer.writeText("\t\ts.dx = t.dx = node.dx;\n\t\tdata.push(d);\n\t});\n", null);
		writer.writeText("\ticeTransition(vis.selectAll(\"path\")).attr(\"d\", function(d) {\n", null);
		writer.writeText("\t\tvar s = d.source,t = d.target;\n\t\treturn ribbonPath(s, t, tension);\n\t})\n", null);
		writer.writeText("\t.style(\"stroke-opacity\", 1);\n", null);
		writer.writeText("\ticeTransition(vis.selectAll(\"text.icicle\").data(data).enter().append(\"text\").attr(\"class\", \"icicle\")\n", null);
		writer.writeText("\t\t.attr(\"text-anchor\", \"middle\").attr(\"dy\", \".3em\").attr(\"transform\", function(d) {\n", null);
		writer.writeText("\t\t\treturn \"translate(\" + [d.source.x0 + d.source.dx / 2, d.source.dimension.y0 + d.target.dimension.y0 >> 1] + \")rotate(90)\";\n", null);
		writer.writeText("\t\t})\n", null);
		writer.writeText("\t.text(function(d) { return d.source.dx > 15 ? d.node.name : null; }).style(\"opacity\", 1e-6))\n", null);
		writer.writeText("\t.style(\"opacity\", 1);\n", null);
		writer.writeText("\ticeTransition(vis.selectAll(\"g.dimension rect, g.category\").style(\"opacity\", 1)).style(\"opacity\", 1e-6)\n", null);
		writer.writeText("\t.each(\"end\", function() { d3.select(this).attr(\"visibility\", \"hidden\"); });\n", null);
		writer.writeText("\ticeTransition(vis.selectAll(\"text.dimension\")).attr(\"transform\", \"translate(0,-5)\");\n", null);
		writer.writeText("\tvis.selectAll(\"tspan.sort\").style(\"visibility\", \"hidden\");\n", null);
		writer.writeText("} else {\n", null);
		writer.writeText("\tvis.on(\"mousedown.icicle\", null).select(\".ribbon\").selectAll(\"path\").each(function(d) {\n", null);
		writer.writeText("\t\tvar s = d.source, t = d.target;\n\t\ts.node.x0 = s.node.x;\n\t\ts.x0 = s.x;\n\t\ts.dx = s.dx0;\n", null);
		writer.writeText("\t\tt.node.x0 = t.node.x;\n\t\tt.x0 = t.x;\n\t\tt.dx = t.dx0;\n\t});\n", null);
		writer.writeText("\ticeTransition(vis.selectAll(\"path\")).attr(\"d\", function(d) {\n", null);
		writer.writeText("\t\tvar s = d.source,t = d.target;\n\t\treturn ribbonPath(s, t, tension);\n\t})\n", null);
		writer.writeText("\t.style(\"stroke-opacity\", null);\n\ticeTransition(vis.selectAll(\"text.icicle\"))\n", null);
		writer.writeText("\t\t.style(\"opacity\", 1e-6).remove();\n", null);
		writer.writeText("\ticeTransition(vis.selectAll(\"g.dimension rect, g.category\").attr(\"visibility\", null)\n", null);
		writer.writeText("\t\t.style(\"opacity\", 1e-6)).style(\"opacity\", 1);\n\t\ticeTransition(vis.selectAll(\"text.dimension\"))\n", null);
		writer.writeText("\t\t\t.attr(\"transform\", \"translate(0,-25)\");\n", null);
		writer.writeText("\t\tvis.selectAll(\"tspan.sort\").style(\"visibility\", null);\n\t}\n", null);
		writer.writeText("\t};\n", null);
		
		writer.writeText("d3.select(\"#icicle\").on(\"change\", icicle).each(icicle);\n", null);
		
		writer.writeText("function iceTransition(g) { return g.transition().duration(1000); }\n", null);
		
		writer.writeText("function ribbonPath(s, t, tension) {\n\tvar sx = s.node.x0 + s.x0, tx = t.node.x0 + t.x0, sy = s.dimension.y0,ty = t.dimension.y0;\n", null);
		writer.writeText("\treturn (tension === 1 ? [\"M\", [sx, sy],\"L\", [tx, ty],\"h\", t.dx,\"L\", [sx + s.dx, sy],\"Z\"]\n", null);
		writer.writeText("\t\t: [\"M\", [sx, sy],\"C\", [sx, m0 = tension * sy + (1 - tension) * ty], \" \",\n", null);
		
		writer.writeText("\t\t[tx, m1 = tension * ty + (1 - tension) * sy], \" \", [tx, ty],\"h\", t.dx,\n", null);
		writer.writeText("\t\t\"C\", [tx + t.dx, m1], \" \", [sx + s.dx, m0], \" \", [sx + s.dx, sy],\"Z\"]).join(\"\");\n\t}\n", null);
		
		writer.writeText("function stopClick() { d3.event.stopPropagation(); }\n", null);

		writer.writeText("function truncateText(text, width) {\n\treturn function(d, i) {\n", null);
		writer.writeText("\t\tvar t = this.textContent = text(d, i),w = width(d, i);\n", null);
		writer.writeText("\t\tif (this.getComputedTextLength() < w) return t;\n", null);
		writer.writeText("\t\tthis.textContent = \"…\" + t;\n\t\tvar lo = 0,hi = t.length + 1,x;\n", null);
		writer.writeText("\t\twhile (lo < hi) {\n\t\t\tvar mid = lo + hi >> 1;\n", null);
		writer.writeText("\t\t\tif ((x = this.getSubStringLength(0, mid)) < w) lo = mid + 1;\n\t\t\telse hi = mid;\n", null);
		writer.writeText("\t\t}\n\t\treturn lo > 1 ? t.substr(0, lo - 2) + \"…\" : \"\";\n", null);
		writer.writeText("\t};\n}\n", null);
		
		/*

			d3.select(\"#file\").on(\"change\", function() {
			  var file = this.files[0],
			      reader = new FileReader;
			  reader.onloadend = function() {
			    var csv = d3.csv.parse(reader.result);
			    vis.datum(csv).call(chart
			        .value(csv[0].hasOwnProperty(\"Number\") ? function(d) { return +d.Number; } : 1)
			        .dimensions(function(d) { return d3.keys(d[0]).filter(function(d) { return d !== \"Number\"; }).sort(); }));
			  };
			  reader.readAsText(file);
			});
		
		*/
		writer.endElement("script");
	}
}