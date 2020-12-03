package org.semagrow.geotools.partition;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.GEO;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.ntriples.NTriplesWriter;
import org.locationtech.jts.io.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ADMDatasetPartitioner {
    final private Logger log = LoggerFactory.getLogger(ADMDatasetPartitioner.class);

    private String rPrfx = "http://";
    private String gPrfx = "http://";

    private List<Statement> buffer = new ArrayList<>();
    private Set<DatasetPartition> partitions = new HashSet<>();

    private ValueFactory vf = SimpleValueFactory.getInstance();
    private IRI REL_PROPERTY = null;
    private IRI HAS_GEOMETRY = vf.createIRI("http://www.opengis.net/ont/geosparql#hasGeometry");

    public void setResourcePrefix(String rPrfx) {
        this.rPrfx = rPrfx;
    }

    public void setGeometryPrefix(String gPrfx) {
        this.gPrfx = gPrfx;
    }

    public void setPropertyOfInterest(String propertyOfInterest) {
        REL_PROPERTY = vf.createIRI(propertyOfInterest);
    }

    public void addDatasetPartition(String id, String boundary, String oPath) throws IOException {
        DatasetPartition dp = new DatasetPartition(id, boundary, oPath);
        partitions.add(dp);
    }

    public void process(String iPath) throws IOException {
        RDFHandler handler;
        RDFParser parser;

        handler = new ADMIdPartitionerRDFHandler();

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

    private class ADMIdPartitionerRDFHandler extends AbstractRDFHandler {

        @Override
        public void handleStatement(Statement statement) throws RDFHandlerException {
            if (statement.getPredicate().equals(REL_PROPERTY)) {
                for (DatasetPartition dp: partitions) {
                    if (statement.getObject().equals(dp.getADMUnit())) {
                        log.info("{} will be placed in {}", statement.getSubject(), dp.getId());
                        dp.addADMUnit(getADMUnitID((IRI) statement.getSubject()));
                    }
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
                if (dp.containsADMUnit(getADMUnitID((IRI) statement.getSubject()))) {
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

    private int getADMUnitID(IRI iri) {
        int i = iri.stringValue().lastIndexOf("/");
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
        private Literal ADMUnit;
        private RDFWriter writer;
        private Set<Integer> ADMUnits = new HashSet<>();

        DatasetPartition(String id, String ADMUnit, String oPath) throws IOException {
            this.id = id;
            this.ADMUnit = vf.createLiteral(ADMUnit, XMLSchema.STRING);
            this.writer = new NTriplesWriter(new FileWriter(oPath));
        }

        public String getId() {
            return id;
        }

        public void addADMUnit(int id) {
            ADMUnits.add(id);
        }

        public boolean containsADMUnit(int id) {
            return ADMUnits.contains(id);
        }

        public Literal getADMUnit() {
            return ADMUnit;
        }

        public RDFWriter getWriter() {
            return writer;
        }
    }

    public static void main(String args[]) throws IOException, ParseException {

        String oDir = "/tmp/out/";

        ADMDatasetPartitioner datasetPartitioner = new ADMDatasetPartitioner();

        datasetPartitioner.setResourcePrefix("http://gadm.org/AUT/");
        datasetPartitioner.setGeometryPrefix("http://gadm.org/AUT/");

        datasetPartitioner.setPropertyOfInterest("http://gadm.org/ontology#has_NAME_1");

        datasetPartitioner.addDatasetPartition("BU", "Burgenland", oDir + "output.1.nt");
        datasetPartitioner.addDatasetPartition("KA", "K\u00C3\u00A4rnten", oDir + "output.2.nt");
        datasetPartitioner.addDatasetPartition("NO", "Nieder\u00C3\u00B6sterreich", oDir + "output.3.nt");
        datasetPartitioner.addDatasetPartition("OO", "Ober\u00C3\u00B6sterreich", oDir + "output.4.nt");
        datasetPartitioner.addDatasetPartition("SZ", "Salzburg", oDir + "output.5.nt");
        datasetPartitioner.addDatasetPartition("ST", "Steiermark", oDir + "output.6.nt");
        datasetPartitioner.addDatasetPartition("TR", "Tirol", oDir + "output.7.nt");
        datasetPartitioner.addDatasetPartition("VO", "Vorarlberg", oDir + "output.8.nt");
        datasetPartitioner.addDatasetPartition("WI", "Wien", oDir + "output.9.nt");

        datasetPartitioner.process("/tmp/input.nt");
    }
}
