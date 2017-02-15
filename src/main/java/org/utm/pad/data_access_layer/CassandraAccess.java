package org.utm.pad.data_access_layer;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.utm.pad.model_layer.Article;
import java.util.concurrent.CopyOnWriteArrayList;

public class CassandraAccess implements DataAccess{

    private Cluster cluster;
    private Session session;


    /**
     * Method retrieves all data from the database
     * and is used by BackgroundLoader class in order to periodically reconstruct
     * all the data from warehouse.
     * @return a thread safe list of all articles
     */
    @Override
    public CopyOnWriteArrayList<Article> getAllData(){

        CopyOnWriteArrayList<Article> articles = new CopyOnWriteArrayList<>();

        connectToDatabase();

        String cqlStatement = "SELECT * FROM articles";
        for (Row row : session.execute(cqlStatement)) {
            articles.add(rowToObject(row));
        }

        disconnectFromDatabase();

        return articles;
    }

    /**
     * Method takes an entry from database
     * and maps its columns to attributes of a new Article object.
     * @param row an entry from the result of the query
     * @return instance of Article class
     */
    private Article rowToObject(Row row){
        Article article = new Article();

        article.setAuthor(row.getString("author"));
        article.setContent(row.getString("content"));
        article.setHead(row.getString("head"));
        article.setDate(row.getString("date"));
        article.setId(row.getInt("id"));
        article.setLink(row.getString("link"));

        return article;
    }

    /**
     * Method updates an entry from database.
     * We can update only 'content' column.
     * HTTP METHOD - PUT
     * @param id of the article
     * @param content new data for content column
     */
    @Override
    public void updateEntry(int id, String content) {

        connectToDatabase();

        String cqlStatement = "UPDATE magazine.articles " +
                "SET content = '" + content + "'" +
                " WHERE id = " + id;

        session.execute(cqlStatement);

        disconnectFromDatabase();

    }

    /**
     * Method creates a new row in database.
     * HTTP METHOD - POST
     * @param article string describing an article in JSON format
     */
    @Override
    public void createEntry(String article) {

        connectToDatabase();

        String cqlStatement = "INSERT INTO magazine.articles JSON '" + article + "'";

        session.execute(cqlStatement);

        disconnectFromDatabase();

    }

    /**
     * Method deletes an entry from database specified by id.
     * HTTP METHOD - DELETE
     * @param id of the article
     */
    @Override
    public void deleteEntry(int id) {

        connectToDatabase();

        String cqlStatement = "DELETE FROM magazine.articles WHERE id = " + id;

        session.execute(cqlStatement);

        disconnectFromDatabase();

    }

    private void connectToDatabase(){
        String serverIP = "127.0.0.1";
        String keyspace = "magazine";

        cluster = Cluster.builder()
                .addContactPoint(serverIP)
                .build();

        session = cluster.connect(keyspace);
    }

    private void disconnectFromDatabase(){
        session.close();
        cluster.close();
    }


}
