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

@FacesComponent("ChordChartComponent")
public class ChordChart extends UIComponentBase {

	private static Logger logger = Logger.getLogger(ChordChart.class
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
		String strInputMatrix = null;
		
		System.out.println("Width: "+getAttributes().get("width") );
		System.out.println("Height: "+getAttributes().get("height") );
		
		// Get input as Data
		strInput = (getAttributes().get("input") != null) ? (String)getAttributes().get("input") : null;
		System.out.println("Input: "+getAttributes().get("input"));
		// Get input matrix
		strInputMatrix = (getAttributes().get("inputmatrix") != null) ? (String)getAttributes().get("inputmatrix") : null;
		System.out.println("Input matrix: "+getAttributes().get("inputmatrix"));
		
		// Is input data a URL?
		if (strInput != null) {
			if (strInput.startsWith("http")) {
				strInput = UrlHelper.urlRequest(strInput);
			}
		}
		// No input found? Take dummy data
		else {
			strInput = UrlHelper.urlRequest(UrlHelper.getAbsoluteApplicationUrl(context)+ "/faces/dummydata/chordchart.json");
		}
		
		// Is input matrix a URL?
		if (strInputMatrix != null) {
			if (strInputMatrix.startsWith("http")) {
				strInputMatrix = UrlHelper.urlRequest(strInputMatrix);
			}
		}
		// No input found? Take dummy data
		else {
			strInputMatrix = UrlHelper.urlRequest(UrlHelper.getAbsoluteApplicationUrl(context)+ "/faces/dummydata/chordmatrix.json");
		}		
		logger.log(Level.INFO, "Creating chordchart from '"+strInput+"' and '"+strInputMatrix+"'. Size: "+nWidth+"x"+nHeight);
		
		// Create unique id for image tag
		Random rand = new Random(1239);
		String strImageTagID = "id" + String.valueOf(rand.nextInt(100000));
		
		// Include D3.js
		IncludeResource.includeJavaScript(context, "../js/d3.v3.min.js");
		
		// Include chordchart css
		IncludeResource.includeCss(context, "../css/chordchart.css");		
		
		// Get response writer
		ResponseWriter writer = context.getResponseWriter();
		
		// Set image Tag
		writer.startElement("div", this);
		writer.writeAttribute("id", strImageTagID, null);
		writer.endElement("div");
		
		// Draw diagram
		writer.startElement("script", this);
		writer.writeAttribute("type", "text/javascript", null);
		
		writer.writeText("var width = "+nWidth+", height = "+nHeight+", outerRadius = Math.min(width, height) / 2 - 10, innerRadius = outerRadius - 24;\n\n", null);
		writer.writeText("var formatPercent = d3.format(\".1%\"); var arc = d3.svg.arc().innerRadius(innerRadius).outerRadius(outerRadius);\n\n", null);

		writer.writeText("var layout = d3.layout.chord().padding(.04).sortSubgroups(d3.descending).sortChords(d3.ascending);\n", null);
		writer.writeText("var path = d3.svg.chord().radius(innerRadius);\n", null);
		writer.writeText("var svg = d3.select(\"#"+strImageTagID+"\").append(\"svg\").attr(\"width\", width).attr(\"height\", height)\n", null);
		writer.writeText("\t.append(\"g\").attr(\"id\", \"circle\").attr(\"transform\", \"translate(\" + width / 2 + \",\" + height / 2 + \")\");\n\n", null);
		
		writer.writeText("svg.append(\"circle\").attr(\"r\", outerRadius);\n", null);
		
		// [ {"Year":"1985", "Month":"12"}, { ... } ]
		writer.writeText("var cities = JSON.parse('"+strInput+"');\n\n", null);
		writer.writeText("var matrix = JSON.parse('"+strInputMatrix+"');\n\n", null);
		
		
			//d3.csv(\"cities.csv\", function(cities) {
			  //d3.json(\"matrix.json\", function(matrix) {

		writer.writeText("layout.matrix(matrix);\n", null);
		writer.writeText("var group = svg.selectAll(\".group\").data(layout.groups).enter().append(\"g\")\n", null);
		writer.writeText("\t.attr(\"class\", \"group\").on(\"mouseover\", mouseover);\n\n", null);
		
		writer.writeText("group.append(\"title\").text(function(d, i) {\n", null);
		writer.writeText("\treturn cities[i].name + \": \" + formatPercent(d.value) + \" of origins\";\n", null);
		writer.writeText("});\n\n", null);
		
		writer.writeText("var groupPath = group.append(\"path\").attr(\"id\", function(d, i) { return \"group\" + i; })\n", null);
		writer.writeText("\t.attr(\"d\", arc).style(\"fill\", function(d, i) { return cities[i].color; });\n\n", null);
		
		writer.writeText("var groupText = group.append(\"text\").attr(\"x\", 6).attr(\"dy\", 15);\n\n", null);
		
		writer.writeText("groupText.append(\"textPath\").attr(\"xlink:href\", function(d, i) { return \"#group\" + i; })\n", null);
		writer.writeText("\t.text(function(d, i) { return cities[i].name; });\n\n", null);
		
		writer.writeText("groupText.filter(function(d, i) { return groupPath[0][i].getTotalLength() / 2 - 16 < this.getComputedTextLength(); }).remove();\n\n", null);
		
		writer.writeText("var chord = svg.selectAll(\".chord\").data(layout.chords).enter().append(\"path\").attr(\"class\", \"chord\")\n", null);
		writer.writeText("\t.style(\"fill\", function(d) { return cities[d.source.index].color; }).attr(\"d\", path);\n\n", null);
		
		writer.writeText("chord.append(\"title\").text(function(d) {\n", null);
		writer.writeText("\treturn cities[d.source.index].name + \" -> \" + cities[d.target.index].name\n", null);
		writer.writeText("\t+ \": \" + formatPercent(d.source.value) + \"\\n\" + cities[d.target.index].name\n", null);
		writer.writeText("\t+ \" -> \" + cities[d.source.index].name + \": \" + formatPercent(d.target.value);\n", null);
		writer.writeText("});\n\n", null);
		
		writer.writeText("function mouseover(d, i) { chord.classed(\"fade\", function(p) { return p.source.index != i && p.target.index != i; });\n}\n\n", null);
		
		writer.endElement("script");
	}
}
