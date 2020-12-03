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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BufferedDatasetPartitioner implements DatasetPartitioner {

    final private Logger log = LoggerFactory.getLogger(BufferedDatasetPartitioner.class);

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

    public void process(String iPath) throws IOException {
        RDFHandler handler = new BufferedDatasetPartitionerRDFHandler();

        RDFParser parser = Rio.createParser(RDFFormat.NTRIPLES);
        parser.getParserConfig().set(BasicParserSettings.VERIFY_DATATYPE_VALUES, false);
        parser.setRDFHandler(handler);
        parser.parse(new FileInputStream(iPath), "");
    }

    private class BufferedDatasetPartitionerRDFHandler extends AbstractRDFHandler {

        @Override
        public void startRDF() throws RDFHandlerException {
            for (DatasetPartition dp: partitions) {
                dp.getWriter().startRDF();
            }
        }

        @Override
        public void handleStatement(Statement statement) throws RDFHandlerException {

            if (statement.getPredicate().equals(GEO.AS_WKT)) {
                try {
                    buffer.add(statement);

                    Literal wkt = (Literal) statement.getObject();
                    Geometry geom = WktHelpers.createGeometry(wkt, DEFAULT_SRID);
                    List<String> ids = new ArrayList<>();

                    for (DatasetPartition dp: partitions) {
                        if (shouldPlace(geom, dp.getBoundary())) {
                            String id = dp.getId();
                            ids.add(id);
                            for (Statement st : buffer) {
                                Resource s;
                                IRI p;
                                Value o;
                                if (st.getPredicate().equals(GEO.AS_WKT)) {
                                    s = newRGeometryIRI((IRI) st.getSubject(), id);
                                    p = GEO.AS_WKT;
                                    o = st.getObject();
                                } else if (st.getPredicate().equals(HAS_GEOMETRY)) {
                                    s = newResourceIRI((IRI) st.getSubject(), id);
                                    p = HAS_GEOMETRY;
                                    o = newRGeometryIRI((IRI) st.getObject(), id);
                                } else {
                                    s = newResourceIRI((IRI) st.getSubject(), id);
                                    p = st.getPredicate();
                                    o = st.getObject();
                                }
                                dp.getWriter().handleStatement(vf.createStatement(s, p, o));
                            }
                        }
                    }

                    log.info("{} was placed in {}", statement.getSubject(), ids);
                    buffer = new ArrayList<>();

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            else {
                buffer.add(statement);
            }
        }

        @Override
        public void endRDF() throws RDFHandlerException {
            for (DatasetPartition dp: partitions) {
                dp.getWriter().endRDF();
            }
        }

        private IRI newResourceIRI(IRI old, String id) {
            return vf.createIRI(old.stringValue().replace(rPrfx, rPrfx + id + "/"));
        }

        private IRI newRGeometryIRI(IRI old, String id) {
            return vf.createIRI(old.stringValue().replace(gPrfx, gPrfx + id + "/"));
        }

        private boolean shouldPlace(Geometry geometry, Geometry partition) {
            return partition.intersects(geometry.buffer(0));
        }
    }

    private class DatasetPartition {

        private String id;
        private Geometry boundary;
        private RDFWriter writer;

        DatasetPartition(String id, Geometry boundary, String oPath) throws IOException {
            this.id = id;
            this.boundary = boundary;
            this.writer = new NTriplesWriter(new FileWriter(oPath));
        }

        public String getId() {
            return id;
        }

        public Geometry getBoundary() {
            return boundary;
        }

        public RDFWriter getWriter() {
            return writer;
        }
    }
}
