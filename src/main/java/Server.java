import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Server {

    public final int NUMBER_THREADS = 64;

    private final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    private final ExecutorService threadPool;
    private Socket socket;
    private final ConcurrentHashMap<String, Map<String, Handler>> handlerMap;
    private final ParseRequest parseRequest;

    public Server() {
        parseRequest = new ParseRequest();
        threadPool = Executors.newFixedThreadPool(NUMBER_THREADS);
        handlerMap = new ConcurrentHashMap<>();
    }

    public void acceptClient(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                socket = serverSocket.accept();
                System.out.println("\n" + socket);
                threadPool.execute(this::workServer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    public void workServer() {

        try (final var in = new BufferedInputStream(socket.getInputStream());
             final var out = new BufferedOutputStream(socket.getOutputStream())) {

//            while (true) {
//                Request request = parseRequest.createRequest(in, out);
//                Handler handler = handlerMap.get(request.getMethod()).get(request.getPath());
//                System.out.println("handler: " + handler);
//
//                final var path = request.getPath();
//                if (!validPaths.contains(path)) {
//                    parseRequest.error404(out);
//                    return;
//                }
//
//                createResponse(request, out);
//                System.out.println();
//            }
            Request request = parseRequest.createRequest(in, out);
            Handler handler = handlerMap.get(request.getMethod()).get(request.getPath());
            System.out.println("handler: " + handler);

            final var path = request.getPath();
            if (!validPaths.contains(path)) {
                parseRequest.error404(out);
                return;
            }

            handler.handle(request, out);
            createResponse(request, out);
            System.out.println();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createResponse(Request request, BufferedOutputStream out) throws IOException {
        final var filePath = Path.of(".", "public", request.getPath());
        final var mimeType = Files.probeContentType(filePath);

        if (request.getPath().equals("/classic.html")) {
            final var template = Files.readString(filePath);
            final var content = template.replace("{time}", LocalDateTime.now().toString()).getBytes();
            out.write(("HTTP/1.1 200 OK\r\n" + "Content-Type: " + mimeType + "\r\n" + "Content-Length: "
                    + content.length + "\r\n" + "Connection: close\r\n" + "\r\n").getBytes());
            out.write(content);
            out.flush();
            return;
        }

        final var length = Files.size(filePath);
        out.write(("HTTP/1.1 200 OK\r\n" + "Content-Type: " + mimeType + "\r\n" + "Content-Length: " + length
                + "\r\n" + "Connection: close\r\n" + "\r\n").getBytes());
        Files.copy(filePath, out);
        out.flush();
    }

    public void addHandler(String method, String path, Handler handler) {
        if (handlerMap.containsKey(method)) {
            handlerMap.get(method).put(path, handler);
        } else {
            handlerMap.put(method, new ConcurrentHashMap<>(Map.of(path, handler)));
        }
        System.out.println(handlerMap);
    }
}
