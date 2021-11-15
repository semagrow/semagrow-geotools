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
import org.semagrow.geotools.helpers.WktHelpers;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class MBBoxer {

    protected final static ValueFactory vf;
    static {
        vf = SimpleValueFactory.getInstance();
    }

    private IRI DEFAULT_SRID = vf.createIRI("http://www.opengis.net/def/crs/EPSG/0/4326");
    private RDFWriter writer;

    public void process(String iPath, String oPath) throws IOException {
        this.writer = new NTriplesWriter(new FileWriter(oPath));

        RDFHandler handler = new MyRDFHandler();
        RDFParser parser = Rio.createParser(RDFFormat.NTRIPLES);
        parser.getParserConfig().set(BasicParserSettings.VERIFY_DATATYPE_VALUES, false);
        parser.setRDFHandler(handler);
        parser.parse(new FileInputStream(iPath), "");
    }

    private class MyRDFHandler extends AbstractRDFHandler {

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
                    Geometry new_geom = geometry.getEnvelope();
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

        String iPath = "/home/antru/Documents/xearth/fs-demo/GADM/DEU/ORG.nt";
        String oPath = "/home/antru/Documents/xearth/fs-demo/GADM/DEU/DEU.nt";

        MBBoxer mbboxer = new MBBoxer();
        mbboxer.process(iPath, oPath);
    }
}
