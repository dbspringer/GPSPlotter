/**
 * 
 */
package com.dbs.gps.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author derek.springer
 *
 */
public class SelectIdDialogue extends JDialog {

	private static final long serialVersionUID = 
			"$Id: SelectIdDialogue.java 2605 2011-10-04 23:59:25Z derek.springer $".hashCode();
	
	public static final int CANCELED = 0;
	public static final int SELECTED = 1;
	
	private Set<Integer> selectedIDs = new TreeSet<Integer>();
	
	private List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
	
	private int choice = CANCELED;
	
	public SelectIdDialogue(
			JFrame parent,
			Collection<Integer> ids,
			int numRows) {
		this(parent, ids, ids, numRows);
	}
	
	public SelectIdDialogue(
			JFrame parent,
			Collection<Integer> ids,
			Collection<Integer> selectedIds,
			int numRows) {
		
		super(parent, "Select IDs", true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		final JPanel topButtonPanel = new JPanel();
		final JButton allButton = new JButton("All");
		final JButton noneButton = new JButton("None");
		allButton.setPreferredSize(new Dimension(80,20));
		noneButton.setPreferredSize(new Dimension(80,20));
		topButtonPanel.add(allButton);
		topButtonPanel.add(noneButton);
		panel.add(topButtonPanel);
		
		allButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for(JCheckBox box : checkBoxes) {
					box.setSelected(true);
				}
			}
		});
		
		noneButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for(JCheckBox box : checkBoxes) {
					box.setSelected(false);
				}
			}
		});
		
		int numPerCol = ids.size()/numRows;
		if(ids.size() % numRows != 0) numPerCol++;
		final List<JPanel> idPanels = new ArrayList<JPanel>();
		for(int i = 0; i < numPerCol; i++) {
			final JPanel sitePanel = new JPanel();
			sitePanel.setLayout(new BoxLayout(sitePanel, BoxLayout.Y_AXIS));
			idPanels.add(sitePanel);
		}
		
		if(ids.size() > 0) {
			int ip = 0;
			int i = 0;
			final JPanel boxPanel = new JPanel();
			JPanel sitePanel = idPanels.get(ip);
			for(Integer id : ids) {
				if(i != 0 && i % numRows == 0) {
					sitePanel = idPanels.get(++ip);
				}
				
				boolean selected = false;
				if(selectedIds.contains(id)) {
					selected = true;
				}
				final JCheckBox box = new JCheckBox(id.toString(), selected);
				checkBoxes.add(box);
				sitePanel.add(box);
				i++;
			}
			
			for(JPanel p : idPanels) {
				boxPanel.add(p);
			}
			panel.add(boxPanel);
		}
		
		final JPanel buttonPanel = new JPanel(); 
		final JButton selectButton = new JButton("Select");
		selectButton.setPreferredSize(new Dimension(90,20));
		buttonPanel.add(selectButton);
		panel.add(buttonPanel);
		selectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for(JCheckBox box : checkBoxes) {
					if(box.isSelected()) {
						selectedIDs.add(new Integer(box.getText()));
					}
				}
				choice = SELECTED;
				dispose();
			}
		});
		
		add(panel);
		pack();
		setLocationRelativeTo(parent);
	}
	
	public Set<Integer> getSelectedIDs() {
		return selectedIDs;
	}
	
	public void setSelectedIDs(Set<Integer> selectedIDs) {
		this.selectedIDs = selectedIDs;
	}

	public int getChoice() {
		return choice;
	}

	public static void main(String[] args) {
		Set<Integer> set = new TreeSet<Integer>();
		set.add(1);
		set.add(2);
		set.add(3);
		set.add(4);
		SelectIdDialogue select = new SelectIdDialogue(null, set, 2);
		select.setVisible(true);
		
		for(Integer id : select.getSelectedIDs()) {
			System.out.println(id);
		}
	}
}
