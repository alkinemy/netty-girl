package al.netty.book.socket.blocking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class BlockingServer {
    public static void main(String[] args) throws Exception {
        BlockingServer server= new BlockingServer();
        server.run();
    }

    private void run() throws IOException {
        ServerSocket server = new ServerSocket(8888);
        System.out.println("접속 대기중");

        while (true) {
            Socket socket = server.accept();
            System.out.println("클라이언트 연결됨");

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            while (true) {
                try {
                    int request = in.read();
                    out.write(request);
                } catch (IOException e) {
                    break;
                }
            }
        }
    }
}
