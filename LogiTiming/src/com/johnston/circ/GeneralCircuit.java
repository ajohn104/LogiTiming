package com.johnston.circ;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.proj.Project;
import com.johnston.timing.TimingDiagram;

public class GeneralCircuit {
	public ArrayList<GeneralComponent> outputs;
	public ArrayList<GeneralComponent> genComps;
	public ArrayList<GeneralComponent> inputs;
	Circuit circ;
	public Project proj;
	
	
	public GeneralCircuit(Circuit c, Project project) {
		this.circ = c;
		this.proj = project;
	}
	
	public void setValueOfInput(int indexOfInput, Value v) {
		outputs.get(indexOfInput).setValueIfInput(v);
	}
	
	public void testComponent(GeneralComponent genComp) {
		if(genComp.isPin()) return;
		Value[] testVals = new Value[] { Value.TRUE, Value.NIL, Value.FALSE, Value.TRUE, Value.FALSE};
		Value valSimulated = GeneralComponent.simV(genComp.getComponent(), this.proj, testVals);
		Value returnVal = genComp.simulateForValues(testVals);
		//System.out.println("We tested a " + genComp.getComponentName());
		//System.out.println("We gave it the values: ");
		/*for(Value val : genComp.getInputValues()) {
			System.out.println(val.toDisplayString());
		}
		System.out.println("And it returned a value of " + returnVal.toDisplayString());
		System.out.println();*/
		/*
		 * The problem here is that it needs to have each of the values set in advance and propagated 
		 * to each component before it can be tested. Which means that the program needs to start at the 
		 * inputs and propagate the values there, and then check to see if any of the inputs to that 
		 * component are clean. If they are clean, they they need to be simulated first, then... GAHH 
		 * I don't know. Because this is where the wrap around comes in nasty, because which ones should
		 * be simulated first. 
		 */
	}
	
	public void testCircuit() {
		/*Value output = Value.ERROR;
		Value[] inputValues = new Value[] { Value.FALSE, Value.TRUE, Value.TRUE, Value.TRUE };
		
		
		for(int i = 0; i < this.inputs.size(); i++) {
			this.inputs.get(i).setValueIfInput(inputValues[i]);
			System.out.println("We just set the value of the input " + this.inputs.get(i).getLabel() + " to " + inputValues[i]);
		}
		
		
		
		System.out.println("The output is " + output.toDisplayString());*/
		
		TimingDiagram diagram = new TimingDiagram(this, 50, 200);
		diagram.simulateToMaxTime();
		diagram.printFinalSimulationResults();
	}
	
	
	// Should build the circuit. Take the code from Storage, and mix it with the code from DataTree
	public void build() {
		//outputs = GeneralCircuitBuilder.buildCircuit(circ, proj);
		GeneralCircuitBuilder builder = new GeneralCircuitBuilder(circ, proj);
		outputs = builder.getGeneralOutputs();
		genComps = builder.getGeneralComponents();
		inputs = new ArrayList<GeneralComponent>();
		for(GeneralComponent genC : genComps) {
			if(genC.isInputPin()) inputs.add(genC);
		}
		Collections.sort(inputs, new Comparator<GeneralComponent>() {
			@Override
			public int compare(GeneralComponent gen1, GeneralComponent gen2) {
				// TODO Auto-generated method stub
				return (gen1.getComponent().getLocation().getY() > gen2.getComponent().getLocation().getY())?1:-1;
			}
	    });
		Collections.sort(outputs, new Comparator<GeneralComponent>() {
			@Override
			public int compare(GeneralComponent gen1, GeneralComponent gen2) {
				// TODO Auto-generated method stub
				return (gen1.getComponent().getLocation().getY() > gen2.getComponent().getLocation().getY())?1:-1;
			}
	    });
		//printStructure();
		/*int count = 0;
		for(GeneralComponent genC : genComps) {
			
			if(genC.isInputPin()) {
				count++;
			}
			if(genC.isInputPin()) {
				System.out.println("Input pin found.");
				System.out.println("Current Value: " + genC.getCurrentValue().toDisplayString());
				System.out.println("Setting Value to true...");
				genC.setValueIfInput(Value.TRUE);
				System.out.println("Current Value: " + genC.getCurrentValue().toDisplayString());
				System.out.println("Setting Value to false...");
				genC.setValueIfInput(Value.FALSE);
				System.out.println("Current Value: " + genC.getCurrentValue().toDisplayString());
				System.out.println("Setting Value to unknown...");
				genC.setValueIfInput(Value.UNKNOWN);
				System.out.println("Current Value: " + genC.getCurrentValue().toDisplayString());
			}
			if(genC.isPin()) {
				System.out.println("One pin label for an " + (genC.isInputPin()?"input ":"output ") + " was found to be " + genC.getLabel());
			}
		}*/
		/*
		System.out.println("We found " + count + " input pins");
		
		System.out.println();
		System.out.println("Here goes nothing...");
		for(GeneralComponent genC : this.genComps) {
			testComponent(genC);
		}*/
		
		//testCircuit();
		
	}
	
	public String allValuesHashCode() {
		String ret = "";
		
		for(GeneralComponent genComp: this.genComps) {
			ret+=genComp.getCurrentValue().toDisplayString();
		}
		
		return ret;
	}
	
	// Obvious, but needs to return stuff
	public void next() {
		
	}
	
	// Obvious, needs to return stuff, and perhaps keep a list of the parsed tree to go back easier?
	public void previous() {
		
	}
	
	public void displayFinalOutputs() {
		for(GeneralComponent genComp: outputs) {
			System.out.println("One GeneralComponent final gate/etc is a " + genComp.getComponent().getFactory().getDisplayName());
		}
	}
	
	
	/*
	 * Simulation/propagation should work similarly to this, where it updates things as needed. Except it would also need things marked as dirty
	 */
	public void printStructure() {
		for(GeneralComponent genComp : outputs) {
			System.out.println("We found an output that is a " + genComp.getComponent().getFactory().getDisplayName());
			for(GeneralComponent input: genComp.getInputs()) {
				System.out.println("The following are inputs to that output:");
				printFrom(input);
			}
			System.out.println("End of output");
		}
		System.out.println();
	}
	
	private void printFrom(GeneralComponent genComp) {
		System.out.println("One " + genComp.getComponent().getFactory().getDisplayName());
		if(genComp.getInputs().size() > 0) System.out.println("The following are inputs to the above");
		for(GeneralComponent genC : genComp.getInputs()) {
			printFrom(genC);
		}
		System.out.println("End of " + genComp.getComponent().getFactory().getDisplayName());
	}
	
	
	
	
	
}
