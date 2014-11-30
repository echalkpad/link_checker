package com.eogren.link_checker.scheduler.commands;

public class ProduceScrapeRequestsCommand implements Command {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "ProduceScrapeRequestsCommand";
    }
}
