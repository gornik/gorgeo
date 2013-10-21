package org.elasticsearch.plugin.geohashcellfacet;

public class GeoHashCellWithGroupingEntry extends GeoHashCellEntry {

    private final String additionalGroupingValue;

    public GeoHashCellWithGroupingEntry(GeoHashCell cell, String additionalGroupingValue) {
        super(cell);
        this.additionalGroupingValue = additionalGroupingValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        GeoHashCellWithGroupingEntry that = (GeoHashCellWithGroupingEntry) o;

        return additionalGroupingValue.equals(that.additionalGroupingValue);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + additionalGroupingValue.hashCode();
        return result;
    }
}
