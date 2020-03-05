package org.semagrow.geotools;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.GEO;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.semagrow.geotools.helpers.BoundingBoxHelpers;
import org.semagrow.geotools.helpers.WktHelpers;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class BoundingBoxExtractor extends AbstractRDFHandler {

    private final GeometryFactory gf = new GeometryFactory();

    private Geometry mbb = gf.createPolygon();
    private IRI crs = null;

    @Override
    public void handleStatement(Statement statement) throws RDFHandlerException {

        Value o = statement.getObject();

        if (o instanceof Literal && (((Literal) o).getDatatype() != null)
                && ((Literal) o).getDatatype().equals(GEO.WKT_LITERAL)) {

            try {
                if (crs == null) {
                    crs = WktHelpers.getCRS((Literal) o);
                }

                assert crs == WktHelpers.getCRS((Literal) o);
                Geometry g = WktHelpers.createGeometry((Literal) o, crs);
                mbb = BoundingBoxHelpers.expandBoundingBox(mbb, g);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public String getBoundingBoxAsWKT() {
        return WktHelpers.createWKTLiteral(mbb, crs).toString();
    }

    public static void main(String args[]) throws IOException {

        String usage = "USAGE:" +
                "\n\t java " + BoundingBoxExtractor.class + " [inputFile] [outputFile]" +
                "\n\t [inputFile] - path of .nt file" +
                "\n\t [outputFile] - path of file to write the result";

        if (args.length < 2) {
            throw new IllegalArgumentException(usage);
        }

        String inPath = args[0];
        String outPath = args[1];

        BoundingBoxExtractor extractor = new BoundingBoxExtractor();

        RDFParser parser = Rio.createParser(RDFFormat.NTRIPLES);
        parser.getParserConfig().set(BasicParserSettings.VERIFY_DATATYPE_VALUES, false);
        parser.setRDFHandler(extractor);
        parser.parse(new FileInputStream(inPath), "");

        FileWriter writer = new FileWriter(outPath);
        writer.write(extractor.getBoundingBoxAsWKT());
        writer.close();
    }
}
