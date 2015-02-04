package com.johnston.timing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.cburch.logisim.data.Value;
import com.cburch.logisim.proj.Project;
import com.johnston.circ.GeneralCircuit;
import com.johnston.circ.GeneralComponent;
import com.johnston.circ.GeneralPropagator;
import com.johnston.circ.ValueMap;

public class TimingDiagram {
	
	/*
	 * So, basically it should just be a while loop along the lines of:
	 * double timeOfChange = -1;
	 * while((timeOfChange = valueChanges.getNextTimeChange(timeOfChange))!= -1) {
	 * 
	 * }
	 * 
	 */
	
	
	public LinkedHashMap<GeneralComponent, ValueChangeList> inputValueChangeList;
	public LinkedHashMap<GeneralComponent, ValueChangeList> outputValueChangeList;
	public LinkedHashMap<GeneralComponent, ValueChangeList> componentValueChangeList;
	public ValueChangeList valueChanges;
	GeneralCircuit genCirc;
	double interval;
	double maxNanoseconds;
	
	public static final int WHOLE = 0;
	public static final int TENTHS = 1;
	public static final int HUNDREDTHS = 2;
	public static final int THOUSANDTHS = 3;
	
	
	/*
	 * Idea: This is really an old idea, but its worth a shot still. Quite simply, allow zero delay. Keep track of all of the last circuit 
	 * states (meaning the values of all gates, etc) that have occurred since the last change in time. Each time we propagate on a zero step
	 * size, get the current circuit state and check to see if it matches any of the ones held in the aforementioned list. If it doesn't, then 
	 * you're free to allow the propagation to continue. If it does, however, then find which components change in any of the circuit states in
	 * the list. Since these are the ones oscillating, clear their value updates (since all of their updates will be the oscillation), and clear
	 * all value updates (and value changes) that are associated with these components in the lists held by both the diagram as well as the 
	 * GeneralPropagator that occur after the current time (of which there shouldn't be any except one or two, I think). Then, add an error value
	 * change with the current time for a timestamp for each of the components to the changeList associated with it (since the diagram and GenProp 
	 * both use the same changeList, it should only be added to one). Then immediately set the current value of all the oscillating components to
	 * Value.ERROR, and finally add a valueChange to GeneralPropagator's list with the current timestamp to allow the error to affect whatever it
	 * should. Then end the current step, and let propagation continue. Warning: this will require the addition of removing timeChanges for GenProp's
	 * overall list once they've been reached. Also, it means that ValueChangeList will need to allow the "next time change" to be one with the same 
	 * time stamp as the current one, so that zero delay can even be supported. 
	 * ---- Lastly, if a step occurs that changes time, clear the list of circuit states, and move on.
	 */
	
