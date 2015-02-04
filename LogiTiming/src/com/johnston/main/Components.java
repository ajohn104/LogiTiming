package com.johnston.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.cburch.logisim.file.LibraryEvent;
import com.cburch.logisim.file.LibraryListener;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.proj.Action;
import com.cburch.logisim.proj.Dependencies;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.Projects;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;
import com.johnston.gui.MousePositionDisplay;

/** The library of components that the user can access. */
public class Components extends Library {
    /** The list of all tools contained in this library. Technically,
     * libraries contain tools, which is a slightly more general concept
     * than components; practically speaking, though, you'll most often want
     * to create AddTools for new components that can be added into the circuit.
     */
    private List<Tool> tools;
    private static ArrayList<Project> loadedProjects;
    private static int ONE_SECOND = 1000;
    private static Timer timer;
    private static TimerTask task;
    
    /** Constructs an instance of this library. This constructor is how
     * Logisim accesses first when it opens the JAR file: It looks for
     * a no-arguments constructor method of the user-designated class.
     */
    public Components() {
        tools = new ArrayList<Tool>(); 
        loadedProjects = new ArrayList<Project>();
        timer = new Timer();				// After many different attempts, this is the only way to 
        task = new TimerTask() {			// ensure the add-on loads into all requesting projects.
			public void run() {
				List<Project> projs = Projects.getOpenProjects();
				for(Project p: projs) {
					if(!loadedProjects.contains(p)) {
						List<Library> libsOwned = p.getLogisimFile().getLibraries();
						for(Library lib: libsOwned) {
							if(isThisLibrary(lib)) {
								loadedProjects.add(p);
								loadInto(p);
							}
						}
					}
				}
			}
        };
       	timer.scheduleAtFixedRate(task, 0, ONE_SECOND*2);
    }
    
    /** Returns the name of the library that the user will see. */ 
    public String getDisplayName() {
        return "LogiTiming";
    }
    
    /** Returns a list of all the tools available in this library. */
    public List<Tool> getTools() {
        return tools;
    }
    
    public static final String libCheckStr = "com.johnston.main.Components";
    
    private boolean isThisLibrary(Library lib) {
    	return lib.getName().equals(libCheckStr);
    }
    
    public static void loadInto(Project p) {
		Frame frame = p.getFrame();
		MousePositionDisplay.track(frame, frame.getCanvas());
		JMenu simMenu = getMenu(frame);
		
		MyAction action = new MyAction(p);
		JMenuItem item = new JMenuItem(action);
		item.setText("Timing Diagram");
		simMenu.add(item);
	}
	
	public static JMenu getMenu(Frame frame) {
		JMenu menu = null;
		try {
			for(int i = 0; i < frame.getJMenuBar().getMenuCount(); i++) {
				if(frame.getJMenuBar().getMenu(i).getText().equals("Simulate")) {
					menu = frame.getJMenuBar().getMenu(i);
				}
			}
		}	catch(Exception e) {
			System.out.println("An error occurred when finding the menu.");
		}
		return menu;
	}
}

