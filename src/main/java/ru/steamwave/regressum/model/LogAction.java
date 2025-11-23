package ru.steamwave.regressum.model;

public abstract class LogAction {
    public final long time;

    public LogAction(long time) {
        this.time = time;
    }

    public abstract String getType(); // "block", "entity", "item", ...
}

