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

@FacesComponent("TransitionDiagramComponent")
public class TransitionDiagram extends UIComponentBase {

	private static Logger logger = Logger.getLogger(TransitionDiagram.class
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
		int nDelay = (getAttributes().get("delay") != null) ? Integer.parseInt(getAttributes().get("delay").toString()) : 1000;
		int nDuration = (getAttributes().get("duration") != null) ? Integer.parseInt(getAttributes().get("duration").toString()) : 2000;		
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
			strInput = UrlHelper.urlRequest(UrlHelper.getAbsoluteApplicationUrl(context)+ "/faces/dummydata/transitiondiagram.json");
		}
		logger.log(Level.INFO, "Creating transitiondiagram from '"+strInput+"'. Size: "+nWidth+"x"+nHeight);
		
		// Create unique id for image tag
		Random rand = new Random(1238);
		String strImageTagID = "id" + String.valueOf(rand.nextInt(100000));
		
		// Include D3.js
		IncludeResource.includeJavaScript(context, "../js/d3.v3.min.js");
		
		// Include hierarchy css
		IncludeResource.includeCss(context, "../css/transitiondiagram.css");		
		
		// Get response writer
		ResponseWriter writer = context.getResponseWriter();
		
		// Set image Tag
		writer.startElement("div", this);
		writer.writeAttribute("id", strImageTagID, null);
		writer.endElement("div");
		
		// Draw diagram
		writer.startElement("script", this);
		writer.writeAttribute("type", "text/javascript", null);
		
		writer.writeText("var m = [0, 0, 0, 0], w = "+nWidth+" - m[1] - m[3], h = "+nHeight+" - m[0] - m[2];\n\n", null);
		
		writer.writeText("var x,y,duration = "+nDuration+",delay = "+nDelay+";\n\n", null);
		
		writer.writeText("var color = d3.scale.category10();\n\n", null);
		
		writer.writeText("var svg = d3.select(\"body\").append(\"svg:svg\").attr(\"width\", w + m[1] + m[3]).attr(\"height\", h + m[0] + m[2])\n", null);
		writer.writeText("\t.append(\"svg:g\").attr(\"transform\", \"translate(\" + m[3] + \",\" + m[0] + \")\");\n\n", null);
		
		writer.writeText("var stocks,symbols;\n", null);
		
		writer.writeText("var line = d3.svg.line().interpolate(\"basis\")\n", null);
		writer.writeText("\t.x(function(d) { return x(d.date); }).y(function(d) { return y(d.price); });\n\n", null);
		
		writer.writeText("var axis = d3.svg.line().interpolate(\"basis\").x(function(d) { return x(d.date); }).y(h);\n\n", null);

		writer.writeText("var area = d3.svg.area().interpolate(\"basis\").x(function(d) { return x(d.date); })\n", null);
		writer.writeText("\t.y1(function(d) { return y(d.price); });\n\n", null);
		
		writer.writeText("var data = JSON.parse('"+strInput+"');\n\n", null);
		//writer.writeText("d3.csv(\"stocks.csv\", function(data) {", null);
		writer.writeText("\tvar parse = d3.time.format(\"%b %Y\").parse;\n", null);
		writer.writeText("\tsymbols = d3.nest().key(function(d) { return d.symbol; }).entries(stocks = data);\n", null);
		writer.writeText("\tsymbols.forEach(function(s) {\n", null);
		writer.writeText("\t\ts.values.forEach(function(d) { d.date = parse(d.date); d.price = +d.price; });\n", null);
		writer.writeText("\t\ts.maxPrice = d3.max(s.values, function(d) { return d.price; });\n", null);
		writer.writeText("\t\ts.sumPrice = d3.sum(s.values, function(d) { return d.price; });\n", null);
		writer.writeText("\t});\n", null);
		
		writer.writeText("symbols.sort(function(a, b) { return b.maxPrice - a.maxPrice; });\n\n", null);
		writer.writeText("var g = svg.selectAll(\"g\").data(symbols).enter().append(\"svg:g\").attr(\"class\", \"symbol\");\n", null);
		writer.writeText("setTimeout(lines, duration);\n", null);
		//writer.writeText("});\n\n", null);

