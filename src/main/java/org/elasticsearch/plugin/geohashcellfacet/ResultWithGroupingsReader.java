package org.elasticsearch.plugin.geohashcellfacet;


import org.elasticsearch.common.io.stream.StreamInput;

import java.io.IOException;

public class ResultWithGroupingsReader {
    public static ResultWithGroupings readFrom(StreamInput in) throws IOException {

        String type = in.readString();

        if (type.equals("GeoHashCellWithGroupings"))
            return GeoHashCellWithGroupings.readFrom(in);

        if (type.equals("MissingGeoValueWithGroupings"))
            return MissingGeoValueWithGroupings.readFrom(in);

        throw new IllegalArgumentException("Type not supported: " + type);
    }
}
