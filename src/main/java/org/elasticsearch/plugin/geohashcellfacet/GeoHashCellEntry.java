package org.elasticsearch.plugin.geohashcellfacet;

import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public class GeoHashCellEntry implements GeoHashCellFacetEntry {

    private final GeoHashCell cell;

    public GeoHashCellEntry(GeoHashCell cell) {
        this.cell = cell;
    }

    @Override
    public int hashCode() {
        return cell.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeoHashCellEntry that = (GeoHashCellEntry) o;

        return cell.equals(that.cell);
    }

    public static GeoHashCellEntry createEntry(GeoHashCell cell, String additionalGroupingValue) {
        return additionalGroupingValue == null || additionalGroupingValue.isEmpty()
                ? new GeoHashCellEntry(cell)
                : new GeoHashCellWithGroupingEntry(cell, additionalGroupingValue);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, AtomicLong value)  throws IOException  {
        GeoPoint center = cell.getCenter();
        builder.startObject();
        builder.field("lat", center.lat());
        builder.field("lon", center.lon());
        builder.field("count", value);
        builder.endObject();
        return builder;
    }
}