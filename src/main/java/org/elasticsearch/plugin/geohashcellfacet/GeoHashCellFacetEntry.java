package org.elasticsearch.plugin.geohashcellfacet;

import com.sun.tools.corba.se.idl.InterfaceState;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public interface GeoHashCellFacetEntry {
    ResultWithGroupings createCellWithGroupings(AtomicLong value);

    String getKey();

    void writeTo(StreamOutput out) throws IOException;
}