		writer.writeText("function lines() {\n\tx = d3.time.scale().range([0, w - 60]);\n\ty = d3.scale.linear().range([h / 4 - 20, 0]);\n", null);
		writer.writeText("\tx.domain([d3.min(symbols, function(d) { return d.values[0].date; }),", null);
		writer.writeText("d3.max(symbols, function(d) { return d.values[d.values.length - 1].date; })]);\n", null);
		writer.writeText("\tvar g = svg.selectAll(\".symbol\").attr(\"transform\", function(d, i) { return \"translate(0,\" + (i * h / 4 + 10) + \")\"; });\n", null);
		writer.writeText("\tg.each(function(d) {\n\tvar e = d3.select(this);\n\te.append(\"svg:path\").attr(\"class\", \"line\");\n", null);
		writer.writeText("\te.append(\"svg:circle\").attr(\"r\", 5).style(\"fill\", function(d) { return color(d.key); })\n", null);
		writer.writeText("\t.style(\"stroke\", \"#000\").style(\"stroke-width\", \"2px\");\n\n", null);
		
		writer.writeText("\te.append(\"svg:text\").attr(\"x\", 12).attr(\"dy\", \".31em\").text(d.key);\n});\n\n", null);
		
		writer.writeText("function draw(k) {\n\tg.each(function(d) {\n\t\tvar e = d3.select(this);\n\t\ty.domain([0, d.maxPrice]);\n", null);
		writer.writeText("\t\te.select(\"path\").attr(\"d\", function(d) { return line(d.values.slice(0, k + 1)); });\n\t\t", null);
		writer.writeText("\t\te.selectAll(\"circle, text\").data(function(d) { return [d.values[k], d.values[k]]; })", null);
		writer.writeText(".attr(\"transform\", function(d) { return \"translate(\" + x(d.date) + \",\" + y(d.price) + \")\"; });\n", null);
		writer.writeText("\t});\n}\n\n", null);
		
		writer.writeText("var k = 1, n = symbols[0].values.length; d3.timer(function() {\n\tdraw(k);\n\tif ((k += 2) >= n - 1) {\n\t\t", null);
		writer.writeText("draw(n - 1);\n\t\tsetTimeout(horizons, 500);\n\t\treturn true;\n\t}\n});\n}\n\n", null);
		
		writer.writeText("function horizons() {\n\tsvg.insert(\"svg:defs\", \".symbol\").append(\"svg:clipPath\").attr(\"id\", \"clip\")\n", null);
		writer.writeText("\t\t.append(\"svg:rect\").attr(\"width\", w).attr(\"height\", h / 4 - 20);\n", null);
		
		writer.writeText("\tvar color = d3.scale.ordinal().range([\"#c6dbef\", \"#9ecae1\", \"#6baed6\"]);\n", null);
		writer.writeText("\tvar g = svg.selectAll(\".symbol\").attr(\"clip-path\", \"url(#clip)\");\n", null);
		
		writer.writeText("\tarea.y0(h / 4 - 20);\n\tg.select(\"circle\").transition().duration(duration)\n", null);
		writer.writeText("\t\t.attr(\"transform\", function(d) { return \"translate(\" + (w - 60) + \",\" + (-h / 4) + \")\"; }).remove();\n", null);
		writer.writeText("g.select(\"text\").transition().duration(duration).attr(\"transform\", function(d) { return \"translate(\" + (w - 60) + \",\" + (h / 4 - 20) + \")\"; })\n", null);
		writer.writeText("\t.attr(\"dy\", \"0em\");\n\n", null);
		
		writer.writeText("\tg.each(function(d) {\n\t\ty.domain([0, d.maxPrice]);\n\t\td3.select(this).selectAll(\".area\")\n", null);
		writer.writeText("\t\t\t.data(d3.range(3)).enter().insert(\"svg:path\", \".line\").attr(\"class\", \"area\")\n", null);
		writer.writeText("\t\t\t.attr(\"transform\", function(d) { return \"translate(0,\" + (d * (h / 4 - 20)) + \")\"; })\n", null);
		writer.writeText("\t\t\t.attr(\"d\", area(d.values)).style(\"fill\", function(d, i) { return color(i); }).style(\"fill-opacity\", 1e-6);\n", null);
		
		writer.writeText("\t\ty.domain([0, d.maxPrice / 3]);\n\n", null);
		
		writer.writeText("\t\td3.select(this).selectAll(\".line\").transition().duration(duration).attr(\"d\", line(d.values)).style(\"stroke-opacity\", 1e-6);\n\n", null);

