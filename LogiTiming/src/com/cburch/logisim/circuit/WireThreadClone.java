/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import com.cburch.logisim.util.SmallSet;

public class WireThreadClone {
	public WireThreadClone parent;
	public SmallSet<CircuitWiresClone.ThreadBundle> bundles
		= new SmallSet<CircuitWiresClone.ThreadBundle>();

	public WireThreadClone() {
		parent = this;
	}

	public SmallSet<CircuitWiresClone.ThreadBundle> getBundles() {
		return bundles;
	}

	public void unite(WireThreadClone other) {
		WireThreadClone group = this.find();
		WireThreadClone group2 = other.find();
		if (group != group2) group.parent = group2;
	}

	public WireThreadClone find() {
		WireThreadClone ret = this;
		if (ret.parent != ret) {
			do ret = ret.parent; while (ret.parent != ret);
			this.parent = ret;
		}
		return ret;
	}
}
