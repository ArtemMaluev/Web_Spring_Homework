public class Main {

    public static final int PORT = 9999;

    public static void main(String[] args) {

        Server server = new Server();

        server.acceptClient(PORT);
    }
}


