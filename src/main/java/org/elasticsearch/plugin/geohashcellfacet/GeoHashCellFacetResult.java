package org.elasticsearch.plugin.geohashcellfacet;

import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class GeoHashCellFacetResult {

    private final Map<String, ResultWithGroupings> counts;

    public GeoHashCellFacetResult() {
        this.counts = Maps.newHashMap();
    }

    public GeoHashCellFacetResult(GeoHashCellFacetEntry entry, AtomicLong value) {
        Map<String, ResultWithGroupings> counts = Maps.newHashMap();
        counts.put(entry.getKey(), entry.createCellWithGroupings(value));

        this.counts = counts;
    }

    private GeoHashCellFacetResult(Map<String, ResultWithGroupings> counts) {
        this.counts = Maps.newHashMap(counts);
    }

    public GeoHashCellFacetResult reduce(GeoHashCellFacetResult other) {

        Map<String, ResultWithGroupings> countsCopy = Maps.newHashMap(this.counts);

        for (Map.Entry<String, ResultWithGroupings> pair : other.counts.entrySet()) {
            String key = pair.getKey();
            ResultWithGroupings value = pair.getValue();

            if (countsCopy.containsKey(key)) {
                ResultWithGroupings oldValue = countsCopy.get(key);
                countsCopy.remove(key);
                countsCopy.put(key, oldValue.merge(value));
            }
            else {
                countsCopy.put(key, value);
            }
        }

        return new GeoHashCellFacetResult(countsCopy);
    }

    public static GeoHashCellFacetResult createFromEntryCounts(Map<GeoHashCellFacetEntry, AtomicLong> counts) {
        GeoHashCellFacetResult result = new GeoHashCellFacetResult();

        for (Map.Entry<GeoHashCellFacetEntry, AtomicLong> entry : counts.entrySet()) {
            result = result.reduce(new GeoHashCellFacetResult(entry.getKey(), entry.getValue()));
        }

        return result;
    }

    public void toXContent(XContentBuilder builder) throws IOException {
        for (Map.Entry<String, ResultWithGroupings> entry : counts.entrySet()) {
            entry.getValue().toXContent(builder);
        }
    }
}