		writer.writeText("\t\td3.select(this).selectAll(\".area\").transition().duration(duration)\n", null);
		writer.writeText("\t\t\t.style(\"fill-opacity\", 1).attr(\"d\", area(d.values)).each(\"end\", function() { d3.select(this).style(\"fill-opacity\", null); });\n", null);
		writer.writeText("\t});\n\tsetTimeout(areas, duration + delay);\n}\n\n", null);
		
		writer.writeText("function areas() {\n\tvar g = svg.selectAll(\".symbol\");\n\taxis.y(h / 4 - 21);\n", null);
		writer.writeText("\tg.select(\".line\").attr(\"d\", function(d) { return axis(d.values); });\n", null);
		writer.writeText("\tg.each(function(d) {\n\t\ty.domain([0, d.maxPrice]);\n", null);
		writer.writeText("\t\td3.select(this).select(\".line\").transition().duration(duration).style(\"stroke-opacity\", 1)\n", null);
		writer.writeText("\t\t\t.each(\"end\", function() { d3.select(this).style(\"stroke-opacity\", null); });\n\n", null);
		
		writer.writeText("\t\td3.select(this).selectAll(\".area\").filter(function(d, i) { return i; }).transition()\n", null);
		writer.writeText("\t\t\t.duration(duration).style(\"fill-opacity\", 1e-6).attr(\"d\", area(d.values)).remove();\n", null);
		
		writer.writeText("\t\td3.select(this).selectAll(\".area\").filter(function(d, i) { return !i; }).transition()\n", null);
		writer.writeText("\t\t.duration(duration).style(\"fill\", color(d.key)).attr(\"d\", area(d.values));\n\t});\n", null);
		
		writer.writeText("\tsvg.select(\"defs\").transition().duration(duration).remove();\n", null);
		
		writer.writeText("\tg.transition().duration(duration).each(\"end\", function() { d3.select(this).attr(\"clip-path\", null); });\n", null);
		writer.writeText("\tsetTimeout(stackedArea, duration + delay);\n}\n\n", null);
		
		writer.writeText("function stackedArea() {\tvar stack = d3.layout.stack().values(function(d) { return d.values; }).x(function(d) { return d.date; })\n", null);
		writer.writeText("\t\t.y(function(d) { return d.price; }).out(function(d, y0, y) { d.price0 = y0; }).order(\"reverse\");\n", null);
		writer.writeText("\tstack(symbols);\n\ty.domain([0, d3.max(symbols[0].values.map(function(d) { return d.price + d.price0; }))]).range([h, 0]);\n", null);
		
		writer.writeText("\tline.y(function(d) { return y(d.price0); });\n\tarea.y0(function(d) { return y(d.price0); }).y1(function(d) { return y(d.price0 + d.price); });\n", null);
		writer.writeText("\tvar t = svg.selectAll(\".symbol\").transition().duration(duration)\n", null);
		writer.writeText("\t\t.attr(\"transform\", \"translate(0,0)\").each(\"end\", function() { d3.select(this).attr(\"transform\", null); });\n", null);
		
		writer.writeText("\tt.select(\"path.area\").attr(\"d\", function(d) { return area(d.values); });\n", null);
		
		writer.writeText("\tt.select(\"path.line\").style(\"stroke-opacity\", function(d, i) { return i < 3 ? 1e-6 : 1; })\n", null);
		writer.writeText("\t\t.attr(\"d\", function(d) { return line(d.values); });\n\n", null);
		
		writer.writeText("\tt.select(\"text\").attr(\"transform\", function(d) { d = d.values[d.values.length - 1]; return \"translate(\" + (w - 60) + \",\" + y(d.price / 2 + d.price0) + \")\"; });\n", null);
		writer.writeText("\tsetTimeout(streamgraph, duration + delay);\n}\n\n", null);
		
