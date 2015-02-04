package com.johnston.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;

import com.cburch.logisim.data.Value;
import com.johnston.circ.GeneralComponent;
import com.johnston.main.MyAction;
import com.johnston.timing.TimingDiagram;
import com.johnston.timing.ValueChange;
import com.johnston.timing.ValueChangeList;

public class FrameDisplayControl {
	
	static final int SCALAR = 3;
	int numCellVis = 4;
	static final int MAX_NUM_CELL_VISIBLE = 8;
	
	MyAction action;
	TimingDiagram diagram;
	GeneralFrame frame;
	ArrayList<ValueChangeDisplayPanel> valueDisplayPanels;
	ArrayList<ChartDisplayPanel> chartDisplayPanels;

	double interval;
	double maxTime;
	int valueDisplayWidth;
	int valueDisplayHeight;
	int chartDisplayWidth;
	int chartDisplayHeight;
	boolean onWhite;
	boolean realTime;
	boolean collation;

	
	/*
	 * Ok, so the way this works is that the TD will create a new FDC when the menu icon is clicked.
	 * Once the FDC is told to set up the frame, it should create a frame, load up all data and add it to the frame,
	 * and then set the frame as visible. The event listeners for when the user drags an inputs value should also be 
	 * here. Since only true and false will be available via a drag, the other Values must be obtainable through a
	 * text editor, similar to that shown in the poster and in the GeneralFrame. However, the values for 
	 */
	
	public FrameDisplayControl(TimingDiagram diagram, MyAction action) {
		this.action = action;
		this.diagram = diagram;
		this.valueDisplayPanels = new ArrayList<ValueChangeDisplayPanel>();
		this.chartDisplayPanels = new ArrayList<ChartDisplayPanel>();
		this.onWhite = false;
		this.realTime = false;
		initiateKeyboardFocus();
	}
	
	public void setFrameOpen(boolean bool) {
		this.frame.setVisible(bool);
		if(bool) {
			revalidateScrollPane();
		}
	}
	
