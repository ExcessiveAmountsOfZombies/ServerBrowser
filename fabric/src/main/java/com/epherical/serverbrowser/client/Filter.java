package com.epherical.serverbrowser.client;

import java.util.Objects;

public class Filter {

    private String tagName;
    private boolean active;

    public Filter(String tagName) {
        this(tagName, false);
    }

    public Filter(String tagName, boolean active) {
        this.tagName = tagName;
        this.active = active;
    }

    public String getTagName() {
        return tagName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Filter filter = (Filter) o;

        return Objects.equals(tagName, filter.tagName);
    }

    @Override
    public int hashCode() {
        return tagName != null ? tagName.hashCode() : 0;
    }
}