		writer.writeText("function streamgraph() {\n\tvar stack = d3.layout.stack().values(function(d) { return d.values; })\n", null);
		writer.writeText("\t.x(function(d) { return d.date; }).y(function(d) { return d.price; })\n", null);
		writer.writeText("\t\t.out(function(d, y0, y) { d.price0 = y0; }).order(\"reverse\").offset(\"wiggle\");\n", null);
		writer.writeText("\tstack(symbols);\n\tline.y(function(d) { return y(d.price0); });\n", null);
		writer.writeText("\tvar t = svg.selectAll(\".symbol\").transition().duration(duration);\n", null);
		writer.writeText("\tt.select(\"path.area\").attr(\"d\", function(d) { return area(d.values); });\n", null);
		writer.writeText("\tt.select(\"path.line\").style(\"stroke-opacity\", 1e-6).attr(\"d\", function(d) { return line(d.values); });\n", null);
		writer.writeText("\tt.select(\"text\").attr(\"transform\", function(d) { d = d.values[d.values.length - 1]; return \"translate(\" + (w - 60) + \",\" + y(d.price / 2 + d.price0) + \")\"; });\n", null);
		writer.writeText("\tsetTimeout(overlappingArea, duration + delay);\n}\n\n", null);
		
		writer.writeText("function overlappingArea() {\n\tvar g = svg.selectAll(\".symbol\");\n\tline.y(function(d) { return y(d.price0 + d.price); });\n", null);
		writer.writeText("\tg.select(\".line\").attr(\"d\", function(d) { return line(d.values); });\n", null);
		writer.writeText("\ty.domain([0, d3.max(symbols.map(function(d) { return d.maxPrice; }))]).range([h, 0]);\n", null);
		writer.writeText("\tarea.y0(h).y1(function(d) { return y(d.price); });\n\tline.y(function(d) { return y(d.price); });\n\n", null);
		writer.writeText("\tvar t = g.transition().duration(duration);\n\n", null);
		
		writer.writeText("\tt.select(\".line\").style(\"stroke-opacity\", 1).attr(\"d\", function(d) { return line(d.values); });\n", null);
		writer.writeText("\tt.select(\".area\").style(\"fill-opacity\", .5).attr(\"d\", function(d) { return area(d.values); });\n", null);
		writer.writeText("\tt.select(\"text\").attr(\"dy\", \".31em\").attr(\"transform\", function(d) { d = d.values[d.values.length - 1]; return \"translate(\" + (w - 60) + \",\" + y(d.price) + \")\"; });\n", null);
		writer.writeText("\tsvg.append(\"svg:line\").attr(\"class\", \"line\").attr(\"x1\", 0).attr(\"x2\", w - 60).attr(\"y1\", h)\n", null);
		writer.writeText("\t\t.attr(\"y2\", h).style(\"stroke-opacity\", 1e-6).transition().duration(duration).style(\"stroke-opacity\", 1);\n\n", null);
		writer.writeText("\tsetTimeout(groupedBar, duration + delay);\n}\n\n", null);
		
		writer.writeText("function groupedBar() {\n\tx = d3.scale.ordinal().domain(symbols[0].values.map(function(d) { return d.date; }))", null);
		writer.writeText(".rangeBands([0, w - 60], .1);\n", null);
		writer.writeText("\tvar x1 = d3.scale.ordinal().domain(symbols.map(function(d) { return d.key; })).rangeBands([0, x.rangeBand()]);\n", null);
		writer.writeText("\tvar g = svg.selectAll(\".symbol\");\n", null);
		writer.writeText("\tvar t = g.transition().duration(duration);\n", null);
		writer.writeText("\tt.select(\".line\").style(\"stroke-opacity\", 1e-6).remove();\n\n", null);
		
		writer.writeText("\tt.select(\".area\").style(\"fill-opacity\", 1e-6).remove();\n", null);
		
		writer.writeText("\tg.each(function(p, j) {\n\t\t d3.select(this).selectAll(\"rect\").data(function(d) { return d.values; })\n", null);
		writer.writeText("\t\t.enter().append(\"svg:rect\").attr(\"x\", function(d) { return x(d.date) + x1(p.key); })\n", null);
		writer.writeText("\t\t.attr(\"y\", function(d) { return y(d.price); }).attr(\"width\", x1.rangeBand())\n", null);
		writer.writeText("\t\t\t.attr(\"height\", function(d) { return h - y(d.price); }).style(\"fill\", color(p.key))\n", null);
		writer.writeText("\t\t\t.style(\"fill-opacity\", 1e-6).transition().duration(duration).style(\"fill-opacity\", 1);\n", null);
		writer.writeText("\t});\n\tsetTimeout(stackedBar, duration + delay);\n}\n\n", null);
		
