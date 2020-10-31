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
        System.out.println(String.format("Ngat ket noi [%s]", session.getId()));
        isDisconnect = true;
        System.out.print("Dang ket noi");
    }

    @OnOpen
    public void onOpen(Session session) {
        isDisconnect = false;
        System.out.println(String.format("\nMo ket noi [%s]", session.getId()));
    }

    @OnMessage
    public void onMessage(String message) {
        LOGGER.info(String.format("Thong tin tu can dien tu [%s]", message));
        linkedQueue.add(message);
    }

    public static void main(String[] args) {
        new Thread(new ElectronicThread()).start();

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        Session session = null;
        System.out.print("Dang ket noi");

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
