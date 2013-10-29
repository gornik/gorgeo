package org.elasticsearch.plugin.geohashcellfacet;


import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.GeoUtils;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.unit.DistanceUnit;

import java.io.IOException;

/**
 * A box representing the current map viewport.
 */
public class MapBox {
    private final GeoPoint topLeft;
    private final GeoPoint bottomRight;
    private final double width;
    private final double height;
    private final int baseLevel;

    /**
     * Creates the map box based on the provided coordinates.
     * @param topLeft Top-left point of the map.
     * @param bottomRight Bottom-right point of the map.
     */
    public MapBox(GeoPoint topLeft, GeoPoint bottomRight) {
        // TODO check if correct
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;

        this.width = calculateWidth();
        this.height = calculateHeight();
        this.baseLevel = calculateBaseLevel();
    }

    /**
     * Top-left point of the map.
     * @return Top-left point of the map.
     */
    public GeoPoint getTopLeft() {
        return topLeft;
    }

    /**
     * Bottom-right point of the map.
     * @return Bottom-right point of the map.
     */
    public GeoPoint getBottomRight() {
        return bottomRight;
    }

    /**
     * Checks if the given point is included inside this box.
     * @param point Point to check.
     * @return True if point is within boundaries of the box.
     */
    public boolean includes(GeoPoint point) {
        // TODO what about crossing date change line

        return topLeft.lat() >= point.lat() &&
               topLeft.lon() <= point.lon() &&
               point.lat() >= bottomRight.lat() &&
               point.lon() <= bottomRight.lon();
    }

    /**
     * Gets the base grouping level (geohash length) for the current viewport.
     * @return Base grouping level.
     */
    public int getBaseLevel() {
        return baseLevel;
    }

    /**
     * Gets the actual grouping level (geohash length), based on the base level
     * for the current map box and the level provided by the user.
     * @param userLevel Grouping level provided by the "level" parameter.
     * @return Grouping level in [1,6] range.
     */
    public int getLevel(int userLevel) {
        return Math.max(GeoHashCell.MIN_GEOHASH_LEVEL,
                Math.min(GeoHashCell.MAX_GEOHASH_LEVEL, baseLevel + userLevel));
    }

    private int calculateBaseLevel() {

        for (int level = 1; level <= GeoHashCell.MAX_GEOHASH_LEVEL; level++) {
            double cellWidth = GeoUtils.geoHashCellWidth(level);
            double cellHeight = GeoUtils.geoHashCellHeight(level);

            if (geoHashCellSmallerThanMapBox(cellWidth, cellHeight))
                return level - 1;
        }

        return GeoHashCell.MAX_GEOHASH_LEVEL - 1;
    }

    private boolean geoHashCellSmallerThanMapBox(double cellWidth, double cellHeight) {
        return cellWidth < width && cellHeight < height;
    }

    private double calculateWidth() {
        return GeoDistance.PLANE.calculate(
                topLeft.lat(), topLeft.lon(),
                topLeft.lat(), bottomRight.lon(),
                DistanceUnit.METERS);
    }

    private double calculateHeight() {
        return GeoDistance.PLANE.calculate(
                topLeft.lat(), topLeft.lon(),
                bottomRight.lat(), topLeft.lon(),
                DistanceUnit.METERS);
    }

    public static MapBox readFrom(StreamInput in) throws IOException {
        GeoPoint topLeft = new GeoPoint();
        GeoPoint bottomRight = new GeoPoint();

        String topLeftHash = in.readString();
        String bottomRightHash = in.readString();
        topLeft.resetFromGeoHash(topLeftHash);
        bottomRight.resetFromGeoHash(bottomRightHash);

        return new MapBox(topLeft, bottomRight);
    }

    public void writeTo(StreamOutput out) throws IOException {
        String topLeftHash = this.getTopLeft().geohash();
        String bottomRightHash = this.getBottomRight().geohash();

        out.writeString(topLeftHash);
        out.writeString(bottomRightHash);
    }
}