		writer.writeText("function stackedBar() {\n\tx.rangeRoundBands([0, w - 60], .1);\n\tvar stack = d3.layout.stack()\n", null);
		writer.writeText("\t\t.values(function(d) { return d.values; }).x(function(d) { return d.date; })\n", null);
		writer.writeText("\t\t.y(function(d) { return d.price; }).out(function(d, y0, y) { d.price0 = y0; }).order(\"reverse\");\n", null);
		writer.writeText("\tvar g = svg.selectAll(\".symbol\");\n\tstack(symbols);\n", null);
		writer.writeText("\ty.domain([0, d3.max(symbols[0].values.map(function(d) { return d.price + d.price0; }))]).range([h, 0]);\n", null);
		writer.writeText("\tvar t = g.transition().duration(duration / 2);\n", null);
		
		writer.writeText("\tt.select(\"text\").delay(symbols[0].values.length * 10)\n", null);
		writer.writeText("\t\t.attr(\"transform\", function(d) { d = d.values[d.values.length - 1]; return \"translate(\" + (w - 60) + \",\" + y(d.price / 2 + d.price0) + \")\"; });\n", null);
		
		writer.writeText("\tt.selectAll(\"rect\").delay(function(d, i) { return i * 10; }).attr(\"y\", function(d) { return y(d.price0 + d.price); })\n", null);
		writer.writeText("\t.attr(\"height\", function(d) { return h - y(d.price); }).each(\"end\", function() {\n\t\t", null);
		writer.writeText("d3.select(this).style(\"stroke\", \"#fff\").style(\"stroke-opacity\", 1e-6).transition()\n", null);
		writer.writeText("\t\t\t.duration(duration / 2).attr(\"x\", function(d) { return x(d.date); }).attr(\"width\", x.rangeBand())\n", null);
		writer.writeText("\t\t\t.style(\"stroke-opacity\", 1);\n\t});\n", null);
		writer.writeText("\tsetTimeout(transposeBar, duration + symbols[0].values.length * 10 + delay);\n}\n\n", null);
		
		writer.writeText("function transposeBar() {\n\tx.domain(symbols.map(function(d) { return d.key; })).rangeRoundBands([0, w], .2);\n", null);
		writer.writeText("\ty.domain([0, d3.max(symbols.map(function(d) { return d3.sum(d.values.map(function(d) { return d.price; })); }))]);\n", null);
		writer.writeText("\tvar stack = d3.layout.stack().x(function(d, i) { return i; }).y(function(d) { return d.price; })\n", null);
		writer.writeText("\t\t.out(function(d, y0, y) { d.price0 = y0; });\n", null);
		
		writer.writeText("\tstack(d3.zip.apply(null, symbols.map(function(d) { return d.values; })));\n", null);
		writer.writeText("\tvar g = svg.selectAll(\".symbol\");\n\tvar t = g.transition().duration(duration / 2);\n", null);
		writer.writeText("\tt.selectAll(\"rect\").delay(function(d, i) { return i * 10; }).attr(\"y\", function(d) { return y(d.price0 + d.price) - 1; })\n", null);
		writer.writeText("\t\t.attr(\"height\", function(d) { return h - y(d.price) + 1; }).attr(\"x\", function(d) { return x(d.symbol); })\n", null);
		writer.writeText("\t\t.attr(\"width\", x.rangeBand()).style(\"stroke-opacity\", 1e-6);\n", null);
		
		writer.writeText("\tt.select(\"text\").attr(\"x\", 0).attr(\"transform\", function(d) { return \"translate(\" + (x(d.key) + x.rangeBand() / 2) + \",\" + h + \")\"; })\n", null);
		writer.writeText("\t\t.attr(\"dy\", \"1.31em\").each(\"end\", function() { d3.select(this).attr(\"x\", null).attr(\"text-anchor\", \"middle\"); });\n", null);
		writer.writeText("\tsvg.select(\"line\").transition().duration(duration).attr(\"x2\", w);\n", null);
		writer.writeText("\tsetTimeout(donut,  duration / 2 + symbols[0].values.length * 10 + delay);\n}\n\n", null);
		
		writer.writeText("function donut() {\n\tvar g = svg.selectAll(\".symbol\");\n\tg.selectAll(\"rect\").remove();\n", null);
		writer.writeText("\tvar pie = d3.layout.pie().value(function(d) { return d.sumPrice; });\n", null);
		writer.writeText("\tvar arc = d3.svg.arc();\n", null);
		writer.writeText("\tg.append(\"svg:path\").style(\"fill\", function(d) { return color(d.key); }).data(function() { return pie(symbols); })\n", null);
		writer.writeText("\t\t.transition().duration(duration).tween(\"arc\", arcTween);\n", null);
		writer.writeText("\tg.select(\"text\").transition().duration(duration).attr(\"dy\", \".31em\");\n", null);
		writer.writeText("\tsvg.select(\"line\").transition().duration(duration).attr(\"y1\", 2 * h).attr(\"y2\", 2 * h).remove();\n", null);
		
