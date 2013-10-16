package org.elasticsearch.plugin.tests;


import org.elasticsearch.plugin.geohashcellfacet.MapBox;
import org.elasticsearch.common.geo.GeoPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class MapBoxTests {

    @Test
    public void mapBoxIncludesCorners() {
        MapBox mapBox = new MapBox(new GeoPoint(0L, 0L), new GeoPoint(-10L, 10L));
        assertTrue(mapBox.includes(new GeoPoint(0L, 0L)));
        assertTrue(mapBox.includes(new GeoPoint(0L, 10L)));
        assertTrue(mapBox.includes(new GeoPoint(-10L, 0L)));
        assertTrue(mapBox.includes(new GeoPoint(-10L, 10L)));
    }

    @Test
    public void mapBoxIncludesPointInside() {
        MapBox mapBox = new MapBox(new GeoPoint(0L, 0L), new GeoPoint(-10L, 10L));
        assertTrue(mapBox.includes(new GeoPoint(-5L, 5L)));
    }

    @Test
    public void mapBoxIncludesBoundaryPoints() {
        MapBox mapBox = new MapBox(new GeoPoint(0L, 0L), new GeoPoint(-10L, 10L));
        assertTrue(mapBox.includes(new GeoPoint(0L, 5L)));
        assertTrue(mapBox.includes(new GeoPoint(-5L, 0L)));
        assertTrue(mapBox.includes(new GeoPoint(-10L, 5L)));
    }

    @Test
    public void mapBoxDoesNotIncludePointOutside() {
        MapBox mapBox = new MapBox(new GeoPoint(0L, 0L), new GeoPoint(-10L, 10L));
        assertFalse(mapBox.includes(new GeoPoint(-15L, 15L)));
        assertFalse(mapBox.includes(new GeoPoint(0L, 15L)));
    }

    @Test
    public void mapBoxForFullMapHasBaseLevel0() {
        MapBox mapBox = new MapBox(new GeoPoint(90L, -180L), new GeoPoint(-90L, 180L));
        assertEquals(0, mapBox.getBaseLevel());
    }

    @Test
         public void mapBoxForOneGeoHashLevelDownHasBaseLevel1() {
        MapBox mapBox = new MapBox(
                new GeoPoint(90L/4L, -180L/8L),
                new GeoPoint(-90L/4L, 180L/8L));
        assertEquals(1, mapBox.getBaseLevel());
    }

    @Test
    public void mapBoxForTwoGeoHashLevelsDownHasBaseLevel2() {
        MapBox mapBox = new MapBox(
                new GeoPoint(90L/32L, -180L/32L),
                new GeoPoint(-90L/32L, 180L/32L));
        assertEquals(2, mapBox.getBaseLevel());
    }
}
