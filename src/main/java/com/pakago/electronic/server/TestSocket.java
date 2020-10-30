package com.pakago.electronic.server;

import org.glassfish.tyrus.server.Server;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

@ServerEndpoint(value = "/electronic")
public class TestSocket {
    private static Queue<Session> queue = new ConcurrentLinkedQueue<>();

    private static Thread rateThread;

    static {
        rateThread = new Thread(){
            public void run() {

                String message = "{\"barcode\":\"OR24032019001\",\"weight\":\"1.09\"}";

                while(true)
                {
                    if(queue!=null) {
                        sendAll(message);
                    }

                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        };
        rateThread.start();
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println(String.format("%s joined the chat room.", session.getId()));
        queue.add(session);
    }

    @OnMessage
    public void onMessage(String msg, Session session) {
        try {
            System.out.println("Received msg "+msg+" from "+session.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void closedConnection(Session session) {
        queue.remove(session);
        System.out.println("Session closed: "+session.getId());
    }

    @OnError
    public void error(Session session, Throwable t) {
        queue.remove(session);
        System.err.println("Error on session "+session.getId());
    }

    private static void sendAll(String msg) {
        try {
            /* Send the new rate to all open WebSocket sessions */
            ArrayList<Session > closedSessions= new ArrayList<>();
            for (Session session : queue) {
                if(!session.isOpen()) {
                    System.err.println("Closed session: "+session.getId());
                    closedSessions.add(session);
                } else {
                    session.getBasicRemote().sendText(msg);
                }
            }
            queue.removeAll(closedSessions);
            System.out.println("Sending "+msg+" to "+queue.size()+" clients");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server("localhost", 8088, "/ws", new HashMap<>(), TestSocket.class);

        try {
            server.start();
            System.out.println("Press any key to stop the server..");
            new Scanner(System.in).nextLine();
        } catch (DeploymentException e) {
            throw new RuntimeException(e);
        } finally {
            server.stop();
        }
    }
}