	/**
	 * 
	 * @param circ
	 * @param interval
	 * @param maxNanoseconds
	 */
	public TimingDiagram(GeneralCircuit circ, int interval, int maxNanoseconds) {
		//ValueMap map = new ValueMap();
		this.inputValueChangeList = new LinkedHashMap<GeneralComponent, ValueChangeList>();
		this.outputValueChangeList = new LinkedHashMap<GeneralComponent, ValueChangeList>();
		this.componentValueChangeList = new LinkedHashMap<GeneralComponent, ValueChangeList>();
		this.valueChanges = new ValueChangeList();
		this.interval = interval;
		this.maxNanoseconds = maxNanoseconds;
		this.genCirc = circ;
		
		for(GeneralComponent input: genCirc.inputs) {
			ValueChangeList changesList = new ValueChangeList();
			this.inputValueChangeList.put(input, changesList);
			this.componentValueChangeList.put(input, changesList);
		}
		for(GeneralComponent comp: genCirc.genComps) {
			ValueChangeList changesList = new ValueChangeList();
			if(comp.isInputPin()); //this.inputValueChangeList.put(comp, changesList);
			if(comp.isOutputPin()); //this.outputValueChangeList.put(comp, changesList);
			this.componentValueChangeList.put(comp, changesList);
		}
		for(GeneralComponent output: genCirc.outputs) {
			ValueChangeList changesList = new ValueChangeList();
			this.outputValueChangeList.put(output, changesList);
			this.componentValueChangeList.put(output, changesList);
		}
		//setInputsToDefault();
		
		// This is perhaps the worst code I'll ever write
		//ArrayList<ValueChangeList> changeLists = new ArrayList<ValueChangeList>();
		//ValueChangeList wChanges = new ValueChangeList(new ValueChange[] {new ValueChange(Value.FALSE, 0), new ValueChange(Value.TRUE, 150)}); // for w
		//ValueChangeList xChanges = new ValueChangeList(new ValueChange[] {new ValueChange(Value.FALSE, 0), new ValueChange(Value.TRUE, 100)}); // for x
		//ValueChangeList yChanges = new ValueChangeList(new ValueChange[] {new ValueChange(Value.FALSE, 0), new ValueChange(Value.TRUE, 50)}); // for y
		//ValueChangeList zChanges = new ValueChangeList(new ValueChange[] {new ValueChange(Value.FALSE, 0), new ValueChange(Value.TRUE, 150)}); // for z
		
		/*this.setValueChangeListOfInputByName("w", wChanges);
		this.setValueChangeListOfInputByName("x", xChanges);
		this.setValueChangeListOfInputByName("y", yChanges);
		this.setValueChangeListOfInputByName("z", zChanges);*/
		
		//setInputsTo(changeLists);
		//setInputsToDefault();
		
		// After this, it should simulate all values (with or without the delay added in) and 
		// Should respond immediately to when the user pulls values low or high.
	}
	
	public Project getProject() {
		return this.genCirc.proj;
	}
	
	public void setMaxTime(double maxTime) {
		this.maxNanoseconds = maxTime;
	}
	
	public void setInterval(double interval) {
		this.interval = interval;
	}
	
	
	/**
	 * This sets the values to simulate all possibilities in numerical order (NOT Grey Code)
	 * 
	 * --Update to the overall valueChanges list has not yet been implemented
	 */
	public void setInputsToDefault() {   
		// This needs to continue to make the inputs switch up until the max nanoseconds. Currently
		// it just does it up through all possibilities
		//double numPossibilities = Math.pow(2, inputMaps.size());
		String bitString;
		ArrayList<ValueChangeList> inputChangeLists = new ArrayList<ValueChangeList>(this.inputValueChangeList.values());
		//ArrayList<GeneralComponent> inputChangeLists = new ArrayList<GeneralComponent>(this.inputValueChangeList.);
		// add while loop to iterate until max nanoseconds is reached
		for(int i = 0; i < maxNanoseconds/interval /*numPossibilities*/; i++) {
			bitString = Integer.toBinaryString(i);
			while(bitString.length() < maxNanoseconds/interval) {
				bitString="0" + bitString;
			}
			String reversed = "";
			for(int k = bitString.length()-1; k >= 0; k--) {
				reversed+=bitString.charAt(k);
			}
			bitString = reversed;
			for(int j = 0; j < this.inputValueChangeList.size(); j++) {
				Value val;
				val = ( bitString.charAt(j)=='0' ? Value.FALSE: Value.TRUE );
				// NIL, UNKNOWN, and ERROR
				inputChangeLists.get(j).addValueChange(val, ((double)i)*interval);
				getKeyByValue(this.inputValueChangeList, inputChangeLists.get(j)).getValMap().addValueUpdateIgnorePrev(val, ((double)i)*interval);
				this.valueChanges.addValueChange(val, ((double)i)*interval);
				this.componentValueChangeList.get(getKeyByValue(this.inputValueChangeList, inputChangeLists.get(j))).addValueChange(val, ((double)i)*interval);
			}
		}
		
		/*ArrayList<ValueChangeList> list = new ArrayList<ValueChangeList>();
		list.addAll(this.inputValueChangeList.values());
		for(ValueChangeList changeList : list) {
			System.out.println("Hey Im a " + getKeyByValue(this.inputValueChangeList, changeList).getComponentName() + " with the label " + getKeyByValue(this.inputValueChangeList, changeList).getLabel());
			getKeyByValue(this.inputValueChangeList, changeList).getValMap().printMap();
			changeList.printSimulationRecord();
		}*/
		
	}
	
