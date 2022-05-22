package simulation;

import java.util.ArrayList;

/**
 *	Queue that stores products until they can be handled on a machine machine
 *	@author Joel Karel
 *	@version %I%, %G%
 */
public class Queue implements CustomerAcceptor
{
	/** List in which the products are kept */
	private ArrayList<Customer> row;
	/** Requests from machine that will be handling the products */
	private ArrayList<CashRegister> requests;

	private boolean open = true;
	public boolean isService = false;
	public Queue serviceQueue = null;

	private static int prevId = 0;
	public final int ID = prevId++;
	/**
	*	Initializes the queue and introduces a dummy machine
	*	the machine has to be specified later
	*/
	public Queue()
	{
		row = new ArrayList<>();
		requests = new ArrayList<>();
	}
	
	/**
	*	Asks a queue to give a product to a machine
	*	True is returned if a product could be delivered; false if the request is queued
	*/
	public boolean askProduct(CashRegister machine)
	{
		// This is only possible with a non-empty queue
		if(row.size()>0)
		{
			// If the machine accepts the product
			if(machine.giveCustomer(row.get(0)))
			{
				row.remove(0);// Remove it from the queue
				return true;
			}
			else
				return false; // Machine rejected; don't queue request
		}
		else
		{
			requests.add(machine);
			return false; // queue request
		}
	}
	
	/**
	*	Offer a product to the queue
	*	It is investigated whether a machine wants the product, otherwise it is stored
	*/
	public boolean giveCustomer(Customer p)
	{
		// Check if the machine accepts it
		if(requests.size()<1)
			row.add(p); // Otherwise store it
		else
		{
			boolean delivered = false;
			while(!delivered & (requests.size()>0))
			{
				delivered=requests.get(0).giveCustomer(p);
				// remove the request regardless of whether or not the product has been accepted
				requests.remove(0);
			}
			if(!delivered)
				row.add(p); // Otherwise store it
		}
		return true;
	}

	public void open(){
		open = true;
		System.out.println("Opening Queue " + ID);
	}

	public void close(){
		open = false;
		System.out.println("Closing Queue " + ID);
	}

	public boolean isOpen(){
		return open;
	}

	public int getSize(){
		if(serviceQueue != null){
			return serviceQueue.getSize() + row.size();
		}
		return row.size();
	}

	@Override
	public String toString(){
		return "Queue " + ID;
	}
}