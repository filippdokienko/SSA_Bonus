package simulation;

import java.util.Random;

/**
 *	Machine in a factory
 *	@author Joel Karel
 *	@version %I%, %G%
 */
public class CashRegister implements CProcess, CustomerAcceptor
{
	/** Whether this is the store-front */
	public boolean isStoreFront = false;
	/** Product that is being handled  */
	private Customer product;
	/** Eventlist that will manage events */
	private final CEventList eventlist;
	/** Queue from which the machine has to take products */
	private Queue queue;
	/** Whether accepts service desk customers	*/
	private boolean serviceDesk = false;
	/** Queue for service desk products */
	private Queue serviceDeskQueue;
	/** Sink to dump products */
	private CustomerAcceptor sink;
	/** Sink to dump service desk products */
	/** Status of the machine (b=busy, i=idle) */
	private char status;
	/** Machine name */
	private final String name;
	/** Mean processing time */
	private double meanProcTime;
	/** SD service time */
	private double sdProcTime;
	/** Processing times (in case pre-specified) */
	private double[] processingTimes;
	/** Processing time iterator */
	private int procCnt;

	public int countAcceptedCustomers = 0;

	/**
	 *	Constructor
	 *
	 *	@param q	Queue from which the machine has to take products
	 *  @param s	Where to send the completed products
	 *	@param e	Eventlist that will manage events
	 *	@param n	The name of the machine
	 *	@param mean	Mean of service time
	 *	@param sd	Standard deviation of service time
	 */
	public CashRegister(Queue q, CustomerAcceptor s, CEventList e, String n, double mean, double sd)
	{
		status='i';
		queue=q;
		sink=s;
		eventlist=e;
		name=n;
		meanProcTime=mean;
		sdProcTime=sd;
		queue.askProduct(this);
	}

	/**
	 *	Constructor
	 *
	 *	@param q	Queue from which the machine has to take products
	 *	@param sq	Queue for service desk products
	 *  @param s	Where to send the completed products
	 *	@param e	Eventlist that will manage events
	 *	@param n	The name of the machine
	 *	@param mean	Mean of service time
	 *	@param sd	Standard deviation of service time
	 */
	public CashRegister(Queue q, Queue sq, CustomerAcceptor s, CEventList e, String n, double mean, double sd)
	{
		status='i';
		queue=q;
		serviceDeskQueue=sq;
		serviceDesk=true;
		sink=s;
		eventlist=e;
		name=n;
		meanProcTime=mean;
		sdProcTime=sd;
		queue.askProduct(this);
	}


	/**
	*	Method to have this object execute an event
	*	@param type	The type of the event that has to be executed
	*	@param tme	The current time
	*/
	public void execute(int type, double tme)
	{
		// show arrival
		System.out.println("Departure at time = " + tme);
		// Remove product from system
		product.stamp(tme,"Production complete",name);
		sink.giveCustomer(product);
		product=null;
		// set machine status to idle
		status='i';
		// Ask the queue for products
		// first all service desk customers if applicable
		if(serviceDesk && serviceDeskQueue.getSize()>0){
			serviceDeskQueue.askProduct(this);
		}
		else if (queue.getSize()>0){
			queue.askProduct(this);
		}
		else if (serviceDesk){
			serviceDeskQueue.askProduct(this);
		}
		else {
			queue.askProduct(this);
		}
	}
	
	/**
	*	Let the machine accept a product and let it start handling it
	*	@param p	The product that is offered
	*	@return	true if the product is accepted and started, false in all other cases
	*/
        @Override
	public boolean giveCustomer(Customer p)
	{
		// Only accept something if the machine is idle
		if(status=='i')
		{
			System.out.println(name + ": accepting customer " + p.ID + " [|Q|=" + queue.getSize() + "]");
			countAcceptedCustomers++;
			// accept the product
			product=p;
			// mark starting time
			product.stamp(eventlist.getTime(),"Production started",name);
			// start production
			startProduction();
			// Flag that the product has arrived
			return true;
		}
		// Flag that the product has been rejected
		else return false;
	}
	
	/**
	*	Starting routine for the production
	*	Start the handling of the current product with an exponentionally distributed processingtime with average 30
	*	This time is placed in the eventlist
	*/
	private void startProduction()
	{
		// generate duration
		if(meanProcTime>0)
		{
			double duration = drawRandomGaussian(meanProcTime, sdProcTime);
			// Create a new event in the eventlist
			double tme = eventlist.getTime();
			eventlist.add(this,0,tme+duration); //target,type,time
			// set status to busy
			status='b';
		}
		else
		{
			if(processingTimes.length>procCnt)
			{
				eventlist.add(this,0,eventlist.getTime()+processingTimes[procCnt]); //target,type,time
				// set status to busy
				status='b';
				procCnt++;
			}
			else
			{
				eventlist.stop();
			}
		}
	}


	public static double drawRandomGaussian(double mean, double stddev){
		return new Random().nextGaussian()*stddev+mean;
	}
}