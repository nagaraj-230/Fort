package com.payoda.smartlock.plugins.pushnotification;

public class RemoteDataEvent {
    public final String status;
    public final String title;
    public final String body;
    public final String command;

    public RemoteDataEvent(String status, String title, String body, String command) {
        this.status = status;
        this.title = title;
        this.body = body;
        this.command = command;
    }

    public String getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return "RemoteDataEvent{" +
                "status='" + status + '\'' +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", command='" + command + '\'' +
                '}';
    }
}
