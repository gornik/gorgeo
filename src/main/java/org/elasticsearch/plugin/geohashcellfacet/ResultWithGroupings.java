package org.elasticsearch.plugin.geohashcellfacet;

import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public abstract class ResultWithGroupings {

    public abstract ResultWithGroupings merge(ResultWithGroupings other);

    protected Map<String, AtomicLong> mergeGroupings(
            Map<String, AtomicLong> firstGroupings, Map<String, AtomicLong> secondGroupings) {
        for (Map.Entry<String, AtomicLong> entry : secondGroupings.entrySet()) {
            if (firstGroupings.containsKey(entry.getKey()))
                firstGroupings.get(entry.getKey()).addAndGet(entry.getValue().get());
            else
                firstGroupings.put(entry.getKey(), entry.getValue());
        }

        return firstGroupings;
    }

    public abstract void toXContent(XContentBuilder builder) throws IOException;

    public abstract void writeTo(StreamOutput out) throws IOException;
}
