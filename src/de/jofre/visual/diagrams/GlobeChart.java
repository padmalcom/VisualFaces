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

@FacesComponent("GlobeComponent")
public class GlobeChart extends UIComponentBase {

	private static Logger logger = Logger.getLogger(GlobeChart.class
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
		
		// Component specific attributes
		int nStartYear = (getAttributes().get("startYear") != null) ? Integer.parseInt(getAttributes().get("startYear").toString()) : 2000;
		int nEndYear = (getAttributes().get("endYear") != null) ? Integer.parseInt(getAttributes().get("endYear").toString()) : 2013;
		
		if (nEndYear < nStartYear) {
			logger.log(Level.SEVERE, "End year is smaller than start year!");
			return;
		}
		
		// Construct year string
		StringBuilder strYears = new StringBuilder();
		for(int i=nStartYear; i<=nEndYear; i++) {
			strYears.append("'"+i+"'");
			if (i < nEndYear) strYears.append(",");
		}
		
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
			strInput = UrlHelper.urlRequest(UrlHelper.getAbsoluteApplicationUrl(context)+ "/faces/dummydata/globe3d.json");
		}
		logger.log(Level.INFO, "Creating Globe3D from '"+strInput+"'. Size: "+nWidth+"x"+nHeight);
		
		// Create unique id for image tag
		Random rand = new Random(1234);
		String strImageTagID = "id" + String.valueOf(rand.nextInt(100000));
		
		// Include JavaScript libs
		IncludeResource.includeJavaScript(context, "../js/ThreeWebGL.js");
		IncludeResource.includeJavaScript(context, "../js/ThreeExtras.js");
		IncludeResource.includeJavaScript(context, "../js/RequestAnimationFrame.js");
		IncludeResource.includeJavaScript(context, "../js/Detector.js");
		IncludeResource.includeJavaScript(context, "../js/Tween.js");
		IncludeResource.includeJavaScript(context, "../js/globe.js");
		
		IncludeResource.includeCss(context, "../css/globe.css");	
		
		// Get response writer
		ResponseWriter writer = context.getResponseWriter();
		
		// Create year table
		writer.startElement("div", this);
		writer.writeAttribute("id", "currentInfo", null);
		for(int i=nStartYear; i<=nEndYear; i++) {
			writer.startElement("span", this);
			writer.writeAttribute("id", "year"+i, null);
			writer.writeAttribute("class", "year", null);
			writer.endElement("span");
		}
		writer.endElement("div");
		
		// Set image Tag
		writer.startElement("div", this);
		writer.writeAttribute("id", strImageTagID, null);
		writer.endElement("div");		
		
		// Draw diagram
		writer.startElement("script", this);
		writer.writeAttribute("type", "text/javascript", null);
		
		// Helper function
		writer.writeText("\tfunction FireEvent( ElementId, EventName )\n\t{\n", null);
		writer.writeText("\t\tif( document.getElementById(ElementId) != null )\n\t\t{\n", null);
		writer.writeText("\t\t\tif( document.getElementById( ElementId ).fireEvent )\n\t\t\t{\n", null);
		writer.writeText("\t\t\t\tdocument.getElementById( ElementId ).fireEvent( 'on' + EventName );\n\t\t\t}\n", null);
		writer.writeText("\t\t\telse\n\t\t\t{\n", null);
		writer.writeText("\t\t\t\tvar evObj = document.createEvent( 'Events' );\n", null);
		writer.writeText("\t\t\t\tevObj.initEvent( EventName, true, false );\n", null);
		writer.writeText("\t\t\t\tdocument.getElementById( ElementId ).dispatchEvent( evObj );\n", null);
		writer.writeText("\t\t\t}\n\t\t}\n\t}\n\n", null);
		
		// Check if WebGL is supported
		writer.writeText("\tif(!Detector.webgl){\n\t\tDetector.addGetWebGLMessage();\n\t} else {\n", null);
		
		writer.writeText("\t\tvar years = ["+strYears+"];\n", null);
		writer.writeText("\t\tvar container = document.getElementById('"+strImageTagID+"');\n", null);
		writer.writeText("\t\tvar globe = new DAT.Globe(container);\n\t\tvar i, j, tweens = [];\n", null);
		
		writer.writeText("\t\tvar settime = function(globe, t) {\n\t\t\treturn function() {\n", null);
		writer.writeText("\t\t\t\tnew TWEEN.Tween(globe).to({time: t/years.length},500).easing(TWEEN.Easing.Cubic.EaseOut).start();\n", null);
		writer.writeText("\t\t\t\tvar y = document.getElementById('year'+years[t]);\n", null);
		writer.writeText("\t\t\t\tif (y.getAttribute('class') === 'year active') { return; }\n", null);
		writer.writeText("\t\t\t\tvar yy = document.getElementsByClassName('year');\n", null);
		writer.writeText("\t\t\t\tfor(i=0; i<yy.length; i++) {\n\t\t\t\t\tyy[i].setAttribute('class','year');\n\t\t\t\t}\n", null);
		writer.writeText("\t\t\ty.setAttribute('class', 'year active');\n\t\t};\n\t};\n\n", null);
		

		
		writer.writeText("\tfor(var i = 0; i<years.length; i++) {\n", null);
		writer.writeText("\t\tvar y = document.getElementById('year'+years[i]);\n", null);
		writer.writeText("\t\ty.addEventListener('mouseover', settime(globe,i), false);\n", null);
		writer.writeText("\t}\n\n", null);
		
		writer.writeText("\tTWEEN.start();\n\n", null);
		
		writer.writeText("\tvar data = JSON.parse('"+strInput+"');\n", null);
		writer.writeText("\twindow.data = data;\n", null);
		writer.writeText("\tvar maxSize = 0;\n\tvar minSize = 10000000;\n", null);
		writer.writeText("\tfor (i = 0; i < data.length; i ++) {\n\t\tvar d2 = data[i][1];\n", null);
		writer.writeText("\t\tfor(j=2; j<(d2).length; j +=3) {\n\t\t\tif (d2[j] > maxSize) {\n", null);
		writer.writeText("\t\t\t\tmaxSize = d2[j];\n", null);
		writer.writeText("\t\t\t}\n\t\t\tif (d2[j] < minSize) {\n\t\t\t\tminSize = d2[j];\n\t\t\t}\n\t\t}\n\t}\n", null);
		
		writer.writeText("\tfor (i = 0; i < data.length; i ++) {\n\t\tvar d2 = data[i][1];\n", null);
		writer.writeText("\t\tfor(j=2; j<(d2).length; j +=3) {\n\t\t\td2[j] = d2[j] / (maxSize - minSize) / 100;\n", null);
		writer.writeText("\t\t}\n\t}\n\n", null);
		
		writer.writeText("\tfor (i=0;i<data.length;i++) {\n", null);
		writer.writeText("\t\tglobe.addData(data[i][1], {format: 'magnitude', name: data[i][0], animated: true});\n", null);
		writer.writeText("\t}\n\tglobe.createPoints();\n\tFireEvent('year'+years[0], 'mouseover');\n\tglobe.animate();\n\t}\n\n", null);		

		writer.endElement("script");
	}
}