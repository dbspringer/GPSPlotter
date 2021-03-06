package com.dbs.gps.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYDataset;

import com.dbs.gps.data.RaDec;


/**
 * @author Derek Springer
 */
public class GPSPlotterGUI extends JFrame {

	private static final long serialVersionUID = 
			"$Id: GPSPlotterGUI.java 2606 2011-10-05 00:02:53Z derek.springer $".hashCode();

	private static final JFreeChart EMPTY_CHART = 
			ChartFactory.createScatterPlot(
					null,
					"Right Ascension",
					"Declination",
					new DefaultXYDataset(),
					PlotOrientation.VERTICAL,
					true, 
					true,
					false );

	/**
	 * Properties for the GUI, default located in inc/application.properties
	 */
	private Properties properties = new Properties();
	
	/**
	 * The chart to display the traces on
	 */
	private ChartPanel chartPanel = new ChartPanel(EMPTY_CHART);
	
	/**
	 * Slider to select the time scale
	 */
	private RangeSlider timeSlider = new RangeSlider(); 
	
	/**
	 * List to store all the RaDec points
	 */
	private List<RaDec> points = new ArrayList<RaDec>();
	
	/**
	 * The currently selected set of points
	 */
	private Set<Integer> selectedIds = new TreeSet<Integer>();
	
	/**
	 * The range Range to set the chart to
	 */
	private Range range = null;
	
	/**
	 * The domain Rainge to set the chart to 
	 */
	private Range domain = null;
	
	/**
	 * Keep track of thum movements
	 */
	private boolean thumbMoved = false;
	
