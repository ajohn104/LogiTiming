package com.johnston.timing;

import com.cburch.logisim.data.Value;

/**
 * ValueChange is pretty much the same thing as ValueUpdate, except that it's 
 * measured from the beginning of the simulation.
 * 
 * -- I think this and ValueMap and everything should be discarded in the sense that
 * it should be taken out of the code, but the code it uses is still helpful because it 
 * has a lot of the things I'm trying to do already written. So study it, copypasta some stuff, 
 * and slowly get rid of it.
 * 
 * -- Actually, this does everything I need it to. It's measured from the start, too.
 * @author Albert
 *
 */
public class ValueChange {
	Value val;
	double timeChanged;
	public ValueChange(Value v, double d) {
		val = v;
		timeChanged = d;
	}
	
	public Value getValue() {
		return val;
	}
	
	public double getTimeChanged() {
		return timeChanged;
	}
	
	public void setValue(Value v) {
		val = v;
	}
	
	public void setTimeChanged(double t) {
		timeChanged = t;
	}
	
	public String toString() {
		String ret = "( ";
		ret+= timeChanged + "ns" + ", ";
		ret+= val.toDisplayString();
		ret+=")";
		return ret;
	}
}
