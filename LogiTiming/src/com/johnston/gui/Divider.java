package com.johnston.gui;

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;




public class Divider extends JPanel {

	static final int HEIGHT = 20;
	static final Color DIVIDER_COLOR = new Color(192, 192, 192);
	static final Color TIMESTAMP_COLOR = new Color(100,100,100);
	
	int width;
	double interval;
	
	/**
	 * Create the panel.
	 */
	public Divider(int width, double interval) {
		this.width = width;
		this.interval = interval;
		setBackground(DIVIDER_COLOR);
		this.setSize(width, HEIGHT);
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		displayIntervalTimestamps(g);
	}
	
	private void displayIntervalTimestamps(Graphics g) {
		final Graphics2D g2D = (Graphics2D) g.create();
		int intervalWidth = (int)(ValueChangeDisplayPanel.TIME_SCALAR*interval);
		int timestamp = 0;
		FontMetrics fm = g2D.getFontMetrics();
		
		for(int x = ValueChangeDisplayPanel.LABEL_AREA_WIDTH + ValueChangeDisplayPanel.DISPLAY_OFFSET; x < width; x+=intervalWidth) {
			String timestampStr = timestamp+"ns";
			g2D.setColor(TIMESTAMP_COLOR);
			Rectangle2D rect = fm.getStringBounds(timestampStr, g2D);

			// Text center code courtesy of Otto Allmendinger
			int textHeight = (int)(rect.getHeight()); 
			int textWidth  = (int)(rect.getWidth());
			int panelHeight = HEIGHT;

			int xLoc = x - textWidth/4;
			int yLoc = (panelHeight - textHeight) / 2  + fm.getAscent();

			g2D.drawString(timestampStr, xLoc, yLoc);
			timestamp+=interval;
		}
	}

}
