package com.dbs.gps.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A class to represent the right ascension & declination of an object at
 * a particular time
 * 
 * @author Derek Springer
 */
public class RaDec {
	
	/**
	 * Time in seconds past start
	 */
	private int time = -1;
	
	/**
	 * ID of object
	 */
	private int id = -1;
	
	/**
	 * Right Ascension in degrees of object at given time
	 */
	private double rightAscension = Double.NaN;
	
	/**
	 * Declination in degrees of object at given time
	 */
	private double declination = Double.NaN;
	
	/**
	 * Default constructor
	 */
	public RaDec() {}

	/**
	 * The right ascension & declination of given object id at a given time
	 * @param time Time in seconds past start
	 * @param id ID of object
	 * @param rightAscension Right Ascension of object at given time
	 * @param declination Declination of object at given time
	 */
	public RaDec(int time, int id, double rightAscension, double declination) {
		this.time = time;
		this.id = id;
		this.rightAscension = rightAscension;
		this.declination = declination;
	}
	
	/**
	 * Loads List of RaDec objects from given URL
	 * @param raDecURL URL to RaDec file
	 * @return List of RaDec objects from given file in given order
	 */
	public static List<RaDec> loadRaDec(URL raDecURL) {
		List<RaDec> raDecs = new ArrayList<RaDec>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(
					new InputStreamReader(raDecURL.openStream()));
			
			String line = null;
			while((line = in.readLine()) != null) {
				String[] splits = line.trim().split("\\s");
				if(splits.length != 4) continue;
				int time = Integer.parseInt(splits[0]);
				int id = Integer.parseInt(splits[1]);
				double rightAscension = Double.parseDouble(splits[2]);
				double declination = Double.parseDouble(splits[3]);
				raDecs.add(new RaDec(time, id, rightAscension, declination));
			}
		}  catch(IOException e) {
			e.printStackTrace();
		} finally {
			if(in != null) {
				try { in.close(); } catch (IOException e) {}
			}
		}
		
		return raDecs;
	}
	
	/**
	 * Returns a the set of ids contained within the list
	 * @param raDecs List of RaDec objects to grab ids from
	 * @return Set of IDs contained in List
	 */
	public static Set<Integer> getIDs(List<RaDec> raDecs) {
		Set<Integer> ids = new TreeSet<Integer>();
		for(RaDec raDec : raDecs) {
			ids.add(raDec.getId());
		}
		
		return ids;
	}

	/**
	 * Returns the start and stop times for the range of given RaDec objects
	 * @param raDecs List of RaDec objects to grab range from
	 * @return int[] w/ start in [0] and stop in [1]
	 */
	public static int[] getRange(List<RaDec> raDecs) {
		int[] startStop = new int[2];
		int start = Integer.MAX_VALUE;
		int stop = 0;
		
		for(RaDec raDec : raDecs) {
			if(raDec.getTime() < start) start = raDec.getTime();
			if(raDec.getTime() > stop)  stop = raDec.getTime();
		}
		startStop[0] = start;
		startStop[1] = stop;
		
		return startStop;
	}
	
	/**
	 * Filters the given list of RaDec objects by the given collection of ids.
	 * 	Every RaDec with id not in not in ids will be excluded.
	 * @param raDecs List of RaDec objects to filter
	 * @param ids Collection of IDs to include
	 * @return List of filtered RaDecs
	 */
	public static List<RaDec> filterById(
			List<RaDec> raDecs,
			Collection<Integer> ids) {
		
		List<RaDec> filtered = new ArrayList<RaDec>();
		for(RaDec raDec : raDecs) {
			if(ids.contains(raDec.getId())) {
				filtered.add(raDec);
			}
		}
		
		return filtered;
	}
	
	/**
	 * Filters the given list of RaDec objects by the given start/stop times.
	 * 	Every RaDec not within start/stop will be excluded. 
	 * @param raDecs raDecs List of RaDec objects to filter
	 * @param start Start time of filter
	 * @param stop End time of filter
	 * @return List of filtered RaDecs
	 */
	public static List<RaDec> filterByRange(
			List<RaDec> raDecs,
			int start,
			int stop) {
		
		List<RaDec> filtered = new ArrayList<RaDec>();
		for(RaDec raDec : raDecs) {
			if(raDec.getTime() >= start && raDec.getTime() <= stop) {
				filtered.add(raDec);
			}
		}
		
		return filtered;
	}
	
	/**
	 * Groups the given list or RaDec objects by their ID.
	 * Note: does not sort results.
	 * @param raDecs List of RaDec objects to group
	 * @return Map of RaDecs, grouped by ID. 
	 * 	key = id, val = List&lt;RaDec&gt; for id
	 */
	public static Map<Integer, List<RaDec>> groupByID(List<RaDec> raDecs) {
		Map<Integer, List<RaDec>> idGroup = 
				new TreeMap<Integer, List<RaDec>>();
		
		for(RaDec raDec : raDecs) {
			if(!idGroup.containsKey(raDec.getId())) {
				idGroup.put(raDec.getId(), new ArrayList<RaDec>());
			}
			idGroup.get(raDec.getId()).add(raDec);
		}
		
		return idGroup;
	}
	
	/**
	 * Groups the given list or RaDec objects by their time.
	 * Note: does not sort results.
	 * @param raDecs List of RaDec objects to group
	 * @return Map of RaDecs, grouped by time. 
	 * 	key = time, val = List&lt;RaDec&gt; for time
	 */
	public static Map<Integer, List<RaDec>> groupByTime(List<RaDec> raDecs) {
		Map<Integer, List<RaDec>> timeGroup = 
				new TreeMap<Integer, List<RaDec>>();
		
		for(RaDec raDec : raDecs) {
			if(!timeGroup.containsKey(raDec.getTime())) {
				timeGroup.put(raDec.getTime(), new ArrayList<RaDec>());
			}
			timeGroup.get(raDec.getTime()).add(raDec);
		}
		
		return timeGroup;
	}
	
	@Override
	public String toString() {
		return String.format(
				"%d\t%d\t%f\t%f", time, id, rightAscension, declination);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof RaDec) || obj == null) return false;
		RaDec other = (RaDec)obj;
		return other.getTime() == time &&
				other.getId() == id &&
				Double.compare(other.getRightAscension(), rightAscension) == 0 &&
				Double.compare(other.getDeclination(), declination) == 0;
	}
	
	@Override
	public int hashCode() {
		return String.format(
				"%d\t%d\t%d\t%d",
				time,
				id,
				(int)rightAscension,
				(int)declination).hashCode();
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getRightAscension() {
		return rightAscension;
	}

	public void setRightAscension(double rightAscension) {
		this.rightAscension = rightAscension;
	}

	public double getDeclination() {
		return declination;
	}

	public void setDeclination(double declination) {
		this.declination = declination;
	}
	
	public static void main(String[] args) {
		try {
			URL raDecURL = new File("radec.txt").toURI().toURL();
			List<RaDec> raDecs = loadRaDec(raDecURL);
			Map<Integer, List<RaDec>> idGroup = groupByID(raDecs);
			for(Integer key : idGroup.keySet()) {
				System.out.println(key);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
}
