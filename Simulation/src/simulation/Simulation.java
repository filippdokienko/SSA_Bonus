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

	
    static double openTime = 6*8*60; // in minutes, 6*8 hours

        /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        // ignore
        if(false){
            int N_AVGS = 1000;
            int N = 100; // number of simulations per simulation

            List<List<Double>> rows = new ArrayList<>();

            for(int i = 0; i < N_AVGS; i++){
                System.out.println(i+"/"+N_AVGS);
                double[] results = simulateAverage(N);
                List<Double> row_list = new ArrayList<>();
                row_list.add(results[0]);
                row_list.add(results[1]);
                row_list.add(results[2]);
                row_list.add(results[3]);
                row_list.add(results[4]);
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


            System.exit(187);
        }


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
            regularQueues[i].close(0);
        }
        // combined queue is always open
        regularQueues[regularQueues.length-1].open(0);
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
            cash_registers[i + number_of_cash_registers_regular] = new CashRegister(regularQueues[number_of_cash_registers_combined+i], queue_service, sink, l,"Combined cash register " + (i+number_of_cash_registers_regular+1), mean_serivce_time_service, sd_service_time_service);
        }


        // start the eventlist
        l.start(openTime);

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

        System.out.println("\n\n");
        System.out.println("Total served customers: " + totalCustomersServed + " (" + (totalCustomersArrived - totalCustomersServed) + " rejected)");
        System.out.println("Total AVG delay: " + total_avg_delay);
        System.out.println("Total AVG regular delay: " + total_avg_delay_regular);
        System.out.println("Total AVG service desk delay: " + total_avg_delay_service);

        int[] customerStillInQueue = new int[regularQueues.length];
        double[] queueOpenTime = new double[regularQueues.length];
        for (int i = 0; i < regularQueues.length; i++) {
            customerStillInQueue[i] = regularQueues[i].getSize();
            if(regularQueues[i].isOpen()) {
                regularQueues[i].close(openTime);
            }
            queueOpenTime[i] = regularQueues[i].runningTime;
        }

        System.out.println("Queue sizes after store closure: " + Arrays.toString(customerStillInQueue));
        System.out.println("Total queue open times: " + Arrays.toString(queueOpenTime));

    }

    private static double[] simulateAverage(int N){
        double total_AVG_arrivals = 0;
        double total_AVG_served = 0;
        double total_AVG_delay = 0;
        double total_AVG_delay_regular = 0;
        double total_AVG_delay_service = 0;

        for(int i = 0; i < N; i++){
            double[] results = simulate();
            total_AVG_arrivals += results[0];
            total_AVG_served += results[1];
            total_AVG_delay += results[2];
            total_AVG_delay_regular += results[3];
            total_AVG_delay_service += results[4];
        }

        double[] results = new double[5];
        results[0] = total_AVG_arrivals/N;
        results[1] = total_AVG_served/N;
        results[2] = total_AVG_delay/N;
        results[3] = total_AVG_delay_regular/N;
        results[4] = total_AVG_delay_service/N;

        return results;
    }

    private static double[] simulate(){
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
            regularQueues[i].close(0);
        }
        // combined queue is always open
        regularQueues[regularQueues.length-1].open(0);
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
            cash_registers[i + number_of_cash_registers_regular] = new CashRegister(regularQueues[number_of_cash_registers_combined+i], queue_service, sink, l,"Combined cash register " + (i+number_of_cash_registers_regular+1), mean_serivce_time_service, sd_service_time_service);
        }


        // start the eventlist
        l.start(openTime);

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
        return new double[] {totalCustomersArrived, totalCustomersServed, total_avg_delay, total_avg_delay_regular, total_avg_delay_service};
    }

}
