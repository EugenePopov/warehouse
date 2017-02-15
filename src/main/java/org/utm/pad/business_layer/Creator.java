package org.utm.pad.business_layer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.utm.pad.data_access_layer.CassandraAccess;
import org.utm.pad.model_layer.Article;
import java.util.List;
import java.util.stream.Collectors;

import static org.utm.pad.WebServerApp.*;

public class Creator {

    /**
     * If the article with specified id is not found in the warehouse, it's created
     * from received JSON after it's validation.
     * @param body string contained in request's body. It must be a JSON element.
     * @return true if created successful, otherwise return false
     * @throws Exception caused by troubles at JSON validation
     */
    public boolean create(String body) throws Exception {

        JsonValidator validator = new JsonValidator();

        if(validator.isValid(body)){
            Article article = jsonToArticle(body);

            List<Article> foundArticles = warehouse.stream()
                    .filter(e->e.getId() == article.getId())
                    .collect(Collectors.toList());

            if(foundArticles.isEmpty()){
                new CassandraAccess().createEntry(body);
                System.out.println("Reconstructing warehouse after create operation...");
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
                System.out.println("Article already exists!");
                return false;
            }
        } else{
            System.out.println("Not a valid JSON!");
            return false;
        }
    }

    private Article jsonToArticle(String json){
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, Article.class);
    }
}
