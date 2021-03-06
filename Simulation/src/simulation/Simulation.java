/**
 *	Example program for using eventlists
 *	@author Joel Karel
 *	@version %I%, %G%
 */

package simulation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Simulation {

	
    static double openTime = 2*60; // How long the simulation runs for in (simulated) minutes. Here: 2 Hours

        /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        // create data file for analysis
        boolean createDataFile = false;
        if(createDataFile){
            sample(1000,50);
            System.exit(187);
        }
        // Otherwise just run a simulation
        else {
            Object[] results = simulate();
            System.out.println("Total customers arrived: " + results[0]);
            System.out.println("Total customers served: " + results[1]);
            System.out.println("Average Delay Time: " + results[2]);
            System.out.println("Average Delay Time (regular): " + results[3]);
            System.out.println("Average Delay Time (service desk): " + results[4]);
        }
    }

    /**
     * Simulates the system for a given number of times.
     * @param N
     * @return Returns the average delay time, the average delay time for regular customers, the average delay time for service desk customers, the total number of customers served, and the total number of customers arrived.
     */
    private static double[] simulateAverage(int N){
        double total_AVG_arrivals = 0;
        double total_AVG_served = 0;
        double total_AVG_delay = 0;
        double total_AVG_delay_regular = 0;
        double total_AVG_delay_service = 0;

        double total_AVG_queue_length = 0;
        double total_AVG_queue_length_NE = 0; // average of non-empty queue length

        double avg_queue1_length_NE = 0;
        double avg_queue2_length_NE = 0;
        double avg_queue3_length_NE = 0;
        double avg_queue4_length_NE = 0;
        double avg_queue5_length_NE = 0;
        double avg_queue6_length_NE = 0;

        double avg_open_time_q1 = 0;
        double avg_open_time_q2 = 0;
        double avg_open_time_q3 = 0;
        double avg_open_time_q4 = 0;
        double avg_open_time_q5 = 0;
        double avg_open_time_q6 = 0;



        for(int i = 0; i < N; i++){
            Object[] results = simulate();
            total_AVG_arrivals += (int) results[0];
            total_AVG_served += (int)  results[1];
            total_AVG_delay += (double) results[2];
            total_AVG_delay_regular += (double) results[3];
            total_AVG_delay_service += (double) results[4];
            total_AVG_queue_length += (double) results[5];
            total_AVG_queue_length_NE += (double) results[6];

            double[] avg_queue_length = (double[]) results[7];
            double[] avg_queue_length_NE = (double[]) results[8];
            avg_queue1_length_NE += avg_queue_length_NE[0];
            avg_queue2_length_NE += avg_queue_length_NE[1];
            avg_queue3_length_NE += avg_queue_length_NE[2];
            avg_queue4_length_NE += avg_queue_length_NE[3];
            avg_queue5_length_NE += avg_queue_length_NE[4];
            avg_queue6_length_NE += avg_queue_length_NE[5];

            double[] open_time_q = (double[]) results[9];
            avg_open_time_q1 += open_time_q[0];
            avg_open_time_q2 += open_time_q[1];
            avg_open_time_q3 += open_time_q[2];
            avg_open_time_q4 += open_time_q[3];
            avg_open_time_q5 += open_time_q[4];
            avg_open_time_q6 += open_time_q[5];
        }

        double[] results = new double[19];
        results[0] = total_AVG_arrivals/N;
        results[1] = total_AVG_served/N;
        results[2] = total_AVG_delay/N;
        results[3] = total_AVG_delay_regular/N;
        results[4] = total_AVG_delay_service/N;
        results[5] = total_AVG_queue_length/N;

        results[6] = total_AVG_queue_length_NE/N;

        results[7] = avg_queue1_length_NE/N;
        results[8] = avg_queue2_length_NE/N;
        results[9] = avg_queue3_length_NE/N;
        results[10] = avg_queue4_length_NE/N;
        results[11] = avg_queue5_length_NE/N;
        results[12] = avg_queue6_length_NE/N;

        results[13] =  avg_open_time_q1/N;
        results[14] =  avg_open_time_q2/N;
        results[15] =  avg_open_time_q3/N;
        results[16] =  avg_open_time_q4/N;
        results[17] =  avg_open_time_q5/N;
        results[18] =  avg_open_time_q6/N;


        return results;
    }

    /**
     * Simulates the system once
     * @return an Object[] containing statistics such as average delay time:
     */
    private static Object[] simulate(){
        // Create an eventlist
        CEventList l = new CEventList();


        /*
        (initialize, 6 cash registers (machines) in total. each cash register has its own regular queue. combined cash register has an additional service queue)
         Cash Registers have normally distributed service times (different for regular/service desk customers) ; provided in manual
         2 sources: regular customers and service desk customers (poisson process)
         customers in source decide which queue to go to
         In the beginning only two regular and the combined cash register are open

         Returns simulation statistics: Mean Delay overall, etc.

        */

        int number_of_cash_registers_regular = 5;
        int number_of_cash_registers_combined = 1;

        double mean_serivce_time_regular = 2.6;
        double sd_service_time_regular = 1.1;
        double mean_serivce_time_service = 4.1;
        double sd_service_time_service = 1.1;

        double service_desk_customers_interarrival_mean = 5.0; // on average 5 minutes between two arrivals
        double regular_customers_rate = 1; // 1 customer per minute
        double regular_customers_interarrival_mean = 1/regular_customers_rate;

        // A queue for service desk customers
        Queue queue_service = new Queue();

        Queue[] regularQueues = new Queue[number_of_cash_registers_regular+number_of_cash_registers_combined];
        for (int i = 0; i < number_of_cash_registers_regular + number_of_cash_registers_combined; i++) {
            regularQueues[i] = new Queue();
        }
        // only two queues are open in the beginning
        for(int i = 2; i < regularQueues.length; i++) {
            regularQueues[i].close(0);
        }
        // combined queue is always open
        regularQueues[regularQueues.length-1].open(0);
        regularQueues[regularQueues.length-1].isService = true;
        regularQueues[regularQueues.length-1].serviceQueue = queue_service;



        // Service desk customers
        Source s_service = new Source(queue_service,l,"Service desk customers",service_desk_customers_interarrival_mean);

        // Regular customers
        Source s_regular = new Source(regularQueues, l, "Regular customers",regular_customers_interarrival_mean);

        // Sinks
        Sink sink = new Sink("Sink");

        // Cash registers
        CashRegister[] cash_registers = new CashRegister[number_of_cash_registers_regular + number_of_cash_registers_combined];
        for (int i = 0; i < number_of_cash_registers_regular; i++) {
            cash_registers[i] = new CashRegister(regularQueues[i],sink, l,"Cash register " + (i+1), mean_serivce_time_regular, sd_service_time_regular);
        }
        for (int i = 0; i < number_of_cash_registers_combined; i++) {
            cash_registers[i + number_of_cash_registers_regular] = new CashRegister(regularQueues[number_of_cash_registers_combined+i], queue_service, sink, l,"Combined cash register " + (i+number_of_cash_registers_regular+1), mean_serivce_time_regular, sd_service_time_regular, mean_serivce_time_service, sd_service_time_service);
        }


        // start the eventlist
        l.start(openTime);


        // Compute statistics

        int totalCustomersArrived = s_regular.numberOfArrivals + s_service.numberOfArrivals;
        int totalCustomersServed = 0;
        double total_avg_delay = 0;
        double total_avg_delay_regular = 0;
        double total_avg_delay_service = 0;

        // how many cash registers have AVG != NaN
        int cr_avg_delay_active = 0;
        int cr_avg_delay_regular_active = 0;
        int cr_avg_delay_service_active = 0;

        for (CashRegister cash_register : cash_registers) {

            double[] perf = cash_register.getPerformance();

            double avg_delay = perf[0];
            double avg_delay_regular = perf[1];
            double avg_delay_service = perf[2];
            int departed_customers = (int) perf[3];
            totalCustomersServed += departed_customers;

            if (!Double.isNaN(avg_delay)) {
                total_avg_delay += avg_delay;
                cr_avg_delay_active++;
            }
            if (!Double.isNaN(avg_delay_regular)) {
                total_avg_delay_regular += avg_delay_regular;
                cr_avg_delay_regular_active++;
            }
            if (!Double.isNaN(avg_delay_service)) {
                total_avg_delay_service += avg_delay_service;
                cr_avg_delay_service_active++;
            }
        }

        total_avg_delay = total_avg_delay/cr_avg_delay_active;
        total_avg_delay_regular = total_avg_delay_regular/cr_avg_delay_regular_active;
        total_avg_delay_service = total_avg_delay_service/cr_avg_delay_service_active;


        double[] queueOpenTime = new double[regularQueues.length];
        double[] queueLength = new double[regularQueues.length];
        double[] queueLengthNonEmpty = new double[regularQueues.length];
        double total_avg_queue_length = 0;
        int n_queues_non_empty = 0;
        double total_avg_queue_NE_length = 0; // non empty queue
        int n_queues_NE_non_empty = 0;

        for (int i = 0; i < regularQueues.length; i++) {
            if(regularQueues[i].isOpen()) {
                regularQueues[i].close(openTime);
            }
            queueLength[i] = regularQueues[i].getAverageQueueLength(openTime, false);
            if(queueLength[i] > 0) n_queues_non_empty++;
            queueLengthNonEmpty[i] = regularQueues[i].getAverageQueueLength(openTime, true);
            if(queueLengthNonEmpty[i] > 0) n_queues_NE_non_empty++;

            total_avg_queue_length += queueLength[i];
            total_avg_queue_NE_length += queueLengthNonEmpty[i];

            queueOpenTime[i] = regularQueues[i].runningTime/openTime; // in percent
        }

        total_avg_queue_length = total_avg_queue_length/n_queues_non_empty;
        total_avg_queue_NE_length = total_avg_queue_NE_length/n_queues_NE_non_empty;



        return new Object[] {totalCustomersArrived, totalCustomersServed, total_avg_delay, total_avg_delay_regular, total_avg_delay_service, total_avg_queue_length, total_avg_queue_NE_length, queueLength, queueLengthNonEmpty, queueOpenTime};
    }

    /**
     * Samples numberOfSamples time , taking the average of a sample with size numberofRunsForAverage
     * Writes the results to 'data.csv'
     * @param numberOfSamples
     * @param numberOfRunsForAverage
     * @throws IOException
     */
    private static void sample(int numberOfSamples, int numberOfRunsForAverage) throws IOException {
        List<List<Double>> rows = new ArrayList<>();

        for(int i = 0; i < numberOfSamples; i++){
            System.out.println(i+"/"+numberOfSamples);
            double[] results = simulateAverage(numberOfRunsForAverage);
            List<Double> row_list = new ArrayList<>();
            for(double result : results){
                row_list.add(result);
            }
            rows.add(row_list);
        }

        FileWriter csvWriter = new FileWriter("data.csv");
        csvWriter.append("AVG Arrivals");
        csvWriter.append(",");
        csvWriter.append("AVG Served");
        csvWriter.append(",");
        csvWriter.append("AVG Delay");
        csvWriter.append(",");
        csvWriter.append("AVG Delay Regular");
        csvWriter.append(",");
        csvWriter.append("AVG Delay Service Desk");
        csvWriter.append(",");
        csvWriter.append("AVG Queue Length");
        csvWriter.append(",");
        csvWriter.append("AVG Queue Length (Non Empty)");
        csvWriter.append(",");
        csvWriter.append("AVG Queue Length (NE) 1");
        csvWriter.append(",");
        csvWriter.append("AVG Queue Length (NE) 2");
        csvWriter.append(",");
        csvWriter.append("AVG Queue Length (NE) 3");
        csvWriter.append(",");
        csvWriter.append("AVG Queue Length (NE) 4");
        csvWriter.append(",");
        csvWriter.append("AVG Queue Length (NE) 5");
        csvWriter.append(",");
        csvWriter.append("AVG Queue Length (NE) 6");
        csvWriter.append(",");
        csvWriter.append("Queue Open Time 1");
        csvWriter.append(",");
        csvWriter.append("Queue Open Time 2");
        csvWriter.append(",");
        csvWriter.append("Queue Open Time 3");
        csvWriter.append(",");
        csvWriter.append("Queue Open Time 4");
        csvWriter.append(",");
        csvWriter.append("Queue Open Time 5");
        csvWriter.append(",");
        csvWriter.append("Queue Open Time 6");
        csvWriter.append("\n");

        for (List<Double> rowData : rows) {
            for(Double data : rowData){
                csvWriter.append(String.valueOf(data));
                csvWriter.append(",");
            }
            csvWriter.append("\n");
        }

        csvWriter.flush();
        csvWriter.close();
    }

}
