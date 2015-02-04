package com.johnston.circ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.cburch.logisim.data.Value;
import com.johnston.timing.TimingDiagram;
import com.johnston.timing.ValueChange;
import com.johnston.timing.ValueChangeList;
import com.johnston.timing.ValueStorage;

public class GeneralPropagator {
	
	GeneralCircuit genCirc;
	double currentTime;
	TimingDiagram diagram;
	
	LinkedHashMap<GeneralComponent, ValueChangeList> inputValueChangeList;
	LinkedHashMap<GeneralComponent, ValueChangeList> outputValueChangeList;
	LinkedHashMap<GeneralComponent, ValueChangeList> componentValueChangeList;
	ValueChangeList valueChanges;
	
	public GeneralPropagator(GeneralCircuit circ, TimingDiagram td) {
		this.genCirc = circ;
		this.currentTime = -1.0;
		this.diagram = td;
		this.inputValueChangeList = td.getInputValueChangeList();
		this.outputValueChangeList = td.getInputValueChangeList();
		this.componentValueChangeList = td.getComponentValueChangeList();
		this.valueChanges = td.getValueChanges();
	}
	
	public void propagate() {
		/*
		boolean valuesChanged = true;
		ArrayList<String> oldValues = new ArrayList<String>();
		String newValues = "";
		
		ArrayList<GeneralComponent> nextSet = new ArrayList<GeneralComponent>();
		ArrayList<GeneralComponent> currSet = genCirc.inputs;
		
		HashMap<GeneralComponent, Value> valuesBefore = new HashMap<GeneralComponent, Value>();
		for(GeneralComponent genComp: genCirc.genComps) {
			valuesBefore.put(genComp, genComp.getCurrentValue());
		}
		
		for(GeneralComponent input : currSet) {
			nextSet.addAll(input.getOutputs());  // Shouldn't add duplicates
		}
		
		
		int loopBreaker = 0;
		while(valuesChanged) {    // Loop until no more changes to values occur.
			loopBreaker++;
			for(GeneralComponent genComp: currSet) {
				genComp.propagate(this.currentTime);
				genComp.step(0); 
			}
			newValues = genCirc.allValuesHashCode();
			if(oldValues.size() > 0) valuesChanged = newValues.equals(oldValues.get(0));
			for(int i = 1; i < oldValues.size(); i++) {
				if(newValues.equals(oldValues.get(i))) {
					System.out.println("Escaping due to confirmed oscillation at time " + this.getTime());
					valuesChanged = false;
				}
			}
			if(loopBreaker > 1000) {
				System.out.println("Escaping while loop");
				valuesChanged = false;
			}
			oldValues.add(newValues);
		}
		currSet = nextSet;
		*/
		
		for(GeneralComponent genComp: genCirc.genComps) {
			ValueChange valChange = genComp.propagate(this.getTime());
			if(valChange != null) {
				//System.out.println("A value change was returned, so I'm adding a Value Change to the diagram at time " + valChange.getTimeChanged());
				diagram.addValueChange(genComp, valChange.getValue(), valChange.getTimeChanged());
			}
		}
	}
	
	public void step() {
		// Set the input values to their correct current values.
		//this.valueChanges.printChanges();
		double oldTime = this.currentTime;
		//System.out.println("currentTime: " + this.currentTime + ", nextTimeChange: " + this.valueChanges.getNextTimeChange(this.currentTime));
		this.currentTime = this.valueChanges.getNextTimeChange(this.currentTime);
		if(this.currentTime > diagram.getMaxNanoseconds() || this.currentTime < 0) {
			return;
		}
		double stepSize = currentTime - oldTime;
		//stepSize = Double.parseDouble(String.format("%.3g%n", stepSize)); I need to stop rounding ffs.
		if(oldTime < 0) {
			stepSize = 0;
		}
		//System.out.println("currentTime: " + this.currentTime + ", stepSize: " + stepSize);
		/*for(GeneralComponent input: genCirc.inputs) {
			input.setValueIfInput(diagram.getInputMap().get(input).getValueAtTime(this.currentTime));
		}*/
		// step all components -- AKA have all components update their current value if it should
		// change due to ended delay. DOES NOT pull any values or simulate any components whatsoever.
		for(GeneralComponent genComp : genCirc.genComps) {
			/*if(genComp.isInputPin()) {
				System.out.println("Looking for update in pin with label " + genComp.getLabel() + " at time " + this.getTime());
			}*/
			genComp.step(stepSize);
		}
		
		propagate();
		// propagate should take note of any value changes and record them to ValueStorage.
	}
	
	public double getTime() {
		return this.currentTime;
	}
	
	public void setInputsTo(ArrayList<Value> inputValues) {
		for(int i = 0; i < genCirc.inputs.size(); i++) {
			genCirc.inputs.get(i).setValueIfInput(inputValues.get(i));  // Hopefully order is maintained
												// throughout the program, or this could get even uglier.
		}
	}
	
	
	
}
