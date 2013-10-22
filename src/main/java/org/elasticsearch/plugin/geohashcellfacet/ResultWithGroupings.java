package org.elasticsearch.plugin.geohashcellfacet;

import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public abstract class ResultWithGroupings {

    public abstract ResultWithGroupings merge(ResultWithGroupings other);

    protected Map<String, AtomicLong> mergeGroupings(
            Map<String, AtomicLong> firstGroupings, Map<String, AtomicLong> secondGroupings) {
        Map<String, AtomicLong> result = Maps.newHashMap(firstGroupings);

        for (Map.Entry<String, AtomicLong> entry : secondGroupings.entrySet()) {
            if (result.containsKey(entry.getKey()))
                result.get(entry.getKey()).addAndGet(entry.getValue().get());
            else
                result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public abstract void toXContent(XContentBuilder builder) throws IOException;
}
