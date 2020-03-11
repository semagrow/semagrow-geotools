package org.semagrow.geotools;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.GEO;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.ntriples.NTriplesUtil;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.semagrow.geotools.helpers.BoundingBoxHelpers;
import org.semagrow.geotools.helpers.MultiRDFFileWriter;
import org.semagrow.geotools.helpers.WktHelpers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DatasetPartitioner extends AbstractRDFHandler {

    protected final static ValueFactory vf;

    static {
        vf = SimpleValueFactory.getInstance();
    }

    protected IRI crs = null;
    private IRI HAS_GEOMETRY = vf.createIRI(GEO.NAMESPACE, "hasGeometry");

    protected List<Geometry> partitions;
    protected MultiRDFFileWriter writer;

    private Map<Resource,Statement> waitingForWKT = new HashMap<>();
    private Map<Resource,Integer> waitingForHasGeom = new HashMap<>();


    public void initialize(String wktString, int Xs, int Ys, MultiRDFFileWriter writer) throws ParseException {

        Literal wkt = NTriplesUtil.parseLiteral(wktString, vf);
        crs = WktHelpers.getCRS(wkt);
        Geometry mbb = WktHelpers.createGeometry(wkt, crs);
        partitions = BoundingBoxHelpers.partitionBoundingBox(mbb, Xs, Ys);

        this.writer = writer;
        assert writer.getBuckets() >= partitions.size();
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        super.endRDF();
    }

    @Override
    public void handleStatement(Statement statement) throws RDFHandlerException {

        if (statement.getPredicate().equals(GEO.AS_WKT)) {

            Literal wkt = (Literal) statement.getObject();
            try {
                Geometry geometry = WktHelpers.createGeometry(wkt, crs);
                int bucket = 0;
                for (Geometry partition: partitions) {
                    if (isMemberOfPartition(geometry, partition)) {
                        writer.handleStatement(statement, bucket);

                        if (waitingForWKT.containsKey(statement.getSubject())) {
                            writer.handleStatement(waitingForWKT.get(statement.getSubject()), bucket);
                            waitingForWKT.remove(statement.getSubject());
                        }
                        else {
                            waitingForHasGeom.put(statement.getSubject(), bucket);
                        }
                        return;
                    }
                    bucket++;
                }

            } catch (ParseException e) {
                e.printStackTrace();
                throw new RDFHandlerException(e);
            }
        }

        if (statement.getPredicate().equals(HAS_GEOMETRY)) {
            if (waitingForHasGeom.containsKey(statement.getObject())) {
                writer.handleStatement(statement, waitingForHasGeom.get(statement.getObject()));
                waitingForHasGeom.remove(statement.getObject());
            }
            else {
                waitingForWKT.put((Resource) statement.getObject(), statement);
            }
            return;
        }

        writer.handleStatement(statement, partitions.size());
    }

    protected boolean isMemberOfPartition(Geometry geometry, Geometry partition) {
        return geometry.getCentroid().coveredBy(partition);
    }

    public static void main(String args[]) throws IOException, ParseException {
        String usage = "USAGE:" +
                "\n\t java " + DatasetPartitioner.class + " [inputDir] [Xs] [Ys] [outputDir]" +
                "\n\t [inputFile] - path of .nt file" +
                "\n\t [mbbFile] - path of the file that contains the MBB of the .nt file" +
                "\n\t [Xs] - mumber of partitions of the X axis" +
                "\n\t [Ys] - mumber of partitions of the Y axis" +
                "\n\t [outputDir] - path of directory to place the output .nt files";

        if (args.length < 5) {
            throw new IllegalArgumentException(usage);
        }

        String inputPath = args[0];
        String mbbPath = args[1];
        int Xs = Integer.parseInt(args[2]);
        int Ys = Integer.parseInt(args[3]);
        String outputDir = args[4];

        BufferedReader bufferedReader = new BufferedReader(new FileReader(mbbPath));
        String mbbAsWKT = bufferedReader.readLine();
        bufferedReader.close();

        DatasetPartitioner partitioner = new DatasetPartitioner();
        MultiRDFFileWriter writer = new MultiRDFFileWriter(outputDir, "dump", (Xs*Ys)+1);
        partitioner.initialize(mbbAsWKT, Xs, Ys, writer);

        writer.startRDF();

        RDFParser parser = Rio.createParser(RDFFormat.NTRIPLES);
        parser.getParserConfig().set(BasicParserSettings.VERIFY_DATATYPE_VALUES, false);
        parser.setRDFHandler(partitioner);
        parser.parse(new FileInputStream(inputPath), "");

        writer.endRDF();
    }
}
