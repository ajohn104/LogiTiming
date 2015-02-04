package com.johnston.circ;

import com.cburch.logisim.data.Value;

public class ValueUpdate {
	private double timestamp;
	private Value val;
	
	public ValueUpdate(Value v, double timestamp) {
		this.timestamp = timestamp;
		this.val = v;
	}
	
	public void setTimestamp(double timestamp) {
		this.timestamp = timestamp;
		//this.timestamp = Double.parseDouble(String.format("%.3g%n", timestamp));
	}
	
	public void step(double modifier) {
		this.timestamp-=modifier;
		//this.timestamp = Double.parseDouble(String.format("%.3g%n", this.timestamp));
	}
	
	public double getTimestamp() {
		return this.timestamp;
	}
	
	public Value getValue() {
		return this.val;
	}
	
	/**
	 * Returns a unique representation of the ValueUpdate. The form is the timestamp followed by a 
	 * number given by the value of the ValueUpdate. False -> 0, True -> 1, Error -> 2, Unknown -> 3, 
	 * Nil -> 4, and finally if none of the above the default case is 5.
	 * Ex: ValueUpdate(Value.UNKNOWN, 1300) -> HashCode: 13003
	 * ValueUpdate(Value.ERROR, 510) -> HashCode: 5102
	 * ValueUpdate(Value.NIL, 0) -> HashCode: 4
	 * ValueUpdate(Value.FALSE, 0) -> HashCode: 0
	 */
	public int hashCode() {
		int endNum;
		if(this.val.equals(Value.FALSE)) endNum = 0;
		else if(this.val.equals(Value.TRUE)) endNum = 1;
		else if(this.val.equals(Value.ERROR)) endNum = 2;
		else if(this.val.equals(Value.UNKNOWN)) endNum = 3;
		else if(this.val.equals(Value.NIL)) endNum = 4;
		else endNum = 5;
		return (int)(10.0*this.timestamp + endNum);  
	}
}
