package ru.steamwave.regressum.storage;

import ru.steamwave.regressum.model.LogAction;

import java.util.concurrent.LinkedBlockingQueue;

public class ActionQueue {
    // Ограничение, чтобы сервер не падал при пике
    public static final LinkedBlockingQueue<LogAction> queue = new LinkedBlockingQueue<>(50_000);
}
