package org.semagrow.geotools.partition;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.io.FileReader;
import java.io.IOException;

public class PartitionerMain {

    public static void main(String args[]) throws IOException, ParseException {

        String path = "/tmp/snow-all/snow-all.nt";
        String wDir = "/home/antru/Documents/xearth/geospatial-selector/grid/wkt/";
        String oDir = "/tmp/out/";
        String prfx = "http://earthanalytics.eu/fs/";
        String part = "";

        if (args.length < 5) {
            System.out.println("[BUFFER|SAMEID] [iPath] [prefix] [wktDir] [oDir]");
            //return;
        }
        else {
            part = args[0];
            path = args[1];
            prfx = args[2];
            wDir = args[3];
            oDir = args[4];
        }

        Geometry[] polygons = new Geometry[7];
        WKTReader reader = new WKTReader();

        polygons[0] = reader.read(new FileReader(wDir + "wkt1.txt"));
        polygons[1] = reader.read(new FileReader(wDir + "wkt2.txt"));
        polygons[2] = reader.read(new FileReader(wDir + "wkt3.txt"));
        polygons[3] = reader.read(new FileReader(wDir + "wkt4.txt"));
        polygons[4] = reader.read(new FileReader(wDir + "wkt5.txt"));
        polygons[5] = reader.read(new FileReader(wDir + "wkt6.txt"));
        polygons[6] = reader.read(new FileReader(wDir + "wkt7.txt"));
        //polygons[7] = reader.read(new FileReader(wDir + "wkt8.txt"));
        //polygons[8] = reader.read(new FileReader(wDir + "wkt9.txt"));

        DatasetPartitioner datasetPartitioner;

        if (part.equals("BUFFER")) {
            datasetPartitioner = new BufferedDatasetPartitioner();
        }
        else {
            datasetPartitioner = new SameIdDatasetPartitioner();
        }

        datasetPartitioner.setResourcePrefix(prfx);
        datasetPartitioner.setGeometryPrefix(prfx);

        datasetPartitioner.addDatasetPartition("a", polygons[0], oDir + "output.1.nt");
        datasetPartitioner.addDatasetPartition("2", polygons[1], oDir + "output.2.nt");
        datasetPartitioner.addDatasetPartition("3", polygons[2], oDir + "output.3.nt");
        datasetPartitioner.addDatasetPartition("4", polygons[3], oDir + "output.4.nt");
        datasetPartitioner.addDatasetPartition("5", polygons[4], oDir + "output.5.nt");
        datasetPartitioner.addDatasetPartition("6", polygons[5], oDir + "output.6.nt");
        datasetPartitioner.addDatasetPartition("7", polygons[6], oDir + "output.7.nt");
        //datasetPartitioner.addDatasetPartition("VO", polygons[7], oDir + "output.8.nt");
        //datasetPartitioner.addDatasetPartition("WI", polygons[8], oDir + "output.9.nt");

        datasetPartitioner.process(path);
    }
}
