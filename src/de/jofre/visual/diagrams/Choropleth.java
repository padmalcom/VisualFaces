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

@FacesComponent("ChoroplethComponent")
public class Choropleth extends UIComponentBase {

	private static Logger logger = Logger.getLogger(Choropleth.class
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

		// Include css
		IncludeResource.includeCss(context, "../css/choropleth.css");
		
		// Include D3.js
		IncludeResource.includeJavaScript(context, "../js/d3.v3.min.js");
		IncludeResource.includeJavaScript(context, "../js/topojson.v0.min.js");
		
		// Get JSF attributes
		int nWidth = (getAttributes().get("width") != null) ? Integer.parseInt(getAttributes().get("width").toString()) : 400;
		int nHeight = (getAttributes().get("height") != null) ? Integer.parseInt(getAttributes().get("height").toString()) : 400;
		
		// Get input as Data
		String strInput = null;
		strInput = (getAttributes().get("input") != null) ? (String)getAttributes().get("input") : null;
		// Is input data a URL?
		if (strInput != null) {
			if (strInput.startsWith("http")) {
				strInput = UrlHelper.urlRequest(strInput);
			}
		}
		// No input found? Take dummy data
		else {
			strInput = UrlHelper.urlRequest(UrlHelper.getAbsoluteApplicationUrl(context)+ "/faces/dummydata/choropleth.json");
		}
		logger.log(Level.INFO, "Creating Choropleth from '"+strInput+"'. Size: "+nWidth+"x"+nHeight);		
		
		// Get country
		String strCountry = null;
		strCountry = (getAttributes().get("country") != null) ? (String)getAttributes().get("country") : null;
		if (!strCountry.equals("DE")) {
			logger.log(Level.SEVERE, "Unknown country code!");
		}
		
		// Get min/max values
		int nMin = (getAttributes().get("min") != null) ? Integer.parseInt(getAttributes().get("min").toString()) : 0;
		int nMax = (getAttributes().get("max") != null) ? Integer.parseInt(getAttributes().get("max").toString()) : 0;
		
		// Map scale
		int nMapScale = (getAttributes().get("mapscale") != null) ? Integer.parseInt(getAttributes().get("mapscale").toString()) : 10000;
		
		// Label subunits
		boolean bLabelSubunits = (getAttributes().get("labelsubunits") != null) ? Boolean.parseBoolean(getAttributes().get("labelsubunits").toString()) : false;
		
		// Label capitals
		boolean bLabelCapitals = (getAttributes().get("labelcapitals") != null) ? Boolean.parseBoolean(getAttributes().get("labelcapitals").toString()) : false;
		
		// Create unique id for image tag
		Random rand = new Random(1234);
		String strImageTagID = "id" + String.valueOf(rand.nextInt(100000));
		
		// Get response writer
		ResponseWriter writer = context.getResponseWriter();
		
		// Set image Tag
		writer.startElement("div", this);
		writer.writeAttribute("id", strImageTagID, null);
		writer.endElement("div");
		
		// Draw diagram
		writer.startElement("script", this);
		writer.writeAttribute("type", "text/javascript", null);
				
		writer.writeText("var width = "+nWidth+", height = "+nHeight+";\n", null);
		writer.writeText("var path = d3.geo.path();\n", null);
		writer.writeText("var svg = d3.select(\"#"+strImageTagID+"\").append(\"svg\").attr(\"width\", width).attr(\"height\", height);\n", null);
		writer.writeText("var rateByName = d3.map();\n\n", null);

		writer.writeText("var minValue = 0, maxValue = 0, counter = 0;\n\n", null);
		
		writer.writeText("var jsonData = JSON.parse('"+strInput+"');\n", null);
		writer.writeText("for (var myKey in jsonData) {\n", null);
		writer.writeText("\tif (jsonData.hasOwnProperty(myKey)) {\n", null);
		writer.writeText("\t\t\trateByName.set(jsonData[myKey].id, jsonData[myKey].rate);\n", null);
		writer.writeText("\t\t\tif(counter == 0) { minValue = jsonData[myKey].rate; maxValue = jsonData[myKey].rate; counter = 1; } else {\n", null);
		writer.writeText("\t\t\t\tif(jsonData[myKey].rate < minValue) { minValue = jsonData[myKey].rate; }\n", null);
		writer.writeText("\t\t\t\tif(jsonData[myKey].rate > maxValue) { maxValue = jsonData[myKey].rate; }\n\t\t\t}\n", null);
		writer.writeText("\t}\n}\n\n", null);
		
		if (getAttributes().get("min") != null) {
			if ((getAttributes().get("max") != null)) {
				writer.writeText("var quantize = d3.scale.quantize().domain(["+nMin+", "+nMax+"]).range(d3.range(9).map(function(i) { return \"q\" + i + \"-9\"; }));\n\n", null);

			} else {
				writer.writeText("var quantize = d3.scale.quantize().domain(["+nMin+", maxValue]).range(d3.range(9).map(function(i) { return \"q\" + i + \"-9\"; }));\n\n", null);
			}
		}
		if ((getAttributes().get("min") == null) && (getAttributes().get("max") == null)) {
			writer.writeText("var quantize = d3.scale.quantize().domain([minValue, maxValue]).range(d3.range(9).map(function(i) { return \"q\" + i + \"-9\"; }));\n\n", null);
			writer.writeText("console.log(minValue);", null);
			writer.writeText("console.log(maxValue);", null);
		}
		
		
		if (strCountry.equals("DE")) {
			writer.writeText("d3.json(\"maps/de.json\", showData);\n\n", null);
		}
		
		writer.writeText("function showData(error, country) {\n", null);
		writer.writeText("\tvar subunits = topojson.object(country, country.objects.subunits);\n", null);
		writer.writeText("\tvar projection = d3.geo.mercator().center([10.5, 51.35]).scale("+nMapScale+").translate([width / 2, height / 2]);\n", null);
		writer.writeText("\tvar path = d3.geo.path().projection(projection).pointRadius(0);\n", null);
		writer.writeText("\tsvg.append(\"path\").datum(subunits).attr(\"d\", path);\n", null);
		writer.writeText("\tsvg.selectAll(\".subunit\").data(topojson.object(country, country.objects.subunits).geometries).enter().append(\"path\")\n", null);
		writer.writeText("\t\t.attr(\"class\", function(d) {\n", null);
		writer.writeText("\t\treturn quantize( rateByName.get(d.properties.abr));\n\t}).attr(\"d\", path);\n", null);
		writer.writeText("\tsvg.append(\"path\").datum(topojson.mesh(country, country.objects.subunits, function(a,b) { if (a!==b || a.properties.name === \"Berlin\"|| a.properties.name === \"Bremen\"){var ret = a;}return ret;}))\n", null);
		writer.writeText("\t\t.attr(\"d\", path).attr(\"class\", \"subunit-boundary\");\n\n", null);
		
		writer.writeText("\tsvg.append(\"path\").datum(topojson.object(country, country.objects.places)).attr(\"d\", path).attr(\"class\", \"place\");\n\n", null);
		
		if (bLabelCapitals) {
			writer.writeText("\tsvg.selectAll(\".place-label\").data(topojson.object(country, country.objects.places).geometries).enter().append(\"text\")\n", null);
			writer.writeText("\t\t.attr(\"class\", \"place-label\").attr(\"transform\", function(d) { return \"translate(\" + projection(d.coordinates) + \")\"; })\n", null);
			writer.writeText("\t\t.attr(\"dy\", \".35em\").text(function(d) { if (d.properties.name!==\"Berlin\"&&d.properties.name!==\"Bremen\"){return d.properties.name;} })\n", null);
			writer.writeText("\t\t.attr(\"x\", function(d) { return d.coordinates[0] > -1 ? 6 : -6; })\n", null);
			writer.writeText("\t\t.style(\"text-anchor\", function(d) { return d.coordinates[0] > -1 ? \"start\" : \"end\"; });\n\n", null);
		}
		
		if (bLabelSubunits) {
			writer.writeText("\tsvg.selectAll(\".subunit-label\").data(topojson.object(country, country.objects.subunits).geometries).enter().append(\"text\")\n", null);
			writer.writeText("\t\t.attr(\"class\", function(d) { return \"subunit-label \" + d.properties.name; })\n", null);
			writer.writeText("\t\t.attr(\"transform\", function(d) { return \"translate(\" + path.centroid(d) + \")\"; })\n", null);
			writer.writeText("\t\t.attr(\"dy\", function(d){\n", null);
			writer.writeText("\t\t\tif(d.properties.name===\"Sachsen\"||d.properties.name===\"Thüringen\"||d.properties.name===\"Sachsen-Anhalt\"||d.properties.name===\"Rheinland-Pfalz\")\n", null);
			writer.writeText("\t\t\t{\n\t\t\t\treturn \".9em\";\n\t\t\t}\n", null);
			writer.writeText("\t\t\telse if(d.properties.name===\"Brandenburg\"||d.properties.name===\"Hamburg\")\n", null);
			writer.writeText("\t\t\t{\n\t\t\t\treturn \"1.5em\";\n\t\t\t}\n", null);
			writer.writeText("\t\t\telse if(d.properties.name===\"Berlin\"||d.properties.name===\"Bremen\")\n", null);
			writer.writeText("\t\t\t{\n\t\t\t\treturn \"-1em\";}else{return \".35em\";}\n", null);
			writer.writeText("\t}).text(function(d) { return d.properties.name; });\n", null);
		}
		
		writer.writeText("}\n\n", null);
		writer.endElement("script");
	}
}