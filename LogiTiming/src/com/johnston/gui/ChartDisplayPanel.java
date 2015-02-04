package com.johnston.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import com.johnston.circ.GeneralComponent;
import com.johnston.timing.TimingDiagram;
import com.johnston.timing.ValueChangeList;

public class ChartDisplayPanel extends JPanel {

	//public static final Color BACKGROUND_COLOR = Color.WHITE;
	public static final int CELL_HEIGHT = 30;
	public static final int LABEL_CELL_WIDTH = 150;
	public static final int DATA_CELL_WIDTH = 100;
	public static final float BORDER_THICKNESS = 1.0f;
	public static final Color DATA_CELL_BACKGROUND = Color.WHITE;
	public static final Color DATA_CELL_OUTLINE = Color.BLACK;
	public static final Color LABEL_CELL_BACKGROUND = Color.WHITE;
	public static final Color LABEL_CELL_OUTLINE = Color.BLACK;
	public static final int EDGE_PIXEL = 1;
	
	
	TimingDiagram diagram;
	GeneralComponent comp;
	ValueChangeList list;
	int fullWidth;
	String componentName = "N/A";
	
	/**
	 * Create the panel.
	 */
	public ChartDisplayPanel(GeneralComponent c, TimingDiagram diagram) {
		comp = c;
		if(comp != null) {
			setComponentName(c.getLabel());
		}
		this.diagram = diagram;
		list = new ValueChangeList();
	}
	
	public void setData(ValueChangeList changes) {
		list.clear();
		list.addAll(changes);
		list.removeUnneeded();
		this.fullWidth = LABEL_CELL_WIDTH + DATA_CELL_WIDTH * list.size() + EDGE_PIXEL;
		this.setSize(this.fullWidth, CELL_HEIGHT + EDGE_PIXEL);
	}
	
	public void updateData() {
		setData(diagram.getSimulationResultsFor(comp));
		/*if(comp.isPin()) {
			System.out.println("ChartDisplayPanel Recieving data for: " + this.comp.getLabel());
		} else {
			System.out.println("ChartDisplayPanel Recieving data for: " + this.comp.getComponentName());
		}
		list.printSimulationRecord();*/
	}
	

	public int getFullWidth() {
		// TODO Auto-generated method stub
		return this.fullWidth;
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawLabelCell(g);
		drawDataCells(g);
		drawEdgeLines(g);
	}
	
	private void drawLabelCell(Graphics g) {
		final Graphics2D g2D = (Graphics2D) g.create();
		g2D.setColor(LABEL_CELL_BACKGROUND);
		Rectangle cell = new Rectangle(0,0,LABEL_CELL_WIDTH,CELL_HEIGHT);
		
		g2D.setColor(LABEL_CELL_BACKGROUND);
		g2D.fill(cell);
		g2D.setColor(LABEL_CELL_OUTLINE);
		g2D.draw(cell);
		drawLabelText(g2D);
	}
	
	private void drawDataCells(Graphics g) {
		
		final Graphics2D g2D = (Graphics2D) g.create();		
		for(int i = 0; i < list.size(); i++) {
			int xStart = LABEL_CELL_WIDTH + i*DATA_CELL_WIDTH;
			int yStart = 0;
			Rectangle cell = new Rectangle(xStart,yStart,DATA_CELL_WIDTH,CELL_HEIGHT);
			g2D.setColor(DATA_CELL_BACKGROUND);
			g2D.fill(cell);
			g2D.setColor(DATA_CELL_OUTLINE);
			g2D.draw(cell);
			String dataText = "( ";
			dataText+= list.get(i).getTimeChanged() + ", ";
			dataText+= list.get(i).getValue().toDisplayString() + ")";
			drawToMiddleOfData(g2D, dataText, xStart, yStart);
		}
	}
	
	private void drawToMiddleOfData(Graphics2D g2D, String text, int x, int y) {
		g2D.setColor(Color.BLACK);
		FontMetrics fm = g2D.getFontMetrics();
		Rectangle2D rect = fm.getStringBounds(text, g2D);

		// Text center code courtesy of Otto Allmendinger
		int textHeight = (int)(rect.getHeight()); 
		int textWidth  = (int)(rect.getWidth());
		int panelHeight = CELL_HEIGHT;

		int xLoc = (DATA_CELL_WIDTH - textWidth)/2;
		int yLoc = (panelHeight - textHeight) / 2  + fm.getAscent();

		g2D.drawString(text, x+ xLoc, y + yLoc);
	}
	
	private void drawLabelText(Graphics2D g2D) {
		g2D.setColor(Color.BLACK);
		FontMetrics fm = g2D.getFontMetrics();
		Rectangle2D rect = fm.getStringBounds(this.componentName, g2D);

		// Text center code courtesy of Otto Allmendinger
		int textHeight = (int)(rect.getHeight()); 
		int textWidth  = (int)(rect.getWidth());
		int panelHeight = CELL_HEIGHT;

		int xLoc = LABEL_CELL_WIDTH/2 - textWidth/2;
		int yLoc = (panelHeight - textHeight) / 2  + fm.getAscent();

		g2D.drawString(this.componentName, xLoc, yLoc);
	}
	
	private void drawEdgeLines(Graphics g) {
		final Graphics2D g2D = (Graphics2D) g.create();
		g2D.setColor(DATA_CELL_OUTLINE);
		g2D.drawLine(fullWidth, 0, fullWidth, CELL_HEIGHT);
		g2D.drawLine(0, CELL_HEIGHT, fullWidth, CELL_HEIGHT);
		g2D.setColor(LABEL_CELL_OUTLINE);
		g2D.setStroke(ValueChangeDisplayPanel.STROKE_BOLD);
		g2D.drawLine(LABEL_CELL_WIDTH,0, LABEL_CELL_WIDTH, CELL_HEIGHT);
	}

	public void setComponentName(String name) {
		this.componentName = name;
	}

}
