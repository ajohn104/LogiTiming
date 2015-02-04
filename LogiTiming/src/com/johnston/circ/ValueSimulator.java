package com.johnston.circ;

import java.util.ArrayList;
import java.util.List;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitMutation;
import com.cburch.logisim.circuit.CircuitMutator;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.data.AbstractAttributeSet;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.wiring.Pin;

public class ValueSimulator {
	
	private Component comp;
	private Component cpy;
	private CircuitState cs;
	private ArrayList<Component> inputPins;
	private ArrayList<InstanceState> inputStates;
	private Component outputPin;
	private InstanceState outputState;
	private Project proj;
	private Circuit circ;
	private ArrayList<GeneralComponent> outputs;
	private ArrayList<GeneralComponent> inputs;
	private List<EndData> inputPorts;
	private AttributeSet origAttrs;
	private Value currentVal;
	private ValueMap valMap;
	private Value prevVal;
	
	
	public ValueSimulator(Component c, Project p) {
		this.comp = c;
		this.proj = p;
		
		this.circ = new Circuit("Circuit for a "+ c.getFactory().getDisplayName());
		this.cs = new CircuitState(proj, circ);
		
		origAttrs = c.getAttributeSet();
		currentVal = Value.UNKNOWN;
		prevVal = Value.UNKNOWN;
		valMap = new ValueMap();
		
		CircuitMutation cMut = new CircuitMutation(circ);
		cpy = this.comp.getFactory().createComponent(Location.create(0, 0), this.comp.getAttributeSet());
		
		inputPins = new ArrayList<Component>();
		outputPin = StaticLibrary.getDefaultPin(StaticLibrary.PIN_IS_OUTPUT, cpy.getLocation());
		
		cMut.add(outputPin);
		inputStates = new ArrayList<InstanceState>();

		InstanceState state = cs.getInstanceState(outputPin);
		state.getData();
		outputState = state;
		
		outputs = new ArrayList<GeneralComponent>();
		inputs = new ArrayList<GeneralComponent>();
		
		inputPorts = comp.getEnds();
		
		cMut.add(cpy);
		cMut.execute();
		
	}
	
	public Component getComponent() {
		return this.comp;
	}
	
	
	/**
	 * Sets the Pins for the GeneralComponent to the given values, and then simulates. The values
	 * given will remain after the method ends.
	 * @param values - the values to be evaluated
	 * @return the output of the component
	 */
	public Value simulate() { 
		grabInputData();
		cs.getPropagator().propagate();
		return getCurrentValue();
	}
	
	public Value simulateV(Value[] values) {
		
		//CircuitMutation cMut = new CircuitMutation(circ);
		//this.setPins(cMut, values.length);
		//cMut.execute();
		Value[] newVals = new Value[inputStates.size()];
		for(int i = 0; i < newVals.length; i++) {
			newVals[i] = values[i];
		}
		setPinValues(newVals);
		cs.getPropagator().propagate();
		Value ret = getCurrentValue();
		reset();
		return ret;
						 // Really, the return of this method should be an array of outputPins, but for now its fine.
	}					 // I retract this statement, upon the fact that GeneralComponent ONLY supports bit to bit operations.
	
	public static Value simV(Component comp, Project proj, Value[] values) {
		ValueSimulator valComp = new ValueSimulator(comp, proj);
		return valComp.simulateV(values);
	}
	
	public Value getCurrentValue() {
		//System.out.println("We're getting a value at the said location of: " + cs.getValue(cpy.getLocation()).toDisplayString());
		//System.out.println("We're getting a value at the location of the first port of " + cs.getValue(cpy.getEnd(0).getLocation()).toDisplayString());
		//System.out.println("The values of all the ports are ");
		/*for(EndData e : cpy.getEnds()) {
			System.out.println(cs.getValue(e.getLocation()).toDisplayString());
		}*/
		if(outputState != null) return Pin.FACTORY.getValue(outputState);
		else {
			//System.out.println("No outputState");
			return Value.ERROR;
		}
	}
	
	private void reset() {
		inputPins.clear();
		inputStates.clear();
		outputs.clear();
		inputs.clear();
		CircuitMutation cMut = new CircuitMutation(circ);
		cMut.removeAll(inputPins);
		cMut.execute();
		cs.getPropagator().propagate();
	}
	
	public ArrayList<GeneralComponent> getOutputs() {
		return outputs;
	}
	
	public ArrayList<GeneralComponent> getInputs() {
		return inputs;
	}
	
	public void clearOutputs() {
		outputs.clear();
	}
	
	public void clearInputs() {
		inputs.clear();
		CircuitMutation cMut = new CircuitMutation(circ);
		cMut.removeAll(inputPins);
		cMut.execute();
		inputPins.clear();
		inputStates.clear();
	}
	
	public void grabInputData() {
		int numInputs = this.getInputs().size();
		Value[] values = new Value[numInputs];
		for(int i = 0; i < numInputs; i++) {
			values[i] = this.inputs.get(i).getCurrentValue();
		}
		setPinValues(values);
	}
	
