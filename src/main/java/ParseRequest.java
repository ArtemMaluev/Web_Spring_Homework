import org.apache.http.client.utils.URLEncodedUtils;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class ParseRequest {

    public static final String GET = "GET";
    public static final String POST = "POST";

    final List<String> allowedMethods;

    public ParseRequest() {
        allowedMethods = List.of(GET, POST);
    }

    public Request createRequest(BufferedInputStream in, BufferedOutputStream out) throws IOException {

        Request request = null;
        final var limit = 4096;

        in.mark(limit);
        final byte[] buffer = new byte[limit];
        final int read = in.read(buffer);

        // ищем request line
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            error404(out);
            return null;
        }

        // читаем request line
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            error404(out);
            return null;
        }

        final var method = requestLine[0];
        if (!allowedMethods.contains(method)) {
            error404(out);
            return null;
        }
        System.out.println(method);

        final var path = requestLine[1];
        if (!path.startsWith("/")) {
            error404(out);
            return null;
        }
        System.out.println(path);

        // ищем заголовки
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            error404(out);
            return null;
        }

        // отматываем на начало буфера
        in.reset();
        // пропускаем requestLine
        in.skip(headersStart);

        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
        System.out.println(headers);

        // для GET тела нет
        String body = null;
        String type = "";
        if (!method.equals(GET)) {
            in.skip(headersDelimiter.length);
            // вычитываем Content-Length, чтобы прочитать body
            final var contentLength = extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);

                body = new String(bodyBytes);
                System.out.println(body);
            }

            final Optional<String> contentType = extractHeader(headers, "Content-Type");
            if (contentType.isPresent()) {
                type = contentType.get();
            }
        }

        request = new Request(method, path, headers, body);
        System.out.println(request);

        if (type.equals("application/x-www-form-urlencoded")) {
            request.setPostParams(URLEncodedUtils.parse(request.getBody(), StandardCharsets.UTF_8));
            System.out.println("\npostParams: " + request.getPostParams());
        }

        out.flush();
        return request;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    public void error404(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }
}
