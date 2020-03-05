package org.semagrow.geotools.geographica;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.helpers.RDFHandlerBase;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.semagrow.geotools.helpers.BoundingBoxHelpers;
import org.semagrow.geotools.helpers.MultiRDFFileWriter;
import org.semagrow.geotools.helpers.WktHelpers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class GeonamesPartitioner {

    private final ValueFactory vf = SimpleValueFactory.getInstance();
    private final IRI AS_WKT = vf.createIRI("http://www.geonames.org/ontology#asWKT");
    private final IRI DEFAULT_CRS = vf.createIRI("http://www.opengis.net/def/crs/EPSG/4326");
    private final IRI SfPoint  = vf.createIRI("http://www.opengis.net/ont/sf#Point");

    private BoundingBoxHelpers helpers = new BoundingBoxHelpers();

    private String path;
    private MultiRDFFileWriter writer;
    private List<Geometry> partitions;

    public GeonamesPartitioner(String path, MultiRDFFileWriter writer, List<Geometry> partitions) {
        this.path = path;
        this.writer = writer;
        this.partitions = partitions;
    }

    public void partition() throws IOException {

        RDFHandler handler = new GeonamesRDFHandler();

        RDFParser parser = Rio.createParser(RDFFormat.NTRIPLES);
        parser.getParserConfig().set(BasicParserSettings.VERIFY_DATATYPE_VALUES, false);
        parser.setRDFHandler(handler);
        parser.parse(new FileInputStream(path), "");
    }

    private class GeonamesRDFHandler extends RDFHandlerBase {

        private List<Statement> buffer = new ArrayList<>();
        private int bucket;

        @Override
        public void handleStatement(Statement statement) throws RDFHandlerException {

            if (statement.getPredicate().equals(RDFS.SUBPROPERTYOF) || statement.getObject().equals(OWL.OBJECTPROPERTY) || statement.getObject().equals(OWL.DATATYPEPROPERTY)) {
                return;
            }

            buffer.add(statement);

            if (statement.getPredicate().equals(AS_WKT)) {

                Literal wkt = (Literal) statement.getObject();

                try {
                    Geometry geometry = WktHelpers.createGeometry(wkt,DEFAULT_CRS);
                    int pos=0;
                    for (Geometry partition: partitions) {
                        if (geometry.getCentroid().coveredBy(partition)) {
                            bucket = pos;
                        }
                        pos++;
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                    throw new RDFHandlerException(e);
                }
            }

            if (statement.getPredicate().equals(RDF.TYPE) && statement.getObject().equals(SfPoint)) {

                for (Statement st: buffer) {
                    writer.handleStatement(st, bucket);
                }
                buffer.clear();
            }
        }
    }

}