	public void setInputsHigh() {
		setInputsTo(Value.TRUE);
	}
	
	public void setInputsLow() {
		setInputsTo(Value.FALSE);
	}
	
	public void setInputsTo(ArrayList<ValueChangeList> changeLists) {
		for(int j = 0; j < this.genCirc.inputs.size(); j++) {
			GeneralComponent inputPin = this.genCirc.inputs.get(j);
			ValueChangeList list = this.inputValueChangeList.get(inputPin);	
			list.setTo(changeLists.get(j));
			inputPin.getValMap().createFrom(list);
			this.valueChanges.addAll(list);
			this.componentValueChangeList.get(inputPin).setTo(list);
		}
	}
	
	/**
	 * Finds the pin with label {@code name}, and sets its ValueChange at time {@code time} 
	 * to a value of {@code val}. If a ValueChange already exists for that time, the new Value will 
	 * replace the old one. If one doesn't exist, then one is created and added. 
	 * 
	 * However, if no input exists with name {@code name}, then no ValueChanges are added or changed.
	 * 
	 * @param name - the name of the input pin
	 * @param val - the value to change to
	 * @param time - the time at which the change should occur
	 */
	public void setValueChangeOfInputByName(String name, Value val, double time) {
		for(GeneralComponent pin: this.genCirc.inputs) {
			if(pin.getLabel().equals(name)) {
				setValueChangeOfInputByComponent(pin, val, time);
			}
		}
	}
	
	public void setValueChangeOfInputByComponent(GeneralComponent pin, Value val, double time) {
		ValueChangeList list = this.inputValueChangeList.get(pin);
		list.setChangeTo(val, time);
		pin.getValMap().createFrom(list);
		this.valueChanges.addAll(list);
		this.componentValueChangeList.get(pin).setTo(list);
		return;
	}
	
	public void clearResults() {
		Set<Entry<GeneralComponent, ValueChangeList>> inputPairs =  inputValueChangeList.entrySet();
		for(Entry<GeneralComponent, ValueChangeList> pair: inputPairs) {
			pair.getValue().reset();
		}
		Set<Entry<GeneralComponent, ValueChangeList>> outputPairs =  outputValueChangeList.entrySet();
		for(Entry<GeneralComponent, ValueChangeList> pair: outputPairs) {
			pair.getValue().reset();
		}
		Set<Entry<GeneralComponent, ValueChangeList>> componentPairs =  componentValueChangeList.entrySet();
		for(Entry<GeneralComponent, ValueChangeList> pair: componentPairs) {
			pair.getValue().reset();
		}
		this.valueChanges.reset();
		for(GeneralComponent comp: this.genCirc.genComps) {
			comp.resetSimulationData();
		}
	}
	
	/**
	 * Adds all ValueChanges from {@code list} to the input pin with name {@code name}. Any times already
	 * specified are overwritten. If no input with name {@code name} exists, no changes are made. 
	 * @param name - the name of the input
	 * @param list - the list of ValueChanges to add
	 */
	public void setValueChangeListOfInputByName(String name, ValueChangeList list) {
		for(ValueChange change: list) {
			this.setValueChangeOfInputByName(name, change.getValue(), change.getTimeChanged());
		}
	}
	
	public void setValueChangeListOfInputByComponent(GeneralComponent pin, ValueChangeList list) {
		for(ValueChange change: list) {
			this.setValueChangeOfInputByComponent(pin, change.getValue(), change.getTimeChanged());
		}
	}
	
	public void setInputsTo(Value val) {
		for(int j = 0; j < this.genCirc.inputs.size(); j++) {
			GeneralComponent inputPin = this.genCirc.inputs.get(j);
			ValueChangeList list = this.inputValueChangeList.get(inputPin);
			list.addValueChange(val, 0);
			inputPin.getValMap().createFrom(list);
			this.valueChanges.setTo(list);
			this.componentValueChangeList.get(inputPin).setTo(list);
		}
	}
	
	public void simulateToMaxTime() {
		GeneralPropagator genProp = new GeneralPropagator(genCirc, this);
		 do {
			genProp.step(); // This is where the changes start.
			// So, rather than have steps for each nanosecond, it should jump forward to each change in value.
		} while(genProp.getTime() < this.maxNanoseconds && genProp.getTime() >= 0);
	}
	
