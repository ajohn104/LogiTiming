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
import com.johnston.timing.ValueChange;

public class GeneralComponent {
	
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
	
	private final double DEFAULT_DELAY = 0.0001;


	private Value prevVal;
	private Value[] inputValues;
	private boolean isDirty;
	
	
	// Currently, with the workaround of the invisible delay, any time there is oscillation, it's going to rack up a ton of 
	// value updates, and a ton of memory and CPU. For now, I'm letting it be until a viable solution is found. I'd really 
	// rather not hard-code a detector for anticipating when an oscillation will occur, since this should be able to work for
	// any gate, even user-added ones. However, the alternative of detecting when an oscillation is occurring will only solve
	// the memory issue, by only adding 1 or two value updates. The CPU drain will still occur. So until a solution is found,
	// its going to be allowed.
	
	public GeneralComponent(Component c, Project p) {
		this.comp = c;
		this.proj = p;
		
		this.circ = new Circuit("Circuit for a " + c.getFactory().getDisplayName());
		this.cs = new CircuitState(proj, circ);
		
		origAttrs = c.getAttributeSet();
		currentVal = Value.UNKNOWN;
		prevVal = Value.UNKNOWN;
		valMap = new ValueMap();
		isDirty = false;
		
		CircuitMutation cMut = new CircuitMutation(circ);
		cpy = this.comp.getFactory().createComponent(Location.create(0, 0), this.comp.getAttributeSet());
		
		inputPins = new ArrayList<Component>();
		outputPin = StaticLibrary.getDefaultPin(StaticLibrary.PIN_IS_OUTPUT, cpy.getLocation());
		
		cMut.add(outputPin);
		inputStates = new ArrayList<InstanceState>();

		outputState = cs.getInstanceState(outputPin);
		outputState.getData();
		//outputState = state;
		
		outputs = new ArrayList<GeneralComponent>();
		inputs = new ArrayList<GeneralComponent>();
		
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
	
	public void resetSimulationData() {
		//if(!this.isInputPin()) {
		origAttrs = this.comp.getAttributeSet();
		currentVal = Value.UNKNOWN;
		prevVal = Value.UNKNOWN;
		valMap.clear();
		isDirty = false;
		/*} else {
			System.out.println("I am input " + this.getLabel() + ", not resetting valMap of size: " + this.valMap.size() );
			origAttrs = this.comp.getAttributeSet();
			currentVal = Value.UNKNOWN;
			prevVal = Value.UNKNOWN;
			isDirty = false;
		}*/
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
	 * Sets the Pins for the GeneralComponent to the given values, and then simulates. The values
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
		Value ret = Pin.FACTORY.getValue((InstanceState)this.outputState);   //this used to use 'getCurrentValue();'
		return ret;
						 // Really, the return of this method should be an array of outputPins, but for now its fine.
	}					 // I retract this statement, upon the fact that GeneralComponent ONLY supports bit to bit operations.
	
	public static Value simV(Component comp, Project proj, Value[] values) {
		values = new Value[] { Value.FALSE, Value.TRUE, Value.FALSE, Value.FALSE };
		GeneralComponent genComp = new GeneralComponent(comp, proj);
		return genComp.simulateV(values);
	}
	
	public Value getCurrentValue() {
		/*if(outputState != null) return Pin.FACTORY.getValue(outputState);
		else {
			System.out.println("No outputState");
			return Value.ERROR;
		}*/
		return this.currentVal;
	}
	
	public boolean isDirty() {
		return this.isDirty;
	}
	
	public void markAsDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}
	
