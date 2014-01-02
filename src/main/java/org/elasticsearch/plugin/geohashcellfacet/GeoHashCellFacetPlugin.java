package org.elasticsearch.plugin.geohashcellfacet;


import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.search.facet.FacetModule;

/**
 * Plugin which extends ElasticSearch with a Geohash Cell facet functionality.
 */
public class GeoHashCellFacetPlugin extends AbstractPlugin {
    private final static String version = "1.0.4";

    /**
     * Name of the plugin.
     *
     * @return Name of the plugin.
     */
    @Override
    public String name() {
        return "gorgeo-" + version;
    }

    /**
     * Description of the plugin.
     *
     * @return Description of the plugin.
     */
    @Override
    public String description() {
        return "Geohash cell facet support";
    }

    /**
     * Hooks up to the FacetModule initialization and adds a new facet parser
     * {@link GeoHashCellFacetParser} which enables using {@link GeoHashCellFacet}.
     *
     * @param facetModule {@link FacetModule} instance
     */
    public void onModule(FacetModule facetModule) {
        facetModule.addFacetProcessor(GeoHashCellFacetParser.class);
    }
}
