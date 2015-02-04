package com.johnston.main;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.Projects;
import com.johnston.circ.GeneralCircuit;
import com.johnston.gui.FrameDisplayControl;
import com.johnston.gui.GeneralFrame;
import com.johnston.timing.TimingDiagram;

public class MyAction extends AbstractAction {
	
	//private Storage storage;
	private Circuit circ;
	private Project proj;
	private FrameDisplayControl control;
	private TimingDiagram diagram;
	
	private final int DEFAULT_INTERVAL = 50;
	
	public MyAction() {
		super("My Menu Item");
	}
	
	public MyAction(Project p) {
		super("My Menu Item");
		this.proj = p;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(control != null) {
			control.unloadFrame();
		}
		
		if(proj == null) {
			Frame frame = Projects.getTopFrame();
			proj = frame.getProject();
		}
		circ = proj.getCurrentCircuit();
		
		GeneralCircuit genCirc = new GeneralCircuit(circ, proj);
		genCirc.build();
		
		setUp();
	}
	
	public void setUp() {
		GeneralCircuit genCirc = new GeneralCircuit(circ, proj);
		genCirc.build();
		
		if(control != null) {
			control.unloadFrame();
		}
		
		diagram = new TimingDiagram(genCirc, DEFAULT_INTERVAL, timeNeeded(genCirc));
		diagram.setInputsToDefault();
		
		control = new FrameDisplayControl(diagram, this);
		control.setUpFrame();
		control.simulateDiagram();
		control.applyData();
		control.setFrameOpen(true);
	}
	
	private int timeNeeded(GeneralCircuit genCirc) {
		return DEFAULT_INTERVAL*(int)Math.pow(2, genCirc.inputs.size());
	}
	
	/*public void setUp(Storage s) {
		storage = s;
	}*/
	
	/*public void setUp(Project p) {
		this.proj = p;
		this.circ = p.getCurrentCircuit();
		System.out.println("Accepting project at mem: " + proj.toString());
		System.out.println("Accepting circ at mem: " + circ.toString());
	}*/

}
