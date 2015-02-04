/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.cburch.logisim.circuit.CircuitPoints;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.Splitter;
import com.cburch.logisim.circuit.SplitterAttributes;
import com.cburch.logisim.circuit.WidthIncompatibilityData;
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.circuit.WireSet;
import com.cburch.logisim.circuit.WireThread;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.std.wiring.PullResistor;
import com.cburch.logisim.std.wiring.Tunnel;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.IteratorUtil;
import com.cburch.logisim.util.SmallSet;
import com.cburch.logisim.circuit.WireBundleClone;

public class CircuitWiresClone {
	static class SplitterData {
		WireBundleClone[] end_bundle; // PointData associated with each end

		SplitterData(int fan_out) {
			end_bundle = new WireBundleClone[fan_out + 1];
		}
	}

	public static class ThreadBundle {
		int loc;
		WireBundleClone b;
		ThreadBundle(int loc, WireBundleClone b) {
			this.loc = loc;
			this.b = b;
		}
	}

	public static class State {
		BundleMapClone BundleMapClone;
		HashMap<WireThread,Value> thr_values = new HashMap<WireThread,Value>();

		State(BundleMapClone BundleMapClone) {
			this.BundleMapClone = BundleMapClone;
		}
		
		@Override
		public Object clone() {
			State ret = new State(this.BundleMapClone);
			ret.thr_values.putAll(this.thr_values);
			return ret;
		}
	}
	
	private class TunnelListener implements AttributeListener {
		public void attributeListChanged(AttributeEvent e) { }

		public void attributeValueChanged(AttributeEvent e) {
			Attribute<?> attr = e.getAttribute();
			if (attr == StdAttr.LABEL || attr == PullResistor.ATTR_PULL_TYPE) {
				voidBundleMapClone();
			}
		}
	}

	public static class BundleMapClone {
		boolean computed = false;
		HashMap<Location,WireBundleClone> pointBundles = new HashMap<Location,WireBundleClone>();
		HashSet<WireBundleClone> bundles = new HashSet<WireBundleClone>();
		boolean isValid = true;
		// NOTE: It would make things more efficient if we also had
		// a set of just the first bundle in each tree.
		HashSet<WidthIncompatibilityData> incompatibilityData = null;

		HashSet<WidthIncompatibilityData> getWidthIncompatibilityData() {
			return incompatibilityData;
		}

		void addWidthIncompatibilityData(WidthIncompatibilityData e) {
			if (incompatibilityData == null) {
				incompatibilityData = new HashSet<WidthIncompatibilityData>();
			}
			incompatibilityData.add(e);
		}

		public WireBundleClone getBundleAt(Location p) {
			return pointBundles.get(p);
		}

		WireBundleClone createBundleAt(Location p) {
			WireBundleClone ret = pointBundles.get(p);
			if (ret == null) {
				ret = new WireBundleClone();
				pointBundles.put(p, ret);
				ret.points.add(p);
				bundles.add(ret);
			}
			return ret;
		}

		boolean isValid() {
			return isValid;
		}

		void invalidate() {
			isValid = false;
		}

		void setBundleAt(Location p, WireBundleClone b) {
			pointBundles.put(p, b);
		}

		public Set<Location> getBundlePoints() {
			return pointBundles.keySet();
		}

		public Set<WireBundleClone> getBundles() {
			return bundles;
		}

		synchronized void markComputed() {
			computed = true;
			notifyAll();
		}

		synchronized void waitUntilComputed() {
			while (!computed) {
				try { wait(); } catch (InterruptedException e) { }
			}
		}
	}

	// user-given data
	private HashSet<Wire> wires = new HashSet<Wire>();
	private HashSet<Splitter> splitters = new HashSet<Splitter>();
	private HashSet<Component> tunnels = new HashSet<Component>(); // of Components with Tunnel factory
	private TunnelListener tunnelListener = new TunnelListener();
	private HashSet<Component> pulls = new HashSet<Component>(); // of Components with PullResistor factory
	final CircuitPointsClone points = new CircuitPointsClone();

