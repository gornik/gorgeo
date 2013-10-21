package org.elasticsearch.plugin.geohashcellfacet;

import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public class MissingGeoValueEntry implements GeoHashCellFacetEntry {

    private final static String MISSING_ENTRY = "_missing";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return (o != null && getClass() != o.getClass());
    }

    @Override
    public int hashCode() {
        return MISSING_ENTRY.hashCode();
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, AtomicLong value) throws IOException {
        builder.field(MISSING_ENTRY, value);
        return builder;
    }
}
