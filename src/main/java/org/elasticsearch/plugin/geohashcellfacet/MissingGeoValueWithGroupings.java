package org.elasticsearch.plugin.geohashcellfacet;

import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class MissingGeoValueWithGroupings extends ResultWithGroupings {

    private final AtomicLong total;
    private final Map<String, AtomicLong> groupings;

    public MissingGeoValueWithGroupings(AtomicLong total) {
        this.total = total;
        this.groupings = Maps.newHashMap();
    }

    public MissingGeoValueWithGroupings(AtomicLong total, Map<String, AtomicLong> groupings) {
        this.total = total;
        this.groupings = groupings;
    }

    @Override
    public ResultWithGroupings merge(ResultWithGroupings other) {
        if (!(other instanceof MissingGeoValueWithGroupings))
            throw new IllegalArgumentException("other");

        MissingGeoValueWithGroupings missingWithGroupings = (MissingGeoValueWithGroupings) other;

        return new MissingGeoValueWithGroupings(
                new AtomicLong(total.get() + missingWithGroupings.total.get()),
                mergeGroupings(this.groupings, missingWithGroupings.groupings));
    }

    @Override
    public void toXContent(XContentBuilder builder) throws IOException {
        builder.startObject();
        if (groupings.size() > 0) {
            for (Map.Entry<String, AtomicLong> entry : groupings.entrySet()) {
                builder.field(entry.getKey(), entry.getValue().get());
            }
        } else {
            builder.field("_missing", total.get());
        }
        builder.endObject();
    }
}