	private void setPinValues(Value[] values) {
		int numInputs = this.getInputs().size();
		for(int i = 0; i < numInputs; i++) {
			Component currPin = inputPins.get(i);
			Value val = values[i];
			Location pinLoc = currPin.getLocation();
			
			((Pin)currPin.getFactory()).setValue((InstanceState)this.inputStates.get(i), val);
			//((Pin) currPin.getFactory()).setValue(inputStates.get(i), val);
			/*System.out.println("Before cs sets value: " + cs.getValue(pinLoc));
			System.out.println("Setting the value " + val.toDisplayString());*/
			cs.setValue(pinLoc, val, currPin, 1);
			//System.out.println("After cs sets value: " + cs.getValue(pinLoc));
			cs.markPointAsDirty(pinLoc);
			//System.out.println("After cs sets value and is marked dirty: " + cs.getValue(pinLoc));
		}
	}
	
	private void addInputPin() {
		int newPinIndex = getInputs().size()-1;
		//System.out.println("I am a " + this.getComponent().getFactory().getDisplayName() + " and I currently have " + newPinIndex + " inputs");
		Component pin = StaticLibrary.getDefaultPin(!StaticLibrary.PIN_IS_OUTPUT, inputPorts.get(newPinIndex).getLocation());
		InstanceState state = cs.getInstanceState(pin);
		state.getData();
		inputStates.add(state);
		inputPins.add(pin);
		CircuitMutation cMut = new CircuitMutation(circ);
		cMut.add(pin);
		cMut.execute();
	}
	
	public void setValueIfInput(Value v) {
		// Should check to see if it's an input pin and if so, it should set the value of its output to value v.
		if(!isInputPin()) return;
		Pin.FACTORY.setValue(outputState, v);
	}
	
	public boolean isInputPin() {
		return this.getInputs().size() == 0;
	}
	
	public boolean isOutputPin() {
		return this.getOutputs().size() == 0;
	}
	
	public boolean isPin() {
		return this.isInputPin() || this.isOutputPin();
	}
	
	/**
	 * Returns the Pin Label iff the GeneralComponent is a Pin.
	 * @return Pin label of represented Pin
	 */
	public String getLabel() {
		if(!this.isPin()) return "<<Not a Pin. Has no label. Error>>";
		return origAttrs.getValue(StdAttr.LABEL);
	}
	
	public Value simulateForValues(Value[] values) {
		//System.out.println("Beginning Simulation for " + this.getComponentDisplayName());
		//System.out.println("It has " + this.inputStates.size() + " inputStates");
		
		/*Value[] prevValues = new Value[this.inputStates.size()];
		Value prevValue = getCurrentValue();
		int inputStatesSize = this.inputStates.size();
		for(int i = 0; i < inputStatesSize; i++) {
			prevValues[i] = Pin.FACTORY.getValue(inputStates.get(i));
		}*/
		//clearInputValues();
		/*for(int i = 0; i < values.length; i++) {
			if(i < inputStates.size()) {
				Pin.FACTORY.setValue(inputStates.get(i), values[i]);
			}
		}*/
		setPinValues(values);
		/*for(int i = 0; i < inputStates.size(); i++) {
			System.out.println("The inputstate has a value of " + Pin.FACTORY.getValue(inputStates.get(i)).toDisplayString());
			System.out.println("The CircuitState finds the above value is " + cs.getValue(inputStates.get(i).getInstance().getLocation()).toDisplayString());
		}*/
		cs.getPropagator().propagate();
		/*cpy.propagate(cs);
		cs.markComponentAsDirty(cpy);
		cs.getPropagator().propagate();
		cpy.propagate(cs);
		cs.markPointAsDirty(cpy.getLocation());
		cs.getPropagator().propagate();
		this.outputPin.propagate(cs);
		cs.markComponentAsDirty(outputPin);
		cs.getPropagator().propagate();
		this.outputPin.propagate(cs);
		Value val = this.getCurrentValue();*/
		Value val = Pin.FACTORY.getValue((InstanceState)this.outputState);
		
		//clearInputValues();
		/*for(int i = 0; i < prevValues.length; i++) {
			Pin.FACTORY.setValue(inputStates.get(i), prevValues[i]);
		}
		
		Pin.FACTORY.setValue(outputState, prevValue);*/
		
		//System.out.println();
		return val;
	}
	
	public void clearInputValues() {
		for(InstanceState state : inputStates) {
			Pin.FACTORY.setValue(state, Value.TRUE);
			cs.setValue(state.getInstance().getLocation(), Value.TRUE, inputPins.get(inputStates.indexOf(state)), 1);
			cs.markPointAsDirty(state.getInstance().getLocation());
			cs.markComponentAsDirty(inputPins.get(inputStates.indexOf(state)));
		}
		cs.getPropagator().propagate();
	}
	
	public String getComponentDisplayName() {
		return this.getComponent().getFactory().getDisplayName();
	}
	
	
}
