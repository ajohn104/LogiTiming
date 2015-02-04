package com.johnston.circ;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.std.wiring.Pin;

public class StaticLibrary {
	
	
	// The StdAttr.LABEL will be important later, when grabbing the user's own label's. But not here.
	
	public static final boolean PIN_IS_OUTPUT = true;
	
	
	/**
	 * Creates an attribute set for a Pin. Will return attributes for an output if
	 * the parameter passed is true, will return attributes for an input otherwise.
	 * @param isOutput - True if output.
	 * @return the attribute set for the specified type of pin.
	 */
	public static AttributeSet getDefaultPinAttrs(boolean isOutput) {
		ComponentFactory factory = Pin.FACTORY;
		AttributeSet attrs = factory.createAttributeSet();
		attrs.setValue(Pin.ATTR_LABEL_LOC, Direction.NORTH);
		attrs.setValue(StdAttr.FACING, (isOutput ? Direction.EAST:Direction.WEST));
		attrs.setValue(Pin.ATTR_TYPE, (isOutput ? Boolean.TRUE:Boolean.FALSE));
		attrs.setValue(StdAttr.LABEL, "this is an " + (isOutput ? "output":"input") + " pin");
		
		return attrs;
	}
	
	public static Component getDefaultPin(boolean isOutput, Location loc) {
		Pin factory = Pin.FACTORY;
		return factory.createComponent(loc, getDefaultPinAttrs(isOutput));
	}
	
	
}
