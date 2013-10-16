package org.elasticsearch.plugin.geohashcellfacet;

import org.apache.lucene.index.AtomicReaderContext;
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.fielddata.GeoPointValues;
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
    private Map<String, AtomicLong> counts = Maps.newHashMap();
    private final IndexGeoPointFieldData indexFieldData;
    private final MapBox mapBox;

    public GeoHashCellFacetExecutor(String fieldName,
                                    SearchContext searchContext,
                                    MapBox mapBox,
                                    int userLevel) {
        this.fieldName = fieldName;
        this.mapBox = mapBox;
        this.userLevel = userLevel;
        this.indexFieldData = searchContext
                .fieldData()
                .getForField(searchContext.smartNameFieldMapper(fieldName));
    }

    @Override
    public InternalFacet buildFacet(String facetName) {
        return new GeoHashCellFacet(
                facetName, counts, fieldName,
                mapBox, userLevel);
    }

    @Override
    public Collector collector() {
        return new Collector();
    }

    final class Collector extends FacetExecutor.Collector {

        protected GeoPointValues values;

        @Override
        public void setNextReader(AtomicReaderContext context)
                throws IOException {
            values = indexFieldData.load(context).getGeoPointValues();
        }

        @Override
        public void collect(int docId) throws IOException {
            final GeoPointValues.Iter iterator = values.getIter(docId);

            if (iterator == null || !iterator.hasNext()) {
                increment("_missing", 1L);
                return;
            }

            while (iterator.hasNext()) {
                GeoPoint point = iterator.next();

                if (!mapBox.includes(point))
                    continue;

                GeoHashCell cell = new GeoHashCell(point, mapBox.getLevel(userLevel));
                increment(cell.toString(), 1L);
            }
        }

        @Override
        public void postCollection() {
            // do nothing
        }

        private void increment(String name, Long value) {
            if (counts.containsKey(name)) {
                counts.get(name).addAndGet(value);
            } else {
                counts.put(name, new AtomicLong(value));
            }
        }
    }
}
