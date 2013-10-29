package org.elasticsearch.plugin.geohashcellfacet;

import org.elasticsearch.common.io.stream.StreamInput;

import java.io.IOException;

public class GeoHashCellFacetEntryReader {

    public static GeoHashCellFacetEntry readFrom(StreamInput in) throws IOException {
        String type = in.readString();

        if (type.equals("GeoHashCellEntry"))
            return GeoHashCellEntry.readFrom(in);

        if (type.equals("GeoHashCellWithGroupingEntry"))
            return GeoHashCellWithGroupingEntry.readFrom(in);

        if (type.equals("MissingGeoValueEntry"))
            return new MissingGeoValueEntry();

        throw new IllegalArgumentException("Type not supported: " + type);
    }
}