	/**
	 * Propagate the component, but do not change currentValue
	 * @param currentTime The current time of the general propagator
	 * @return a ValueChange object if the component's value changed
	 * and, null if the component's value did not change.
	 */
	public ValueChange propagate(double currentTime) {
		if(this.isInputPin()) {
			//System.out.println("I am a: " + this.getComponentName() + " with the label " + this.getLabel() + " and my current value is " + this.getCurrentValue().toDisplayString());
			return null;
		}
		this.prevVal = this.valMap.getNewestAdded(); //Pin.FACTORY.getValue((InstanceState)this.outputState);
		//System.out.println("time: " + currentTime);
		//System.out.println("I am a: " + this.getComponentName() + " with the label " + this.getLabel());
		Value[] values = new Value[this.inputs.size()];
		for(GeneralComponent genComp : this.inputs) {
			
			//String inputInfo = genComp.isPin() ? (" with the label " + genComp.getLabel()):"";
			
			//System.out.println("Getting data from my input that is a " + genComp.getComponentName() + inputInfo);
			//System.out.println("and his current value is: " + genComp.getCurrentValue().toDisplayString());
			values[this.inputs.indexOf(genComp)] = this.grabValueFromInput(genComp);
		}
		
		cs.getPropagator().propagate();
		Value newVal = GeneralComponent.simV(this.getComponent(), this.getInputs().size(), this.getOutputs().size(), this.proj, values);//this.simulateForValues(values);//Pin.FACTORY.getValue((InstanceState)this.outputState); // Used to be assigned to this.getCurrentValue();
		if(newVal.equals(Value.ERROR)) {
			//System.out.println("Evaluation to Error. Changing to unknown.");
			newVal = Value.UNKNOWN;
		}
		//System.out.println("so I got a value of: " + newVal.toDisplayString());
		//System.out.println("and and my previous value was : " + prevVal.toDisplayString());
		//Value valSimulated = GeneralComponent.simV(this.getComponent(), this.getInputs().size(), this.getOutputs().size(), this.proj, values);
		//System.out.println("And my simulated value is: " + valSimulated.toDisplayString());
		//System.out.println(Pin.FACTORY.getValue((InstanceState)this.outputState).toDisplayString());
		double delay = DEFAULT_DELAY;
		if(newVal.toDisplayString().equals("1") && this.prevVal.toDisplayString().equals("0")) {
			delay = this.getDelay(true, this.getLabel());
			//System.out.println("This value has a delay of " + delay);
		}
		else if(newVal.toDisplayString().equals("0") && this.prevVal.toDisplayString().equals("1")) {
			delay = this.getDelay(false, this.getLabel());
			//System.out.println("This value has a delay of " + delay);
		}
		
		this.markAsDirty(true); // I'm pretty sure this code doesn't do anything.
		if(!newVal.equals(this.prevVal)) {
			this.prevVal = newVal;
			//System.out.println("My value changed, so I'm adding a Value Update with delay " + delay);
			
			this.valMap.addValueUpdate(newVal, delay); // Timestamp delay needs to be found using strings. // it used to be getCurrentValue() not newVal
			double trueDelay = this.valMap.getNewestAddedUpdate().getTimestamp();
			//System.out.println("I'm also returning a Value Change at time " + (currentTime + trueDelay));
			//System.out.println("Because the current time is " + (currentTime));
			//System.out.println("And my trueDelay is " + (trueDelay));
			return new ValueChange(newVal, currentTime + trueDelay/*delay*/); //If the newValue is different, return the delay + currentTime, otherwise return -1
		}
		return null;
	}
	
	// Does NOT propagate, merely pushes the valueMap along.
	public void step(double stepSize) {
		this.valMap.step(stepSize);
		//System.out.println("The following map is for " + this.getComponentName() + " with the label " + this.getLabel());
		//this.valMap.printMap();
		ValueUpdate update = this.valMap.getUpdateIfExists(); 
		
		/*if(this.isInputPin()) {
			if(update != null) {
				System.out.println("Found update to value: " + update.getValue().toDisplayString());
			} else {
				System.out.println("No update found");
			}
			
		}*/
		
		if(update != null) {
			this.currentVal = update.getValue();
			//this.prevVal = currentVal;
			this.valMap.removeUpdate(update);
		}
	}
	
	public double getDelay(boolean lowToHigh, String gateType) {
		if(lowToHigh) {
			if(gateType.equals("NAND Gate")) {
				return 11.0;
			} else if(gateType.equals("NOR Gate")) {
				return 12.0;
			} else if(gateType.equals("NOT Gate")) {
				return 12.0;
			} else if(gateType.equals("AND Gate")) {
				return 17.5;
			} else if(gateType.equals("OR Gate")) {
				return 10.0;
			} else if(gateType.equals("XOR Gate")) {
				return 15.0;
			} else if(gateType.equals("XNOR Gate")) {
				return 27.0;
			} else {
				return DEFAULT_DELAY;
			}
		} else {
			if(gateType.equals("NAND Gate")) {
				return 7.0;
			} else if(gateType.equals("NOR Gate")) {
				return 8.0;
			} else if(gateType.equals("NOT Gate")) {
				return 8.0;
			} else if(gateType.equals("AND Gate")) {
				return 12.0;
			} else if(gateType.equals("OR Gate")) {
				return 14.0;
			} else if(gateType.equals("XOR Gate")) {
				return 11.0;
			} else if(gateType.equals("XNOR Gate")) {
				return 19.0;
			} else {
				return DEFAULT_DELAY;
			}
		}
	}
	
