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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LgdPartitioner {

    private final ValueFactory vf = SimpleValueFactory.getInstance();
    private final IRI AS_WKT = vf.createIRI("http://linkedgeodata.org/ontology/asWKT");
    private final IRI DEFAULT_CRS = vf.createIRI("http://www.opengis.net/def/crs/EPSG/4326");
    private String geometryPrefix = "http://linkedgeodata.org/geometry/way";
    private String areaPrefix = "http://linkedgeodata.org/triplify/way";
    private String relationPrefix = "http://linkedgeodata.org/triplify/relation";

    private BoundingBoxHelpers helpers = new BoundingBoxHelpers();

    private String path;
    private MultiRDFFileWriter writer;
    private List<Geometry> partitions;

    private Map<Integer,Integer> idBucketMap = new HashMap<>();

    public LgdPartitioner(String path, MultiRDFFileWriter writer, List<Geometry> partitions) {
        this.path = path;
        this.writer = writer;
        this.partitions = partitions;
    }

    public void partition() throws IOException {

        RDFHandler handler = new LgdRDFHandler();

        RDFParser parser = Rio.createParser(RDFFormat.NTRIPLES);
        parser.getParserConfig().set(BasicParserSettings.VERIFY_DATATYPE_VALUES, false);
        parser.setRDFHandler(handler);
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

    private class LgdRDFHandler extends RDFHandlerBase {

        final private Logger log = LoggerFactory.getLogger(this.getClass());

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
                            idBucketMap.put(getID((IRI) statement.getSubject()), pos);
                            break;
                        }
                        pos++;
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                    throw new RDFHandlerException(e);
                }
            }
            else {
                if (statement.getSubject().toString().startsWith(relationPrefix)) {
                    writer.handleStatement(statement, partitions.size());
                }
                else {
                    int pos = idBucketMap.get(getID((IRI) statement.getSubject()));
                    writer.handleStatement(statement, pos);
                }
            }
        }
    }

}