	// derived data
	private Bounds bounds = Bounds.EMPTY_BOUNDS;
	private BundleMapClone BundleMapClone = null;

	CircuitWiresClone() { }

	//
	// query methods
	//
	boolean isMapVoided() {
		return BundleMapClone == null;
	}
	
	Set<WidthIncompatibilityData> getWidthIncompatibilityData() {
		return getBundleMapClone().getWidthIncompatibilityData();
	}

	void ensureComputed() {
		getBundleMapClone();
	}

	BitWidth getWidth(Location q) {
		BitWidth det = points.getWidth(q);
		if (det != BitWidth.UNKNOWN) return det;

		BundleMapClone bmap = getBundleMapClone();
		if (!bmap.isValid()) return BitWidth.UNKNOWN;
		WireBundleClone qb = bmap.getBundleAt(q);
		if (qb != null && qb.isValid()) return qb.getWidth();

		return BitWidth.UNKNOWN;
	}

	Location getWidthDeterminant(Location q) {
		BitWidth det = points.getWidth(q);
		if (det != BitWidth.UNKNOWN) return q;

		WireBundleClone qb = getBundleMapClone().getBundleAt(q);
		if (qb != null && qb.isValid()) return qb.getWidthDeterminant();

		return q;
	}

	Iterator<? extends Component> getComponents() {
		return IteratorUtil.createJoinedIterator(splitters.iterator(),
			wires.iterator());
	}

	Set<Wire> getWires() {
		return wires;
	}

	Bounds getWireBounds() {
		Bounds bds = bounds;
		if (bds == Bounds.EMPTY_BOUNDS) {
			bds = recomputeBounds();
		}
		return bds;
	}
	
	public WireBundleClone getWireBundleClone(Location query) {
		BundleMapClone bmap = getBundleMapClone();
		return bmap.getBundleAt(query);
	}
	
	WireSet getWireSet(Wire start) {
		WireBundleClone bundle = getWireBundleClone(start.getEnd0());
		if (bundle == null) return WireSet.EMPTY;
		HashSet<Wire> wires = new HashSet<Wire>();
		for (Location loc : bundle.points) {
			wires.addAll(points.getWires(loc));
		}
		return new WireSet(wires);
	}

	//
	// action methods
	//
	// NOTE: this could be made much more efficient in most cases to
	// avoid voiding the bundle map.
	boolean add(Component comp) {
		boolean added = true;
		if (comp instanceof Wire) {
			added = addWire((Wire) comp);
		} else if (comp instanceof Splitter) {
			splitters.add((Splitter) comp);
		} else {
			Object factory = comp.getFactory();
			if (factory instanceof Tunnel) {
				tunnels.add(comp);
				comp.getAttributeSet().addAttributeListener(tunnelListener);
			} else if (factory instanceof PullResistor) {
				pulls.add(comp);
				comp.getAttributeSet().addAttributeListener(tunnelListener);
			}
		}
		if (added) {
			points.add(comp);
			voidBundleMapClone();
		}
		return added;
	}

	void remove(Component comp) {
		if (comp instanceof Wire) {
			removeWire((Wire) comp);
		} else if (comp instanceof Splitter) {
			splitters.remove(comp);
		} else {
			Object factory = comp.getFactory();
			if (factory instanceof Tunnel) {
				tunnels.remove(comp);
				comp.getAttributeSet().removeAttributeListener(tunnelListener);
			} else if (factory instanceof PullResistor) {
				pulls.remove(comp);
				comp.getAttributeSet().removeAttributeListener(tunnelListener);
			}
		}
		points.remove(comp);
		voidBundleMapClone();
	}
	
	void add(Component comp, EndData end) {
		points.add(comp, end);
		voidBundleMapClone();
	}
	
	void remove(Component comp, EndData end) {
		points.remove(comp, end);
		voidBundleMapClone();
	}
	
	void replace(Component comp, EndData oldEnd, EndData newEnd) {
		points.remove(comp, oldEnd);
		points.add(comp, newEnd);
		voidBundleMapClone();
	}

