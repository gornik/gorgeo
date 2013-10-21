package org.elasticsearch.plugin.geohashcellfacet;

import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public interface GeoHashCellFacetEntry {
    XContentBuilder toXContent(XContentBuilder builder, AtomicLong value) throws IOException;
}
