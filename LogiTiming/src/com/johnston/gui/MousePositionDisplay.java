package com.johnston.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import com.cburch.logisim.gui.main.Canvas;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.proj.Project;

public class MousePositionDisplay {
	
	public static final int HORIZ_MARGIN = 5;
	public static final int VERT_MARGIN = 5;
	public static final Color BACKGROUND_COLOR = Color.LIGHT_GRAY;
	
	public static void track(final Frame frame, final Canvas canv) {
		final Container cont = frame.getContentPane();
		
		
		canv.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent arg0) { }

			@Override
			public void mouseMoved(MouseEvent event) {
				// TODO Auto-generated method stub
				
				String pointStr = "( " + (int)event.getPoint().getX() + ", " + (int)event.getPoint().getY() + " )";
				
				final Graphics2D g2D = (Graphics2D) frame.getContentPane().getGraphics().create();
				FontMetrics fm = g2D.getFontMetrics();
				Rectangle2D rect = fm.getStringBounds(pointStr, g2D);
				
				int textHeight = (int)(rect.getHeight()); 
				int textWidth  = (int)(rect.getWidth());
				
				int width = 2*HORIZ_MARGIN + textWidth;
				int height = 2*VERT_MARGIN + textHeight;
				
				int startX = cont.getWidth() - width;
				int startY = cont.getHeight() - height;
				
				int yLoc = (height - textHeight) / 2  + fm.getAscent();

				Graphics2D g2image2;
				BufferedImage offscreen2 = null;
				offscreen2 = new BufferedImage(frame.getContentPane().getWidth(), frame.getContentPane().getHeight(), BufferedImage.TYPE_INT_ARGB);
				g2image2 = offscreen2.createGraphics();
				frame.getContentPane().paint(g2image2);
				
				g2image2.setColor(BACKGROUND_COLOR);
				g2image2.fillRect(startX, startY, width, height);
				
				g2image2.setColor(Color.BLACK);
				g2image2.drawString(pointStr, startX + HORIZ_MARGIN, startY + yLoc);
				
				g2D.drawImage(offscreen2, 0, 0, null);
			}
		});
	}
}
