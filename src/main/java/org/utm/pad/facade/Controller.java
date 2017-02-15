package org.utm.pad.facade;

import org.apache.commons.lang3.StringUtils;
import org.utm.pad.business_layer.Creator;
import org.utm.pad.business_layer.Eraser;
import org.utm.pad.business_layer.Updater;
import org.utm.pad.model_layer.Article;
import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;
import static org.utm.pad.WebServerApp.NEIGHBOUR_URI;
import static org.utm.pad.WebServerApp.warehouse;
import static org.utm.pad.business_layer.ReconstructListener.reconstruct;

@Path("/articles")
public class Controller {

    /**
     * This method id used to create a new article in the warehouse.
     * The article to create must be a JSON element.
     * @param body string contained in request's body. It must be in JSON format.
     * @return HTTP response with status code 201 in case of successful creation,
     * or status code 500 in case of some troubles (article already exists, not a valid JSON, other
     * unknown exceptions)
     * @throws Exception caused by troubles at JSON validation
     */
    @POST
    public Response create(String body) throws Exception {

        return new Creator().create(body) ? Response.status(201).build():
                Response.status(400).build();
    }

    /**
     * Method gets all articles from warehouse and puts them into response body in XML format.
     * If warehouse is empty, body is loaded with error message.
     * @return HTTP response with status code 200
     */
    @GET
    @Produces("application/xml")
    public Response getAllArticlesXml() throws InterruptedException {

        sleep(5000);

        List<Article> articles = warehouse;
        String body = articles.isEmpty() ? "Warehouse is empty :(" : constructXml(articles);

        CacheControl control = new CacheControl();
        control.setMaxAge(60);

        return Response.status(200).cacheControl(control).entity(body).build();
    }

    /**
     * Method gets all articles from warehouse and puts them into response body in JSON format.
     * If warehouse is empty, body is loaded with error message.
     * @return HTTP response with status code 200
     */
    @GET
    @Produces("application/json")
    public Response getAllArticlesJson() throws InterruptedException {

        sleep(5000);

        List<Article> articles = warehouse;
        String body = articles.isEmpty() ? "Warehouse is empty :(" : constructJson(articles);

        CacheControl control = new CacheControl();
        control.setMaxAge(60);

        return Response.status(200).cacheControl(control).entity(body).build();
    }

    /**
     * This method is used to retrieve an article in XML format specified by the id in the URI.
     * @param id article to retrieve
     * @return article in XML format
     */
    @GET
    @Path("/{id}")
    @Produces("application/xml")
    public Response getArticleXml(@PathParam("id") String id) throws InterruptedException {

        sleep(5000);

        String errorMessage = "Sorry, no such article...";
        List<Article> article = new ArrayList<>();

        if(StringUtils.isNumeric(id)) {
            article = warehouse.stream()
                    .filter(e -> e.getId() == Integer.valueOf(id))
                    .collect(Collectors.toList());
        }

        String body;

        if(article.isEmpty()){
            body = errorMessage;
            return Response.status(200).entity(
                    body
            ).build();
        } else {
            body = article.get(0).toXml();

            CacheControl control = new CacheControl();
            control.setMaxAge(60);
            return Response.status(200).cacheControl(control).entity(
                    body
            ).build();
        }
    }

    /**
     * This method is used to retrieve an article in JSON format specified by the id in the URI.
     * @param id article to retrieve
     * @return article in JSON format
     */
    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response getArticleJson(@PathParam("id") String id) throws InterruptedException {

        sleep(5000);

        String errorMessage = "Sorry, no such article...";
        List<Article> article = new ArrayList<>();

        if(StringUtils.isNumeric(id)) {
            article = warehouse.stream()
                    .filter(e -> e.getId() == Integer.valueOf(id))
                    .collect(Collectors.toList());
        }

        String body;

        if(article.isEmpty()){
            body = errorMessage;
            return Response.status(200).entity(
                    body
            ).build();
        } else {
            body = article.get(0).toJson();

            CacheControl control = new CacheControl();
            control.setMaxAge(60);
            return Response.status(200).cacheControl(control).entity(
                    body
            ).build();
        }
    }

    /**
     * Method gets the id of the article from URI, the content from the body of the request
     * and passes them to business layer in order to update the database.
     * If id is not a number, client will be provided with a Bad Request Error.
     * If there isn't an article with specified id in the warehouse, client will receive an error
     * message.
     * @param id of the article to update
     * @param body of the request which is new value for "content" column
     * @return HTTP response with status code 200, 204 or 400 with an error message in the body
     */
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") String id, String body){

        if(StringUtils.isNumeric(id)){

            return new Updater().update(Integer.valueOf(id), body) ?
                                Response.status(200).build() :
                                Response.status(400).entity("Sorry, no such article...").build();
        } else {
            return Response.status(400).entity("Wrong id!").build();
        }

    }

    /**
     * Method gets the id of the article from URI and passes it to business layer in order to
     * delete the article from warehouse.
     * If id is not a number client will be prided with  a Bad Request Error.
     * If there isn't an article with specified id, client will receive an error message.
     * @param id of the article to delet
     * @return HTTP response with status code 200, 204 or 400 with an error message in the body
     */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id){

        if(StringUtils.isNumeric(id)){
            return new Eraser().delete(Integer.valueOf(id)) ?
                    Response.status(200).build() :
                    Response.status(400).entity("Sorry, no such article...").build();
        } else {
            return Response.status(400).entity("Wrong id!").build();
        }

    }

    /**
     * Method finds all articles which contain the keyword indicated in the query.
     * @param keyword string to search
     * @return all the articles which contain the keyword
     */
    @GET
    @Path("/search")
    @Produces("application/json")
    public Response search(@QueryParam("keyword") String keyword){

        List<Article> articles = warehouse.stream()
                .filter(e->e.getContent().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
        String body = articles.isEmpty() ? "Keyword not found :(" : constructJson(articles);

        return Response.status(200).entity(body).build();


    }

    @HEAD
    public void signal(){
        reconstruct();
    }

    private String constructXml(List<Article> articles){
        String body = articles
                .stream()
                .map(Article::toXml)
                .collect(Collectors.joining());

        return  "<articles>\n\n" + body +
                    "</articles>\n";
    }

    private String constructJson(List<Article> articles){
        String body = articles
                .stream()
                .map(Article::toJson)
                .collect(Collectors.joining());

        return  "\"articles:\"[\n" +
                "\t" + body +
                "]";
    }
}
