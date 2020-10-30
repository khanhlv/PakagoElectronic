package com.pakago.electronic;

import com.pakago.electronic.thread.ElectronicThread;
import com.pakago.electronic.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.net.URI;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@ClientEndpoint
public class Application {

    private static boolean isDisconnect = false;
    private static int count = 0;
    private final static Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static Queue<String> linkedQueue = new ConcurrentLinkedQueue<>();

    @OnClose
    public void onClose(Session session) {
        System.out.println(String.format("Ng\u1EAFt k\u1EBFt n\u1ED1i [%s]", session.getId()));
        isDisconnect = true;
        System.out.print("\u0110ang k\u1EBFt n\u1ED1i");
    }

    @OnOpen
    public void onOpen(Session session) {
        isDisconnect = false;
        System.out.println(String.format("\nM\u1EDF k\u1EBFt n\u1ED1i [%s]", session.getId()));
    }

    @OnMessage
    public void onMessage(String message) {
        LOGGER.info(String.format("Th\u00F4ng tin t\u1EEB c\u00E2n \u0111i\u1EC7n t\u1EED [%s]", message));
        linkedQueue.add(message);
    }

    public static void main(String[] args) {
        new Thread(new ElectronicThread()).start();

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        Session session = null;
        System.out.print("\u0110ang k\u1EBFt n\u1ED1i");

        while (true) {
            showConnect(isDisconnect, session);

            try {
                if (isDisconnect || session == null) {
                    session = container.connectToServer(Application.class, URI.create(ResourceUtils.getValue("websocket")));
                }
            } catch (Exception ex) {
                closeSession(session);
                session = null;
                isDisconnect = true;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }

        }
    }

    private static void closeSession(Session session) {
        if (session != null) {
            try {
                session.close();
            } catch (Exception ex) {
            }
        }
    }

    private static void showConnect(boolean isDisconnect, Session session) {
        if (isDisconnect || session == null) {
            count++;

            if (count == 80) {
                System.out.print("\n");
                count = 0;
            }

            System.out.print(".");
        }
    }
}
