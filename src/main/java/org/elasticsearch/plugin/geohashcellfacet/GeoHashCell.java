package org.elasticsearch.plugin.geohashcellfacet;

import org.elasticsearch.common.geo.GeoHashUtils;
import org.elasticsearch.common.geo.GeoPoint;

/**
 * Represents a cell established by the geo hash prefix.
 */
public class GeoHashCell {
    public final static int MIN_GEOHASH_LEVEL = 1;
    public final static int MAX_GEOHASH_LEVEL = 6;

    private final String geoHashPrefix;
    private final GeoPoint topLeft = new GeoPoint();
    private final GeoPoint bottomRight = new GeoPoint();

    /**
     * Creates a geo hash cell of the provided level which includes the given point.
     *
     * @param point Point which should be included in this cell.
     * @param level Level for the cell, i.e. the length of the geo hash.
     */
    public GeoHashCell(GeoPoint point, int level) {
        this(GeoHashUtils.encode(point.lat(), point.lon()).substring(0, level));
    }

    /**
     * Creates a geo hash cell instance based on the given geo hash prefix.
     * @param geoHashPrefix Prefix for the geo hash cell.
     */
    public GeoHashCell(String geoHashPrefix) {
        if (geoHashPrefix == null || geoHashPrefix.isEmpty())
            throw new IllegalArgumentException("GeoHash value is required");
        this.geoHashPrefix = geoHashPrefix;

        GeoHashUtils.decodeCell(geoHashPrefix, topLeft, bottomRight);
    }

    /**
     * Gets the point which is a center of this geo hash cell.
     * @return Center of this cell.
     */
    public GeoPoint getCenter() {
        return new GeoPoint(
                (topLeft.lat() + bottomRight.lat()) / 2D,
                (topLeft.lon() + bottomRight.lon()) / 2D
        );
    }

    /**
     * Compare if this object equals another object.
     * @param o Object to compare.
     * @return True if objects are equal, otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeoHashCell that = (GeoHashCell) o;

        return geoHashPrefix.equals(that.geoHashPrefix);
    }

    /**
     * Gets a hash code of this instance.
     * @return Hash code.
     */
    @Override
    public int hashCode() {
        return geoHashPrefix.hashCode();
    }

    /**
     * Gets a string representation of this instance.
     * @return String representation.
     */
    @Override
    public String toString() {
        return getCenter().toString();
    }
}
