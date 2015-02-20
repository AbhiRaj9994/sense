package com.mechanitis.demo.sense.client;

import com.mechanitis.demo.sense.infrastructure.BroadcastingServerEndpoint;
import com.mechanitis.demo.sense.infrastructure.DaemonThreadFactory;
import com.mechanitis.demo.sense.infrastructure.WebSocketServer;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class StubService implements Runnable {
    private final BroadcastingServerEndpoint<String> fakeUserDataEndpoint = new BroadcastingServerEndpoint<>();
    private final WebSocketServer server;
    private final String path;
    private final int port;
    private final MessageGenerator messageGenerator;

    public StubService(String path, int port, MessageGenerator messageGenerator) {
        this.path = path;
        this.port = port;
        this.server = new WebSocketServer(path, port, fakeUserDataEndpoint);
        this.messageGenerator = messageGenerator;
    }

    @Override
    public void run() {
        newSingleThreadExecutor(new DaemonThreadFactory()).submit(server);
        Random random = new Random();

        ScheduledFuture<?> scheduledFuture = newScheduledThreadPool(1).scheduleAtFixedRate(
                () -> fakeUserDataEndpoint.onMessage(messageGenerator.generateMessage()),
                0, 500, MILLISECONDS);
        try {
            scheduledFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    public interface MessageGenerator {
        String generateMessage();
    }

}