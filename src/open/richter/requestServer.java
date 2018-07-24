package open.richter;

import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

public class requestServer {

    static String response = "This is the response at ";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        HttpContext colorContext = server.createContext("/color");
        colorContext.setHandler(requestServer::handleRequest);
        server.start();
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        URI requestURI = exchange.getRequestURI();
        response +=  requestURI + "\n";
        printRequestInfo(exchange);
        changeColor(exchange);
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private static void printRequestInfo(HttpExchange exchange) {
        System.out.println("-- headers --");
        Headers requestHeaders = exchange.getRequestHeaders();
        requestHeaders.entrySet().forEach(System.out::println);

        System.out.println("-- principle --");
        HttpPrincipal principal = exchange.getPrincipal();
        System.out.println(principal);

        System.out.println("-- HTTP method --");
        String requestMethod = exchange.getRequestMethod();
        System.out.println(requestMethod);

        System.out.println("-- query --");
        URI requestURI = exchange.getRequestURI();
        String query = requestURI.getQuery();
        System.out.println(requestURI);
        System.out.println(query);
    }

    private static String[] extractColorsFromExchange(HttpExchange exchange){
        System.out.println("-- extractColorsFromExchange --");
        String query = exchange.getRequestURI().getQuery();
        String[] colors = query.split("&");
        for (String s : colors){
            System.out.println(s);
        }
        return colors;
    }

    private static void changeColor(HttpExchange exchange){
        System.out.println("-- changeColor --");
        String[] colors = extractColorsFromExchange(exchange);
        response += "I recieved the arguments: \n";
        for (String s : colors){
            response += s + "   ";
        }
    }
}
