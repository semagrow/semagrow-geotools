package org.semagrow.geotools.geographica;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Queries {

    public final static String Q7;
    public final static String Q8;
    public final static String Q9;
    public final static String Q10;
    public final static String Q11;
    public final static String Q12;
    public final static String Q13;
    public final static String Q14;
    public final static String Q15;
    public final static String Q16;
    public final static String Q17;

    static {

        String POINT = "POINT EMPTY";
        String LINE1 = "LINESTRING EMPTY";
        String LINE2 = "LINESTRING EMPTY";
        String LINE3 = "LINESTRING EMPTY";
        String POLYGON = "POLYGON EMPTY";

        try (InputStream input = Queries.class.getResourceAsStream("geographica/givenWKTs.properties")) {

            Properties prop = new Properties();
            prop.load(input);

            POINT = prop.getProperty("point");
            LINE1 = prop.getProperty("line1");
            LINE2 = prop.getProperty("line2");
            LINE3 = prop.getProperty("line3");
            POLYGON = prop.getProperty("polygon");

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Q7 = "" + // Equals_LGD_GivenLine:
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX lgd: <http://linkedgeodata.org/ontology/>\n" +
                "\n" +
                "SELECT ?s ?o\n" +
                "WHERE {\n" +
                "    ?s lgd:asWKT ?o.\n" +
                "    FILTER(geof:sfEquals(?o, \"" + LINE1 + "\"^^opengis:wktLiteral)) .\n" +
                "}";

        Q8 = "" + // Equals_GAG_GivenPolygon:
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX gag: <http://geo.linkedopendata.gr/gag/ontology/>\n" +
                "\n" +
                "SELECT ?s ?o\n" +
                "WHERE {\n" +
                "    ?s gag:asWKT ?o.\n" +
                "    FILTER(geof:sfEquals(?o, \"" + POLYGON + "\"^^geo:wktLiteral)).\n" +
                "}";

        Q9 = "" + // Intersects_LGD_GivenPolygon:
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX lgd: <http://linkedgeodata.org/ontology/>\n" +
                "\n" +
                "SELECT ?s ?o\n" +
                "WHERE {\n" +
                "    ?s lgd:asWKT ?o.\n" +
                "    FILTER(geof:sfIntersects(?o, \"" + POLYGON + "\"^^geo:wktLiteral)).\n" +
                "}\n";

        Q10 = "" + // Intersects_CLC_GivenLine:
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX clc: <http://geo.linkedopendata.gr/corine/ontology#>\n" +
                "\n" +
                "SELECT ?s ?o\n" +
                "WHERE {\n" +
                "    ?s clc:asWKT ?o.\n" +
                "    FILTER(geof:sfIntersects(?o, \"" + LINE2 + "\"^^geo:wktLiteral)).\n" +
                "}";

        Q11 = "" + // Overlaps_CLC_GivenPolygon:
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX clc: <http://geo.linkedopendata.gr/corine/ontology#>\n" +
                "\n" +
                "SELECT ?s ?o\n" +
                "WHERE {\n" +
                "    ?s clc:asWKT ?o.\n" +
                "    FILTER(geof:sfOverlaps(?o, \"" + POLYGON + "\"^^geo:wktLiteral)).\n" +
                "}";

        Q12 = "" + // Crosses_LGD_GivenLine:
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX lgd: <http://linkedgeodata.org/ontology/>\n" +
                "\n" +
                "SELECT ?s ?o\n" +
                "WHERE {\n" +
                "    ?s lgd:asWKT ?o.\n" +
                "    FILTER(geof:sfCrosses(?o, \"" + LINE3 + "\"^^geo:wktLiteral)).\n" +
                "}";

        Q13 = "" + // Within_GeoNames_GivenPolygon:
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geonames: <http://www.geonames.org/ontology#>\n" +
                "\n" +
                "SELECT ?s ?o\n" +
                "WHERE {\n" +
                "    ?s geonames:asWKT ?o.\n" +
                "    FILTER(geof:sfWithin(?o, \"" + POLYGON + "\"^^geo:wktLiteral)).\n" +
                "}";

        Q14 = "" + // Within_GeoNames_Point_Buffer:
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX opengis: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "PREFIX geonames: <http://www.geonames.org/ontology#>\n" +
                "\n" +
                "SELECT ?s ?o\n" +
                "WHERE {\n" +
                "    ?s geonames:asWKT ?o.\n" +
                "    FILTER(geof:sfWithin(?o, geof:buffer(\"" + POINT + "\"^^geo:wktLiteral, 3000, opengis:metre))).\n" +
                "}";

        Q15 = "" + // GeoNames_Point_Distance:
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX opengis: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "PREFIX geonames: <http://www.geonames.org/ontology#>\n" +
                "\n" +
                "SELECT ?s ?o\n" +
                "WHERE {\n" +
                "    ?s geonames:asWKT ?o.\n" +
                "    FILTER(geof:distance(?o, \"" + POINT + "\"^^geo:wktLiteral, opengis:metre) <= 3000).\n" +
                "}";

        Q16 = "" + // Disjoint_GeoNames_GivenPolygon:
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX opengis: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "PREFIX geonames: <http://www.geonames.org/ontology#>\n" +
                "\n" +
                "SELECT ?s ?o\n" +
                "WHERE {\n" +
                "    ?s geonames:asWKT ?o.\n" +
                "    FILTER(geof:sfDisjoint(?o, \"" + POLYGON + "\"^^geo:wktLiteral)).\n" +
                "}";

        Q17 = "" + // Disjoint_LGD_GivenPolygon:
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX lgd: <http://linkedgeodata.org/ontology/>\n" +
                "\n" +
                "SELECT ?s ?o\n" +
                "WHERE {\n" +
                "    ?s lgd:asWKT ?o.\n" +
                "    FILTER(geof:sfDisjoint(?o, \"" + POLYGON + "\"^^geo:wktLiteral)).\n" +
                "}";
    }

}

