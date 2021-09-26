package org.semagrow.geotools.partition;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class GsynthDatasetPartitioner {

    final private Logger log = LoggerFactory.getLogger(GsynthDatasetPartitioner.class);

    private ValueFactory vf = SimpleValueFactory.getInstance();
    private IRI DEFAULT_SRID = vf.createIRI("http://www.opengis.net/def/crs/EPSG/0/4326");
    private IRI HAS_GEOMETRY = vf.createIRI("http://www.opengis.net/ont/geosparql#hasGeometry");
    private IRI HAS_TAG = vf.createIRI("http://geographica.di.uoa.gr/ontology/hasTag");

    String base = "http://geographica.di.uoa.gr/generator/";
    String kind = "state/";

    private Set<DatasetPartition> partitions = new HashSet<>();

    public void setKind(String kind) {
        this.kind = kind + "/";
    }

    public void addDatasetPartition(String id, Geometry boundary, String oPath) throws IOException {
        DatasetPartition dp = new DatasetPartition(id, boundary, oPath);
        partitions.add(dp);
    }

    public void process(String iPath) throws IOException {
        RDFHandler handler;
        RDFParser parser;

        handler = new ResourcePartitionerRDFHandler();

        parser = Rio.createParser(RDFFormat.NTRIPLES);
        parser.getParserConfig().set(BasicParserSettings.VERIFY_DATATYPE_VALUES, false);
        parser.setRDFHandler(handler);
        parser.parse(new FileInputStream(iPath), "");

        handler = new DatasetPartitionerRDFHandler();

        parser = Rio.createParser(RDFFormat.NTRIPLES);
        parser.getParserConfig().set(BasicParserSettings.VERIFY_DATATYPE_VALUES, false);
        parser.setRDFHandler(handler);
        parser.parse(new FileInputStream(iPath), "");
    }

    private class ResourcePartitionerRDFHandler extends AbstractRDFHandler {

        @Override
        public void handleStatement(Statement statement) throws RDFHandlerException {
            if (statement.getPredicate().equals(GEO.AS_WKT)) {
                try {
                    Geometry geom = WktHelpers.createGeometry((Literal) statement.getObject(), DEFAULT_SRID);
                    for (DatasetPartition dp: partitions) {
                        if (dp.getBoundary().intersects(geom)) {
                            log.info("{} will be placed in {}", statement.getSubject(), dp.getId());
                            dp.addResourceID(getIDfromGeometryIRI((IRI) statement.getSubject()));
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    throw new RDFHandlerException(e);
                }
            }
        }
    }

    private class DatasetPartitionerRDFHandler extends AbstractRDFHandler {

        @Override
        public void startRDF() throws RDFHandlerException {
            for (DatasetPartition dp: partitions) {
                dp.getWriter().startRDF();
            }
        }

        @Override
        public void handleStatement(Statement statement) throws RDFHandlerException {

            log.info("processing {}", statement.getSubject());

            int IRIid;
            if (statement.getPredicate().equals(GEO.AS_WKT)) {
                IRIid = getIDfromGeometryIRI((IRI) statement.getSubject());
            }
            else {
                IRIid = getIDfromResourceIRI((IRI) statement.getSubject());
            }

            for (DatasetPartition dp: partitions) {
                if (dp.containsResourceID(IRIid)) {
                    String id = dp.getId();
                    Resource s;
                    IRI p;
                    Value o;
                    if (statement.getPredicate().equals(GEO.AS_WKT)) {
                        s = newIRI((IRI) statement.getSubject(), id);
                        p = GEO.AS_WKT;
                        o = statement.getObject();
                    } else if (statement.getPredicate().equals(HAS_GEOMETRY)) {
                        s = newIRI((IRI) statement.getSubject(), id);
                        p = HAS_GEOMETRY;
                        o = newIRI((IRI) statement.getObject(), id);
                    } else if (statement.getPredicate().equals(HAS_TAG)) {
                        s = newIRI((IRI) statement.getSubject(), id);
                        p = HAS_TAG;
                        o = newIRI((IRI) statement.getObject(), id);
                    } else {
                        s = newIRI((IRI) statement.getSubject(), id);
                        p = statement.getPredicate();
                        o = statement.getObject();
                    }
                    dp.getWriter().handleStatement(vf.createStatement(s, p, o));
                }
            }
        }

        @Override
        public void endRDF() throws RDFHandlerException {
            for (DatasetPartition dp: partitions) {
                dp.getWriter().endRDF();
            }
        }
    }

    private int getIDfromGeometryIRI(IRI iri) {
        int i = (base + kind + "geometry/").length();
        String tmp = iri.stringValue().substring(i);
        int j = tmp.indexOf('/');
        String id = tmp.substring(0,j);
        return Integer.parseInt(id);
    }

    private int getIDfromResourceIRI(IRI iri) {
        int i = (base + kind).length();
        String tmp = iri.stringValue().substring(i);
        int j = tmp.indexOf('/');
        String id = tmp.substring(0,j);
        return Integer.parseInt(id);
    }

    private IRI newIRI(IRI old, String id) {
        return vf.createIRI(old.stringValue().replace(base, base + id + "/"));
    }

    private class DatasetPartition {

        private String id;
        private Geometry boundary;
        private RDFWriter writer;
        private Set<Integer> ResourceIDs = new HashSet<>();

        DatasetPartition(String id, Geometry boundary, String oPath) throws IOException {
            this.id = id;
            this.boundary = boundary;
            this.writer = new NTriplesWriter(new FileWriter(oPath));
        }

        public String getId() {
            return id;
        }

        public void addResourceID(int id) {
            ResourceIDs.add(id);
        }

        public boolean containsResourceID(int id) {
            return ResourceIDs.contains(id);
        }

        public Geometry getBoundary() {
            return boundary;
        }

        public RDFWriter getWriter() {
            return writer;
        }
    }
}
