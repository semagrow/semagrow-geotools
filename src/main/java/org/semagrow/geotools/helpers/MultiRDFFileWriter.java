package org.semagrow.geotools.helpers;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.eclipse.rdf4j.rio.ntriples.NTriplesWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MultiRDFFileWriter extends AbstractRDFHandler {

    private List<RDFWriter> writers = new ArrayList<>();

    public MultiRDFFileWriter(String dir, String prefix, int n) throws IOException {
        for (int i=0; i<n; i++) {
            RDFWriter writer = new NTriplesWriter(new FileWriter(dir + "/" + prefix + "." + i + ".nt"));
            writers.add(writer);
        }
    }

    public int getBuckets() {
        return writers.size();
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        for (RDFWriter writer: writers) {
            writer.startRDF();
        }
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        for (RDFWriter writer: writers) {
            writer.endRDF();
        }
    }

    public void handleStatement(Statement st, int i) {
        writers.get(i).handleStatement(st);
    }
}
