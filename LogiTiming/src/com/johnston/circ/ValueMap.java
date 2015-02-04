package com.johnston.circ;



import java.util.ArrayList;

import com.cburch.logisim.data.Value;
import com.johnston.timing.ValueChange;
import com.johnston.timing.ValueChangeList;

public class ValueMap {
	private ArrayList<ValueUpdate> valMap;
	private ValueUpdate mostRecentlyAdded = null;
	
	public ValueMap() {
		valMap = new ArrayList<ValueUpdate>();
	}
	
	public void addValueUpdate(Value v, double timestamp) {
		//timestamp = Double.parseDouble(String.format("%.3g%n", timestamp));
		//System.out.println("Rounded timestamp to " + timestamp);
		/*for(int i = 0; i < valMap.size(); i++) {
			timestamp+=valMap.get(i).getTimestamp();
		}*/
		if(valMap.size() > 0) {
			timestamp+= valMap.get(valMap.size()-1).getTimestamp();
		}
		ValueUpdate upd = new ValueUpdate(v, timestamp);
		this.mostRecentlyAdded = upd;
		valMap.add(upd);
		
	}
	
	public void addValueUpdateIgnorePrev(Value v, double timestamp) {
		//timestamp = Double.parseDouble(String.format("%.3g%n", timestamp));
		//System.out.println("Rounded timestamp to " + timestamp);
		ValueUpdate upd = new ValueUpdate(v, timestamp);
		this.mostRecentlyAdded = upd;
		valMap.add(upd);
	}
	
	public void createFrom(ValueChangeList changeList) {
		this.clear();
		for(ValueChange change: changeList.toArrayList()) {
			this.addValueUpdateIgnorePrev(change.getValue(), change.getTimeChanged());
		}
	}
	
	/**
	 * Subtracts the specified modifier from all listed ValueUpdates.
	 * @param modifier
	 */
	public void step(double modifier) {
		/*for(ValueUpdate valUp : valMap) {
			valUp.step(modifier);
		}*/
		if(valMap.size() < 1) return;
		
		for(int i = 0; (i < valMap.size() ) && (modifier > 0); i++) {
			valMap.get(i).step(modifier);
		}
		
		
		//valMap.get(0).step(modifier);		// It should only affect the first update. That update's
											// miss amount should then ripple onto the next if needed.
		
	}
	
	/**
	 * Returns a list of the updates whose timestamps have been passed (timestamp < 0)
	 * Warning: Does NOT remove the timestamps from the map.
	 * @return the list of updates whose timestamps have been passed
	 */
	public ValueUpdate getUpdateIfExists() {
		//System.out.println("map size: " + valMap.size());
		for(ValueUpdate valUp : valMap) {
			//System.out.println("Found valUp with " + valUp.getTimestamp() + " as a timestamp");
			if(valUp.getTimestamp() <= 0) {
				return valUp;
			}
		}
		return null;
	}

	public void removeUpdate(ValueUpdate update) {
		for(ValueUpdate upd: valMap) {
			if(upd.getTimestamp() == update.getTimestamp()) {
				valMap.remove(upd);
				return;
			}
		}
	}
	
	public void printMap() {
		String output = "I'm a value map. Updates: ";
		for(ValueUpdate upd: valMap) {
			output+= "( " + upd.getTimestamp() + ", " + upd.getValue() + "), ";
		}
		System.out.println(output);
	}
	
	public ValueUpdate getNewestAddedUpdate() {
		if(this.mostRecentlyAdded != null) {
			return this.mostRecentlyAdded;
		} else {
			return null;
		}
	}

	public Value getNewestAdded() {
		if(this.mostRecentlyAdded != null) {
			return this.mostRecentlyAdded.getValue();
		} else {
			return Value.UNKNOWN;
		}
	}

	public void clear() {
		this.valMap.clear();
		this.mostRecentlyAdded = null;
	}

	public int size() {
		// TODO Auto-generated method stub
		return this.valMap.size();
	}
}
