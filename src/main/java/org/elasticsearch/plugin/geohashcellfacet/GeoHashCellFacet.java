package org.elasticsearch.plugin.geohashcellfacet;

import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.geo.GeoHashUtils;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentBuilderString;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.InternalFacet;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An ElasticSearch facet which allows grouping geo points by cells established
 * by geo hash.
 * Example usage:
 *  "geohashcell": {
 *      "level" : 3,
 *      "top_left": "11.21,51.33",
 *      "bottom_right": "-20.21,80.65"
 *  }
 */
public class GeoHashCellFacet extends InternalFacet {

    public static final String TYPE = "geohashcell";
    private static final BytesReference STREAM_TYPE = new BytesArray(TYPE);
    private MapBox mapBox;

    private Map<GeoHashCellFacetEntry, AtomicLong> counts = Maps.newHashMap();
    private String fieldName;
    private int userLevel;
    private String additionalGroupingField;

    private GeoHashCellFacetResult results;

    /**
     * Creates the facet instance.
     * @param facetName Name of the facet.
     * @param results
     * @param fieldName Name of the field used for grouping.
     * @param mapBox Current map viewport.
     * @param userLevel Level for geo hash grouping.
     * @param additionalGroupingField
     */
    public GeoHashCellFacet(String facetName, GeoHashCellFacetResult results,
                            String fieldName, MapBox mapBox,
                            int userLevel, String additionalGroupingField) {
        super(facetName);
        this.results = results;
        this.fieldName = fieldName;
        this.mapBox = mapBox;
        this.userLevel = userLevel;
        this.additionalGroupingField = additionalGroupingField;
    }

    private GeoHashCellFacet() {
        this.fieldName = null;
        this.mapBox = null;
        this.userLevel = 0;
        this.results = new GeoHashCellFacetResult();
        this.additionalGroupingField = null;
    }


    public static void registerStreams() {
        Streams.registerStream(STREAM, STREAM_TYPE);
    }

    static InternalFacet.Stream STREAM = new InternalFacet.Stream() {
        @Override
        public Facet readFacet(StreamInput in) throws IOException {
            GeoHashCellFacet facet = new GeoHashCellFacet();
            facet.readFrom(in);
            return facet;
        }
    };

    @Override
    public BytesReference streamType() {
        return STREAM_TYPE;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        this.mapBox = MapBox.readFrom(in);

        int countsSize = in.readInt();

        for (int i = 0; i<countsSize; i++) {
            GeoHashCellFacetEntry entry = GeoHashCellFacetEntryReader.readFrom(in);
            long value = in.readLong();
            counts.put(entry, new AtomicLong(value));
        }

        this.results = GeoHashCellFacetResult.readFrom(in);

        this.fieldName = in.readString();
        this.userLevel = in.readInt();
        this.additionalGroupingField = in.readOptionalString();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);

        this.mapBox.writeTo(out);

        out.writeInt(counts.size());

        for (Map.Entry<GeoHashCellFacetEntry, AtomicLong> entry : counts.entrySet()) {
            entry.getKey().writeTo(out);
            out.writeLong(entry.getValue().get());
        }

        this.results.writeTo(out);

        out.writeString(this.fieldName);
        out.writeInt(this.userLevel);
        out.writeOptionalString(this.additionalGroupingField);
    }

    @Override
    public Facet reduce(ReduceContext reduceContext) {
        List<Facet> facets = reduceContext.facets();
        GeoHashCellFacet geoHashCellFacet = (GeoHashCellFacet) facets.get(0);

        for (int i = 1; i < facets.size(); i++) {
            Facet facet = facets.get(i);

            if (facet instanceof GeoHashCellFacet) {
                GeoHashCellFacet hashCellFacet = (GeoHashCellFacet) facet;

                geoHashCellFacet.results = geoHashCellFacet.results.reduce(hashCellFacet.results);
            }
        }

        return geoHashCellFacet;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(getName());
        builder.field(Fields._TYPE, STREAM_TYPE);
        builder.field(GeoHashCellFacetParser.ParamName.FIELD, fieldName);
        builder.field(GeoHashCellFacetParser.ParamName.TOP_LEFT, mapBox.getTopLeft());
        builder.field(GeoHashCellFacetParser.ParamName.BOTTOM_RIGHT, mapBox.getBottomRight());
        builder.field(GeoHashCellFacetParser.ParamName.LEVEL, mapBox.getLevel(userLevel));
        builder.field("base_map_level", mapBox.getBaseLevel());
        builder.startArray("regions");

        results.toXContent(builder);

        builder.endArray();
        builder.endObject();
        return builder;
    }

    static final class Fields {
        static final XContentBuilderString _TYPE = new XContentBuilderString("_type");
    }
}
