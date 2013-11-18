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

@FacesComponent("CalendarChartComponent")
public class CalendarChart extends UIComponentBase {

	private static Logger logger = Logger.getLogger(CalendarChart.class
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
		int nWidth = (getAttributes().get("width") != null) ? Integer.parseInt(getAttributes().get("width").toString()) : 960;
		int nHeight = (getAttributes().get("height") != null) ? Integer.parseInt(getAttributes().get("height").toString()) : 200;
		int nCellSize = (getAttributes().get("cellsize") != null) ? Integer.parseInt(getAttributes().get("cellsize").toString()) : 17;
		int nYearStart = (getAttributes().get("yearstart") != null) ? Integer.parseInt(getAttributes().get("yearstart").toString()) : 2010;
		int nYearEnd = (getAttributes().get("yearend") != null) ? Integer.parseInt(getAttributes().get("yearend").toString()) : 2013;
		nYearEnd +=1;
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
			strInput = UrlHelper.urlRequest(UrlHelper.getAbsoluteApplicationUrl(context)+ "/faces/dummydata/calendarchart.json");
		}
		logger.log(Level.INFO, "Creating calendarchart from '"+strInput+"'. Size: "+nWidth+"x"+nHeight);
		
		// Create unique id for image tag
		Random rand = new Random(1235);
		String strImageTagID = "id" + String.valueOf(rand.nextInt(100000));
		
		// Include D3.js
		IncludeResource.includeJavaScript(context, "../js/d3.v3.min.js");
		
		// Include calendar css
		IncludeResource.includeCss(context, "../css/calendarchart.css");
		
		// Get response writer
		ResponseWriter writer = context.getResponseWriter();
		
		// Set image Tag
		writer.startElement("div", this);
		writer.writeAttribute("id", strImageTagID, null);
		writer.endElement("div");
		
		// Draw diagram
		writer.startElement("script", this);
		writer.writeAttribute("type", "text/javascript", null);
		
		writer.writeText("var width = "+nWidth+",\nheight = "+nHeight+",\ncellSize = "+nCellSize+";\n\n", null); 
		writer.writeText("var day = d3.time.format(\"%w\"),\nweek = d3.time.format(\"%U\"),\npercent = d3.format(\".1%\"),\nformat = d3.time.format(\"%Y-%m-%d\");\n\n", null);
		writer.writeText("var color = d3.scale.quantize().domain([-.05, .05]).range(d3.range(11).map(function(d) { return \"q\" + d + \"-11\"; }));\n\n", null);
		writer.writeText("var svg = d3.select(\"#"+strImageTagID+"\").selectAll(\"svg\").data(d3.range("+nYearStart+", "+nYearEnd+")).enter().append(\"svg\")", null);
		writer.writeText(".attr(\"width\", width).attr(\"height\", height).attr(\"class\", \"RdYlGn\").append(\"g\")", null);
		writer.writeText(".attr(\"transform\", \"translate(\" + ((width - cellSize * 53) / 2) + \",\" + (height - cellSize * 7 - 1) + \")\");\n\n", null);
		
		writer.writeText("svg.append(\"text\").attr(\"transform\", \"translate(-6,\" + cellSize * 3.5 + \")rotate(-90)\")", null);
		writer.writeText(".style(\"text-anchor\", \"middle\").text(function(d) { return d; });\n\n", null);
		
		writer.writeText("var rect = svg.selectAll(\".day\").data(function(d) { return d3.time.days(new Date(d, 0, 1), new Date(d + 1, 0, 1)); })", null);
		writer.writeText(".enter().append(\"rect\").attr(\"class\", \"day\").attr(\"width\", cellSize).attr(\"height\", cellSize)", null);
		writer.writeText(".attr(\"x\", function(d) { return week(d) * cellSize; }).attr(\"y\", function(d) { return day(d) * cellSize; }).datum(format);\n\n", null);

		writer.writeText("rect.append(\"title\").text(function(d) { return d; });\n\n", null);
		
		writer.writeText("svg.selectAll(\".month\").data(function(d) { return d3.time.months(new Date(d, 0, 1), new Date(d + 1, 0, 1)); })", null);
		writer.writeText(".enter().append(\"path\").attr(\"class\", \"month\").attr(\"d\", monthPath);\n\n", null);
		
		//Format:  [{"Date":"2010-10-01","Open":"10789.72","Close":"10829.68"},{"Date":"2010-09-30","Open":"10835.96","Close":"10788.05"}];
		writer.writeText("var csv ="+strInput+";\n\n", null);

		writer.writeText("var data = d3.nest().key(function(d) { return d.Date; }).rollup(function(d) { return (d[0].Close - d[0].Open) / d[0].Open; }).map(csv);\n\n", null);
		writer.writeText("rect.filter(function(d) { return d in data; }).attr(\"class\", function(d) { return \"day \" + color(data[d]); })", null);
		writer.writeText(".select(\"title\").text(function(d) { return d + \": \" + percent(data[d]); });\n\n", null);
		
		writer.writeText("function monthPath(t0) { var t1 = new Date(t0.getFullYear(), t0.getMonth() + 1, 0), d0 = +day(t0), w0 = +week(t0),", null);
		writer.writeText("d1 = +day(t1), w1 = +week(t1); return \"M\" + (w0 + 1) * cellSize + \",\" + d0 * cellSize + \"H\" + w0 * cellSize + \"V\" + 7 * cellSize", null);
		writer.writeText("+ \"H\" + w1 * cellSize + \"V\" + (d1 + 1) * cellSize + \"H\" + (w1 + 1) * cellSize + \"V\" + 0 + \"H\" + (w0 + 1) * cellSize + \"Z\"; }\n\n", null);
		
		// ToDo
		//writer.writeText("d3.select(self.frameElement).style(\"height\", \"2910px\");\n", null);
		writer.endElement("script");
	}
	
}
