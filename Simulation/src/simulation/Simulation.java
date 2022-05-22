/**
 *	Example program for using eventlists
 *	@author Joel Karel
 *	@version %I%, %G%
 */

package simulation;

public class Simulation {

    public CEventList list;
    public Queue queue;
    public Source source;
    public Sink sink;
    public CashRegister mach;
	
    static double openTime = 8*60; // in minutes, 8 hours

        /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Create an eventlist
        CEventList l = new CEventList();


        int number_of_cash_registers_regular = 5;
        int number_of_cash_registers_combined = 1;

        // A queue for service desk customers
        Queue queue_service = new Queue();

        Queue[] regularQueues = new Queue[number_of_cash_registers_regular+number_of_cash_registers_combined];
        for (int i = 0; i < number_of_cash_registers_regular + number_of_cash_registers_combined; i++) {
            regularQueues[i] = new Queue();
        }
        // only two queues are open in the beginning
        for(int i = 2; i < regularQueues.length; i++) {
            regularQueues[i].close();
        }
        // combined queue is always open
        regularQueues[regularQueues.length-1].open();
        regularQueues[regularQueues.length-1].isService = true;
        regularQueues[regularQueues.length-1].serviceQueue = queue_service;



        // Service desk customers
        double service_desk_customers_interarrival_mean = 5.0; // on average 5 minutes between two arrivals
        Source s_service = new Source(queue_service,l,"Service desk customers",service_desk_customers_interarrival_mean);

        // Regular customers
        double regular_customers_rate = 1; // 1 customer per minute
        double regular_customers_interarrival_mean = 1/regular_customers_rate;
        Source s_regular = new Source(regularQueues, l, "Regular customers",regular_customers_interarrival_mean);

        // Sinks
        Sink sink = new Sink("Sink");



        double mean_serivce_time_regular = 2.6;
        double sd_service_time_regular = 1.1;
        double mean_serivce_time_service = 4.1;
        double sd_service_time_service = 1.1;

        // Cash registers
        CashRegister[] cash_registers = new CashRegister[number_of_cash_registers_regular + number_of_cash_registers_combined];
        for (int i = 0; i < number_of_cash_registers_regular; i++) {
            cash_registers[i] = new CashRegister(regularQueues[i],sink, l,"Cash register " + (i+1), mean_serivce_time_regular, sd_service_time_regular);
        }
        for (int i = 0; i < number_of_cash_registers_combined; i++) {
            cash_registers[i + number_of_cash_registers_regular] = new CashRegister(regularQueues[number_of_cash_registers_combined+i], queue_service, sink, l,"Combined cash register " + (i+1), mean_serivce_time_service, sd_service_time_service);
        }


        // start the eventlist
        l.start(openTime);

        System.out.println("Arrived regular customers: " + s_regular.numberOfArrivals);
        System.out.println("Arrived service desk customers: " + s_service.numberOfArrivals);

        for(int i = 0; i < cash_registers.length; i++) {
            System.out.println("Accepted customers in cash register " + (i+1) + ": " + cash_registers[i].countAcceptedCustomers);
        }

        for(Queue q : regularQueues) {
            System.out.println(q + " is open: " + q.isOpen());
            System.out.println("Customer in " + q + ": " + q.getSize());
        }

    }
    
}
