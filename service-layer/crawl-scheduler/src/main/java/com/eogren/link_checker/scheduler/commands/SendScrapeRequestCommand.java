package com.eogren.link_checker.scheduler.commands;

public class SendScrapeRequestCommand implements Command {
    private final String url;

    public SendScrapeRequestCommand(String url) {
        if (url == null) {
            throw new IllegalArgumentException("url cannot be null");
        }

        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SendScrapeRequestCommand that = (SendScrapeRequestCommand) o;

        if (!url.equals(that.url)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public String toString() {
        return "SendScrapeRequestCommand{" +
                "url='" + url + '\'' +
                '}';
    }
}
