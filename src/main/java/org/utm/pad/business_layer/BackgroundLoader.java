package org.utm.pad.business_layer;

import org.utm.pad.data_access_layer.CassandraAccess;
import org.utm.pad.data_access_layer.DataAccess;

import static org.utm.pad.WebServerApp.warehouse;

public class BackgroundLoader extends Thread {

    /**
     * This thread is running in the background
     * and once in a minute retrieves all data from database
     * and inserts it in warehouse list.
     * warehouse is a thread safe list that contains all articles.
     */
    @Override
    public void run() {
        CassandraAccess databaseReader = new CassandraAccess();

        long timeout = 60000; // timeout in milliseconds (60 seconds)

        //noinspection InfiniteLoopStatement
        while(true){

            System.out.println("Retrieving data...");
            warehouse = databaseReader.getAllData();

            try {
                System.out.println("Waiting timeout...");
                sleep(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