		writer.writeText("\tfunction arcTween(d) {\n\t\tvar path = d3.select(this), text = d3.select(this.parentNode.appendChild(this.previousSibling)),\n", null);
		writer.writeText("\t\t\tx0 = x(d.data.key),y0 = h - y(d.data.sumPrice);\n", null);
		writer.writeText("\t\treturn function(t) {\n\t\t\tvar r = h / 2 / Math.min(1, t + 1e-3),a = Math.cos(t * Math.PI / 2),\n\t\t\t\t", null);
		writer.writeText("xx = (-r + (a) * (x0 + x.rangeBand()) + (1 - a) * (w + h) / 2), yy = ((a) * h + (1 - a) * h / 2),\n", null);
		writer.writeText("\t\t\t\tf = { innerRadius: r - x.rangeBand() / (2 - a), outerRadius: r, startAngle: a * (Math.PI / 2 - y0 / r) + (1 - a) * d.startAngle,", null);
		writer.writeText("endAngle: a * (Math.PI / 2) + (1 - a) * d.endAngle };\n", null);
		
		writer.writeText("\t\t\tpath.attr(\"transform\", \"translate(\" + xx + \",\" + yy + \")\");\n", null);
		writer.writeText("\t\t\tpath.attr(\"d\", arc(f));\n", null);
		writer.writeText("\t\t\t text.attr(\"transform\", \"translate(\" + arc.centroid(f) + \")translate(\" + xx + \",\" + yy + \")rotate(\" + ((f.startAngle + f.endAngle) / 2 + 3 * Math.PI / 2) * 180 / Math.PI + \")\");\n", null);
		writer.writeText("\t\t};\n\t}\n\tsetTimeout(donutExplode, duration + delay);\n}\n\n", null);
		
		writer.writeText("function donutExplode() {\n\tvar r0a = h / 2 - x.rangeBand() / 2, r1a = h / 2, r0b = 2 * h - x.rangeBand() / 2,\n", null);
		writer.writeText("\n\t\tr1b = 2 * h, arc = d3.svg.arc();\n", null);
		writer.writeText("\tsvg.selectAll(\".symbol path\").each(transitionExplode);\n", null);
		writer.writeText("\tfunction transitionExplode(d, i) {\n\t\t", null);
		writer.writeText("d.innerRadius = r0a; d.outerRadius = r1a;\n\t\td3.select(this).transition().duration(duration / 2)\n", null);
		writer.writeText("\t\t\t.tween(\"arc\", tweenArc({ innerRadius: r0b, outerRadius: r1b }));\n", null);
		writer.writeText("\t}\n\n", null);
		
		writer.writeText("function tweenArc(b) {\n\treturn function(a) {\n\t\tvar path = d3.select(this),\n", null);
		writer.writeText("\n\t\t\ttext = d3.select(this.nextSibling), i = d3.interpolate(a, b);\n", null);
		writer.writeText("\t\tfor (var key in b) a[key] = b[key];\n", null);
		writer.writeText("\t\treturn function(t) {\n\t\t\tvar a = i(t); path.attr(\"d\", arc(a));\n", null);
		writer.writeText("\t\t\ttext.attr(\"transform\", \"translate(\" + arc.centroid(a) + \")translate(\" + w / 2 + \",\" + h / 2 +\")rotate(\" + ((a.startAngle + a.endAngle) / 2 + 3 * Math.PI / 2) * 180 / Math.PI + \")\");\n", null);
		writer.writeText("\t\t\t};\n\t\t}\n\t}\n", null);
		writer.writeText("\tsetTimeout(function() {\n\t\tsvg.selectAll(\"*\").remove();\n", null);
		writer.writeText("\t\tsvg.selectAll(\"g\").data(symbols).enter().append(\"svg:g\").attr(\"class\", \"symbol\");\n", null);
		writer.writeText("\t\tlines();\n\t}, duration);\n}\n\n", null);
		
		writer.endElement("script");
	}
}