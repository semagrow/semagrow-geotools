package org.semagrow.geotools;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.semagrow.geotools.partition.GsynthDatasetPartitioner;

import java.io.FileReader;
import java.io.IOException;

public class GsynthPBMain {

    static final String path = "/home/antru/Documents/xearth/pb-experiment/512/";

    static final String iDir = path + "parts/";
    static final String wDir = path + "wkt/";
    static final String tDir = path + "tmp/";
    static final String oDir = path + "out/";

    public static void main(String args[]) throws IOException, ParseException {

        Geometry[] polygons = new Geometry[100];
        WKTReader reader = new WKTReader();

        for (int i=0; i<9; i++) {
            polygons[i] = reader.read(new FileReader(wDir + "wkt.0" + i + ".txt"));
        }
        for (int i=10; i<99; i++) {
            polygons[i] = reader.read(new FileReader(wDir + "wkt." + i + ".txt"));
        }

        //split(polygons, "state");
        //shape(polygons, "state");

        split(polygons, "landOwnership");
        shape(polygons, "landOwnership");

        split(polygons, "pointOfInterest");
        shape(polygons, "pointOfInterest");
    }

    static void split(Geometry[] polygons, String kind) throws IOException {
        GsynthDatasetPartitioner partitioner = new GsynthDatasetPartitioner();
        partitioner.setKind(kind);
        for (int i=0; i<9; i++) {
            String id = "0" + i;
            String filename = kind + ".0" + i + ".nt";
            partitioner.addDatasetPartition(id, polygons[i], tDir + "/" + filename);
        }
        for (int i=10; i<99; i++) {
            String id = "" + i;
            String filename = kind + "." + i + ".nt";
            partitioner.addDatasetPartition(id, polygons[i], tDir + "/" + filename);
        }
        partitioner.process(iDir + kind + ".nt");
    }

    static void shape(Geometry[] polygons, String kind) throws IOException {
        DatasetShaper shaper = new DatasetShaper();
        for (int i=0; i<9; i++) {
            String filename = kind + ".0" + i + ".nt";
            shaper.process(tDir + filename, oDir + filename, polygons[i]);
        }
        for (int i=10; i<99; i++) {
            String filename = kind + "." + i + ".nt";
            shaper.process(tDir + filename, oDir + filename, polygons[i]);
        }
    }
}