	private boolean addWire(Wire w) {
		boolean added = wires.add(w);
		if (!added) return false;

		if (bounds != Bounds.EMPTY_BOUNDS) { // update bounds
			bounds = bounds.add(w.getEnd0()).add(w.getEnd1());
		}
		return true;
	}

	private void removeWire(Wire w) {
		boolean removed = wires.remove(w);
		if (!removed) return;

		if (bounds != Bounds.EMPTY_BOUNDS) {
			// bounds is valid - invalidate if endpoint on border
			Bounds smaller = bounds.expand(-2);
			if (!smaller.contains(w.getEnd0()) || !smaller.contains(w.getEnd1())) {
				bounds = Bounds.EMPTY_BOUNDS;
			}
		}
	}

	void draw(ComponentDrawContext context, Collection<Component> hidden) {
		boolean showState = context.getShowState();
		CircuitState state = context.getCircuitState();
		Graphics g = context.getGraphics();
		g.setColor(Color.BLACK);
		GraphicsUtil.switchToWidth(g, Wire.WIDTH);
		WireSet highlighted = context.getHighlightedWires();

		BundleMapClone bmap = getBundleMapClone();
		boolean isValid = bmap.isValid();
		if (hidden == null || hidden.size() == 0) {
			for (Wire w : wires) {
				Location s = w.getEnd0();
				Location t = w.getEnd1();
				WireBundleClone wb = bmap.getBundleAt(s);
				if (!wb.isValid()) {
					g.setColor(Value.WIDTH_ERROR_COLOR);
				} else if (showState) {
					if (!isValid) g.setColor(Value.NIL_COLOR);
					else         g.setColor(state.getValue(s).getColor());
				} else {
					g.setColor(Color.BLACK);
				}
				if (highlighted.containsWire(w)) {
					GraphicsUtil.switchToWidth(g, Wire.WIDTH + 2);
					g.drawLine(s.getX(), s.getY(), t.getX(), t.getY());
					GraphicsUtil.switchToWidth(g, Wire.WIDTH);
				} else {
					g.drawLine(s.getX(), s.getY(), t.getX(), t.getY());
				}
			}

			for (Location loc : points.getSplitLocations()) {
				if (points.getComponentCount(loc) > 2) {
					WireBundleClone wb = bmap.getBundleAt(loc);
					if (wb != null) {
						if (!wb.isValid()) {
							g.setColor(Value.WIDTH_ERROR_COLOR);
						} else if (showState) {
							if (!isValid) g.setColor(Value.NIL_COLOR);
							else         g.setColor(state.getValue(loc).getColor());
						} else {
							g.setColor(Color.BLACK);
						}
						if (highlighted.containsLocation(loc)) {
							g.fillOval(loc.getX() - 5, loc.getY() - 5, 10, 10);
						} else {
							g.fillOval(loc.getX() - 4, loc.getY() - 4, 8, 8);
						}
					}
				}
			}
		} else {
			for (Wire w : wires) {
				if (!hidden.contains(w)) {
					Location s = w.getEnd0();
					Location t = w.getEnd1();
					WireBundleClone wb = bmap.getBundleAt(s);
					if (!wb.isValid()) {
						g.setColor(Value.WIDTH_ERROR_COLOR);
					} else if (showState) {
						if (!isValid) g.setColor(Value.NIL_COLOR);
						else         g.setColor(state.getValue(s).getColor());
					} else {
						g.setColor(Color.BLACK);
					}
					if (highlighted.containsWire(w)) {
						GraphicsUtil.switchToWidth(g, Wire.WIDTH + 2);
						g.drawLine(s.getX(), s.getY(), t.getX(), t.getY());
						GraphicsUtil.switchToWidth(g, Wire.WIDTH);
					} else {
						g.drawLine(s.getX(), s.getY(), t.getX(), t.getY());
					}
				}
			}

			// this is just an approximation, but it's good enough since
			// the problem is minor, and hidden only exists for a short
			// while at a time anway.
			for (Location loc : points.getSplitLocations()) {
				if (points.getComponentCount(loc) > 2) {
					int icount = 0;
					for (Component comp : points.getComponents(loc)) {
						if (!hidden.contains(comp)) ++icount;
					}
					if (icount > 2) {
						WireBundleClone wb = bmap.getBundleAt(loc);
						if (wb != null) {
							if (!wb.isValid()) {
								g.setColor(Value.WIDTH_ERROR_COLOR);
							} else if (showState) {
								if (!isValid) g.setColor(Value.NIL_COLOR);
								else         g.setColor(state.getValue(loc).getColor());
							} else {
								g.setColor(Color.BLACK);
							}
							if (highlighted.containsLocation(loc)) {
								g.fillOval(loc.getX() - 5, loc.getY() - 5, 10, 10);
							} else {
								g.fillOval(loc.getX() - 4, loc.getY() - 4, 8, 8);
							}
						}
					}
				}
			}
		}
	}

