package org.elasticsearch.plugin.geohashcellfacet;

import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
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
        this.counts = counts;
    }

    public GeoHashCellFacetResult reduce(GeoHashCellFacetResult other) {

        for (Map.Entry<String, ResultWithGroupings> pair : other.counts.entrySet()) {
            String key = pair.getKey();
            ResultWithGroupings value = pair.getValue();

            if (this.counts.containsKey(key)) {
                ResultWithGroupings oldValue = this.counts.get(key);
                this.counts.remove(key);
                this.counts.put(key, oldValue.merge(value));
            }
            else {
                this.counts.put(key, value);
            }
        }

        return new GeoHashCellFacetResult(this.counts);
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

    public void writeTo(StreamOutput out) throws IOException {
        out.writeInt(counts.size());

        for (Map.Entry<String, ResultWithGroupings> entry : counts.entrySet()) {
            out.writeString(entry.getKey());
            entry.getValue().writeTo(out);
        }

    }

    public static GeoHashCellFacetResult readFrom(StreamInput in) throws IOException {
        int countsSize = in.readInt();

        Map<String, ResultWithGroupings> counts = Maps.newHashMap();

        for (int i = 0; i<countsSize; i++) {
            String key = in.readString();
            counts.put(key, ResultWithGroupingsReader.readFrom(in));
        }

        return new GeoHashCellFacetResult(counts);
    }
}


