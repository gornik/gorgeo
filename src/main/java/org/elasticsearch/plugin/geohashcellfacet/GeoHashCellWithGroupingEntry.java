package org.elasticsearch.plugin.geohashcellfacet;

import org.elasticsearch.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class GeoHashCellWithGroupingEntry extends GeoHashCellEntry {

    private final String additionalGroupingValue;

    public GeoHashCellWithGroupingEntry(GeoHashCell cell, String additionalGroupingValue) {
        super(cell);
        this.additionalGroupingValue = additionalGroupingValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        GeoHashCellWithGroupingEntry that = (GeoHashCellWithGroupingEntry) o;

        return additionalGroupingValue.equals(that.additionalGroupingValue);
    }

    @Override
    public GeoHashCellWithGroupings createCellWithGroupings(AtomicLong value) {
        Map<String, AtomicLong> counts = Maps.newHashMap();
        counts.put(additionalGroupingValue, value);
        return new GeoHashCellWithGroupings(cell, value.get(), counts);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + additionalGroupingValue.hashCode();
        return result;
    }
}
