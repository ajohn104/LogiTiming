package com.johnston.main;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.cburch.logisim.gui.main.Canvas;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.Projects;
import com.johnston.gui.MousePositionDisplay;

public class Preset extends InstanceFactory {
	
	public Preset() {
		super("Preset");
	}
	
	
	public void paintInstance(InstancePainter painter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagate(InstanceState state) {
		// TODO Auto-generated method stub

	}

}