	//
	// helper methods
	//
	private void voidBundleMapClone() {
		BundleMapClone = null;
	}

	public BundleMapClone getBundleMapClone() {
		// Maybe we already have a valid bundle map (or maybe
		// one is in progress).
		BundleMapClone ret = BundleMapClone;
		if (ret != null) {
			ret.waitUntilComputed();
			return ret;
		}
		try {
			// Ok, we have to create our own.
			for (int tries = 4; tries >= 0; tries--) {
				try {
					ret = new BundleMapClone();
					computeBundleMapClone(ret);
					BundleMapClone = ret;
					break;
				} catch (Throwable t) {
					if (tries == 0) {
						t.printStackTrace();
						BundleMapClone = ret;
					}
				}
			}
		} catch (RuntimeException ex) {
			ret.invalidate();
			ret.markComputed();
			throw ex;
		} finally {
			// Mark the BundleMapClone as computed in case anybody is waiting for the result.
			ret.markComputed();
		}
		return ret;
	}

	// To be called by getBundleMapClone only
	private void computeBundleMapClone(BundleMapClone ret) {
		// create bundles corresponding to wires and tunnels
		connectWires(ret);
		connectTunnels(ret);
		connectPullResistors(ret);

		// merge any WireBundleClone objects united by previous steps
		for (Iterator<WireBundleClone> it = ret.getBundles().iterator(); it.hasNext(); ) {
			WireBundleClone b = it.next();
			WireBundleClone bpar = b.find();
			if (bpar != b) { // b isn't group's representative
				for (Location pt : b.points) {
					ret.setBundleAt(pt, bpar);
					bpar.points.add(pt);
				}
				bpar.addPullValue(b.getPullValue());
				it.remove();
			}
		}

		

		// set the width for each bundle whose size is known
		// based on components
		for (Location p : ret.getBundlePoints()) {
			WireBundleClone pb = ret.getBundleAt(p);
			BitWidth width = points.getWidth(p);
			if (width != BitWidth.UNKNOWN) {
				pb.setWidth(width, p);
			}
		}

		

		

		/*// merge any threads united by previous step
		for (WireBundleClone b : ret.getBundles()) {
			if (b.isValid() && b.threads != null) {
				for (int i = 0; i < b.threads.length; i++) {
					WireThread thr = b.threads[i].find();
					b.threads[i] = thr;
					thr.getBundles().add(new ThreadBundle(i, b));
				}
			}
		}*/

		// All threads are sewn together! Compute the exception set before leaving
		Collection<WidthIncompatibilityData> exceptions = points.getWidthIncompatibilityData();
		if (exceptions != null && exceptions.size() > 0) {
			for (WidthIncompatibilityData wid : exceptions) {
				ret.addWidthIncompatibilityData(wid);
			}
		}
		for (WireBundleClone b : ret.getBundles()) {
			WidthIncompatibilityData e = b.getWidthIncompatibilityData();
			if (e != null) ret.addWidthIncompatibilityData(e);
		}
	}
	
