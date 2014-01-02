package org.elasticsearch.plugin.geohashcellfacet;


import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.text.BytesText;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.fielddata.BytesValues;
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
    private final String additionalGroupingField;
    private final IndexFieldData additionalGroupingFieldData;
    private final GeoHashCellCounts counts = new GeoHashCellCounts();
    private final IndexGeoPointFieldData indexFieldData;
    private final MapBox mapBox;

    public GeoHashCellFacetExecutor(String fieldName,
                                    SearchContext searchContext,
                                    MapBox mapBox,
                                    int userLevel, String additionalGroupingField) {
        this.fieldName = fieldName;
        this.mapBox = mapBox;
        this.userLevel = userLevel;
        this.additionalGroupingField = additionalGroupingField;

        this.indexFieldData = searchContext
                .fieldData()
                .getForField(searchContext.smartNameFieldMapper(fieldName));

        if (additionalGroupingField != null && !additionalGroupingField.isEmpty()) {
            this.additionalGroupingFieldData = searchContext
                    .fieldData()
                    .getForField(searchContext.smartNameFieldMapper(additionalGroupingField));
        } else {
            this.additionalGroupingFieldData = null;
        }
    }

    @Override
    public InternalFacet buildFacet(String facetName) {
        GeoHashCellFacetResult results = counts.createResults();
        return new GeoHashCellFacet(
                facetName, results, fieldName,
                mapBox, userLevel, additionalGroupingField);
    }

    @Override
    public Collector collector() {
        return new Collector();
    }

    final class Collector extends FacetExecutor.Collector {

        protected GeoPointValues values;
        private BytesValues additionalValues;

        @Override
        public void setNextReader(AtomicReaderContext context)
                throws IOException {
            values = indexFieldData.load(context).getGeoPointValues();

            if (additionalGroupingFieldData != null) {
                additionalValues = additionalGroupingFieldData
                        .load(context)
                        .getBytesValues(false);
            } else {
                additionalValues = null;
            }
        }

        @Override
        public void collect(int docId) throws IOException {
            final int numPoints = values.setDocument(docId);

            if (numPoints == 0) {
                counts.addMissing();
                return;
            }

            for (int i = 0; i < numPoints; i++) {

                GeoPoint point = values.nextValue();

                if (!mapBox.includes(point))
                    continue;

                GeoHashCell cell = new GeoHashCell(point, mapBox.getLevel(userLevel));

                if (additionalValues == null) {
                    counts.addEntry(GeoHashCellEntry.createEntry(cell));
                } else {
                    final int numValues = additionalValues.setDocument(docId);

                    if (numValues == 0)
                        counts.addEntry(GeoHashCellEntry.createEntry(cell));

                    for (int j = 0; j < numValues; j++) {
                        BytesRef bytesRef = additionalValues.nextValue();
                        Text text = new BytesText(new BytesArray(bytesRef));
                        counts.addEntry(GeoHashCellEntry.createEntry(cell, text.string()));
                    }
                }
            }
        }

        @Override
        public void postCollection() {

        }
    }
}
