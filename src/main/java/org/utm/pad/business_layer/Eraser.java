package org.utm.pad.business_layer;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.utm.pad.data_access_layer.CassandraAccess;
import org.utm.pad.model_layer.Article;

import java.util.List;
import java.util.stream.Collectors;

import static org.utm.pad.WebServerApp.*;

public class Eraser {

    /**
     * If the article with specified id is found in the warehouse, it's deleted.
     * @param id of the article to erase
     * @return true if deleted successful, otherwise return false
     */
    public boolean delete(int id){

        List<Article> article = warehouse.stream()
                .filter(e->e.getId() == id)
                .collect(Collectors.toList());

        if(!article.isEmpty()){
            new CassandraAccess().deleteEntry(id);
            System.out.println("Reconstructing warehouse after delete operation...");
            warehouse = new CassandraAccess().getAllData();

            if(isAvailable(NEIGHBOUR_PORT, 1000)){
                Client client = Client.create();
                WebResource webResource = client
                        .resource(NEIGHBOUR_URI);

                webResource.type("application/xml")
                        .head();
            }
            return true;
        } else{
            return false;
        }
    }
}
