/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import com.cburch.logisim.circuit.WidthIncompatibilityData;
import com.cburch.logisim.circuit.WireThreadClone;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.util.SmallSet;

public class WireBundleClone {
	private BitWidth width = BitWidth.UNKNOWN;
	public Value pullValue = Value.UNKNOWN;
	private WireBundleClone parent;
	private Location widthDeterminant = null;
	WireThreadClone[] threads = null;
	public SmallSet<Location> points = new SmallSet<Location>(); // points bundle hits
	private WidthIncompatibilityData incompatibilityData = null;

	WireBundleClone() {
		parent = this;
	}

	boolean isValid() {
		return incompatibilityData == null;
	}

	void setWidth(BitWidth width, Location det) {
		if (width == BitWidth.UNKNOWN) return;
		if (incompatibilityData != null) {
			incompatibilityData.add(det, width);
			return;
		}
		if (this.width != BitWidth.UNKNOWN) {
			if (width.equals(this.width)) {
				return; // the widths match, and the bundle is already set; nothing to do
			} else {    // the widths are broken: Create incompatibilityData holding this info
				incompatibilityData = new WidthIncompatibilityData();
				incompatibilityData.add(widthDeterminant, this.width);
				incompatibilityData.add(det, width);
				return;
			}
		}
		this.width = width;
		this.widthDeterminant = det;
		this.threads = new WireThreadClone[width.getWidth()];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new WireThreadClone();
		}
	}

	BitWidth getWidth() {
		if (incompatibilityData != null) {
			return BitWidth.UNKNOWN;
		} else {
			return width;
		}
	}

	Location getWidthDeterminant() {
		if (incompatibilityData != null) {
			return null;
		} else {
			return widthDeterminant;
		}
	}

	WidthIncompatibilityData getWidthIncompatibilityData() {
		return incompatibilityData;
	}

	void isolate() {
		parent = this;
	}

	void unite(WireBundleClone other) {
		WireBundleClone group = this.find();
		WireBundleClone group2 = other.find();
		if (group != group2) group.parent = group2;
	}

	WireBundleClone find() {
		WireBundleClone ret = this;
		if (ret.parent != ret) {
			do ret = ret.parent; while (ret.parent != ret);
			this.parent = ret;
		}
		return ret;
	}
	
	void addPullValue(Value val) {
		pullValue = pullValue.combine(val);
	}
	
	Value getPullValue() {
		return pullValue;
	}
}
