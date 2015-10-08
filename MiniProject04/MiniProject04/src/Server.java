import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebsocketVersion;
import io.vertx.core.net.NetServer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by jiyoungpark on 15. 10. 6..
 */
public class Server extends AbstractVerticle {

    static private int count;
    private NetServer server;

    ConcurrentMap<Integer, ServerWebSocket> sockets;
    List<ServerWebSocket> websockets;
    Vertx vertx;

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Server server = new Server();
        server.init();
    }

    public Server() {
        websockets = new ArrayList<ServerWebSocket>();
        sockets = new ConcurrentHashMap<>();
        vertx = Vertx.vertx();
        count = 0;
    }

    public void init() {
        vertx.deployVerticle(this);
    }

    public void start() {
        vertx.createHttpServer().websocketHandler(new Handler<ServerWebSocket>() {
            public void handle(final ServerWebSocket ws) {
                if (ws.path().equals("/myapp")) {
                    ws.handler(new Handler<Buffer>() {
                        public void handle(Buffer data) {
                            if (!sockets.containsValue(ws)) {
                                sockets.put(count, ws);
                                count++;
                            }

                            System.out.println("sessions : " + data);
                            for (int i = 0; i < sockets.size(); i++) {
                                try {
                                    sockets.get(i).writeFinalTextFrame(data.toString());
                                } catch (IllegalStateException e) {

                                }
                            }
                        }
                    });
                } else {
                    ws.reject();
                }
            }
        }).requestHandler(new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest req) {
                if (req.path().equals("/"))
                    req.response().sendFile("ws.html"); // Serve the html
            }
        }).listen(8080);
    }
}