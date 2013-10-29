package org.elasticsearch.plugin.geohashcellfacet;

import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class GeoHashCellWithGroupings extends ResultWithGroupings {

    private final GeoHashCell cell;
    private final long total;
    private final Map<String, AtomicLong> groupings;

    public GeoHashCellWithGroupings(GeoHashCell cell, long total) {

        this.cell = cell;
        this.total = total;
        this.groupings = Maps.newHashMap();
    }

    public GeoHashCellWithGroupings(GeoHashCell cell, long total, Map<String, AtomicLong> groupings) {
        this.cell = cell;
        this.total = total;
        this.groupings = Maps.newHashMap(groupings);
    }

    @Override
    public ResultWithGroupings merge(ResultWithGroupings other) {
        if (!(other instanceof GeoHashCellWithGroupings))
            throw new IllegalArgumentException("other");

        GeoHashCellWithGroupings cellWithGroupings = (GeoHashCellWithGroupings) other;

        return new GeoHashCellWithGroupings(cell,
                total + cellWithGroupings.total,
                mergeGroupings(this.groupings, cellWithGroupings.groupings));
    }

    @Override
    public void toXContent(XContentBuilder builder) throws IOException {
        GeoPoint center = cell.getCenter();
        builder.startObject();
        builder.field("lat", center.lat());
        builder.field("lon", center.lon());

        if (groupings.size() > 0) {
            builder.startObject("count");

            for (Map.Entry<String, AtomicLong> entry : groupings.entrySet()) {
                builder.field(entry.getKey(), entry.getValue().get());
            }

            builder.endObject();
        } else {
            builder.field("count", total);
        }

        builder.endObject();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString("GeoHashCellWithGroupings");
        cell.writeTo(out);
        out.writeLong(total);
        out.writeInt(groupings.size());

        for (Map.Entry<String, AtomicLong> entry : groupings.entrySet()) {
            out.writeString(entry.getKey());
            out.writeLong(entry.getValue().get());
        }
    }

    public static ResultWithGroupings readFrom(StreamInput in) throws IOException {
        GeoHashCell cell = GeoHashCell.readFrom(in);
        long total = in.readLong();
        int groupingsSize = in.readInt();

        Map<String, AtomicLong> groupings = Maps.newHashMap();

        for (int i = 0; i<groupingsSize; i++) {
            String key = in.readString();
            long value = in.readLong();

            groupings.put(key, new AtomicLong(value));
        }

        return new GeoHashCellWithGroupings(cell, total, groupings);
    }
}