	public Value grabValueFromInput(GeneralComponent genComp) {
		Value ret = genComp.getCurrentValue();
		this.setValueOfPin(this.inputs.indexOf(genComp), ret);
		return ret;
	}
	
	public boolean allOutputsAreDirty() {
		for(GeneralComponent genComp : this.outputs) {
			if(!genComp.isDirty()) return false;
		}
		return true;
	}
	
	public boolean allInputsAreDirty() {
		for(GeneralComponent genComp : this.inputs) {
			if(!genComp.isDirty()) return false;
		}
		return true;
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
	
	public void addOutputTo(GeneralComponent c) {
		outputs.add(c);
		if(!c.getInputs().contains(this)) c.addInputFrom(this);
	}
	
	public void addOutputsTo(ArrayList<GeneralComponent> outputs) {
		this.outputs.addAll(outputs);
	}
	
	public void addInputFrom(GeneralComponent c) {
		inputs.add(c);
		addInputPin();
		if(!c.getOutputs().contains(this)) c.addOutputTo(this);
	}
	
	public void addInputsFrom(ArrayList<GeneralComponent> inputs) {
		for(GeneralComponent genComp : inputs) {
			addInputFrom(genComp);
		}
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
		this.inputValues = new Value[Math.min(values.length, numInputs)];
		
		for(int i = 0; i < inputValues.length; i++) {
			//Component currPin = (Component)this.inputPins.get(i);
			Value val = values[i];
			/*inputValues[i] = values[i];
			((Pin)currPin.getFactory()).setValue((InstanceState)this.inputStates.get(i), val);
		     this.cs.markPointAsDirty(currPin.getLocation());*/
			setValueOfPin(i, val);
		}
	}
	
	private void setPinValuesOldVersion(Value[] values) {
		int numInputs = this.getInputs().size();
		//System.out.println("Found " + numInputs + " inputs.");
		this.inputValues = new Value[Math.min(values.length, numInputs)];
		//System.out.println(inputValues.length);
		for(int i = 0; i < inputValues.length; i++) {
			Component currPin = (Component)this.inputPins.get(i);
			Value val = values[i];
			inputValues[i] = values[i];
			((Pin)currPin.getFactory()).setValue((InstanceState)this.inputStates.get(i), val);
		     this.cs.markPointAsDirty(currPin.getLocation());
		}
	}
	
	private void setValueOfPin(int index, Value val) {
		Component currPin = (Component)this.inputPins.get(index);
		if(inputValues == null) {
			this.inputValues = new Value[this.inputs.size()];
		}
		inputValues[index] = val; // It isn't finding the inputvalues array. It's null, for some reason.
		((Pin)currPin.getFactory()).setValue((InstanceState)this.inputStates.get(index), val);
	     this.cs.markPointAsDirty(currPin.getLocation());
	}
	
	void addInputPin() {
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
	 * Returns the associated label if the GeneralComponent is a Pin.
	 * @return Pin label of represented Pin
	 */
	public String getLabel() {
		String label = origAttrs.getValue(StdAttr.LABEL);
		if(label.equals("")) {
			return this.getComponentName();
		} else {
			return label;
		}
		//if(!this.isPin()) return "No label";//"<<Not a Pin. Has no label. Error>>";
		//return origAttrs.getValue(StdAttr.LABEL);
	}
	
	/*public String getComponentDisplayName()*/
	
	public String getComponentName() {
		return this.cpy.getFactory().getName();
	}
	
	public Value simulateForValues(Value[] values) {
		
		setPinValuesOldVersion(values);
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
	
	public ValueMap getValMap() {
		return valMap;
	}
	
	public static Value simV(Component comp, int numInputs, int numOutputs, Project proj, Value[] values) {
		GeneralComponent genComp = new GeneralComponent(comp,proj);
		for(int i = 0; i < numInputs; i++) {
			genComp.addInputPin();
		}
		return genComp.simulateForValuesIgnoreInputs(values, numInputs);
	}
	
	public Value simulateForValuesIgnoreInputs(Value[] values, int numInputs) {
		
		this.setPinValuesIgnoreNumInputs(values, numInputs);
		cs.getPropagator().propagate();
		Value val = Pin.FACTORY.getValue((InstanceState)this.outputState);
		
		return val;
	}
	
	private void setPinValuesIgnoreNumInputs(Value[] values, int numInputValues) {
		//int numInputs = this.getInputs().size();
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
}