	public LinkedHashMap<GeneralComponent, ValueChangeList> getInputMap() {
		return this.inputValueChangeList;
	}
	
	public void addValueChange(GeneralComponent genComp, Value val, double timeChanged) {
		//System.out.println("Adding value change at time " + timeChanged + " to diagram's list.");
		//System.out.println("Also adding value change to value change list for " + genComp.getComponentName() + " at time " + timeChanged + " to the diagram's list for it.");
		this.valueChanges.addValueChange(val, timeChanged);
		this.componentValueChangeList.get(genComp).addValueChange(val, timeChanged);
	}
	
	
	/**
	 * Rounds the specified number to the provided precision. Named precisions are:
	 * <ul>
	 * 	<li> WHOLE - rounds to nearest whole number</li>
	 * 	<li> TENS - rounds to nearest tenth</li>
	 * 	<li> HUNDREDTHS - rounds to nearest hundred</li>
	 * 	<li> THOUSANDTHS - rounds to nearest thousandth</li>
	 * </ul>
	 * Should a precision other than these be required, then provide the number of places
	 * to the right of the radix point to round the number. Negative precisions are supported.
	 * @param a - the number to be rounded
	 * @param precision - the number of places to the right of the radix point to round to
	 * @return the specified number rounded to the given precision.
	 */
	public static double roundToNearest(double a, int precision) {
		//System.out.println("Rounding: " + a);
		double precisionSize = Math.pow(10.0, precision);
		return ((double)Math.round(a * precisionSize)) / precisionSize;
	}
	
	private void roundSimulationResults(Set<Entry<GeneralComponent, ValueChangeList>> pairs) {
		for(Entry<GeneralComponent, ValueChangeList> pair : pairs ) {
			ValueChangeList list = pair.getValue();
			for(ValueChange change : list) {
				change.setTimeChanged(roundToNearest(change.getTimeChanged(), TimingDiagram.TENTHS));
			}
		}
	}
	
	private void formatResults(Set<Entry<GeneralComponent, ValueChangeList>> pairs) {
		for(Entry<GeneralComponent, ValueChangeList> pair : pairs ) {
			ValueChangeList list = pair.getValue();
			list.removeUnneeded();
		}
	}
	
	/**
	 * Provides a set of entries (pairs) of each of the ValueChangeList's and their
	 * corresponding input components, with all times rounded to the nearest tenth.
	 * This is assuming that no delay will amount to less than a tenth of a nanosecond.
	 * @return the set of entries of ValueChangeList's and inputs
	 */
	public Set<Entry<GeneralComponent, ValueChangeList>> getInputSimulationResultsRounded() {
		Set<Entry<GeneralComponent, ValueChangeList>> inputPairs = this.inputValueChangeList.entrySet();
		roundSimulationResults(inputPairs);
		formatResults(inputPairs);
		return inputPairs;
	}
	
	/**
	 * Provides a set of entries (pairs) of each of the ValueChangeList's and their
	 * corresponding non-pin components, with all times rounded to the nearest hundredth.
	 * This is assuming that no delay will amount to less than a hundredth of a nanosecond.
	 * @return the set of entries of ValueChangeList's and inputs
	 */
	public Set<Entry<GeneralComponent, ValueChangeList>> getComponentSimulationResultsRounded() {
		Set<Entry<GeneralComponent, ValueChangeList>> componentPairs = new HashSet<Entry<GeneralComponent, ValueChangeList>>();
		componentPairs.addAll(this.componentValueChangeList.entrySet());
		Iterator<Entry<GeneralComponent, ValueChangeList>> it = componentPairs.iterator();
		while(it.hasNext()) {
			Entry<GeneralComponent, ValueChangeList> pair = it.next();
			if(genCirc.inputs.contains(pair.getKey())) {
				it.remove();
			} else if(genCirc.outputs.contains(pair.getKey())) {
				it.remove();
			}
		}
		roundSimulationResults(componentPairs);
		formatResults(componentPairs);
		return componentPairs;
	}
	
