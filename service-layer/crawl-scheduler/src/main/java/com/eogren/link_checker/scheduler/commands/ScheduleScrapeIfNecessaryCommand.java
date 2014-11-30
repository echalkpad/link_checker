package com.eogren.link_checker.scheduler.commands;

public class ScheduleScrapeIfNecessaryCommand implements Command {
    protected final String url;

    public String getUrl() {
        return url;
    }

    public ScheduleScrapeIfNecessaryCommand(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScheduleScrapeIfNecessaryCommand that = (ScheduleScrapeIfNecessaryCommand) o;

        if (!url.equals(that.url)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public String toString() {
        return "ScheduleScrapeIfNecessaryCommand{" +
                "url='" + url + '\'' +
                '}';
    }
}
