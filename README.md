## Overview

VisualFaces is a JSF tag library that can be included to draw diagrams in any Java Server Faces based web application.
All visualizations are based on [D3.js](http://d3js.org/), a free Java Script SVG library. VisualFaces not only serves as a growing library
but also as an example for using my [JSF component tutorial](http://www.jofre.de/?p=874).

## License

VisualFaces is published under the MIT [license](LICENSE.md). Additionally, the word cloud requires a separate [license](WORDCLOUDLICENSE.md).
Whenever you use VisualFaces in a commercial or non commercial project I'd be happy to be contacted via my blog and named in a kind of credits file.

## Features

	* Bubble Chart
	* Calendar Chart
	* Word Cloud
	* Flare Chart
	* Hierarchy Bar
	* Collapsible Tree
	* Zoomable Treemap
	* Chord Chart
	* Sunburst Chart
	* Parallel Set
	* Transition Diagram
	* Collapsible Intended Treemap
	* Choropleth
	* 3D Globe

## Usage

	1. Include lib/VisualFaces.jar in your JSF 2 project (WEB-INF/lib)
	2. Add the namespace xmlns:j="http://www.jofre.de/visualfaces" in your xhtml file 
	3. Use code completion to see the tags for the charts included in the tag library, 
           e.g. <j:bubblechart width="600" height="600" />
	4. Specify the input via the input tag. You can either use Strings or URLs to files containing a txt file.
           If you do not use the enter tag, the library picks a dummy input from META-INF\resources\dummydata.
		   
## Planned improvements

	* Some components are difficult to align on an HTML page. This is because of some troubles I had during positioning the SVG structures.
	* The Choropleth is only enabled to show maps of Germany. If there is anyone having topojson files for any other country I'd be happe to get them.

## Screenshots

![BubbleChart](/screenshots/BubbleChart.png "BubbleChart")
![CalendarChart](/screenshots/CalendarChart.png "CalendarChart")
![CollapsibleTreemap](/screenshots/CollapsibleTreemap.png "CollapsibleTreemap")
![FlareChart](/screenshots/FlareChart.png "FlareChart")
![HierarchyBar](/screenshots/HierarchyBar.png "HierarchyBar")
![WordCloud](/screenshots/WordCloud.png "WordCloud")
![ZoomableTreemap](/screenshots/ZoomableTreemap.png "ZoomableTreemap")
![ChordChart](/screenshots/ChordChart.png "ChordChart")
![ParallelSet](/screenshots/ParallelSet.png "ParallelSet")
![SunburstChart](/screenshots/SunburstChart.png "SunburstChart")
![TransitionDiagram](/screenshots/TransitionDiagram.png "TransitionDiagram")
![CollapsibleIntendedTree](/screenshots/CollapsibleIntendedTree.png "CollapsibleIntendedTree")
![Choropleth](/screenshots/Choropleth.png "Choropleth")
![3DGlobe](/screenshots/3DGlobe.png "3DGlobe")
