package org.elasticsearch.plugin.geohashcellfacet;

import org.apache.lucene.index.AtomicReaderContext;
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.fielddata.GeoPointValues;
import org.elasticsearch.index.fielddata.IndexFieldData;
import org.elasticsearch.index.fielddata.IndexGeoPointFieldData;
import org.elasticsearch.search.facet.FacetExecutor;
import org.elasticsearch.search.facet.InternalFacet;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class GeoHashCellFacetExecutor extends FacetExecutor {
    private final String fieldName;
    private final int userLevel;
    private final String additionalGrouping;
    private Map<GeoHashCellFacetEntry, AtomicLong> counts = Maps.newHashMap();
    private final IndexGeoPointFieldData indexFieldData;
    private final IndexFieldData additionalGroupingFieldData;
    private final MapBox mapBox;

    public GeoHashCellFacetExecutor(String fieldName,
                                    SearchContext searchContext,
                                    MapBox mapBox,
                                    int userLevel, String additionalGrouping) {
        this.fieldName = fieldName;
        this.mapBox = mapBox;
        this.userLevel = userLevel;
        this.additionalGrouping = additionalGrouping;

        this.indexFieldData = searchContext
                .fieldData()
                .getForField(searchContext.smartNameFieldMapper(fieldName));

        if (additionalGrouping != null && !additionalGrouping.isEmpty()) {
            this.additionalGroupingFieldData = searchContext
                    .fieldData()
                    .getForField(searchContext.smartNameFieldMapper(additionalGrouping));
        } else {
            this.additionalGroupingFieldData = null;
        }
    }

    @Override
    public InternalFacet buildFacet(String facetName) {
        return new GeoHashCellFacet(
                facetName, counts, fieldName,
                mapBox, userLevel, additionalGrouping);
    }

    @Override
    public Collector collector() {
        return new Collector();
    }

    final class Collector extends FacetExecutor.Collector {

        protected GeoPointValues values;
        protected String additionalGroupingValue;

        @Override
        public void setNextReader(AtomicReaderContext context)
                throws IOException {
            values = indexFieldData.load(context).getGeoPointValues();

            if (additionalGroupingFieldData != null)
                additionalGroupingValue = additionalGroupingFieldData.load(context).toString();
        }

        @Override
        public void collect(int docId) throws IOException {
            final GeoPointValues.Iter iterator = values.getIter(docId);

            if (iterator == null || !iterator.hasNext()) {
                increment(new MissingGeoValueEntry(), 1L);
                return;
            }

            while (iterator.hasNext()) {
                GeoPoint point = iterator.next();

                if (!mapBox.includes(point))
                    continue;

                GeoHashCell cell = new GeoHashCell(point, mapBox.getLevel(userLevel));
                GeoHashCellEntry entry = GeoHashCellEntry.createEntry(cell, additionalGroupingValue);
                increment(entry, 1L);
            }
        }

        @Override
        public void postCollection() {
            // do nothing
        }

        private void increment(GeoHashCellFacetEntry entry, Long value) {
            if (counts.containsKey(entry)) {
                counts.get(entry).addAndGet(value);
            } else {
                counts.put(entry, new AtomicLong(value));
            }
        }
    }
}
