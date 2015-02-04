package com.johnston.timing;

import java.util.ArrayList;

import com.cburch.logisim.data.Value;

public class ValueStorage {
	ArrayList<ValueChange> valueChangeMap;
	
	public ValueStorage() {
		valueChangeMap = new ArrayList<ValueChange>();
	}
	
	public void valueChanged(Value newVal, double currTime) {
		valueChangeMap.add(new ValueChange(newVal, currTime));
	}
	
	public void addValueChange(Value val, double d) {
		for(int i = 0; i < valueChangeMap.size(); i++) {
			if(valueChangeMap.get(i).timeChanged > d) {
				valueChangeMap.add(i, new ValueChange(val, d));
				return;
			}
		}
		valueChangeMap.add(new ValueChange(val, d));
	}
		
	public Value getValueAtTime(double time) {
		for(int i = 0; i < valueChangeMap.size()-1; i++) {
			if(valueChangeMap.get(i+1).timeChanged > time) {  // Look into this method as to whether it is written correctly
				return valueChangeMap.get(i).val;
			}
		}
		return Value.UNKNOWN;
	}
	
	public double getNextTimeChange(double time) {
		for(int i = 0; i < valueChangeMap.size()-1; i++) {
			if(valueChangeMap.get(i+1).timeChanged > time) {
				return valueChangeMap.get(i+1).timeChanged;
			}
		}
		return -1;
	}
	
	public void printSimulationRecord() {
		System.out.println();
		String outputStr = "";
		for(ValueChange change : this.valueChangeMap) {
			outputStr+= change.toString() + ", ";
		}
		if(outputStr.length()>1) outputStr.substring(0, outputStr.length()-2);
		System.out.println(outputStr);
		System.out.println();
	}
}
