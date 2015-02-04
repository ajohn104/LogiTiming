package com.johnston.main;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.cburch.logisim.analyze.model.AnalyzerModel;
import com.cburch.logisim.circuit.Analyze;
import com.cburch.logisim.circuit.AnalyzeException;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitMutation;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.Propagator;
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.gui.main.Canvas;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceDataSingleton;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.Projects;
import com.cburch.logisim.std.arith.Adder;
import com.cburch.logisim.std.gates.Gates;
import com.cburch.logisim.std.wiring.Pin;
import com.cburch.logisim.util.StringUtil;
import com.johnston.circ.GeneralCircuit;
import com.johnston.circ.GeneralComponent;
import com.johnston.gui.MousePositionDisplay;

public class Storage {
	
	private Project project;
	private Circuit circuit;
	private AnalyzerModel model;
	Canvas canvas;
	ArrayList<String> inputNames;
	ArrayList<String> outputNames;
	private JFrame frame;
	JLabel label;
	
	public Storage(Project proj, Canvas canv) {
		project = proj;
		canvas = canv;
		circuit = project.getCurrentCircuit();
		
		
		//initializeAnalyzerModel();
		//setUpFrame();
		/*for(Component c: project.getCurrentCircuit().getNonWires()) {
			System.out.println(c.getFactory().getDisplayName());
		}*/
		
		/*
		 * Possible parse method: given the equation string, create 
		 * an array storing each the different levels of parenthesis 
		 * separately, such that, for example:
		 * 
		 * (~(a a) + ~(b b) ~(a b))
		 * 
		 * would be stored as: (a a), (b b), (a b), ~(a a), ~(b b), ~(a b),
		 * ~(b b) ~(a b), ~(a a) + ~(b b) ~(a b). 
		 * 
		 * The above followed PCMA (parent, comple, mult, addi) rules, 
		 * but would also need to account for unities of functions, so
		 * it would actually produce:
		 * 
		 * ~(a a), ~(b b), ~(a b), ~(b b) ~(a b), ~(a a) + ~(b b) ~(a b)
		 * 
		 * as the functions it identifies. But, how would it deal with 
		 * an and gate followed by an inverter opposed to a nand gate? 
		 * Because it might not even see the separation in the first case.
		 * Test with Logisim.
		 * 
		 * 
		 * After further analysis, this will not work because gates that
		 * are added in by what I will call "mods" will not necessarily 
		 * have an appropriate symbol. Test w/ logisim.
		 * 
		 */
		
		//model.setVariables(proj.getCurrentCircuit().g), outputs);
		// Needs to store all data about the circuit, such that when it is 
		//instantiated, its getters and setters have the ability to get ANYTHING
		// TODO Auto-generated constructor stub
	}
	
