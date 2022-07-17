import org.apache.http.NameValuePair;

import java.util.List;
import java.util.stream.Collectors;

public class Request {

    private final String method;
    private final String path;
    private final List<String> headers;
    private final String body;
    private List<NameValuePair> postParams;

    public Request(String method, String path, List<String> headers, String body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    public void setPostParams(List<NameValuePair> params) {
        this.postParams = params;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public List<NameValuePair> getPostParams() {
        return postParams;
    }

    public String getPostParam(String postParam) {
        return postParams.stream()
                .filter(p -> p.getName().equals(postParam))
                .map(NameValuePair::getValue)
                .collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return "Request{" +
                "\n method='" + method + '\'' +
                "\n path='" + path + '\'' +
                "\n headers=" + headers +
                "\n body='" + body + '\'' +
                "\n queryParams=" + postParams +
                '}';
    }
}
