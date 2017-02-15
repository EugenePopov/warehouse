package org.utm.pad;

import com.sun.net.httpserver.HttpServer;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import org.utm.pad.business_layer.BackgroundLoader;
import org.utm.pad.model_layer.Article;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.System.*;

public class WebServerApp
{
    private static String BASE_URI ;
    public static String NEIGHBOUR_URI;
    public static int NEIGHBOUR_PORT;

    public static CopyOnWriteArrayList<Article> warehouse = new CopyOnWriteArrayList<>();

    public static void main( String[] args ) throws Exception {
        try {
            init(Integer.valueOf(args[0]));
            HttpServer server = HttpServerFactory.create(BASE_URI);
            BackgroundLoader loader = new BackgroundLoader();


            loader.start();
            server.start();

            out.println("Press Enter to stop the server. ");
            //noinspection ResultOfMethodCallIgnored
            in.read();
            server.stop(0);
            exit(0);
        } catch (IllegalArgumentException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void init(int port){
        BASE_URI = "http://localhost:" + port + "/api";

        if(port == 11219){
            NEIGHBOUR_URI = "http://localhost:11220/api/articles/";
            NEIGHBOUR_PORT = 11220;
        } else{
            NEIGHBOUR_URI = "http://localhost:11219/api/articles/";
            NEIGHBOUR_PORT = 11219;
        }
    }

    public static boolean isAvailable(int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port), timeout);
            return true;
        } catch (IOException e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }
}