	private void setUpFrame() {
		frame = new JFrame();
		frame.setSize(200, 100);
		label = new JLabel();
		frame.add(label);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	
	public void displayExpression() {
		updateModel();
		label.setText(model.getOutputExpressions().getExpression(outputNames.get(outputNames.size()-1)).toString());
		frame.setVisible(true);
	}
	private void initializeAnalyzerModel() {
		model = new AnalyzerModel();
		updateModel();
	}
	
	// The output is from analyze, by the way
	
	// Needs to prevent the formation of the table when a splitter (or any other unnatural wiring, such as a 7 segment display) is present.
	private void updateModel() {
		Map<Instance, String> pinNames = Analyze.getPinLabels(circuit);
		inputNames = new ArrayList<String>();
		outputNames = new ArrayList<String>();
		for (Map.Entry<Instance, String> entry : pinNames.entrySet()) {
			Instance pin = entry.getKey();
			boolean isInput = Pin.FACTORY.isInputPin(pin);
			if (isInput) {
				inputNames.add(entry.getValue());
			} else {
				outputNames.add(entry.getValue());
			}
			if (pin.getAttributeValue(StdAttr.WIDTH).getWidth() > 1) {
				System.out.println("ERROR: Input size greater than 1 bit.");
				return;
			}
		}
		
		
		//CircuitWires wires = new CircuitWires();
		
		if (inputNames.size() > AnalyzerModel.MAX_INPUTS) {
			System.out.println("ERROR: Max inputs exceeded. Max is 12");
			return;
		}
		if (outputNames.size() > AnalyzerModel.MAX_OUTPUTS) {
			System.out.println("ERROR: Max outputs exceeded. Max is 12");
			return;
		}
		
		model.setCurrentCircuit(project, project.getCurrentCircuit());
		model.setVariables(inputNames, outputNames);
		
		try {
			//buildTree();
			GeneralCircuit genCirc = new GeneralCircuit(circuit, project);
			genCirc.build();
			genCirc.displayFinalOutputs();
		} catch(Exception e) {
			//e.getCause().printStackTrace();
			e.printStackTrace();
		}
		
		// Gates (aka AbstractGate) can computeOutputs based off a given value. Its a method.
		
		try {
			Analyze.computeExpression(model, circuit, pinNames);
		} catch (AnalyzeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//private 
	
	// Caution is needed, this method might not work on functions that output to themselves. 
	// Infinite loops shouldn't be possible, but further testing is recommended.
	private void buildTree() {
		ArrayList<Component> circuitItems = new ArrayList<Component>();
		ArrayList<Component> wireList = new ArrayList<Component>();
		
		
		// Gets all output pins, and then constructs an array of the components by 
		// backing up from those pins.
		for (Map.Entry<Instance, String> entry : Analyze.getPinLabels(circuit).entrySet()) {  
			Instance pin = entry.getKey();
			boolean isInput = Pin.FACTORY.isInputPin(pin);
			//System.out.println("As defined by logisim, an example of an " + (isInput?" input":" output") + " pin is " + pin.getAttributeSet().getValue(StdAttr.LABEL));
			if (!isInput) {
				System.out.println("Starting with the output pin " + pin.getAttributeSet().getValue(StdAttr.LABEL));
				// Much shorter, it locates each input pin and builds from the component connected to its port.
				for(Component c : circuit.getComponents(pin.getPortLocation(0))) {
					if(c != pin && !circuitItems.contains(c)) {
						buildFromComponent(c, circuitItems, wireList);
					}
				}
			}
		}
		System.out.println();
		System.out.println();
		//boolean tested = false;
		try {
		for (Component comp: circuitItems) {
			
			//Pins can't be used here, and the length of vals needs to match the size of the component.
			if(!(comp.getFactory() instanceof Pin)) {
				
				GeneralComponent genComp = new GeneralComponent(comp, project);
				
				Value[] values = new Value[] { Value.TRUE, Value.FALSE, Value.FALSE};
				
				int numInputPorts = 0;
				for(EndData end : comp.getEnds()) {
					if(end.isInput()) numInputPorts++;
				}
				
				int numInputValues = Math.min(values.length, numInputPorts);
				String inputs = " ";
						
				Value[] vals = new Value[numInputValues];
				
				for(int i = 0; i < numInputValues; i++) {
					vals[i] = values[i];
					inputs += vals[i] + " ";
				}
				
				try {
					System.out.println("Type: " + comp.getFactory().getDisplayName());
					System.out.println("Inputs:" + inputs);
					//Value simVal = genComp.simulateV(vals);
					//System.out.println("Output: " + simVal.toDisplayString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
			} else {
				System.out.println("A " + comp.getFactory().getDisplayName() + " was found which is an " + (Pin.FACTORY.isInputPin(Instance.getInstanceFor(comp))? "input " : "output ") + "pin");
			}
			
			
			
			/*if(comp.getFactory().getDisplayName().equals("AND Gate") && !tested) {
				System.out.println("Anding 1 and 1 results in " + (new GeneralComponent(comp, proj)).simulate(new Value[] {Value.TRUE, Value.TRUE}).toDisplayString());
				System.out.println("Anding 1 and 0 results in " + (new GeneralComponent(comp, proj)).simulate(new Value[] {Value.TRUE, Value.FALSE}).toDisplayString());
				System.out.println("Anding 0 and 1 results in " + (new GeneralComponent(comp, proj)).simulate(new Value[] {Value.FALSE, Value.TRUE}).toDisplayString());
				System.out.println("Anding 0 and 0 results in " + (new GeneralComponent(comp, proj)).simulate(new Value[] {Value.FALSE, Value.FALSE}).toDisplayString());
				System.out.println("Anding 1, 1, and 1 results in " + (new GeneralComponent(comp, proj)).simulate(new Value[] {Value.TRUE, Value.TRUE, Value.TRUE}).toDisplayString());
				System.out.println("Anding 1, 1, and 0 results in " + (new GeneralComponent(comp, proj)).simulate(new Value[] {Value.TRUE, Value.TRUE, Value.FALSE}).toDisplayString());
				System.out.println("Anding 1, 0, and 1 results in " + (new GeneralComponent(comp, proj)).simulate(new Value[] {Value.TRUE, Value.FALSE, Value.TRUE}).toDisplayString());
				System.out.println("Anding 0, 1, and 1 results in " + (new GeneralComponent(comp, proj)).simulate(new Value[] {Value.FALSE, Value.TRUE, Value.TRUE}).toDisplayString());
				System.out.println("Anding 0, 0, and 0 results in " + (new GeneralComponent(comp, proj)).simulate(new Value[] {Value.FALSE, Value.FALSE, Value.FALSE}).toDisplayString());
				tested = true;
			}*/
			System.out.println();
		}
		} catch (Exception e) { e.printStackTrace(); }
		/*
		Value[] v1A = { Value.FALSE,  Value.TRUE,  Value.TRUE,  Value.TRUE };
		Value[] v2A = { Value.FALSE,  Value.FALSE,  Value.TRUE,  Value.TRUE };
		
		Value v1 = Value.create(v1A);
		Value v2 = Value.create(v2A);
		Value carry_in = Value.FALSE;
		
		Value result = computeSum(BitWidth.create(4), v1, v2, carry_in)[0];
		Value carry_result = computeSum(BitWidth.create(4), v1, v2, carry_in)[1];

		
		System.out.println("After adding " + v1.toString() + " and " + v2.toString() + " with a carry of " + carry_in.toString());
		System.out.println("We got a result of " + result.toString());
		System.out.println("And a carry of " + carry_result.toString());
		System.out.println();*/
	}
	
	// For testing. Assumes to be "and" gate.
	public Value computeValue(Value[] inputs, Component c) {
		//CircuitState state = new CircuitState();
		Circuit circ = new Circuit("testingCirc");
		CircuitMutation cMut = new CircuitMutation(circ); // This is what would allow me to drop things into the circuit, hopefully.
		Component cpy = c.getFactory().createComponent(Location.create(-50, -50), c.getAttributeSet());
		Instance inst = Instance.getInstanceFor(cpy);
		System.out.println("The instance in the new circuit has location : " + inst.getLocation().toString());
		for(int i = 0; i < inst.getPorts().size(); i++) {
			System.out.println("One port occurs at " + inst.getPortLocation(i).toString());
		}
		for(int i = 0; i < cpy.getEnds().size(); i++) {
			System.out.println("One end occurs at " + cpy.getEnd(i).getLocation().toString());
		}
		
		ArrayList<Component> fakeInputs = new ArrayList<Component>();
		ArrayList<Component> fakeOutputs = new ArrayList<Component>();
		
		ComponentFactory factory = Pin.FACTORY;
		cMut.add(cpy);
		
		for(EndData end : cpy.getEnds()) {
			if(end.isOutput()) {
				
				AttributeSet attrs = factory.createAttributeSet();
				attrs.setValue(StdAttr.FACING, Direction.EAST);
				attrs.setValue(Pin.ATTR_TYPE, Boolean.TRUE);
				attrs.setValue(StdAttr.LABEL, "WHO CARES!");
				attrs.setValue(Pin.ATTR_LABEL_LOC, Direction.NORTH);
				
				
				Component output = factory.createComponent(end.getLocation(), attrs);
				fakeOutputs.add(output);
				cMut.add(output);
				System.out.println("Adding output pin...");
			}
			else if(end.isInput()) {
				
				AttributeSet attrs = factory.createAttributeSet();
				attrs.setValue(StdAttr.FACING, Direction.WEST);
				attrs.setValue(Pin.ATTR_TYPE, Boolean.FALSE);
				attrs.setValue(StdAttr.LABEL, "WHO CARES!");
				attrs.setValue(Pin.ATTR_LABEL_LOC, Direction.NORTH);
				
				
				Component input = factory.createComponent(end.getLocation(), attrs);
				fakeInputs.add(input);
				cMut.add(input);
				System.out.println("Adding input pin...");
			}
		}
		
		cMut.execute();
		System.out.println("getting this far...");
		System.out.println("num all within: " + circ.getNonWires().size());
		for(Component comp : circ.getNonWires()) {
			System.out.println("In our fake circuit we added a " + comp.getFactory().getDisplayName() + " and gave it a location " + comp.getLocation().toString());
			for(int i = 0; i < Instance.getInstanceFor(comp).getPorts().size(); i++) {
				System.out.println("Said component has a port at location " + Instance.getInstanceFor(comp).getPortLocation(i).toString());
			}
		}
		
		CircuitState cs = new CircuitState(project, circ);
		
		try {
			for(int i = 0; i < fakeInputs.size(); i++) {
				poke(fakeInputs.get(i), cs);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			for(int i = 0; i < fakeOutputs.size(); i++) {
				//InstanceData state = ((InstanceState) cs.getInstanceState(fakeOutputs.get(i))).getData(); // The problem is now that InstanceStateImpl cannot be cast to InstanceData
				InstanceState stateNull = cs.getInstanceState(fakeOutputs.get(i));
				stateNull.getData();
				if((fakeOutputs.get(i).getFactory() instanceof Pin)) {
					
					//((Pin) fakeOutputs.get(i).getFactory()).getValue(stateNull);
					//((Pin) fakeOutputs.get(i)).setValue((InstanceState) state, Value.FALSE);
					//cs.setData(fakeOutputs.get(i), state);
				}
				else {}//((Pin) fakeOutputs.get(i).getFactory()).getValue(stateNull); //cs.setData(fakeOutputs.get(i), state); 
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			//Propagator prop = cs.getPropagator();
			//prop.propagate();
			
			
			for(Component input : fakeOutputs) {
				try {
					//((InstanceFactory)input.getFactory()).propagate(((InstanceFactory) input.getFactory()).createInstanceState(cs, input));
					for(int i = 0; i < ((InstanceFactory)input.getFactory()).getPorts().size(); i++) {
						//((InstanceFactory)input.getFactory()).set
						// Needs to set the value of its ports so that EndData is updated?
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			for(int i = 0; i < fakeInputs.size(); i++) {
				/*InstanceState stInput = cs.getInstanceState(fakeInputs.get(i));
				//InstanceState stComp = cs.getInstanceState(cpy);
				//System.out.println("Before we do anything stupid, we can verify the value is " + ((Value)((InstanceDataSingleton) stInput.getData()).getValue()).toDisplayString());
				//cs.setValue(fakeInputs.get(i).getLocation(), ((Value)((Pin) fakeInputs.get(i).getFactory()).getValue(stInput)), fakeInputs.get(i), 1);
				System.out.println("Num of ports: " + stInput.getFactory().getPorts().size());
				System.out.println("And the circuitState now thinks the value at Location " +  fakeInputs.get(i).getLocation().toString() + " is " + cs.getValue(fakeInputs.get(i).getLocation()));
				//stComp.setPort(i, stInput.getPort(0), 1);
				//((InstanceFactory) fakeInputs.get(i).getFactory()).propagate(stInput);
				System.out.println("Value at port 0: " + stInput.getPort(0).toDisplayString());*/
			}
			
			try {
				//((InstanceFactory) cpy.getFactory()).propagate(((InstanceFactory) cpy.getFactory()).createInstanceState(cs, cpy));
				//cs.markComponentAsDirty(cpy);
				//cs.getPropagator().propagate();
				//cs.markPointAsDirty(cpy.getLocation());
				//cs.getPropagator().propagate();
			}
			catch (Exception e) {
				try {
					//cpy.propagate(cs);
					
				}
				catch (Exception e2) {
					e2.printStackTrace();
				}
			}
			
			for(EndData end : cpy.getEnds()) {
				if(end.isInput()) System.out.println("For this input end, we have a value of " + cs.getValue(end.getLocation()));
			}
			
			for(Component output : fakeOutputs) {
				try {
					//((InstanceFactory)output.getFactory()).propagate(((InstanceFactory) output.getFactory()).createInstanceState(cs, output));
				}
				catch (Exception e) {
					e.printStackTrace();
				} // What we need to do is look into the EndData because the EndData of at least the input pins isn't being updated.
			}
			//if(fakeOutputs.size() > 0) fakeOutputs.get(0).propagate(cs);
			
			for(int i = 0; i < fakeOutputs.size(); i++) {
				//InstanceState stOutput = cs.getInstanceState(fakeOutputs.get(i));
				//InstanceState stComp = cs.getInstanceState(cpy);
				//stOutput.setPort(i, stComp.getPort(0), 1);
			}
			
			if(fakeOutputs.size() > 0) {
				//cs.markPointAsDirty(fakeOutputs.get(0).getLocation());
				cs.getPropagator().propagate();
				/*InstanceState state = ((InstanceFactory) fakeOutputs.get(0).getFactory()).createInstanceState(cs, fakeOutputs.get(0));
				List<EndData> dataList = Instance.getComponentFor(state.getInstance()).getEnds();
				Value v = cs.getValue(dataList.get(0).getLocation()); // For the output pins, who only have inputs.
				for(EndData end : dataList) {
					if(end.isOutput()) v = cs.getValue(end.getLocation());
				}
				
				// I need to do this to the inputs!!! This should make a pinstate!! 
				
				
				 * 
				 * InstanceState stateNull = cs.getInstanceState(fakeOutputs.get(i));
				InstanceData d = stateNull.getData();
				d = null;
				 * 
				 
				
				System.out.println("For the circuit we simulated, we have the value " + v.toDisplayString() + " with a width " + v.getWidth());
			*/
				System.out.println( "for this pin we have the value " + Pin.FACTORY.getValue(cs.getInstanceState(fakeOutputs.get(0))).toDisplayString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
		
		
		/*
		System.out.println("We tried to make a pin in the new circuit and we got a " + input.getFactory().getDisplayName() + " with Location: " + input.getLocation().toString());
		for(int i = 0; i < Instance.getInstanceFor(input).getPorts().size(); i++) {
			System.out.println("It has a port at location: " + Instance.getInstanceFor(input).getPortLocation(i));
		}
		*/
		
		// What this needs to do is create a gate, and place enough inputs on the gate's ports to represent each input specified
		// It also needs to place an output on the output port of the gate.
		// Then it would set each of the inputs to their specified levels, and propagate the circuit.
		// Then it would just need to get the output's level.
		
		// My current plan is to see where the ports line up with other ports, but perhaps I should re-check
		// the code I made for buildFromComponent and see if it's recyclable? Might save time and a headache.
		
		//if(InstanceFactory cpy)
		//return Gate.computeValue(c, inputs);
		return null;
	}
	
	// affects the outputs
	static void poke(Component pin, CircuitState cs) {
		if(Instance.getInstanceFor(pin).getAttributeValue(StdAttr.WIDTH).getWidth() != 1) {
			System.out.println("You cannot use bits with length greater than 1");
			return;
		}
		else {
			//needs to poke the pin!
			InstanceState stateNull = cs.getInstanceState(pin);
			stateNull.getData();
			//Value val = ((Pin) pin.getFactory()).getValue(stateNull);
			((Pin) pin.getFactory()).setValue(stateNull, Value.TRUE);
			cs.setValue(pin.getLocation(), (Value) ((Pin) pin.getFactory()).getValue(stateNull), pin, 1);
			cs.markPointAsDirty(pin.getLocation());
			//cs.getPropagator().propagate();
			//cs.setData(pin, new InstanceDataSingleton(Value.TRUE)); // Sets the value of the pin
			//Pin.PinPoker poker = new Pin.PinPoker(); // Does nothing.
			
			//InstanceState state = ((InstanceFactory) pin.getFactory()).createInstanceState(cs, pin);
			System.out.println("For the pin we created, we have the value " + ((Value) ((Pin) pin.getFactory()).getValue(stateNull)).toDisplayString());
			for(EndData d : pin.getEnds()) {
				if(d.isOutput()) {
					System.out.println("After finding said value, we find that the circuitState (via the 'endData') thinks the value is " + cs.getValue(d.getLocation()).toDisplayString() + " which is equal to Value.UNKNOWN: " + (cs.getValue(d.getLocation()) == Value.UNKNOWN));
				}
			}
			// We now can set and get the value of pins. We will need to set all the values 
			// of the pins to the appropriate values, and we will need to propagate.
			// All in all, propagation is the next step!
			
		}
	}
	
	
	
	/*static Value[] computeSum(BitWidth width, Value a, Value b, Value c_in) {
		int w = width.getWidth();
		if (c_in == Value.UNKNOWN || c_in == Value.NIL) c_in = Value.FALSE;
		if (a.isFullyDefined() && b.isFullyDefined() && c_in.isFullyDefined()) {
			if (w >= 32) {
				long mask = (1L << w) - 1;
				long ax = (long) a.toIntValue() & mask;
				long bx = (long) b.toIntValue() & mask;
				long cx = (long) c_in.toIntValue() & mask;
				long sum = ax + bx + cx;
				return new Value[] { Value.createKnown(width, (int) sum),
					((sum >> w) & 1) == 0 ? Value.FALSE : Value.TRUE };
			} else {
				int sum = a.toIntValue() + b.toIntValue() + c_in.toIntValue();
				return new Value[] { Value.createKnown(width, sum),
					((sum >> w) & 1) == 0 ? Value.FALSE : Value.TRUE };
			}
		} else {
			Value[] bits = new Value[w];
			Value carry = c_in;
			for (int i = 0; i < w; i++) {
				if (carry == Value.ERROR) {
					bits[i] = Value.ERROR;
				} else if (carry == Value.UNKNOWN) {
					bits[i] = Value.UNKNOWN;
				} else {
					Value ab = a.get(i);
					Value bb = b.get(i);
					if (ab == Value.ERROR || bb == Value.ERROR) {
						bits[i] = Value.ERROR;
						carry = Value.ERROR;
					} else if (ab == Value.UNKNOWN || bb == Value.UNKNOWN) {
						bits[i] = Value.UNKNOWN;
						carry = Value.UNKNOWN;
					} else {
						int sum = (ab == Value.TRUE ? 1 : 0)
							+ (bb == Value.TRUE ? 1 : 0)
							+ (carry == Value.TRUE ? 1 : 0);
						bits[i] = (sum & 1) == 1 ? Value.TRUE : Value.FALSE;
						carry = (sum >= 2) ? Value.TRUE : Value.FALSE;
					}
				}
			}
			return new Value[] { Value.create(bits), carry };
		}
	}*/
	
	
	
	private void buildFromComponent(Component comp, ArrayList<Component> compList, ArrayList<Component> wireList) {
		for(EndData end : comp.getEnds()) {
			if(end.isInput()) {
				for(Component c : circuit.getComponents(end.getLocation())) {
					
					if(!compList.contains(c) && (comp != c) && !(c.getFactory() instanceof Pin) && !(wireList.contains(c))) {
						boolean isNotInputPort = true;
						for(EndData e : c.getEnds()) {
							if(e.getLocation().equals(end.getLocation()) && e.isInput()) isNotInputPort = false;
						}
						
						if(!(c instanceof Wire)) {
							if(isNotInputPort) {    // This works perfectly, but may block the detection of inputs that have already been seen. 
													// And paths, actually. Not quite sure. It seems to work, and doesn't block paths, but I have no idea, honestly.
								if(!compList.contains(c) && (comp != c) && (c.getFactory() instanceof Pin) && !(c.getAttributeSet().getValue(Pin.ATTR_TYPE)==Boolean.TRUE?true:false)) {
									compList.add(c);
									System.out.println("Found a " + c.getFactory().getDisplayName() + " named " + c.getAttributeSet().getValue(StdAttr.LABEL) + " who is an " + (c.getAttributeSet().getValue(Pin.ATTR_TYPE)==Boolean.TRUE?" output":" input") + " pin.");
								}
								else {
									compList.add(c);
									System.out.println("Found a " + c.getFactory().getDisplayName() + " who is an input to " + comp.getFactory().getDisplayName());
								}
							}
						}
						else {
							wireList.add(c);
						}
						buildFromComponent(c, compList, wireList);
						
					}
					
					
				}
			}
		}
	}

}
