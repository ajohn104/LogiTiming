package com.johnston.timing;

import java.util.ArrayList;
import java.util.Iterator;

import com.cburch.logisim.data.Value;
import com.johnston.circ.ValueUpdate;

public class ValueChangeList implements Iterable<ValueChange> {
	ArrayList<ValueChange> changeList;
	
	public ValueChangeList() {
		changeList = new ArrayList<ValueChange>();
		this.addValueChange(Value.UNKNOWN, 0);
	}
	
	public ValueChangeList(ValueChange[] changes) {
		changeList = new ArrayList<ValueChange>();
		this.addAll(changes);
	}
	
	public void setTo(ValueChangeList list) {
		this.clear();
		this.addAll(list);
	}
	
	public void setChangeTo(Value val, double time) {
		for(ValueChange valChange: this.changeList) {
			if(valChange.timeChanged == time) {
				valChange.setValue(val);
				return;
			}
		}
		this.addValueChange(val, time);
	}
	
	public void addAll(ValueChange[] changes) {
		for(ValueChange change: changes) {
			this.addValueChange(change.getValue(), change.getTimeChanged());
		}
	}
	
	public void addAll(ValueChangeList list) {
		for(ValueChange change: list.toArrayList()) {
			this.addValueChange(change.getValue(), change.getTimeChanged());
		}
	}
	
	public void clear() {
		this.changeList.clear();
	}
	
	public void reset() {
		changeList = new ArrayList<ValueChange>();
		this.addValueChange(Value.UNKNOWN, 0);
	}
	
	public ArrayList<ValueChange> toArrayList() {
		return this.changeList;
	}
	
	/**
	 * This isn't really used.
	 * @param newVal
	 * @param currTime
	 */
	public void valueChanged(Value newVal, double currTime) {
		//currTime = Double.parseDouble(String.format("%.3g%n", currTime));
		changeList.add(new ValueChange(newVal, currTime));
	}
	
	/**
	 * This is the primary way to add a change
	 * @param val
	 * @param d
	 */
	public void addValueChange(Value val, double timeChanged) {
		//timeChanged = Double.parseDouble(String.format("%.3g%n", timeChanged));
		for(int i = 0; i < changeList.size(); i++) {
			if(changeList.get(i).getTimeChanged() == timeChanged) {
				changeList.get(i).setValue(val);
				return; // Prevents duplicate time changes for the same timestamp
			}
			if(changeList.get(i).timeChanged > timeChanged) {
				changeList.add(i, new ValueChange(val, timeChanged));
				return;
			}
		}
		changeList.add(new ValueChange(val, timeChanged));
	}
	
	/**
	 * Finds the value that was held at the specified point in time.
	 * @param time the point in time to get the held value
	 * @return the held value at the specified time.
	 */
	public Value getValueAtTime(double time) {
		if(time < 0) return Value.UNKNOWN;
		for(int i = 0; i < changeList.size()-1; i++) {
			if(changeList.get(i+1).timeChanged > time) {
				return changeList.get(i).val;
			}
		}
		return changeList.get(changeList.size()-1).val;
	}
	
	public ValueChange get(int index) {
		return this.changeList.get(index);
	}
	
	/**
	 * Finds the time at which the next change occurs
	 * @param time the point in time to start the search at
	 * @return the time of the next value change, -1 if not found
	 */
	public double getNextTimeChange(double time) {
		if(time < 0 && changeList.size() > 0) return changeList.get(0).timeChanged;
		for(int i = 0; i < changeList.size()-1; i++) {
			if(changeList.get(i+1).timeChanged > time) {
				return changeList.get(i+1).timeChanged;
			}
		}
		return -1;
	}
	
	public int size() {
		return this.changeList.size();
	}
	
	public void printSimulationRecord() {
		System.out.println();
		String outputStr = "";
		for(ValueChange change : this.changeList) {
			outputStr+= change.toString() + ", ";
		}
		if(outputStr.length()>1) outputStr.substring(0, outputStr.length()-2);
		System.out.println(outputStr);
		System.out.println();
	}
	
	public void printChanges() {
		System.out.println("This is the current change list. Changes: ");
		for(ValueChange chng: this.changeList) {
			System.out.println("( " + chng.getTimeChanged() + "ns, " + chng.getValue() + "), ");
		}
	}
	
	public static void main(String[] args) {
		ValueChangeList changes = new ValueChangeList();
		changes.addValueChange(Value.FALSE, 0);
		changes.addValueChange(Value.TRUE, 50);
		changes.addValueChange(Value.FALSE, 100);
		changes.addValueChange(Value.TRUE, 150);
		changes.addValueChange(Value.FALSE, 67.5);
		changes.addValueChange(Value.UNKNOWN, 175.5);
		changes.addValueChange(Value.FALSE, 193);
		changes.printSimulationRecord();
		System.out.println(changes.getValueAtTime(0).toDisplayString());
		System.out.println(changes.getNextTimeChange(-1));
		
		System.out.println(Value.ERROR.equals(Value.ERROR));
	}

	
	public Iterator<ValueChange> iterator() {
		return this.changeList.iterator();
	}

	public void removeUnneeded() {
		Value currVal = null;
		Iterator<ValueChange> it = this.iterator();
		while(it.hasNext()) {
			ValueChange next = it.next();
			if(!next.val.equals(currVal)) {
				currVal = next.val;
			} else {
				it.remove();
			}
		}
		
	}
}