	public void revalidateScrollPane() {
		numCellVis = Math.min(MAX_NUM_CELL_VISIBLE, this.chartDisplayPanels.size());
		this.frame.infoPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE,numCellVis*ChartDisplayPanel.CELL_HEIGHT));
		this.frame.valueDiagram.setPreferredSize(new Dimension(valueDisplayWidth, valueDisplayHeight));
		this.frame.diagramScrollPane.revalidate();
		
		this.frame.propertiesPanel.setPreferredSize(new Dimension(this.chartDisplayWidth,this.chartDisplayHeight));
		
		//this.frame.propertiesPanel.setSize(new Dimension(this.chartDisplayWidth,this.chartDisplayHeight));
		this.frame.chartScrollPane.revalidate();
	}
	
	/*
	 * Since the data is only loaded from the table, whenever the user makes a change via a drag, it should
	 * also affect the table as well. Also, the user should be given the option (with some warning about the
	 * performance hit) to turn on real-time analysis, so that when they change the value of an input in the
	 * frame, the frame will immediately recalculate and reflect that change. This applies to when the user
	 * simply changes the value via a drag (in which case it reflects after mouse release if a change occurred),
	 * and to the table (via exiting focus on the cell if a change occurred). It may also help to add a button
	 * that brings up a dialog for users to add ValueChanges to the time of their choice (limit to intervals?),
	 * which would also cause a recalculation if a change occurred. 
	 */
	
	/**
	 * Creates a new frame, disposing of the old frame if still open. Then it loads in 
	 * the default input settings and values, then opens the new frame. 
	 */
	public void setUpFrame() {
		
		frame = new GeneralFrame();
		if(diagram != null) {
			this.setInterval((int)diagram.getInterval());
			this.setMaxTime((int)diagram.getMaxNanoseconds());
			this.setValueDisplayWidth();
			frame.setTitle("Timing Diagram for " + diagram.getProject().getLogisimFile().getDisplayName());
		}
		frame.refreshBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshFrameData();
			}
		});
		frame.loadTimePrefsBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadTimePrefs();
			}
		});
		frame.chckbxRealTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				realTime = frame.chckbxRealTime.isSelected();
				if(realTime) {
					refreshFrameData();
				}
			}
		});
		frame.chckbxCollation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				collation = !collation;		// Its a straight toggle, since ctrl may be depressed at the time of the event
			}
		});
		frame.reloadCircBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String confirmText = "Are you sure? This will clear your current"
						+ " preferences and load the data from your current circuit.";
				int response = JOptionPane.showConfirmDialog(null, confirmText, "Confirm", 
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				
				switch(response) {
					case JOptionPane.NO_OPTION:
						return;
					case JOptionPane.YES_OPTION:
						action.setUp();
						return;
					case JOptionPane.CANCEL_OPTION:
						return;
				}
			}
		});
		/*KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			public boolean dispatchKeyEvent(KeyEvent e) {				
				switch(e.getKeyCode()) {
					case KeyEvent.VK_CONTROL:
					case KeyEvent.META_DOWN_MASK:
						System.out.println("Changing collation to: " + !collation);
						System.out.println("That was caused by: " + ((e.getID()==KeyEvent.KEY_PRESSED)?"key pressed":"key released"));
						collation = !collation;
						break;
					default:
						break;
				}
				return false;
			}
		});*/
		this.frame.diagramScrollPane.getVerticalScrollBar().setUnitIncrement(3);
		this.frame.chartScrollPane.getVerticalScrollBar().setUnitIncrement(3);
	}
	
	public void loadTimePrefs() {
		loadMaxTime();
		loadInterval();
		refreshFrameData();
	}
	
	public void loadMaxTime() {
		this.maxTime = Double.parseDouble(this.frame.maxTimeInput.getValue().toString());
		this.diagram.setMaxTime(this.maxTime);
		for(ValueChangeDisplayPanel display: this.valueDisplayPanels) {
			if(display.comp.isInputPin()) {
				Iterator<ValueChange> it = display.list.iterator();
				while(it.hasNext()) {
					if(it.next().getTimeChanged() >= this.maxTime) {
						it.remove();
					}
				}
			}
		}
	}
	
	public void loadInterval() {
		this.interval = Double.parseDouble(this.frame.intervalInput.getValue().toString());
		this.diagram.setInterval(this.interval);
		for(ValueChangeDisplayPanel display: this.valueDisplayPanels) {
			if(display.comp.isInputPin()) {
				Iterator<ValueChange> it = display.list.iterator();
				while(it.hasNext()) {
					ValueChange change = it.next();
					change.setTimeChanged(change.getTimeChanged()/display.interval*this.interval);
				}
			}
		}
	}
	
	/**
	 * Reads the user specified input values, creates ValueChangeLists, and 
	 * applies them to the TimingDiagram, recalculating everything.
	 */
	public void loadUserInputData() {
		this.diagram.clearResults();
		//System.out.print("I just cleared the data. Here are the pins' remaining data");
		//diagram.printInputValMaps();
		for(ValueChangeDisplayPanel display: this.valueDisplayPanels) {
			if(display.comp.isInputPin()) {
				ValueChangeList list = display.getList();
				diagram.setValueChangeListOfInputByComponent(display.comp, list);
			}
		}
		//System.out.print("I just filled the data. Here are the pins' data");
		//diagram.printInputValMaps();
	}
	
	/**
	 * Simulates the Timing Diagram with its current input settings.
	 */
	public void simulateDiagram() {
		this.diagram.simulateToMaxTime();
	}
	
	/**
	 * Loads the user's data specified by the table, simulates it, then applies the
	 * new data to the frame, adding the appropriate interface portions.
	 */
	public void refreshFrameData() {
		//System.out.println("------------------------------------------------------------------");
		loadUserInputData();
		simulateDiagram();
		applyData();
		revalidateScrollPane();
		frame.repaint();
	}
	
	/**
	 * Takes all loaded data and creates the associated interface portions,
	 * adding listeners where needed. 
	 */
	public void applyData() {
		// These should have already happened in loadData. Or the diagram itself. We only need the components.
		Set<Entry<GeneralComponent, ValueChangeList>> inputPairs = diagram.getInputSimulationResultsRounded();
		Set<Entry<GeneralComponent, ValueChangeList>> componentPairs = diagram.getComponentSimulationResultsRounded();
		Set<Entry<GeneralComponent, ValueChangeList>> outputPairs = diagram.getOutputSimulationResultsRounded();
		
		this.frame.valueDiagram.removeAll();
		this.frame.propertiesPanel.removeAll();
		this.valueDisplayPanels.clear();
		this.chartDisplayPanels.clear();
		
		this.chartDisplayWidth = 0;
		this.chartDisplayHeight = 0;
		this.valueDisplayHeight = 0;
		this.setValueDisplayWidth();
		
		this.onWhite = false;
		
		ArrayList<Component> components = new ArrayList<Component>();
		Component[] comps = this.frame.valueDiagram.getComponents();
		for(int i = 0; i < comps.length; i++) {
			components.add(comps[i]);
		}
		
		
		for(Entry<GeneralComponent, ValueChangeList> pair: inputPairs) {
			this.addValueDisplayPanel(pair.getKey());
			this.addChartDisplayPanel(pair.getKey());
		}
		//System.out.println("Starting the gencomps stuff");
		for(Entry<GeneralComponent, ValueChangeList> pair: componentPairs) {
			this.addValueDisplayPanel(pair.getKey());
			this.addChartDisplayPanel(pair.getKey());
		}
		//System.out.println("Starting the outputs stuff");
		for(Entry<GeneralComponent, ValueChangeList> pair: outputPairs) {
			this.addValueDisplayPanel(pair.getKey());
			this.addChartDisplayPanel(pair.getKey());
		}
		for(ValueChangeDisplayPanel valDisplay: this.valueDisplayPanels) {
			valDisplay.updateData();
		}
		for(ChartDisplayPanel chart: this.chartDisplayPanels) {
			chart.updateData();
			this.chartDisplayWidth = Math.max(this.chartDisplayWidth, chart.getFullWidth());
		}
		//diagram.printFinalSimulationResults();
	}
	
	private void addValueDisplayPanel(GeneralComponent comp) {
		ValueChangeDisplayPanel display = new ValueChangeDisplayPanel(comp, diagram, this.onWhite, this.valueDisplayWidth, this.interval, this);
		display.setLocation(0, valueDisplayHeight);
		this.valueDisplayPanels.add(display);
		this.frame.valueDiagram.add(display);
		valueDisplayHeight+=ValueChangeDisplayPanel.HEIGHT;
		Divider div = new Divider(this.valueDisplayWidth, this.interval);
		div.setLocation(0, valueDisplayHeight);
		this.frame.valueDiagram.add(div);
		valueDisplayHeight+=Divider.HEIGHT;
		onWhite = !onWhite;
		revalidateScrollPane();
	}
	
	private void addChartDisplayPanel(GeneralComponent comp) {
		ChartDisplayPanel display = new ChartDisplayPanel(comp, diagram);
		display.setLocation(0, chartDisplayHeight);
		this.chartDisplayPanels.add(display);
		this.frame.propertiesPanel.add(display);
		this.chartDisplayHeight+=ChartDisplayPanel.CELL_HEIGHT;
		this.chartDisplayWidth = Math.max(this.chartDisplayWidth, display.getFullWidth());
		revalidateScrollPane();
	}
	
	private void refreshChartWidth() {
		for(ChartDisplayPanel panel: this.chartDisplayPanels) {
			this.chartDisplayWidth = Math.max(this.chartDisplayWidth, panel.getFullWidth());
		}
		revalidateScrollPane();
	}

	private void setInterval(int interval) {
		this.interval = interval;
		this.frame.intervalInput.setValue(new Integer(interval));
	}

	private void setMaxTime(int maxTime) {
		this.maxTime = maxTime;
		this.frame.maxTimeInput.setValue(new Integer(maxTime));
	}
	
	private void setValueDisplayWidth() {
		this.valueDisplayWidth = (int)(this.maxTime * SCALAR + ValueChangeDisplayPanel.LABEL_AREA_WIDTH + ValueChangeDisplayPanel.DISPLAY_OFFSET);
	}
	
	public void inputChanged() {
		if(realTime) {
			refreshFrameData();
		}
	}
	
	public void unloadFrame() {
		this.frame.dispose();
	}
	
	
	private static boolean collateKeyPressed = false;
    public static boolean collateKeyPressed() {
        synchronized (FrameDisplayControl.class) {
            return collateKeyPressed;
        }
    }
    
 // Key Pressed Code courtesy of Elist on stack overflow

    static void initiateKeyboardFocus() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

            public boolean dispatchKeyEvent(KeyEvent ke) {
                synchronized (FrameDisplayControl.class) {
                    switch (ke.getID()) {
                    case KeyEvent.KEY_PRESSED:
                        if (ke.getKeyCode() == KeyEvent.VK_CONTROL) {
                        	collateKeyPressed = true;
                        }
                        if (ke.getKeyCode() == KeyEvent.META_DOWN_MASK) {
                        	collateKeyPressed = true;
                        }
                        break;

                    case KeyEvent.KEY_RELEASED:
                        if (ke.getKeyCode() == KeyEvent.VK_CONTROL) {
                        	collateKeyPressed = false;
                        }
                        if (ke.getKeyCode() == KeyEvent.META_DOWN_MASK) {
                        	collateKeyPressed = false;
                        }
                        break;
                    }
                    return false;
                }
            }
        });
    }
	
	
	
	
	public static void main(String[] args) {
		
		FrameDisplayControl frameControl = new FrameDisplayControl(null, null);
		
		frameControl.setInterval(50);
		frameControl.setMaxTime(300);
		frameControl.setValueDisplayWidth();
		frameControl.setUpFrame();
		//frameControl.frame = new GeneralFrame();
		
		frameControl.addValueDisplayPanel(null);
		frameControl.addValueDisplayPanel(null);
		frameControl.addValueDisplayPanel(null);
		frameControl.addValueDisplayPanel(null);
		
		ValueChangeList wChanges = new ValueChangeList(new ValueChange[] {new ValueChange(Value.FALSE, 0), new ValueChange(Value.TRUE, 150)}); // for w
		ValueChangeList xChanges = new ValueChangeList(new ValueChange[] {new ValueChange(Value.FALSE, 0), new ValueChange(Value.TRUE, 100)}); // for x
		ValueChangeList yChanges = new ValueChangeList(new ValueChange[] {new ValueChange(Value.FALSE, 0), new ValueChange(Value.TRUE, 50)}); // for y
		ValueChangeList zChanges = new ValueChangeList(new ValueChange[] {new ValueChange(Value.FALSE, 0), new ValueChange(Value.TRUE, 150)}); // for z
		
		frameControl.valueDisplayPanels.get(0).setData(wChanges);
		frameControl.valueDisplayPanels.get(0).setComponentName("w");
		frameControl.valueDisplayPanels.get(1).setData(xChanges);
		frameControl.valueDisplayPanels.get(1).setComponentName("x");
		frameControl.valueDisplayPanels.get(2).setData(yChanges);
		frameControl.valueDisplayPanels.get(2).setComponentName("y");
		frameControl.valueDisplayPanels.get(3).setData(zChanges);
		frameControl.valueDisplayPanels.get(3).setComponentName("z");
		
		frameControl.addChartDisplayPanel(null);
		frameControl.addChartDisplayPanel(null);
		frameControl.addChartDisplayPanel(null);
		frameControl.addChartDisplayPanel(null);
		frameControl.chartDisplayPanels.get(0).setData(wChanges);
		frameControl.chartDisplayPanels.get(0).setComponentName("w");
		frameControl.chartDisplayPanels.get(1).setData(xChanges);
		frameControl.chartDisplayPanels.get(1).setComponentName("x");
		frameControl.chartDisplayPanels.get(2).setData(yChanges);
		frameControl.chartDisplayPanels.get(2).setComponentName("y");
		frameControl.chartDisplayPanels.get(3).setData(zChanges);
		frameControl.chartDisplayPanels.get(3).setComponentName("z");
		
		frameControl.refreshChartWidth();
		
		frameControl.frame.refreshBtn.setEnabled(false);
		
		/*JScrollBar barH = frameControl.frame.valueDiagram.createHorizontalScrollBar();
		JScrollBar barV = frameControl.frame.valueDiagram.createVerticalScrollBar();
		barH.*/
		//frameControl.frame.valueDiagram.s
		frameControl.setFrameOpen(true);
		//frameControl.refreshChartWidth();
		frameControl.frame.diagramScrollPane.getVerticalScrollBar().setUnitIncrement(3);
		
		/*System.out.println(frameControl.frame.scrollPane.getVerticalScrollBar().getUnitIncrement());
		System.out.println(frameControl.frame.valueDiagram.getSize().toString());
		System.out.println(frameControl.frame.scrollPane.getSize().toString());*/
		
		
	}
	
	

}
