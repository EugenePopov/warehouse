package org.utm.pad.business_layer;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.utm.pad.data_access_layer.CassandraAccess;
import org.utm.pad.model_layer.Article;
import java.util.List;
import java.util.stream.Collectors;

import static org.utm.pad.WebServerApp.*;

public class Updater {

    /**
     * If the article with specified id is found in the warehouse, its content is updated.
     * @param id of the row in the database
     * @param content of the request which is new value for "content" column
     * @return true if updated successful, otherwise return false
     */
    public boolean update(int id, String content){

        List<Article> article = warehouse.stream()
                 .filter(e->e.getId() == id)
                 .collect(Collectors.toList());

        if(!article.isEmpty()){
            new CassandraAccess().updateEntry(id, content);
            System.out.println("Reconstructing warehouse after update operation...");
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
