package org.semagrow.geotools;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.GEO;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.semagrow.geotools.helpers.MultiRDFFileWriter;
import org.semagrow.geotools.helpers.PropertiesHelpers;
import org.semagrow.geotools.helpers.WktHelpers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AdmDatasetPartitioner extends DatasetPartitioner {

    public void initialize(MultiRDFFileWriter writer) throws ParseException {
        crs = vf.createIRI("http://www.opengis.net/def/crs/EPSG/0/4326");

        partitions = new ArrayList<>();
        List<String> strings = PropertiesHelpers.getSystemStringProperties("/geomAsWKT.properties", "AUSTRIA_ADM");

        for (String str: strings) {
            Literal wkt = vf.createLiteral("<http://www.opengis.net/def/crs/EPSG/0/4326> " + str, GEO.AS_WKT);
            Geometry geometry = WktHelpers.createGeometry(wkt, crs);
            partitions.add(geometry);
        }

        this.writer = writer;
    }

    public static void main(String args[]) throws IOException, ParseException {
        String usage = "USAGE:" +
                "\n\t java " + DatasetPartitioner.class + " [inputFile] [outputDir]" +
                "\n\t [inputFile] - path of .nt file" +
                "\n\t [outputDir] - path of directory to place the output .nt files";

        if (args.length < 2) {
            throw new IllegalArgumentException(usage);
        }

        String inputPath = args[0];
        String outputDir = args[1];

        AdmDatasetPartitioner partitioner = new AdmDatasetPartitioner();
        MultiRDFFileWriter writer = new MultiRDFFileWriter(outputDir, "dump", 10);
        partitioner.initialize(writer);

        writer.startRDF();

        RDFParser parser = Rio.createParser(RDFFormat.NTRIPLES);
        parser.getParserConfig().set(BasicParserSettings.VERIFY_DATATYPE_VALUES, false);
        parser.setRDFHandler(partitioner);
        parser.parse(new FileInputStream(inputPath), "");

        writer.endRDF();
    }
}
