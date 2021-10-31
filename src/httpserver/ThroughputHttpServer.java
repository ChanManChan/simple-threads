package httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ThroughputHttpServer {
    private static final String INPUT_FILE = "./resources/war_and_peace.txt";
    private static final int NUMBER_OF_THREADS = 8;
    // Single threaded throughput: 598.114 requests/second
    // 8 Threaded throughput: 968.343 requests/second

    public static void main(String[] args) throws IOException {
        String text = new String(Files.readAllBytes(Paths.get(INPUT_FILE)));
        startServer(text);
    }

    private static void startServer(String text) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0); // second parameter is the backlog size which defines the size of the queue for the http server request. We are going to keep it as 0 since all the request should end up in the thread pools queue instead.
        server.createContext("/search", new WordCountHandler(text));
        // Handling of the requests is delegated to a fixed-size pool of threads.
        Executor executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS); // this will schedule each incoming http request to a pool of worker threads
        server.setExecutor(executor);
        server.start();
    }

    private static class WordCountHandler implements HttpHandler {
        private final String text;

        public WordCountHandler(String text) {
            this.text = text;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String[] keyValue = query.split("=");
            String action = keyValue[0];
            String word = keyValue[1];
            if (!action.equals("word")) {
                exchange.sendResponseHeaders(400, 0);
                return;
            }

            long count = countWord(word);
            byte[] response = Long.toString(count).getBytes();
            exchange.sendResponseHeaders(200, response.length);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(response);
            outputStream.close();
        }

        private long countWord(String word) {
            long count = 0;
            int index = 0;

            while (index >= 0) {
                index = text.indexOf(word, index);
                if (index >= 0) {
                    count++;
                    index++;
                }
            }
            return count;
        }
    }
}
