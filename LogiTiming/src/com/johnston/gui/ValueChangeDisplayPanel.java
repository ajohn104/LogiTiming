package com.johnston.gui;

import javax.swing.JPanel;

import com.cburch.logisim.data.Value;
import com.johnston.circ.GeneralComponent;
import com.johnston.timing.TimingDiagram;
import com.johnston.timing.ValueChange;
import com.johnston.timing.ValueChangeList;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

public class ValueChangeDisplayPanel extends JPanel {

	public static final int LABEL_AREA_WIDTH = 75;
	public static final int HEIGHT = 75;
	public static final int MIDDLE_LINE_Y = (int)Math.round((double)HEIGHT/2.0);
	public static final int TIME_SCALAR = 3;
	public static final int DISPLAY_OFFSET = 10;
	public static final double UNKNOWN_SELECT_RATIO = 0.25;
	public static float TIMESTAMP_FONT_SCALAR = 0.8f;
	
	public static final Color BACKGROUND_COLOR_MAIN = Color.WHITE;
	public static final Color BACKGROUND_COLOR_ALT = new Color(211, 211, 211);
	public static final Color MIDDLE_LINE_COLOR = new Color(150, 150, 150);
	public static final Color LABEL_AREA_COLOR = new Color(105, 105, 105);
	public static final Color INTERVAL_COLOR = new Color(145, 145, 145);
	public static Color TIMESTAMP_COLOR = new Color(130,130,130);
	
	
	// Dash thanks to http://docs.oracle.com/javase/tutorial/2d/geometry/strokeandfill.html
	final static float DASH_FREQ[] = {10.0f};
	public static final BasicStroke STROKE_DASHED = new BasicStroke( 1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, DASH_FREQ, 5.0f);
	public static final BasicStroke STROKE_BOLD = new BasicStroke( 2.0f);
	public static final BasicStroke STROKE_EXTRA_BOLD = new BasicStroke( 4.0f);
	
	public static final int LINE_WIDTH_CLIP = 2;
	public ValueChangeList list;
	
	GeneralComponent comp;
	
	double interval;
	Graphics2D graphics;
	int width;
	String componentName = "N/A";
	TimingDiagram diagram;
	FrameDisplayControl control;
	
