package de.jofre.visual.datatypes;

import java.util.List;

public class BubbleChartData {

	private String name;
	private int size;
	
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	private List<BubbleChartData> children;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<BubbleChartData> getChildren() {
		return children;
	}
	public void setChildren(List<BubbleChartData> children) {
		this.children = children;
	}
	
	public String toString(StringBuilder stringBuilder) {
		StringBuilder sb = null;
		
		if (stringBuilder == null) {
			sb = new StringBuilder();
		} else {
			sb = stringBuilder;
		}
		sb.append("{").append("\n").append("\"name\": \"").append(this.getName()).append("\",\n");
		if (this.getChildren() != null) {
			if (this.getChildren().size() > 0) {
				sb.append("\"children\": [\n");
				
				for(int i=0; i<this.getChildren().size(); i++) {
					sb.append(this.getChildren().get(i).toString(sb));
					if (i<this.getChildren().size()-1) {
						sb.append(",\n");
					}
				}
				sb.append("]\n");
			} else {
				sb.append("\"size\" : ").append(this.getSize()).append("\n");
			}
		}
		sb.append("}");
		return sb.toString();
	}
}
