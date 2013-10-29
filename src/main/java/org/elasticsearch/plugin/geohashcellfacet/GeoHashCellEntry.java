package org.elasticsearch.plugin.geohashcellfacet;

import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public class GeoHashCellEntry implements GeoHashCellFacetEntry {

    protected final GeoHashCell cell;

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

    public static GeoHashCellEntry createEntry(GeoHashCell cell) {
        return createEntry(cell, null);
    }

    public static GeoHashCellEntry createEntry(GeoHashCell cell, String additionalGroupingValue) {
        return additionalGroupingValue == null || additionalGroupingValue.isEmpty()
                ? new GeoHashCellEntry(cell)
                : new GeoHashCellWithGroupingEntry(cell, additionalGroupingValue);
    }

    @Override
    public GeoHashCellWithGroupings createCellWithGroupings(AtomicLong value) {
        return new GeoHashCellWithGroupings(cell, value.get());
    }

    @Override
    public String getKey() {
        return cell.toString();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString("GeoHashCellEntry");
        cell.writeTo(out);
    }

    public static GeoHashCellFacetEntry readFrom(StreamInput in) throws IOException {
        GeoHashCell cell = GeoHashCell.readFrom(in);
        return new GeoHashCellEntry(cell);
    }
}