	/**
	 * A plotter to display GPS trajectories
	 * @param properties Properties for the GUI
	 */
	public GPSPlotterGUI(Properties properties) {
		super("GPS Plotter");
		this.properties = properties;
		
		URL logoURL = getClass().getResource("/img/logo.gif");
		try {
			BufferedImage logoImg = ImageIO.read(logoURL);
			setIconImage(logoImg);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						closeGUI();
					}
				});
			}
		});
		
		createGUI();
		pack();
	}
	
	private void createGUI() {
		createMenuBar();
		
		final JPanel panel = new JPanel(new BorderLayout());
		add(panel);
		
		panel.add(chartPanel, BorderLayout.CENTER);
		
		final JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
		
		sliderPanel.add(timeSlider);
		timeSlider.setValue(0);
		timeSlider.setUpperValue(timeSlider.getMaximum());
		updateSlider();
		
		final JPanel labelPanel = new JPanel();
		final JLabel startLabel = new JLabel("Start: ", JLabel.LEFT);
		final JLabel startValLabel = 
				new JLabel(String.valueOf(timeSlider.getValue()));
		final JLabel endLabel = new JLabel("End: ", JLabel.LEFT);
		final JLabel endValLabel = 
				new JLabel(String.valueOf(timeSlider.getUpperValue()));
		labelPanel.add(startLabel);
		labelPanel.add(startValLabel);
		labelPanel.add(endLabel);
		labelPanel.add(endValLabel);
		sliderPanel.add(labelPanel);
		
		timeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                RangeSlider slider = (RangeSlider) e.getSource();
                startValLabel.setText(String.valueOf(slider.getValue()));
                endValLabel.setText(String.valueOf(slider.getUpperValue()));
                thumbMoved = true;
            }
        });
		
		timeSlider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(thumbMoved) {
					plotPoints();
					thumbMoved = !thumbMoved;
				}
			}
		});
		
		panel.add(sliderPanel, BorderLayout.SOUTH);
		
		sliderPanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateSlider();
			}
		});
	}
	
	private void createMenuBar() {
		final JFrame me = this;
		final JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		final JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		
		final JMenuItem loadPlotItem = new JMenuItem("Load Plot(s)");
		fileMenu.add(loadPlotItem);
		loadPlotItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JFileChooser chooser = 
					new JFileChooser(System.getProperty("user.dir"));
				chooser.setMultiSelectionEnabled(true);
				int val = chooser.showOpenDialog(me);
				if(val == JFileChooser.APPROVE_OPTION) {
					File[] files = chooser.getSelectedFiles();
					try {
						for(File file : files) {
							if(file.isFile()) {
								List<RaDec> raDecs = 
										RaDec.loadRaDec(file.toURI().toURL());
								points.addAll(raDecs);
								selectedIds = RaDec.getIDs(points);
							}
						}
						
						int[] startStop = RaDec.getRange(points);
						timeSlider.setMinimum(startStop[0]);
						timeSlider.setValue(startStop[0]);
						timeSlider.setMaximum(startStop[1]);
						timeSlider.setUpperValue(startStop[1]);
						plotPoints();
					} catch (MalformedURLException ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		
		final JMenuItem clearPlotItem = new JMenuItem("Clear Plots");
		fileMenu.add(clearPlotItem);
		clearPlotItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				points.clear();
				selectedIds.clear();
				chartPanel.setChart(EMPTY_CHART);
			}
		});

		fileMenu.addSeparator();
		final JMenuItem exitItem = new JMenuItem("Exit");
		fileMenu.add(exitItem);
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeGUI();
			}
		});
		
		final JMenu selectMenu = new JMenu("Select");
		menuBar.add(selectMenu);
		
		final JMenuItem selectIdsItem = new JMenuItem("IDs");
		selectMenu.add(selectIdsItem);
		selectIdsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SelectIdDialogue select = new SelectIdDialogue(
						me, RaDec.getIDs(points), selectedIds, 5);
				select.setVisible(true);
				if(select.getChoice() == SelectIdDialogue.SELECTED) {
					selectedIds = select.getSelectedIDs();
					plotPoints();
				}
			}
		});
	}
	
	private void closeGUI() {
		properties.setProperty(
				"loc.x", Integer.toString(getLocation().x));
		properties.setProperty(
				"loc.y", Integer.toString(getLocation().y));
		properties.setProperty(
				"size.width", Integer.toString(getSize().width));
		properties.setProperty(
				"size.height", Integer.toString(getSize().height));
		
		File propertiesFile = new File(properties.getProperty("self.path"));
		try {
			if(!propertiesFile.exists()) {
				propertiesFile.getParentFile().mkdirs();
			}

			properties.remove("self.path");
			properties.store(new BufferedWriter(
					new FileWriter(propertiesFile)), null);
		} catch (IOException e) {
			e.printStackTrace();
		}

		dispose();
	}
	
	private void updateSlider() {
		timeSlider.setPreferredSize(new Dimension(
				chartPanel.getSize().width > 0 ?
						chartPanel.getSize().width-10 : 
						Integer.parseInt(properties.getProperty("size.width", "500"))-10,
				20));
	}
	
	public void plotPoints() {
		Map<Integer, List<RaDec>> pointsById = RaDec.groupByID(
				RaDec.filterById(
						RaDec.filterByRange(
								points,
								timeSlider.getValue(),
								timeSlider.getUpperValue()),
				selectedIds));
		DefaultXYDataset dataset = new DefaultXYDataset();
		for(Integer key : pointsById.keySet()) {
			double[][] data = new double[2][pointsById.get(key).size()];
			for(int i = 0; i < pointsById.get(key).size(); i++) {
				data[0][i] = pointsById.get(key).get(i).getRightAscension();
				data[1][i] = pointsById.get(key).get(i).getDeclination();
			}
			dataset.addSeries(key, data);
		}
		
		JFreeChart chart = ChartFactory.createScatterPlot(
				null,
				"Right Ascension",
				"Declination",
				dataset,
				PlotOrientation.VERTICAL,
				true, 
				true,
				false);
		
		Range thisRange = chart.getXYPlot().getRangeAxis().getRange();
		if(range == null) {
			range = thisRange;
		} else {
			range = new Range(
					thisRange.getLowerBound() < range.getLowerBound() ?
							thisRange.getLowerBound() : range.getLowerBound(),
					thisRange.getUpperBound() > range.getUpperBound() ?
							thisRange.getUpperBound() : range.getUpperBound());
		}
		chart.getXYPlot().getRangeAxis().setAutoRange(false);
		chart.getXYPlot().getRangeAxis().setRange(range);
		
		Range thisDomain = chart.getXYPlot().getDomainAxis().getRange();
		if(domain == null) {
			domain = thisDomain;
		} else {
			domain = new Range(
					thisDomain.getLowerBound() < domain.getLowerBound() ?
							thisDomain.getLowerBound() : domain.getLowerBound(),
					thisDomain.getUpperBound() > domain.getUpperBound() ?
							thisDomain.getUpperBound() : domain.getUpperBound());
		}
		chart.getXYPlot().getDomainAxis().setAutoRange(false);
		chart.getXYPlot().getDomainAxis().setRange(domain);
		
		chart.setAntiAlias(false);
		chartPanel.setChart(chart);
	}

	public static void main(final String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				String propertiesPath = "inc/application.properties";
				if(args.length == 1 && new File(args[0]).isFile()) {
					propertiesPath = args[0];
				}
				
				File propertiesFile = new File(propertiesPath);
				Properties properties = new Properties();
				try {
					properties.load(
							new BufferedReader(new FileReader(propertiesFile)));
				} catch (IOException e) {
					System.out.println(
							propertiesFile + " not found, using defaults");
				}
				properties.setProperty("self.path", propertiesFile.getPath());

				GPSPlotterGUI gui = 
						new GPSPlotterGUI(properties);
				int x = Integer.parseInt(properties.getProperty("loc.x", "0"));
				int y = Integer.parseInt(properties.getProperty("loc.y", "0"));
				int width = Integer.parseInt(
						properties.getProperty("size.width", "500"));
				int height = Integer.parseInt(
						properties.getProperty("size.height", "500"));
				gui.setSize(width, height);
				gui.setLocation(new Point(x,y));
				gui.setVisible(true);
			}
		});
	}
}
