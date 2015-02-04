package com.johnston.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Point;

import javax.swing.border.LineBorder;
import javax.swing.JLabel;

import java.awt.Font;
import java.awt.FlowLayout;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.SwingConstants;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.border.EmptyBorder;

public class MouseTrackPanel extends JPanel {
	JLabel label;
	final int LABEL_BORDER_TOP = 1;
	final int LABEL_BORDER_LEFT = 4;
	final int LABEL_BORDER_BOTTOM = 1;
	final int LABEL_BORDER_RIGHT = 4;
	
	/**
	 * Create the panel.
	 */
	public MouseTrackPanel() {
		
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setBackground(Color.LIGHT_GRAY);
		setLayout(null);
		setSize(0,0);
		
		label = new JLabel("0, 0");
		label.setFont(new Font("SansSerif", Font.ITALIC, 12));
		
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setLocation(LABEL_BORDER_LEFT, 0);
		label.setBorder(new EmptyBorder(LABEL_BORDER_TOP, LABEL_BORDER_LEFT, LABEL_BORDER_BOTTOM,  LABEL_BORDER_LEFT + LABEL_BORDER_RIGHT));
		//label.setBounds(0, 0, 70, 22);
		
		
		add(label);
	}
	
	public void reformat(Container parentCont, Point p) {
		String pointStr = "";
		pointStr += p.getX() + ", " + p.getY();
		label.setText(pointStr);
		label.validate();
		label.setSize(label.getPreferredSize());
		
		int contWidth = parentCont.getWidth();
		int contHeight = parentCont.getHeight();
		
		int width = label.getWidth()+ LABEL_BORDER_LEFT + LABEL_BORDER_RIGHT;
		int height = label.getHeight() + LABEL_BORDER_TOP + LABEL_BORDER_BOTTOM;
		
		this.setBounds(contWidth-width,contHeight-height,width, height);
		
	}
	
}