	public Set<Entry<GeneralComponent, ValueChangeList>> getAllSimulationResults() {
		Set<Entry<GeneralComponent, ValueChangeList>> componentPairs = new HashSet<Entry<GeneralComponent, ValueChangeList>>();
		componentPairs.addAll(this.componentValueChangeList.entrySet());
		return componentPairs;
	}
	
	/**
	 * Provides a set of entries (pairs) of each of the ValueChangeList's and their
	 * corresponding output components, with all times rounded to the nearest hundredth.
	 * This is assuming that no delay will amount to less than a hundredth of a nanosecond.
	 * @return the set of entries of ValueChangeList's and inputs
	 */
	public Set<Entry<GeneralComponent, ValueChangeList>> getOutputSimulationResultsRounded() {
		Set<Entry<GeneralComponent, ValueChangeList>> outputPairs = this.outputValueChangeList.entrySet();
		roundSimulationResults(outputPairs);
		formatResults(outputPairs);
		return outputPairs;
	}
	
	public void printFinalSimulationResults() {
		Set<Entry<GeneralComponent, ValueChangeList>> inputPairs = this.getInputSimulationResultsRounded();
		Set<Entry<GeneralComponent, ValueChangeList>> componentPairs = this.getComponentSimulationResultsRounded();
		Set<Entry<GeneralComponent, ValueChangeList>> outputPairs = this.getOutputSimulationResultsRounded();
		
		//System.out.println("Starting the inputs stuff");
		for(Entry<GeneralComponent, ValueChangeList> pair: inputPairs) {
			//System.out.println("Doing some of the inputs stuff");
			System.out.println("The following is the timing tested for the input " + pair.getKey().getLabel());
			pair.getValue().printSimulationRecord();
		}
		//System.out.println("Starting the gencomps stuff");
		for(Entry<GeneralComponent, ValueChangeList> pair: componentPairs) {
			//System.out.println("Doing some of the gencomps stuff");
			System.out.println("The following is the timing tested for a " + pair.getKey().getLabel());
			pair.getValue().printSimulationRecord();
		}
		//System.out.println("Starting the outputs stuff");
		for(Entry<GeneralComponent, ValueChangeList> pair: outputPairs) {
			//System.out.println("Doing some of the outputs stuff");
			System.out.println("The following is the timing tested for the output " + pair.getKey().getLabel());
			pair.getValue().printSimulationRecord();
		}
	}
	
	public ValueChangeList getSimulationResultsFor(GeneralComponent genComp) {
		Set<Entry<GeneralComponent, ValueChangeList>> set = this.getAllSimulationResults();
		for(Entry<GeneralComponent, ValueChangeList> entry : set) {
			if(entry.getKey() == genComp) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	public LinkedHashMap<GeneralComponent, ValueChangeList> getInputValueChangeList() {
		return inputValueChangeList;
	}


	public LinkedHashMap<GeneralComponent, ValueChangeList> getOutputValueChangeList() {
		return outputValueChangeList;
	}


	public LinkedHashMap<GeneralComponent, ValueChangeList> getComponentValueChangeList() {
		return componentValueChangeList;
	}


	public ValueChangeList getValueChanges() {
		return valueChanges;
	}
	
	public double getMaxNanoseconds() {
		return this.maxNanoseconds;
	}
	
	public double getInterval() {
		return this.interval;
	}
	
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
	    for (Entry<T, E> entry : map.entrySet()) {
	        if (value.equals(entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}

	public void printInputValMaps() {
		
		for(GeneralComponent pin : this.genCirc.inputs) {
			System.out.println("The following are for the input: " + pin.getLabel());
			System.out.println("Here is this.inputValueChangesList: ");
			this.inputValueChangeList.get(pin).printSimulationRecord();
			System.out.println("Here is pin.getValMap: ");
			pin.getValMap().printMap();
			System.out.println("Here is this.componentValueChangesList: ");
			this.componentValueChangeList.get(pin).printSimulationRecord();
			System.out.println("----Next----");
		}
		System.out.println("Finally, here is this.valueChanges: ");
		this.valueChanges.printSimulationRecord();
	}
	
}
