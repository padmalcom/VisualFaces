package de.jofre.visual.datatypes;

import java.util.ArrayList;
import java.util.List;

public class CalendarChartData {

	class CalendarChartDataEntry {
		String date;
		double value;
		
		public CalendarChartDataEntry(String _date, double _value) {
			this.date = _date;
			this.value = _value;
		}
	}
	
	private List<CalendarChartDataEntry> values;
	
	public CalendarChartData() {
		values = new ArrayList<CalendarChartDataEntry>();
	}
	
	public void add(String date, double value) {
		values.add(new CalendarChartDataEntry(date, value));
	}
	
	public void add(CalendarChartDataEntry _entry) {
		values.add(_entry);
	}
	
	public void delete(int _index) {
		values.remove(_index);
	}
	
	public CalendarChartDataEntry get(int _index) {
		return values.get(_index);
	}
	
	public List<CalendarChartDataEntry> getList() {
		return values;
	}
	
	public void setList(List<CalendarChartDataEntry> _list) {
		values = _list;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[\n");
		for(int i=0; i<values.size(); i++) {
			sb.append("{\n").append("Date:\"").append(values.get(i).date).append("\",\n").append("Value:\"").append(values.get(i).value).append("\"\n").append("}");
			if (i<values.size()-1) {
				sb.append(",\n");
			} else {
				sb.append("\n");
			}
		}
		sb.append("]\n");
		
		
		return sb.toString();
	}
	
}
