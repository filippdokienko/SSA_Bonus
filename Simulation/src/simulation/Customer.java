package simulation;

import java.util.ArrayList;
/**
 *	Product that is send trough the system
 *	@author Joel Karel
 *	@version %I%, %G%
 */
class Customer
{

	private static int idCounter = 0;
	public final int ID = idCounter++;

	/** Stamps for the products */
	private ArrayList<Double> times;
	private ArrayList<String> events;
	private ArrayList<String> stations;
	
	/** 
	*	Constructor for the product
	*	Mark the time at which it is created
	*	@return create The current time
	*/
	public Customer()
	{
		times = new ArrayList<>();
		events = new ArrayList<>();
		stations = new ArrayList<>();
	}
	
	
	public void stamp(double time,String event,String station)
	{
		times.add(time);
		events.add(event);
		stations.add(station);
	}
	
	public ArrayList<Double> getTimes()
	{
		return times;
	}

	public ArrayList<String> getEvents()
	{
		return events;
	}

	public ArrayList<String> getStations()
	{
		return stations;
	}
	
	public double[] getTimesAsArray()
	{
		times.trimToSize();
		double[] tmp = new double[times.size()];
		for (int i=0; i < times.size(); i++)
		{
			tmp[i] = (times.get(i)).doubleValue();
		}
		return tmp;
	}

	public String[] getEventsAsArray()
	{
		String[] tmp = new String[events.size()];
		tmp = events.toArray(tmp);
		return tmp;
	}

	public String[] getStationsAsArray()
	{
		String[] tmp = new String[stations.size()];
		tmp = stations.toArray(tmp);
		return tmp;
	}
}