package de.jofre.visual.datatypes;

import java.util.ArrayList;
import java.util.List;

import de.jofre.visual.support.ColorGenerator;

public class ChordChartData {
	
	class ChordChartDataEntry {
		String name;
		List<ChordChartDataEntry> relations;
		
		public List<ChordChartDataEntry> getRelations() {
			return relations;
		}

		public void setRelations(List<ChordChartDataEntry> relations) {
			this.relations = relations;
		}

		public ChordChartDataEntry(String _name) {
			this.name = _name;
			this.relations = new ArrayList<ChordChartDataEntry>();
		}
	}
	
	private List<ChordChartDataEntry> values;
	
	public ChordChartData() {
		values = new ArrayList<ChordChartDataEntry>();
	}
	
	public void add(String name) {
		values.add(new ChordChartDataEntry(name));
	}
	
	public void add(ChordChartDataEntry _entry) {
		values.add(_entry);
	}
	
	public void delete(int _index) {
		values.remove(_index);
	}
	
	public ChordChartDataEntry get(int _index) {
		return values.get(_index);
	}
	
	public List<ChordChartDataEntry> getList() {
		return values;
	}
	
	public void setList(List<ChordChartDataEntry> _list) {
		values = _list;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(int i=0; i<values.size(); i++) {

			//sb.append("{\n").append("name:\"").append(values.get(i).name).append("\",\n").append("color:\"").append(values.get(i).color).append("\"\n").append("}");
			sb.append("{").append("\"name\":\"").append(values.get(i).name).append("\",").append("\"color\":\"").append(ColorGenerator.generateRandomHtmlColor()).append("\"").append("}");
			if (i<values.size()-1) {
				sb.append(",");
			} else {
				sb.append("");
			}
		}
		sb.append("]");
		
		System.out.println("Data: "+sb);
		
		return sb.toString();
	}
	
	public String getMatrix() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		
		// Iterate through all items and count relations
		for(int i=0; i<values.size(); i++) {
			
			ChordChartDataEntry ccde = values.get(i);
			
			sb.append("[");
			
			for(int k=0; k<values.size(); k++) {
				double sum = 0;
				for(int j=0; j<ccde.relations.size(); j++) {
					if (values.get(k).name.equals(ccde.relations.get(j).name)) {
						sum +=1;
					}
				}
				sb.append(sum / values.size());
				if (k != values.size()-1) {
					sb.append(",");
				}
			}
			sb.append("]");
			
			// Add a comma until we reached the last item
			if (i != values.size()-1)
				sb.append(",");
			
		}
		sb.append("]");
		
		System.out.println("Matrix: "+sb);
		
		return sb.toString();
	}
	
	public static ChordChartData dummyData() {
		ChordChartData ccd = new ChordChartData();
		ChordChartDataEntry c1 = ccd.new ChordChartDataEntry("Jonas");
		ChordChartDataEntry c2 = ccd.new ChordChartDataEntry("Hannes");
		ChordChartDataEntry c3 = ccd.new ChordChartDataEntry("Daniel");
		ChordChartDataEntry c4 = ccd.new ChordChartDataEntry("Peter");
		ChordChartDataEntry c5 = ccd.new ChordChartDataEntry("Ludwig");
		ChordChartDataEntry c6 = ccd.new ChordChartDataEntry("Mario");
		ChordChartDataEntry c7 = ccd.new ChordChartDataEntry("Luigi");
		ChordChartDataEntry c8 = ccd.new ChordChartDataEntry("Amin");
		ChordChartDataEntry c9 = ccd.new ChordChartDataEntry("Berta");
		ChordChartDataEntry c10 = ccd.new ChordChartDataEntry("Cäsar");
		ChordChartDataEntry c11 = ccd.new ChordChartDataEntry("David");
		ChordChartDataEntry c12 = ccd.new ChordChartDataEntry("Erwin");
		ChordChartDataEntry c13 = ccd.new ChordChartDataEntry("Friedrich");
		ChordChartDataEntry c14 = ccd.new ChordChartDataEntry("Gustl");
		ChordChartDataEntry c15 = ccd.new ChordChartDataEntry("Helmut");
		
		c1.relations.add(c2); c1.relations.add(c4); c1.relations.add(c6); c1.relations.add(c8); c1.relations.add(c10);
		c2.relations.add(c3); c2.relations.add(c5); c2.relations.add(c7); c2.relations.add(c9); c2.relations.add(c11);
		c3.relations.add(c4); c3.relations.add(c6); c3.relations.add(c8); c3.relations.add(c10); c3.relations.add(c12);
		c4.relations.add(c5); c4.relations.add(c7); c4.relations.add(c9); c4.relations.add(c11); c4.relations.add(c13);
		c5.relations.add(c6); c5.relations.add(c8); c5.relations.add(c10); c5.relations.add(c12); c5.relations.add(c14);
		c6.relations.add(c7); c6.relations.add(c9); c6.relations.add(c11); c6.relations.add(c13); c6.relations.add(c15);
		c7.relations.add(c8); c7.relations.add(c10); c7.relations.add(c12); c7.relations.add(c14); c7.relations.add(c1);
		c8.relations.add(c9); c8.relations.add(c11); c8.relations.add(c13); c8.relations.add(c15); c8.relations.add(c2);
		c9.relations.add(c10); c9.relations.add(c12); c9.relations.add(c14); c9.relations.add(c1); c9.relations.add(c3);
		c10.relations.add(c11); c10.relations.add(c13); c10.relations.add(c15); c10.relations.add(c2); c10.relations.add(c4);
		c11.relations.add(c12); c11.relations.add(c14); c11.relations.add(c1); c11.relations.add(c3); c11.relations.add(c5);
		c12.relations.add(c13); c12.relations.add(c15); c12.relations.add(c2); c12.relations.add(c4); c12.relations.add(c6);
		c13.relations.add(c14); c13.relations.add(c1); c13.relations.add(c3); c13.relations.add(c5); c13.relations.add(c7);
		c14.relations.add(c15); c14.relations.add(c2); c14.relations.add(c4); c14.relations.add(c6); c14.relations.add(c8);
		c15.relations.add(c1); c15.relations.add(c3); c15.relations.add(c5); c15.relations.add(c7); c15.relations.add(c9);
		
		ccd.add(c1);
		ccd.add(c2);
		ccd.add(c3);
		ccd.add(c4);
		ccd.add(c5);
		ccd.add(c6);
		ccd.add(c7);
		ccd.add(c8);
		ccd.add(c9);
		ccd.add(c10);
		ccd.add(c11);
		ccd.add(c12);
		ccd.add(c13);
		ccd.add(c14);
		ccd.add(c15);
		
		return ccd;
	}
	
}
