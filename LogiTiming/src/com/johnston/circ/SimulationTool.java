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

public class SimulationTool {
	
	private Component comp;
	private Component cpy;
	private CircuitState cs;
	private ArrayList<Component> inputPins;
	private ArrayList<InstanceState> inputStates;
	private Component outputPin;
	private InstanceState outputState;
	private Project proj;
	private Circuit circ;
	private ArrayList<SimulationTool> outputs;
	private ArrayList<SimulationTool> inputs;
	private List<EndData> inputPorts;
	private AttributeSet origAttrs;
	private Value currentVal;
	private ValueMap valMap;
	private Value prevVal;
	private Value[] inputValues;
	
	//Its a rehash of a working SimulationTool build, opposed to the simulation-broken current one.
	public SimulationTool(Component c, Project p) {
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

		outputState = cs.getInstanceState(outputPin);
		outputState.getData();
		//outputState = state;
		
		outputs = new ArrayList<SimulationTool>();
		inputs = new ArrayList<SimulationTool>();
		
		//inputPorts = comp.getEnds();
		inputPorts = new ArrayList<EndData>();
		for(EndData data : cpy.getEnds()) {
			if(/*data != null && */data.isInput()) inputPorts.add(data);
		}
		//System.out.println("cpy has " + cpy.getEnds().size() + " ends");
		
		cMut.add(cpy);
		cMut.execute();
		
	}
	
	public Component getComponent() {
		return this.comp;
	}
	
	private void setPins(CircuitMutation cMut, int n) {
		int numInputs = 0;
		for(EndData end : cpy.getEnds()) {
			boolean isOutput = end.isOutput();
			Component pin = StaticLibrary.getDefaultPin(isOutput, end.getLocation());
			InstanceState state = cs.getInstanceState(pin);
			state.getData();
			
			if(!isOutput) {
				if(numInputs < n) {
					inputStates.add(state);
					inputPins.add(pin);
					cMut.add(pin);
					numInputs++;
				}
			}
		}
	}
	
	
	/**
	 * Sets the Pins for the SimulationTool to the given values, and then simulates. The values
	 * given will remain after the method ends.
	 * @param values - the values to be evaluated
	 * @return the output of the component
	 */
	public Value simulate() { 
		grabInputData();
		cs.getPropagator().propagate();
		return ValueSimulator.simV(this.comp, this.proj, this.inputValues)/*getCurrentValue()*/;
	}  // This ^ needs to instead place the value into the map with the associated value, rather than simply simulate it.
	
	public Value simulateV(Value[] values) {
		setPinValues(values);
		cs.getPropagator().propagate();
		Value ret = getCurrentValue();
		return ret;
						 // Really, the return of this method should be an array of outputPins, but for now its fine.
	}					 // I retract this statement, upon the fact that SimulationTool ONLY supports bit to bit operations.
	
	public static Value simV(Component comp, Project proj, Value[] values) {
		SimulationTool genComp = new SimulationTool(comp, proj);
		for(int i = 0; i < values.length; i++) {
			//genComp.addInputPin();
		}
		return Value.ERROR;//genComp.simulateForValues(values);
	}
	
	public static Value simV(Component comp, int numInputs, int numOutputs, Project proj, Value[] values) {
		SimulationTool simTool = new SimulationTool(comp,proj);
		for(int i = 0; i < numInputs; i++) {
			simTool.addInputPin();
		}
		return simTool.simulateForValuesIgnoreInputs(values, numInputs);
	}
	
	public Value getCurrentValue() {
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
	
	public void addOutputTo(SimulationTool c) {
		outputs.add(c);
		if(!c.getInputs().contains(this)) c.addInputFrom(this);
	}
	
	public void addOutputsTo(ArrayList<SimulationTool> outputs) {
		this.outputs.addAll(outputs);
	}
	
	public void addInputFrom(SimulationTool c) {
		inputs.add(c);
		addInputPin();
		if(!c.getOutputs().contains(this)) c.addOutputTo(this);
	}
	
	public void addInputsFrom(ArrayList<SimulationTool> inputs) {
		for(SimulationTool genComp : inputs) {
			addInputFrom(genComp);
		}
	}
	
	public ArrayList<SimulationTool> getOutputs() {
		return outputs;
	}
	
	public ArrayList<SimulationTool> getInputs() {
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
		//System.out.println("Found " + numInputs + " inputs.");
		this.inputValues = new Value[Math.min(values.length, numInputs)];
		
		for(int i = 0; i < inputValues.length; i++) {
			Component currPin = (Component)this.inputPins.get(i);
			Value val = values[i];
			inputValues[i] = values[i];
			((Pin)currPin.getFactory()).setValue((InstanceState)this.inputStates.get(i), val);
		     this.cs.markPointAsDirty(currPin.getLocation());
		}
	}
	
	private void setPinValuesIgnoreNumInputs(Value[] values, int numInputValues) {
		int numInputs = this.getInputs().size();
		//System.out.println("Found " + numInputs + " inputs.");
		this.inputValues = new Value[numInputValues];
		
		for(int i = 0; i < inputValues.length; i++) {
			Component currPin = (Component)this.inputPins.get(i);
			Value val = values[i];
			inputValues[i] = values[i];
			((Pin)currPin.getFactory()).setValue((InstanceState)this.inputStates.get(i), val);
		     this.cs.markPointAsDirty(currPin.getLocation());
		}
	}
	
	private void addInputPin() {
		int newPinIndex = inputPins.size();
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
	 * Returns the Pin Label iff the SimulationTool is a Pin.
	 * @return Pin label of represented Pin
	 */
	public String getLabel() {
		if(!this.isPin()) return "<<Not a Pin. Has no label. Error>>";
		return origAttrs.getValue(StdAttr.LABEL);
	}
	
	public String getComponentName() {
		return this.cpy.getFactory().getName();
	}
	
	public Value simulateForValues(Value[] values) {
		
		setPinValues(values);
		cs.getPropagator().propagate();
		Value val = Pin.FACTORY.getValue((InstanceState)this.outputState);
		
		return val;
	}
	
	public Value simulateForValuesIgnoreInputs(Value[] values, int numInputs) {
		
		this.setPinValuesIgnoreNumInputs(values, numInputs);
		cs.getPropagator().propagate();
		Value val = Pin.FACTORY.getValue((InstanceState)this.outputState);
		
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
	
	
	public Value[] getInputValues() {
		return this.inputValues;    // This should return a copy of the input values.
	}
	
	
}
