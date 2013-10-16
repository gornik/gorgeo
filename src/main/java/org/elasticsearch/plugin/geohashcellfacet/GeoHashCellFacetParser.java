package org.elasticsearch.plugin.geohashcellfacet;


import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.search.facet.FacetExecutor;
import org.elasticsearch.search.facet.FacetParser;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;

public class GeoHashCellFacetParser extends AbstractComponent implements FacetParser {

    static final class ParamName {
        final static String FIELD = "field";
        final static String TOP_LEFT = "top_left";
        final static String BOTTOM_RIGHT = "bottom_right";
        final static String LEVEL = "level";
    }

    static final class Default {
        final static String FIELD = "location";
        final static GeoPoint TOP_LEFT = new GeoPoint(90L, -180L);
        final static GeoPoint BOTTOM_RIGHT = new GeoPoint(-90L, 180L);
        final static int LEVEL = 3;
    }

    @Inject
    public GeoHashCellFacetParser(Settings settings) {
        super(settings);
        GeoHashCellFacet.registerStreams();
    }

    @Override
    public String[] types() {
        return new String[] {GeoHashCellFacet.TYPE};
    }

    @Override
    public FacetExecutor.Mode defaultMainMode() {
        return FacetExecutor.Mode.COLLECTOR;
    }

    @Override
    public FacetExecutor.Mode defaultGlobalMode() {
        return FacetExecutor.Mode.COLLECTOR;
    }

    @Override
    public FacetExecutor parse(String facetName, XContentParser parser,
                               SearchContext searchContext) throws IOException {
        String field = Default.FIELD;
        GeoPoint topLeft = new GeoPoint(
                Default.TOP_LEFT.lat(), Default.TOP_LEFT.lon());
        GeoPoint bottomRight = new GeoPoint(
                Default.BOTTOM_RIGHT.lat(), Default.BOTTOM_RIGHT.lon());

        int level = Default.LEVEL;

        String currentFieldName = null;
        XContentParser.Token token;

        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            }
            else if (token.isValue()) {
                if (currentFieldName.equals(ParamName.FIELD)) {
                    field = parser.text();
                }
                else if (currentFieldName.equals(ParamName.TOP_LEFT)) {
                    GeoPoint.parse(parser, topLeft);
                }
                else if (currentFieldName.equals(ParamName.BOTTOM_RIGHT)) {
                    GeoPoint.parse(parser, bottomRight);
                }
                else if (currentFieldName.equals(ParamName.LEVEL)) {
                    level = parser.intValue();
                }
            }
        }

        MapBox mapBox = new MapBox(topLeft, bottomRight);

        return new GeoHashCellFacetExecutor(field, searchContext,
                mapBox, level);
    }
}
