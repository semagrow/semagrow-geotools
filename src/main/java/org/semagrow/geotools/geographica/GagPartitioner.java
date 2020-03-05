package org.semagrow.geotools.geographica;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
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
import java.util.List;

public class GagPartitioner {

    private final ValueFactory vf = SimpleValueFactory.getInstance();
    private final IRI AS_WKT = vf.createIRI("http://geo.linkedopendata.gr/gag/ontology/asWKT");
    private final IRI DEFAULT_CRS = vf.createIRI("http://www.opengis.net/def/crs/EPSG/4326");
    private String geometryPrefix = "http://geo.linkedopendata.gr/gag/geometry/";
    private String areaPrefix = "http://geo.linkedopendata.gr/gag/id/";

    private BoundingBoxHelpers helpers = new BoundingBoxHelpers();

    private String path;
    private MultiRDFFileWriter writer;
    private List<Geometry> partitions;

    private int[] id = new int[9326];

    public GagPartitioner(String path, MultiRDFFileWriter writer, List<Geometry> partitions) {
        this.path = path;
        this.writer = writer;
        this.partitions = partitions;
    }

    public void partition() throws IOException {

        RDFHandler handler1 = new GagWKTRDFHandler();
        RDFHandler handler2 = new GagBucketRDFHandler();

        RDFParser parser = Rio.createParser(RDFFormat.NTRIPLES);
        parser.getParserConfig().set(BasicParserSettings.VERIFY_DATATYPE_VALUES, false);

        parser.setRDFHandler(handler1);
        parser.parse(new FileInputStream(path), "");

        parser.setRDFHandler(handler2);
        parser.parse(new FileInputStream(path), "");
    }

    private int getID(IRI uri) {
        String str = uri.toString();

        if (str.startsWith(geometryPrefix)) {
            String strInt = str.substring(geometryPrefix.length());
            return Integer.parseInt(strInt);
        }
        if (str.startsWith(areaPrefix)) {
            String strInt = str.substring(areaPrefix.length());
            return Integer.parseInt(strInt);
        }
        return -1;
    }

    private class GagWKTRDFHandler extends RDFHandlerBase {

        @Override
        public void handleStatement(Statement statement) throws RDFHandlerException {

            if (statement.getPredicate().equals(RDFS.SUBPROPERTYOF) || statement.getObject().equals(OWL.OBJECTPROPERTY) || statement.getObject().equals(OWL.DATATYPEPROPERTY)) {
                return;
            }

            if (statement.getPredicate().equals(AS_WKT)) {

                Literal wkt = (Literal) statement.getObject();

                try {
                    Geometry geometry = WktHelpers.createGeometry(wkt,DEFAULT_CRS);
                    int pos=0;
                    for (Geometry partition: partitions) {
                        if (geometry.getCentroid().coveredBy(partition)) {
                            writer.handleStatement(statement, pos);
                            id[getID((IRI) statement.getSubject())] = pos;
                            break;
                        }
                        pos++;
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                    throw new RDFHandlerException(e);
                }
            }
        }
    }

    private class GagBucketRDFHandler extends RDFHandlerBase {

        @Override
        public void handleStatement(Statement statement) throws RDFHandlerException {

            if (statement.getPredicate().equals(RDFS.SUBPROPERTYOF) || statement.getObject().equals(OWL.OBJECTPROPERTY) || statement.getObject().equals(OWL.DATATYPEPROPERTY)) {
                return;
            }
            int bucket = getID((IRI) statement.getSubject());
            if (bucket > 10000) {
                writer.handleStatement(statement, partitions.size());
            }
            else {
                writer.handleStatement(statement, id[bucket]);
            }
        }
    }

}
