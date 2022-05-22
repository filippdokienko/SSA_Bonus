package simulation;

import java.util.ArrayList;
import java.util.List;

/**
 *	A source of products
 *	This class implements CProcess so that it can execute events.
 *	By continuously creating new events, the source keeps busy.
 *	@author Joel Karel
 *	@version %I%, %G%
 */
public class Source implements CProcess
{
	/** Eventlist that will be requested to construct events */
	private CEventList list;
	/** Queue that buffers products for the machine */
	private CustomerAcceptor queue;
	private CustomerAcceptor[] queues;
	/** Name of the source */
	private String name;
	/** Mean interarrival time */
	private double meanArrTime;
	/** Interarrival times (in case pre-specified) */
	private double[] interarrivalTimes;
	/** Interarrival time iterator */
	private int interArrCnt;

	public int numberOfArrivals = 0;


	/**
	*	Constructor, creates objects
	*        Interarrival times are exponentially distributed with specified mean
	*	@param q	The receiver of the products
	*	@param l	The eventlist that is requested to construct events
	*	@param n	Name of object
	*	@param m	Mean arrival time
	*/
	public Source(CustomerAcceptor q, CEventList l, String n, double m)
	{
		list = l;
		queue = q;
		name = n;
		meanArrTime=m;
		// put first event in list for initialization
		list.add(this,0,drawRandomExponential(meanArrTime)); //target,type,time
	}

	/**
	 *	Constructor, creates objects
	 *        Interarrival times are exponentially distributed with specified mean
	 *	@param q	Array of receiver of the products
	 *	@param l	The eventlist that is requested to construct events
	 *	@param n	Name of object
	 *	@param m	Mean arrival time
	 */
	public Source(CustomerAcceptor[] q, CEventList l, String n, double m)
	{
		list = l;
		queues = q;
		name = n;
		meanArrTime=m;
		// put first event in list for initialization
		list.add(this,0,drawRandomExponential(meanArrTime)); //target,type,time
	}
	
        @Override
	public void execute(int type, double tme)
	{
		// show arrival
		System.out.println("Arrival at time = " + tme);
		numberOfArrivals++;
		// give arrived product to queue
		Customer p = new Customer();
		p.stamp(tme,"Creation",name);
		// pick queue to send customer to
		if(queue == null){
			Queue bestQueue = null;
			for(CustomerAcceptor ca : queues){
				Queue q = (Queue) ca;
				if(q.isOpen() && q.getSize() < 4){
					if(bestQueue == null) bestQueue = q;
					int bestSize = bestQueue.getSize();
					if(q.getSize() < bestSize) bestQueue = q;
				}
			}
			// if all queues have 4 or more customers waiting, try to open new one
			if(bestQueue == null) {
				for (CustomerAcceptor ca : queues) {
					Queue q = (Queue) ca;
					if (!q.isOpen()) {
						q.open();
						bestQueue = q;
						break;
					}
				}
			}
			// if all queues are open already, send customer to the queue with the least customers
			if(bestQueue == null) {
				for (CustomerAcceptor ca : queues) {
					Queue q = (Queue) ca;
					if (bestQueue == null) bestQueue = q;
					int bestSize = bestQueue.getSize();
					if (q.getSize() < bestSize) bestQueue = q;
				}
			}
			System.out.println("Best queue: " + bestQueue);
			bestQueue.giveCustomer(p);

			// check whether any queue may be closed (at least 3 open queues (2 regular + combined))
			int openQueues = 0;
			List<Queue> emptyOpenQueues = new ArrayList<>();
			for(CustomerAcceptor ca : queues){
				Queue q = (Queue) ca;
				if(q==bestQueue) continue; // ignore current queue
				if(q.isOpen() && q.serviceQueue==null){ // ignore combined
					openQueues++;
					if(q.getSize() == 0) emptyOpenQueues.add(q);
				}
			}
			while(openQueues > 2 && emptyOpenQueues.size() > 0){
				Queue q = emptyOpenQueues.get(0);
				q.close();
				emptyOpenQueues.remove(0);
				openQueues--;
			}
		} else {
			queue.giveCustomer(p);
		}
		// generate duration
		if(meanArrTime>0)
		{
			double duration = drawRandomExponential(meanArrTime);
			// Create a new event in the eventlist
			list.add(this,0,tme+duration); //target,type,time
		}
		else
		{
			interArrCnt++;
			if(interarrivalTimes.length>interArrCnt)
			{
				list.add(this,0,tme+interarrivalTimes[interArrCnt]); //target,type,time
			}
			else
			{
				list.stop();
			}
		}
	}
	
	public static double drawRandomExponential(double mean)
	{
		// draw a [0,1] uniform distributed number
		double u = Math.random();
		// Convert it into a exponentially distributed random variate with mean 33
		double res = -mean*Math.log(u);
		return res;
	}
}