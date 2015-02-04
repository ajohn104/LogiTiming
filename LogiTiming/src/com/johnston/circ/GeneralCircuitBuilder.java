package com.johnston.circ;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cburch.logisim.circuit.Analyze;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitClone;
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.circuit.WireBundleClone;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.wiring.Pin;

public class GeneralCircuitBuilder {
	private Map<Component, GeneralComponent> compMap = new HashMap<Component, GeneralComponent>();
	private Project prj;
	private Circuit crct;
	private CircuitClone circClone;
	private ArrayList<GeneralComponent> outputs;
	public GeneralCircuitBuilder(Circuit circ, Project proj) {
		this.prj = proj;
		this.crct = circ;
		this.circClone = new CircuitClone(circ);
		this.outputs = new ArrayList<GeneralComponent>();
		
		ArrayList<Component> outputs = new ArrayList<Component>();
		
		for (Map.Entry<Instance, String> entry : Analyze.getPinLabels(circ).entrySet()) {  
			Instance pin = entry.getKey();
			boolean isOutput = !(Pin.FACTORY.isInputPin(pin));
			if (isOutput) {
				outputs.add(Instance.getComponentFor(pin));
			}
		}
		
		for(Component pinComp: outputs) {
			GeneralComponent pinGen = new GeneralComponent(pinComp, prj);
			this.outputs.add(pinGen);
			bldFrmCmp(pinComp, pinGen);
		}
		
	}
	
	public ArrayList<GeneralComponent> getGeneralOutputs() {
		return this.outputs;
	}
	
	public ArrayList<GeneralComponent> getGeneralComponents() {
		ArrayList<GeneralComponent> components = new ArrayList<GeneralComponent>();
		components.addAll(compMap.values());
		return components;
	}
	
	private void bldFrmCmp(Component c, GeneralComponent genC) {
		compMap.put(c, genC);
		ArrayList<Component> inputComps = getInputsTo(c);
		for(Component comp : inputComps) {
			GeneralComponent genCompForComp;
			if(compMap.containsKey(comp)) {
				genCompForComp = compMap.get(comp);
				genCompForComp.addOutputTo(genC);
			}
			if(!compMap.containsKey(comp)) {
				genCompForComp = new GeneralComponent(comp, prj);
				genCompForComp.addOutputTo(genC);
				bldFrmCmp(comp, genCompForComp);			
			}
		}
	}
	
	/**
	 * In theory, this will gather up all the inputs to any given component.
	 * @param c
	 * @return
	 */
	private ArrayList<Component> getInputsTo(Component c) {
		ArrayList<Component> comps = new ArrayList<Component>(); // Create list of inputs
		for(EndData end: c.getEnds()) {			// Iterate through ports
			if(end.isInput()) {					// Only input ports
				Object[] points = getBundledPoints(end.getLocation());	// Create list of points in bundle
				for(Object loc : points) {			// Iterate through all bundled points of current input
					Location pt = (Location) loc;		// Cast it to a Location
					for(Component comp: crct.getComponents(pt)) {	// Iterate through all components at each point
						/* Stop if new component is found and the connection is at an output of said
						 * component (Relies on the component's location being the same as its output. 
						 * To be stronger/reliable would require more processing power. It would need 
						 * to iterate through the enddata of the component found and find its output, 
						 * then compare the two locations, outputting false if not equal or if no 
						 * output was found )
						 */
						if(comp != c && !(comp instanceof Wire) && pt.equals(comp.getLocation()) /*&& !comps.contains(comp)*/) {	
							comps.add(comp);
						}
					}
				}
			}
		}
		return comps;
	}
	
	private Object[] getBundledPoints(Location loc) {
		WireBundleClone cloneAt = circClone.wires.getWireBundleClone(loc);
		Object[] points = {};
		if(cloneAt != null) points = cloneAt.points.toArray();
		return points;
	}
}
