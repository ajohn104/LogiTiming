package com.johnston.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.Rectangle;
import java.awt.Dimension;

import javax.swing.JTabbedPane;

import java.awt.FlowLayout;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;




import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.BoxLayout;
import javax.swing.JTable;

import java.awt.GridLayout;
import java.awt.GridBagLayout;

import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import java.awt.Component;

import javax.swing.table.DefaultTableModel;
import javax.swing.text.NumberFormatter;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JFormattedTextField;
import javax.swing.JCheckBox;

public class GeneralFrame extends JFrame {

	private JPanel contentPane;
	public JPanel timingPanel;
	public JScrollPane diagramScrollPane;
	public JPanel valueDiagram;
	public JScrollPane chartScrollPane;
	public JPanel propertiesPanel;
	public JPanel infoPanel;
	public JButton refreshBtn;
	public JFormattedTextField maxTimeInput;
	public JFormattedTextField intervalInput;
	public JButton loadTimePrefsBtn;
	public JCheckBox chckbxRealTime;
	public JButton reloadCircBtn;
	public JCheckBox chckbxCollation;

	/**
	 * Create the frame.
	 */
	public GeneralFrame() {
		setBackground(Color.WHITE);
		setTitle("General Simulation GUI");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 1024, 768);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),}));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, "2, 2, fill, fill");
		
		final JPanel timingTab = new JPanel();
		timingTab.setBackground(Color.WHITE);
		tabbedPane.addTab("Timing Diagram", null, timingTab, null);
		timingTab.setLayout(new BorderLayout(0, 0));
		
		valueDiagram = new JPanel();
		
		timingPanel = new JPanel();
		timingPanel.setBackground(Color.WHITE);
		
		diagramScrollPane = new JScrollPane(valueDiagram);
		valueDiagram.setLayout(null);
		diagramScrollPane.setBackground(new Color(255, 255, 255));
		timingPanel.setLayout(null);
		timingTab.add(diagramScrollPane, BorderLayout.CENTER);
		diagramScrollPane.setLayout(new ScrollPaneLayout());
		
		infoPanel = new JPanel();
		timingTab.add(infoPanel, BorderLayout.SOUTH);
		
		String[][] data = { new String[] {"Gate", "0ns", "0ns" } };
		String[] colNames = {"Gate Name", "High to Low", "Low to High"};
		infoPanel.setLayout(new BorderLayout(0, 0));
		BorderLayout layout = new BorderLayout(0,0);
				
		propertiesPanel = new JPanel();
		//infoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
		

		//buttonPanel.setSize(btnNewButton.getWidth(), height);
		chartScrollPane = new JScrollPane(propertiesPanel);
		
		propertiesPanel.setLayout(null);
		
		chartScrollPane.setLayout(new ScrollPaneLayout());
		infoPanel.add(chartScrollPane, BorderLayout.CENTER);
		//chartScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
		
		JPanel buttonPanel = new JPanel();
		infoPanel.add(buttonPanel, BorderLayout.EAST);
		GridBagLayout gbl_buttonPanel = new GridBagLayout();
		gbl_buttonPanel.columnWidths = new int[]{111, 0};
		gbl_buttonPanel.rowHeights = new int[]{25, 25, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_buttonPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_buttonPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		buttonPanel.setLayout(gbl_buttonPanel);
		
		refreshBtn = new JButton("Refresh Data");
		refreshBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		refreshBtn.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		GridBagConstraints gbc_refreshBtn = new GridBagConstraints();
		gbc_refreshBtn.fill = GridBagConstraints.HORIZONTAL;
		gbc_refreshBtn.insets = new Insets(0, 0, 5, 0);
		gbc_refreshBtn.gridx = 0;
		gbc_refreshBtn.gridy = 0;
		buttonPanel.add(refreshBtn, gbc_refreshBtn);
		
		reloadCircBtn = new JButton("Reload Circuit");
		reloadCircBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		reloadCircBtn.setAlignmentY(Component.TOP_ALIGNMENT);
		GridBagConstraints gbc_reloadCircBtn = new GridBagConstraints();
		gbc_reloadCircBtn.insets = new Insets(0, 0, 5, 0);
		gbc_reloadCircBtn.fill = GridBagConstraints.HORIZONTAL;
		gbc_reloadCircBtn.anchor = GridBagConstraints.NORTH;
		gbc_reloadCircBtn.gridx = 0;
		gbc_reloadCircBtn.gridy = 1;
		buttonPanel.add(reloadCircBtn, gbc_reloadCircBtn);
		
		JLabel maxTimeLabel = new JLabel("Max time:");
		GridBagConstraints gbc_maxTimeLabel = new GridBagConstraints();
		gbc_maxTimeLabel.gridx = 0;
		gbc_maxTimeLabel.gridy = 2;
		buttonPanel.add(maxTimeLabel, gbc_maxTimeLabel);
		
		
		JPanel timeInputPanel = new JPanel();
		timeInputPanel.setBackground(new Color(240,240,240));
		GridBagConstraints gbc_timeInputPanel = new GridBagConstraints();
		gbc_timeInputPanel.insets = new Insets(0, 0, 5, 0);
		gbc_timeInputPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_timeInputPanel.gridx = 0;
		gbc_timeInputPanel.gridy = 3;
		buttonPanel.add(timeInputPanel, gbc_timeInputPanel);
		timeInputPanel.setLayout(new BoxLayout(timeInputPanel, BoxLayout.X_AXIS));
		
		NumberFormatter numFormMaxTime = new NumberFormatter();
		numFormMaxTime.setMinimum(new Double (0.1));
		numFormMaxTime.setAllowsInvalid(true);
		
		maxTimeInput = new JFormattedTextField(numFormMaxTime);
		timeInputPanel.add(maxTimeInput);
		maxTimeInput.setHorizontalAlignment(SwingConstants.RIGHT);
		
		
		JLabel lblNs = new JLabel("ns");
		lblNs.setBorder(new EmptyBorder(0, 0, 0, 5));
		lblNs.setBackground(Color.WHITE);
		timeInputPanel.add(lblNs);
		
		JLabel lblInterval = new JLabel("Interval:");
		GridBagConstraints gbc_lblInterval = new GridBagConstraints();
		gbc_lblInterval.gridx = 0;
		gbc_lblInterval.gridy = 4;
		buttonPanel.add(lblInterval, gbc_lblInterval);
		
		JPanel intervalInputPanel = new JPanel();
		GridBagConstraints gbc_intervalInputPanel = new GridBagConstraints();
		gbc_intervalInputPanel.insets = new Insets(0, 0, 5, 0);
		gbc_intervalInputPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_intervalInputPanel.gridx = 0;
		gbc_intervalInputPanel.gridy = 5;
		buttonPanel.add(intervalInputPanel, gbc_intervalInputPanel);
		intervalInputPanel.setLayout(new BoxLayout(intervalInputPanel, BoxLayout.X_AXIS));
		
		NumberFormatter numFormInterval = new NumberFormatter();
		numFormInterval.setMinimum(new Double(0.1));
		numFormInterval.setAllowsInvalid(true);
		
		intervalInput = new JFormattedTextField(numFormInterval);
		intervalInputPanel.add(intervalInput);
		intervalInput.setHorizontalAlignment(SwingConstants.RIGHT);
		
		JLabel lblNs_1 = new JLabel("ns");
		lblNs_1.setBorder(new EmptyBorder(0, 0, 0, 5));
		intervalInputPanel.add(lblNs_1);
		
		
		loadTimePrefsBtn = new JButton("Load Time Prefs");
		loadTimePrefsBtn.setMargin(new Insets(2, 6, 2, 6));
		loadTimePrefsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		GridBagConstraints gbc_loadTimePrefsBtn = new GridBagConstraints();
		gbc_loadTimePrefsBtn.fill = GridBagConstraints.HORIZONTAL;
		gbc_loadTimePrefsBtn.gridx = 0;
		gbc_loadTimePrefsBtn.gridy = 6;
		buttonPanel.add(loadTimePrefsBtn, gbc_loadTimePrefsBtn);
		
		
		chckbxRealTime = new JCheckBox("Real Time:");
		chckbxRealTime.setHorizontalAlignment(SwingConstants.RIGHT);
		chckbxRealTime.setHorizontalTextPosition(SwingConstants.LEFT);
		GridBagConstraints gbc_chckbxRealTime = new GridBagConstraints();
		gbc_chckbxRealTime.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxRealTime.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxRealTime.gridx = 0;
		gbc_chckbxRealTime.gridy = 7;
		buttonPanel.add(chckbxRealTime, gbc_chckbxRealTime);
		
		chckbxCollation = new JCheckBox("Collation:");
		chckbxCollation.setHorizontalAlignment(SwingConstants.RIGHT);
		chckbxCollation.setHorizontalTextPosition(SwingConstants.LEFT);
		GridBagConstraints gbc_chckbxCollation = new GridBagConstraints();
		gbc_chckbxCollation.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxCollation.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxCollation.gridx = 0;
		gbc_chckbxCollation.gridy = 8;
		buttonPanel.add(chckbxCollation, gbc_chckbxCollation);
		
		JPanel unusedPanel1 = new JPanel();
		tabbedPane.addTab("Preferences", null, unusedPanel1, null);
		
		JPanel unusedPanel2 = new JPanel();
		tabbedPane.addTab("Help", null, unusedPanel2, null);
	}
	
	
	/**
	 * Will load the correct properties into the GFrame
	 */
	/*public void loadProperties() {
		
	}*/
	
	
	/**
	 * Returns the timingPanel of the display.
	 * @return the Timing Panel of the display.
	 */
	public JPanel getTimingPanel() {
		return this.timingPanel;
	}
}
