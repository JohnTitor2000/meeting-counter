package ru.tinkoff.service.enums;

public enum ServiceCommand {
    HELP("/help"),
    START("/start"),
    CANCEL("/cancel");

    @Override
    public String toString() {
        return "ServiceCommand{" +
                "cmd='" + cmd + '\'' +
                '}';
    }

    public boolean equals(String cmd) {
        return this.toString().equals(cmd);
    }

    private final String cmd;

    ServiceCommand(String cmd) {
        this.cmd = cmd;
    }

}
