package org.semagrow.geotools;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.GEO;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.ntriples.NTriplesWriter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.semagrow.geotools.helpers.WktHelpers;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DatasetShaper {

    protected final static ValueFactory vf;
    static {
        vf = SimpleValueFactory.getInstance();
    }

    private IRI DEFAULT_SRID = vf.createIRI("http://www.opengis.net/def/crs/EPSG/0/4326");
    private Geometry boundary;
    private RDFWriter writer;

    public void process(String iPath, String oPath, Geometry boundary) throws IOException {
        this.boundary = boundary;
        this.writer = new NTriplesWriter(new FileWriter(oPath));

        RDFHandler handler = new DatasetShaperHandler();
        RDFParser parser = Rio.createParser(RDFFormat.NTRIPLES);
        parser.getParserConfig().set(BasicParserSettings.VERIFY_DATATYPE_VALUES, false);
        parser.setRDFHandler(handler);
        parser.parse(new FileInputStream(iPath), "");
    }

    private class DatasetShaperHandler extends AbstractRDFHandler {

        @Override
        public void startRDF() throws RDFHandlerException {
            super.startRDF();
            writer.startRDF();
        }

        @Override
        public void handleStatement(Statement statement) throws RDFHandlerException {
            if (statement.getPredicate().equals(GEO.AS_WKT)) {
                Literal wkt = (Literal) statement.getObject();
                try {
                    Geometry geometry = WktHelpers.createGeometry(wkt, DEFAULT_SRID);
                    Geometry new_geom = geometry.buffer(0).intersection(boundary.buffer(0));
                    Resource s = statement.getSubject();
                    IRI p = GEO.AS_WKT;
                    Literal o = WktHelpers.createWKTLiteral(new_geom, DEFAULT_SRID);
                    writer.handleStatement(vf.createStatement(s, p, o));
                } catch (ParseException e) {
                    e.printStackTrace();
                    throw new RDFHandlerException(e);
                }
            } else {
                writer.handleStatement(statement);
            }
        }

        @Override
        public void endRDF() throws RDFHandlerException {
            super.endRDF();
            writer.endRDF();
        }
    }

    public static void main(String args[]) throws IOException, ParseException {

        String base = "/home/antru/Documents/xearth/geospatial-selector/";
        String wDir = base + "poly/";
        String iDir = base + "datasets/orig/";
        String oDir = base + "datasets/sh/";

        Geometry[] polygons = new Geometry[9];
        WKTReader reader = new WKTReader();

        polygons[0] = reader.read(new FileReader(wDir + "wkt1.txt"));
        polygons[1] = reader.read(new FileReader(wDir + "wkt2.txt"));
        polygons[2] = reader.read(new FileReader(wDir + "wkt3.txt"));
        polygons[3] = reader.read(new FileReader(wDir + "wkt4.txt"));
        polygons[4] = reader.read(new FileReader(wDir + "wkt5.txt"));
        polygons[5] = reader.read(new FileReader(wDir + "wkt6.txt"));
        polygons[6] = reader.read(new FileReader(wDir + "wkt7.txt"));
        polygons[7] = reader.read(new FileReader(wDir + "wkt8.txt"));
        polygons[8] = reader.read(new FileReader(wDir + "wkt9.txt"));


        DatasetShaper shaper = new DatasetShaper();

        shaper.process(iDir + "snow1.nt", oDir + "snow1.nt", polygons[0]);
        shaper.process(iDir + "snow2.nt", oDir + "snow2.nt", polygons[1]);
        shaper.process(iDir + "snow3.nt", oDir + "snow3.nt", polygons[2]);
        shaper.process(iDir + "snow4.nt", oDir + "snow4.nt", polygons[3]);
        shaper.process(iDir + "snow5.nt", oDir + "snow5.nt", polygons[4]);
        shaper.process(iDir + "snow6.nt", oDir + "snow6.nt", polygons[5]);
        shaper.process(iDir + "snow7.nt", oDir + "snow7.nt", polygons[6]);
        shaper.process(iDir + "snow8.nt", oDir + "snow8.nt", polygons[7]);
        shaper.process(iDir + "snow9.nt", oDir + "snow9.nt", polygons[8]);


    }
}
