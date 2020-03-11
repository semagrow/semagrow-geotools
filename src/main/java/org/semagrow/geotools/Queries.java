package org.semagrow.geotools;

public final class Queries {

    private final static String prefixes_str = "" +
            "PREFIX invekos: <http://ai.di.uoa.gr/invekos/ontology/>\n" +
            "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
            "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
            "PREFIX opengis: <http://www.opengis.net/def/uom/OGC/1.0/>\n";

    private final static String thematic_str = "" +
            "  ?s invekos:hasID ?id .\n" +
            "  ?s invekos:hasDescription ?desc .\n" +
            "  ?s invekos:hasCropTypeNumber ?ct_number .\n" +
            "  ?s invekos:hasCropTypeName ?ct_name .\n" +
            "  ?s invekos:hasArea ?area .\n";

    public static String equalsPolygonQuery(String polygon, boolean thematic) {
        return prefixes_str +
                "SELECT * WHERE {\n" +
                ((thematic) ? thematic_str : "") +
                "  ?s geo:hasGeometry ?g .\n" +
                "  ?g geo:asWKT ?o .\n" +
                "  FILTER(geof:sfEquals(?o, \"" + polygon + "\"^^geo:wktLiteral))\n" +
                "}";
    }

    public static String containsPointQuery(String point, boolean thematic) {
        return prefixes_str +
                "SELECT * WHERE {\n" +
                ((thematic) ? thematic_str : "") +
                "  ?s geo:hasGeometry ?g .\n" +
                "  ?g geo:asWKT ?o .\n" +
                "  FILTER(geof:sfContains(?o, \"" + point + "\"^^geo:wktLiteral))\n" +
                "}";
    }

    public static String hasDistanceQuery(String point, boolean thematic) {
        return prefixes_str +
                "SELECT * WHERE {\n" +
                ((thematic) ? thematic_str : "") +
                "  ?s geo:hasGeometry ?g .\n" +
                "  ?g geo:asWKT ?o .\n" +
                "  FILTER(geof:distance(?o, \"" + point + "\"^^geo:wktLiteral, opengis:metre) < 10)\n" +
                "}";
    }

    public static String withinPolygon(String polygon, boolean thematic) {
        return prefixes_str +
                "SELECT * WHERE {\n" +
                ((thematic) ? thematic_str : "") +
                "  ?s geo:hasGeometry ?g .\n" +
                "  ?g geo:asWKT ?o .\n" +
                "  FILTER(geof:sfWithin(?o, \"" + polygon + "\"^^geo:wktLiteral))\n" +
                "}";
    }

}
