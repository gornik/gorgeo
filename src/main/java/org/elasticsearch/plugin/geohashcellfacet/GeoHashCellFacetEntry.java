package org.elasticsearch.plugin.geohashcellfacet;

import com.sun.tools.corba.se.idl.InterfaceState;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public interface GeoHashCellFacetEntry {
    ResultWithGroupings createCellWithGroupings(AtomicLong value);

    String getKey();
}
