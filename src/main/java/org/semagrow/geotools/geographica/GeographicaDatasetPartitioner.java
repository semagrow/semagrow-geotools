package org.semagrow.geotools.geographica;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.semagrow.geotools.helpers.BoundingBoxHelpers;
import org.semagrow.geotools.helpers.MultiRDFFileWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class GeographicaDatasetPartitioner {

    final private Logger log = LoggerFactory.getLogger(this.getClass());

    private String inputDir;
    private int Xs;
    private int Ys;
    private String outputDir;

    public GeographicaDatasetPartitioner(String inputDir, int Xs, int Ys, String outputDir) {
        this.inputDir = inputDir;
        this.Xs = Xs;
        this.Ys = Ys;
        this.outputDir = outputDir;
    }

    public void partition() throws IOException {

        Coordinate[] p = new Coordinate[5];

        p[0] = new Coordinate(19.368838534149194, 34.790210040827226);
        p[1] = new Coordinate(19.368838534149194, 42.133222);
        p[2] = new Coordinate(29.66667, 42.133222);
        p[3] = new Coordinate(29.66667, 34.790210040827226);
        p[4] = new Coordinate(19.368838534149194, 34.790210040827226);

        GeometryFactory gf = new GeometryFactory();
        BoundingBoxHelpers helpers = new BoundingBoxHelpers();

        Geometry mbb = gf.createPolygon(p);

        List<Geometry> partitions = BoundingBoxHelpers.partitionBoundingBox(mbb, Xs, Ys);

        MultiRDFFileWriter writer = new MultiRDFFileWriter(outputDir, "geographica", partitions.size()+1);

        writer.startRDF();

        log.info("Partitioning Corine Dataset...");
        new CorinePartitioner(inputDir + "/corine.nt", writer, partitions).partition();
        log.info("Partitioning GAG Dataset...");
        new GagPartitioner(inputDir + "/gag.nt", writer, partitions).partition();
        log.info("Partitioning GeoNames Dataset...");
        new GeonamesPartitioner(inputDir + "/geonames.nt", writer, partitions).partition();
        log.info("Partitioning LinkedGeoData Dataset...");
        new LgdPartitioner(inputDir + "/linkedgeodata.nt", writer, partitions).partition();

        writer.endRDF();
    }

    public static void main(String[] args) throws IOException {

        String usage = "USAGE:" +
                "\n\t java " + GeographicaDatasetPartitioner.class + " [inputDir] [Xs] [Ys] [outputDir]" +
                "\n\t [inputDir] - path of directory that contains corine.nt, gag.nt, geonames.nt, linkedgeodata.nt" +
                "\n\t [Xs] - mumber of partitions of the X axis" +
                "\n\t [Ys] - mumber of partitions of the Y axis" +
                "\n\t [outputDir] - path of directory to place the output .nt files";

        if (args.length < 4) {
            throw new IllegalArgumentException(usage);
        }

        String inputDir = args[0];
        int Xs = Integer.parseInt(args[1]);
        int Ys = Integer.parseInt(args[2]);
        String outputDir = args[3];

        new GeographicaDatasetPartitioner(inputDir, Xs, Ys, outputDir).partition();
    }
}
