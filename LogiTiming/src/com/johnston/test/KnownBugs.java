package com.johnston.test;


/**
 * This is a compilation of all known bugs as well as
 * all tasks that still need to be done. Deal with it.
 * @author Albert
 *
 */
public class KnownBugs {

	// Known bugs
	static final String BUG = "";
					// Solved
	static final String INCORRECT_DELAY = "The timing diagram produces some delay periods calculated by"
			+ " the simulation that aren't possible, since they are shorter than the smallest actual delay"
			+ " but longer than the bypass delay could have feasibly created.";
					// Solved
	static final String GUI_REFRESH_BROKEN = "The timing diagram gui doesn't refresh correctly. This might"
			+ " be due to another bug (INCORRECT_DELAY): '" + INCORRECT_DELAY + "'. A more likely cause is"
			+ " that when the TimingDiagram is refreshed, it doesn't correctly reset all components and data"
			+ " to their original state.";
	static final String DOUBLE_BUFFER_MENU_OVERLAP = "Due to the double buffer, any open menus get painted"
			+ " over, because they aren't part of the frame's content pane. So, they will remain open but"
			+ " be invisible until hovered over, leaving only the few who have been hovered over visible to"
			+ " the user, usually floating in space.";
					// Possibly not an issue
	static final String RAPID_USER_INPUT = "If the user were to rapidly change a value while the gui was on"
			+ " real time simulation, it might cause issues within the simulator. But I can't be sure, since"
			+ " the simulator still has bugs and I haven't tested real time simulation. One solution might to"
			+ " simply disable or remove the MouseListeners while results are being calculated. It's also"
			+ " possibly that the simulator will be so fast that it won't be an issue, but then the user might"
			+ " give a circuit for which it will be a problem. So it's better off being solved anyways.";
					// Solved, but remaining ideas moved to future changes
	static final String LEFTOVER_VALUE_CHANGES = "In the gui, when the user changes a value of a pin at a given"
			+ " time, if they change the pin to update to a value and that value was already going to be updated"
			+ " to soon after, the gui will still maintain that following value change, even though there isn't a"
			+ " change anymore. So just make the gui check for needless value changes when the user makes a change.";
	static final String ROUND_OFF_ERROR = "As expected, the workaround's delay causes round-off errors, leaving"
			+ " some ValueUpdates unrealized, as they are skipped over. This should be solved with the simulation"
			+ " overhaul, so I don't plan on trying to solve this.";
	
	
	// As-of-yet unimplemented changes and/or additions
	
	static final String CHANGE = "";
	static final String SIMULATION_OVERHAUL = "Rather than creating a tiny delay in components that have no"
			+ " delay, oscillation should actually be constantly checked for. In order to accomplish this, the"
			+ " code will need to allow zero delay for some components (and, therefore, allow the ValueMap to"
			+ " return ValueUpdates that are at zero delay). Also, the propagator will need to keep track of a"
			+ " list of all the previous 'GeneralCircuitStates' that have occurred since the last movement through"
			+ " time. That class would need to be designed to capture a 'snapshot' of all current Values (and all"
			+ " ValueMaps) of each component, such that it can determine if any two 'snapshots' are the same."
			+ " The propagator would then let all components propagate themselves, updating as needed. Then when"
			+ " all propagation was done for that step, it would generate a new 'GeneralCircuitState' (thereby"
			+ " getting a 'snapshot' of the current conditions) and check to see if that 'snapshot' matched any"
			+ " previous ones. If not, then push on. If it does however, then use the 'snapshots' to find which"
			+ " components are oscillating, clear their ValueMaps and add a Value.ERROR back into it (and the"
			+ " associated ValueChangeList?). Then, the propagation can continue. The list of 'GeneralCircuitStates'"
			+ " should be cleared once any step taken is greater than 0ns.";
					// Completed
	static final String GUI_ALLOW_INPUT_UNKNOWN = "Change the mouse listeners (and accompanying method) so the"
			+ " user can also set the value of a pin to Unknown at any time they choose in the GUI";
					// Completed
	static final String GUI_ALLOW_SET_MAX_TIME = "Add an input text area to the preferences so the user can change"
			+ " the maximum simulated time to whatever they choose, so long as its greater than 0ns.";
					// Completed
	static final String GUI_ALLOW_SET_INTERVAL = "Add an input text area to the preferences so the user can change"
			+ " the interval size to whatever they choose, so long as its greater than 0ns.";
					// Completed
	static final String GUI_ALLOW_TOGGLE_REALTIME = "Add a checkbox to allow the user to turn on and off real time"
			+ " simulation, AKA the GUI updates the second they change a pin's value.";
					// Completed
	static final String GUI_RELOAD_CIRCUIT = "Make the second button an option to reload the whole circuit, basically"
			+ " fast-forwarding what would normally require the user to close the window and open it again from the"
			+ " menu. However, it would be fine if it just closed the window and opened a new one, just as soon as the"
			+ " data was ready. That would also give it minimal down time for the user.";
	static final String SET_GATE_DELAY = "The user should be able to change the delay values for each gate. Eventually,"
			+ " this should be possible via a config file, but for now it can be in prefs.";
					// Completed
	static final String GATES_HAVE_LABELS = "Change the getLabel method to allow for gates, with the return giving back"
			+ " the label if set, and the component name if unset. This will mean a lot of code will need to update to"
			+ " the new standard, but it'll make it much more clean.";
					// Completed
	static final String GUI_TOGGLE_COLLATION = "Add a checkbox to allow the user to turn on and off collation. Collation,"
			+ " which usually refers to collecting and combining things in their proper order, will mean that when the user"
			+ " changes an input's value at a given time, the values changed will include those that followed it up until"
			+ " the next change in value. This option will be off by default, meaning the user will set the value of each"
			+ " input interval by interval.";
					// Completed
	static final String GUI_DISPLAY_TIMESTAMP = "Display the timestamp of each value change that isn't along an interval"
			+ " to the left of the line describing it in the ValueChangeDisplayPanel. Apprx 5 px to left of line. Use graphics"
			+ " and font width for text. Possibly use smaller text";
	static final String GUI_SELECT_MULTIPLE = "Add in capability for the user to select a section of a ValueDisplay, allowing"
			+ " them to possibly copy-paste (not cut) that section, or to change that section's values as a whole.";
	
	public static void main(String[] args) {
		
		
		
		
	}
	
	static void print(Object s) {	System.out.println(s);	}
	static void print(int s) {		System.out.println(s);	}
	static void print(double s) {	System.out.println(s);	}
	static void print(boolean s) {	System.out.println(s);	}

}
