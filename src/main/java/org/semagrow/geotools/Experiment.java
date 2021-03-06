package org.semagrow.geotools;

import org.semagrow.geotools.helpers.PropertiesHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;

public class Experiment {

    final private Logger log = LoggerFactory.getLogger(Experiment.class);

    private String outputDirPath;
    private final String propFile = "/geomAsWKT.properties";


    public void setOutputDirPath(String outputDirPath) {
        this.outputDirPath = outputDirPath;
    }


    public void writeQueries() throws IOException {

        String equalsPolygon = "equalsPolygon";
        String containsPoint = "containsPoint";
        String hasLeDistance = "hasLeDistance";
        String withinPolygon = "withinPolygon";

        String thematic = "Thematic";

        int i;

        i=0;
        for (String g : PropertiesHelpers.getSystemStringProperties(propFile, "INV_POLYGON")) {
            writeQuery("1" + equalsPolygon + i, Queries.equalsPolygonQuery(g, false));
            writeQuery("1" + equalsPolygon + thematic + i, Queries.equalsPolygonQuery(g, true));
            i++;
        }

        i=0;
        for (String g : PropertiesHelpers.getSystemStringProperties(propFile, "INNER_POINT")) {
            writeQuery("2" + containsPoint + i, Queries.containsPointQuery(g, false));
            writeQuery("2" + containsPoint + thematic + i, Queries.containsPointQuery(g, true));
            i++;
        }

        i=0;
        for (String g : PropertiesHelpers.getSystemStringProperties(propFile, "LUCAS_POINT")) {
            writeQuery("3" + hasLeDistance + i, Queries.hasDistanceQuery(g, false));
            writeQuery("3" + hasLeDistance + thematic + i, Queries.hasDistanceQuery(g, true));
            i++;
        }

        i=0;
        for (String g : PropertiesHelpers.getSystemStringProperties(propFile, "AUSTRIA_ADM")) {
            writeQuery("4" + withinPolygon + i, Queries.withinPolygonQuery(g, false));
            writeQuery("4" + withinPolygon + thematic + i, Queries.withinPolygonQuery(g, true));
            i++;
        }
    }

    private void writeQuery(String nStr, String qStr) throws IOException {

        FileWriter writer = new FileWriter(outputDirPath + "/" + nStr);
        writer.write(qStr);
        writer.close();
    }

    public static void main(String[] args) throws IOException {

        String usage = "USAGE:" +
                "\n\t java " + Experiment.class + " [outputPath]" +
                "\n\t [outputPath] - path to write the queries";

        if (args.length < 1) {
            throw new IllegalArgumentException(usage);
        }

        Experiment experiment = new Experiment();
        experiment.setOutputDirPath(args[0]);
        experiment.writeQueries();
    }

}
