package org.utm.pad.business_layer;


import org.utm.pad.data_access_layer.CassandraAccess;

import static org.utm.pad.WebServerApp.warehouse;

public class ReconstructListener extends Thread{

    public static  void reconstruct(){
        System.out.println("Reconstructing warehouse...");
        warehouse = new CassandraAccess().getAllData();
    }
}
