package org.elasticsearch.plugin.geohashcellfacet;

import org.elasticsearch.common.collect.Maps;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class GeoHashCellCounts {

    private Map<GeoHashCellFacetEntry, AtomicLong> counts = Maps.newHashMap();

    public void addMissing() {
        increment(new MissingGeoValueEntry(), 1L);
    }

    public void addEntry(GeoHashCellEntry entry) {
        increment(entry, 1L);
    }

    public GeoHashCellFacetResult createResults() {
        return GeoHashCellFacetResult.createFromEntryCounts(counts);
    }

    private void increment(GeoHashCellFacetEntry entry, Long value) {
        if (counts.containsKey(entry)) {
            counts.get(entry).addAndGet(value);
        } else {
            counts.put(entry, new AtomicLong(value));
        }
    }
}