	private void connectWires(BundleMapClone ret) {
		// make a WireBundleClone object for each tree of connected wires
		for (Wire w : wires) {
			WireBundleClone b0 = ret.getBundleAt(w.getEnd0());
			if (b0 == null) {
				WireBundleClone b1 = ret.createBundleAt(w.getEnd1());
				b1.points.add(w.getEnd0()); ret.setBundleAt(w.getEnd0(), b1);
			} else {
				WireBundleClone b1 = ret.getBundleAt(w.getEnd1());
				if (b1 == null) { // t1 doesn't exist
					b0.points.add(w.getEnd1()); ret.setBundleAt(w.getEnd1(), b0);
				} else {
					b1.unite(b0); // unite b0 and b1
				}
			}
		}
	}
	
	private void connectTunnels(BundleMapClone ret) {
		// determine the sets of tunnels
		HashMap<String,ArrayList<Location>> tunnelSets = new HashMap<String,ArrayList<Location>>();
		for (Component comp : tunnels) {
			String label = comp.getAttributeSet().getValue(StdAttr.LABEL);
			label = label.trim();
			if (!label.equals("")) {
				ArrayList<Location> tunnelSet = tunnelSets.get(label);
				if (tunnelSet == null) {
					tunnelSet = new ArrayList<Location>(3);
					tunnelSets.put(label, tunnelSet);
				}
				tunnelSet.add(comp.getLocation());
			}
		}
		
		// now connect the bundles that are tunnelled together
		for (ArrayList<Location> tunnelSet : tunnelSets.values()) {
			WireBundleClone foundBundle = null;
			Location foundLocation = null;
			for (Location loc : tunnelSet) {
				WireBundleClone b = ret.getBundleAt(loc);
				if (b != null) {
					foundBundle = b;
					foundLocation = loc;
					break;
				}
			}
			if (foundBundle == null) {
				foundLocation = tunnelSet.get(0);
				foundBundle = ret.createBundleAt(foundLocation); 
			}
			for (Location loc : tunnelSet) {
				if (loc != foundLocation) {
					WireBundleClone b = ret.getBundleAt(loc);
					if (b == null) {
						foundBundle.points.add(loc);
						ret.setBundleAt(loc, foundBundle);
					} else {
						b.unite(foundBundle);
					}
				}
			}
		}
	}
	
	private void connectPullResistors(BundleMapClone ret) {
		for (Component comp : pulls) {
			Location loc = comp.getEnd(0).getLocation();
			WireBundleClone b = ret.getBundleAt(loc);
			if (b == null) {
				b = ret.createBundleAt(loc);
				b.points.add(loc);
				ret.setBundleAt(loc, b);
			}
			Instance instance = Instance.getInstanceFor(comp);
			b.addPullValue(PullResistor.getPullValue(instance));
		}
	}
	
	private static Value pullValue(Value base, Value pullTo) {
		if (base.isFullyDefined()) {
			return base;
		} else if (base.getWidth() == 1) {
			if (base == Value.UNKNOWN) return pullTo;
			else return base;
		} else {
			Value[] ret = base.getAll();
			for (int i = 0; i < ret.length; i++) {
				if (ret[i] == Value.UNKNOWN) ret[i] = pullTo;
			}
			return Value.create(ret);
		}
	}

	private Bounds recomputeBounds() {
		Iterator<Wire> it = wires.iterator();
		if (!it.hasNext()) {
			bounds = Bounds.EMPTY_BOUNDS;
			return Bounds.EMPTY_BOUNDS;
		}

		Wire w = it.next();
		int xmin = w.getEnd0().getX();
		int ymin = w.getEnd0().getY();
		int xmax = w.getEnd1().getX();
		int ymax = w.getEnd1().getY();
		while (it.hasNext()) {
			w = it.next();
			int x0 = w.getEnd0().getX(); if (x0 < xmin) xmin = x0;
			int x1 = w.getEnd1().getX(); if (x1 > xmax) xmax = x1;
			int y0 = w.getEnd0().getY(); if (y0 < ymin) ymin = y0;
			int y1 = w.getEnd1().getY(); if (y1 > ymax) ymax = y1;
		}
		Bounds bds = Bounds.create(xmin, ymin,
			xmax - xmin + 1, ymax - ymin + 1);
		bounds = bds;
		return bds;
	}
}