	/**
	 * Create the panel.
	 */
	public ValueChangeDisplayPanel(GeneralComponent c, TimingDiagram diagram, boolean isMainColor, int width, double interval, final FrameDisplayControl control) {
		if(c != null) {
			componentName = c.getLabel();
		}
		this.control = control;
		this.diagram = diagram;
		this.width = width;
		this.interval = interval;
		if(isMainColor) {
			setBackground(BACKGROUND_COLOR_MAIN);
		} else {
			setBackground(BACKGROUND_COLOR_ALT);
		}
		this.setSize(width, HEIGHT);
		this.comp = c;
		list = new ValueChangeList();
		if(this.comp != null && this.comp.isInputPin()) {
			this.addMouseListener(new MouseListener() {
				
				public void mouseClicked(MouseEvent e) { }
	
				public void mousePressed(MouseEvent e) { }
	
				public void mouseReleased(MouseEvent e) {
					Value newVal = getValueForY(e.getY());
					setValueChangeAt(e.getX(), newVal);
					
					control.inputChanged();
				}
	
				public void mouseEntered(MouseEvent e) { }
	
				public void mouseExited(MouseEvent e) { }
				
			});
			this.addMouseMotionListener(new MouseMotionListener() {

				@Override
				public void mouseDragged(MouseEvent e) {
					// TODO Auto-generated method stub
					Value newVal = getValueForY(e.getY());
					setValueChangeAt(e.getX(), newVal);
				}

				@Override
				public void mouseMoved(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
			});
		}
	}
	
	public void updateData() {
		setData(diagram.getSimulationResultsFor(comp));
		/*if(comp.isPin()) {
			System.out.println("Recieving data for: " + this.comp.getLabel());
		} else {
			System.out.println("Recieving data for: " + this.comp.getComponentName());
		}
		list.printSimulationRecord();*/
	}
	
	public void setData(ValueChangeList changes) {
		list.clear();
		list.addAll(changes);
		list.removeUnneeded();
	}
	
	public void setComponentName(String name) {
		this.componentName = name;
	}
	
	public ValueChangeList getList() {
		return this.list;
	}
	
	public boolean isRaisedHigh(int mouseY) {
		return mouseY < MIDDLE_LINE_Y;
	}
	
	public Value getValueForY(int mouseY) {
		double distFromMiddleLine = Math.abs(MIDDLE_LINE_Y - mouseY);
		if(distFromMiddleLine < (HEIGHT * UNKNOWN_SELECT_RATIO)) {
			return Value.UNKNOWN;
		} else {
			return (isRaisedHigh(mouseY)?Value.TRUE:Value.FALSE);
		}
	}
	
	public void setValueChangeAt(int mouseX, Value val) {
		if(mouseX <= LABEL_AREA_WIDTH) {
			return;
		}
		mouseX-=LABEL_AREA_WIDTH;
		int intervalWidth = (int)(TIME_SCALAR*interval);
		int intervalNum = mouseX/intervalWidth;
		
		// Grab time after and keep it constant for value
		double intervalRightAfter = (intervalNum+1)*interval;
		Value valRightAfter = list.getValueAtTime(intervalRightAfter);
		
		this.list.setChangeTo(val, intervalNum*interval);
		
		boolean collation = this.control.collation;
		if(FrameDisplayControl.collateKeyPressed()) {
			collation = !collation;
		}
		if( intervalRightAfter*TIME_SCALAR + LABEL_AREA_WIDTH + DISPLAY_OFFSET < width && !collation) {
			this.list.setChangeTo(valRightAfter, intervalRightAfter); // This is new
		}
		this.list.removeUnneeded();
		this.repaint();
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawMiddleLine(g);
		drawLabelArea(g);
		drawIntervalLines(g);
		drawData(g);
	}
	
	public void drawIntervalLines(Graphics g) {
		final Graphics2D g2D = (Graphics2D) g.create();
		int intervalWidth = (int)(TIME_SCALAR*interval);
		for(int x = LABEL_AREA_WIDTH + DISPLAY_OFFSET; x <= width; x+=intervalWidth) {
			g2D.setColor(INTERVAL_COLOR);
			g2D.drawLine(x, 0, x, HEIGHT);
		}
	}
	
	private void drawMiddleLine(Graphics g) {
		final Graphics2D g2D = (Graphics2D) g.create();
		g2D.setColor(MIDDLE_LINE_COLOR);
		g2D.setStroke(STROKE_DASHED);
		g2D.drawLine(LABEL_AREA_WIDTH + DISPLAY_OFFSET, MIDDLE_LINE_Y, width, MIDDLE_LINE_Y);
	}
	
	private void drawLabelArea(Graphics g) {
		final Graphics2D g2D = (Graphics2D) g.create();
		g2D.setColor(LABEL_AREA_COLOR);
		g2D.setStroke(STROKE_BOLD);
		g2D.drawLine(LABEL_AREA_WIDTH, 0, LABEL_AREA_WIDTH, HEIGHT);
		
		g2D.setColor(Color.BLACK);
		
		// Add word wrap for too long names?
		
		
		FontMetrics fm = g2D.getFontMetrics();
		Rectangle2D rect = fm.getStringBounds(componentName, g2D);

		// Text center code courtesy of Otto Allmendinger
		int textHeight = (int)(rect.getHeight()); 
		int textWidth  = (int)(rect.getWidth());
		int panelHeight = HEIGHT;

		int xLoc = LABEL_AREA_WIDTH/2 - textWidth/2;
		int yLoc = (panelHeight - textHeight) / 2  + fm.getAscent();

		g2D.drawString(componentName, xLoc, yLoc);
	}
	
	public void drawData(Graphics g) {
		final Graphics2D g2D = (Graphics2D) g.create();
		
		//int interval
		int intervalWidth = (int)(TIME_SCALAR*interval);
		Font prevFont = g2D.getFont();
		g2D.setFont(prevFont.deriveFont(prevFont.getSize2D()*TIMESTAMP_FONT_SCALAR));
		for(int x = LABEL_AREA_WIDTH + DISPLAY_OFFSET; x <= width; x+=intervalWidth) {
			
			g2D.setColor(INTERVAL_COLOR);
			g2D.drawLine(x, 0, x, HEIGHT);
		}
		
		int offset = LABEL_AREA_WIDTH + DISPLAY_OFFSET;
		
		ValueChange change;
		int listSize = list.size();
		for(int i = 0; i < listSize; i++) {
			change = list.toArrayList().get(i);
			Color color = change.getValue().getColor();
			int xStart = (int) (offset + TIME_SCALAR*change.getTimeChanged());
			int yStart = 0;
			if(i != 0) {
				yStart = getYForValue(list.toArrayList().get(i-1).getValue());
			} else {
				yStart = MIDDLE_LINE_Y;
			}
			int xEnd = 0;
			
			int yEnd = getYForValue(change.getValue());
			if(i + 1 >= listSize) {
				xEnd = width;
			} else {
				xEnd = (int) (offset+ TIME_SCALAR*list.toArrayList().get(i+1).getTimeChanged());
			}
			
			g2D.setColor(color);
			g2D.setStroke(STROKE_BOLD);
			g2D.drawLine(xStart, yStart, xStart, yEnd);			
			g2D.setStroke(STROKE_EXTRA_BOLD);
			
			if(yEnd == MIDDLE_LINE_Y) {
				g2D.setStroke(STROKE_BOLD);
			}
			
			g2D.drawLine(xStart+LINE_WIDTH_CLIP, yEnd, xEnd-LINE_WIDTH_CLIP, yEnd);
			
			if(change.getTimeChanged() % interval != 0) {
				g2D.setColor(TIMESTAMP_COLOR);
				String timeText = change.getTimeChanged() + "";
				
				FontMetrics fm = g2D.getFontMetrics();
				Rectangle2D rect = fm.getStringBounds(timeText, g2D);

				// Text center code courtesy of Otto Allmendinger
				int textHeight = (int)(rect.getHeight()); 
				
				int lineHeight = Math.abs(yStart - yEnd);

				int xLoc = xStart + 5;
				int yLoc = (lineHeight - textHeight) / 2  + fm.getAscent();
				
				// I'm sure there's a far more concise way to do this, but this isn't my biggest concern right now
				int finalY = (yStart + yLoc)%HEIGHT;
				if(yStart == MIDDLE_LINE_Y && yEnd == 0) {
					 finalY -= HEIGHT/2;
				}
				
				double dist = Math.abs(MIDDLE_LINE_Y - finalY);
				if(Math.abs(MIDDLE_LINE_Y - finalY) <= textHeight) {
					finalY -= textHeight - dist;
				}
				g2D.drawString(timeText, xLoc, finalY);
			}			
		}
		
		
		
		
		
	}
	
	int getYForValue(Value val) {
		if(val.equals(Value.TRUE)) {
			return 0;
		} else if(val.equals(Value.FALSE)) {
			return HEIGHT;
		} else {
			return MIDDLE_LINE_Y;
		}
	}
	

}
