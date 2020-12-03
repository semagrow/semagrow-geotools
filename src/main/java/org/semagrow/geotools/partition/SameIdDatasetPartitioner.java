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
import java.util.*;

public class SameIdDatasetPartitioner implements DatasetPartitioner {

    final private Logger log = LoggerFactory.getLogger(SameIdDatasetPartitioner.class);

    private String rPrfx = "http://";
    private String gPrfx = "http://";

    private List<Statement> buffer = new ArrayList<>();
    private Set<DatasetPartition> partitions = new HashSet<>();

    private ValueFactory vf = SimpleValueFactory.getInstance();
    private IRI DEFAULT_SRID = vf.createIRI("http://www.opengis.net/def/crs/EPSG/0/4326");
    private IRI HAS_GEOMETRY = vf.createIRI("http://www.opengis.net/ont/geosparql#hasGeometry");

    public void setResourcePrefix(String rPrfx) {
        this.rPrfx = rPrfx;
    }

    public void setGeometryPrefix(String gPrfx) {
        this.gPrfx = gPrfx;
    }

    public void addDatasetPartition(String id, Geometry boundary, String oPath) throws IOException {
        DatasetPartition dp = new DatasetPartition(id, boundary, oPath);
        partitions.add(dp);
    }

    @Override
    public void process(String iPath) throws IOException {
        RDFHandler handler;
        RDFParser parser;

        handler = new SameIdResourcePartitionerRDFHandler();

        parser = Rio.createParser(RDFFormat.NTRIPLES);
        parser.getParserConfig().set(BasicParserSettings.VERIFY_DATATYPE_VALUES, false);
        parser.setRDFHandler(handler);
        parser.parse(new FileInputStream(iPath), "");

        handler = new ADMDatasetPartitionerRDFHandler();

        parser = Rio.createParser(RDFFormat.NTRIPLES);
        parser.getParserConfig().set(BasicParserSettings.VERIFY_DATATYPE_VALUES, false);
        parser.setRDFHandler(handler);
        parser.parse(new FileInputStream(iPath), "");
    }

    private class SameIdResourcePartitionerRDFHandler extends AbstractRDFHandler {

        @Override
        public void handleStatement(Statement statement) throws RDFHandlerException {
            if (statement.getPredicate().equals(GEO.AS_WKT)) {
                try {
                    Geometry geom = WktHelpers.createGeometry((Literal) statement.getObject(), DEFAULT_SRID);
                    for (DatasetPartition dp: partitions) {
                        if (dp.getBoundary().intersects(geom)) {
                            log.info("{} will be placed in {}", statement.getSubject(), dp.getId());
                            dp.addResourceID(getResourceID((IRI) statement.getSubject()));
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    throw new RDFHandlerException(e);
                }
            }
        }
    }

    private class ADMDatasetPartitionerRDFHandler extends AbstractRDFHandler {

        @Override
        public void startRDF() throws RDFHandlerException {
            for (DatasetPartition dp: partitions) {
                dp.getWriter().startRDF();
            }
        }

        @Override
        public void handleStatement(Statement statement) throws RDFHandlerException {

            log.info("processing {}", statement.getSubject());

            for (DatasetPartition dp: partitions) {
                if (dp.containsResourceID(getResourceID((IRI) statement.getSubject()))) {
                    String id = dp.getId();
                    Resource s;
                    IRI p;
                    Value o;
                    if (statement.getPredicate().equals(GEO.AS_WKT)) {
                        s = newRGeometryIRI((IRI) statement.getSubject(), id);
                        p = GEO.AS_WKT;
                        o = statement.getObject();
                    } else if (statement.getPredicate().equals(HAS_GEOMETRY)) {
                        s = newResourceIRI((IRI) statement.getSubject(), id);
                        p = HAS_GEOMETRY;
                        o = newRGeometryIRI((IRI) statement.getObject(), id);
                    } else {
                        s = newResourceIRI((IRI) statement.getSubject(), id);
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

    private int getResourceID(IRI iri) {
        int i = iri.stringValue().lastIndexOf("_");
        String id = iri.stringValue().substring(i+1);
        return Integer.parseInt(id);
    }

    private IRI newResourceIRI(IRI old, String id) {
        return vf.createIRI(old.stringValue().replace(rPrfx, rPrfx + id + "/"));
    }

    private IRI newRGeometryIRI(IRI old, String id) {
        return vf.createIRI(old.stringValue().replace(gPrfx, gPrfx + id + "/"));
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
