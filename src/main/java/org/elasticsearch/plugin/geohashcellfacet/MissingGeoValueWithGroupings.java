package org.elasticsearch.plugin.geohashcellfacet;

import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
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

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString("MissingGeoValueWithGroupings");

        out.writeLong(total.get());
        out.writeInt(groupings.size());

        for (Map.Entry<String, AtomicLong> entry : groupings.entrySet()) {
            out.writeString(entry.getKey());
            out.writeLong(entry.getValue().get());
        }
    }

    public static ResultWithGroupings readFrom(StreamInput in) throws IOException {

        long total = in.readLong();
        int groupingsSize = in.readInt();

        Map<String, AtomicLong> groupings = Maps.newHashMap();

        for (int i = 0; i<groupingsSize; i++) {
            String key = in.readString();
            long value = in.readLong();

            groupings.put(key, new AtomicLong(value));
        }

        return new MissingGeoValueWithGroupings(new AtomicLong(total), groupings);
    }
}
