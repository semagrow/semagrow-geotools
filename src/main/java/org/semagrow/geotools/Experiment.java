package org.semagrow.geotools;

import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.semagrow.geotools.helpers.PropertiesHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Experiment {

    final private Logger log = LoggerFactory.getLogger(Experiment.class);

    private Set<String> endpoints;
    private String writerPath;

    private final String propFile = "/geomAsWKT.properties";
    private FileWriter writer;

    public void setEndpoints(Set<String> endpoints) {
        this.endpoints = endpoints;
    }

    public void setWriterPath(String writerPath) {
        this.writerPath = writerPath;
    }

    public void run() throws IOException {

        writer = new FileWriter(writerPath);

        for (String endpoint: endpoints) {
            runQueries(endpoint, false);
        }
        for (String endpoint: endpoints) {
            runQueries(endpoint, true);
        }

        writer.close();
    }

    private void runQueries(String endpoint, boolean thematic) throws IOException {

        Repository repo = new SPARQLRepository(endpoint);
        repo.init();

        log.info("Using endpoint: " + endpoint);

        int id;

        id=0;

        for (String g: PropertiesHelpers.getSystemStringProperties(propFile, "INV_POLYGON")) {
            String qID = endpoint + "," + "equalsPolygon" + id + "," + thematic;
            runQuery(repo, Queries.equalsPolygonQuery(g, thematic), qID);
            id++;
            break;
        }

        id=0;

        for (String g: PropertiesHelpers.getSystemStringProperties(propFile, "INNER_POINT")) {
            String qID = endpoint + "," + "containsPoint" + id + "," + thematic;
            runQuery(repo, Queries.containsPointQuery(g, thematic), qID);
            id++;
            break;
        }

        id=0;

        for (String g: PropertiesHelpers.getSystemStringProperties(propFile, "LUCAS_POINT")) {
            String qID = endpoint + "," + "hasDistance" + id + "," + thematic;
            runQuery(repo, Queries.hasDistanceQuery(g, thematic), qID);
            id++;
            break;
        }

        id=0;

        for (String g: PropertiesHelpers.getSystemStringProperties(propFile, "AUSTRIA_ADM")) {
            String qID = endpoint + "," + "withinPolygon" + id + "," + thematic;
            runQuery(repo, Queries.withinPolygon(g, thematic), qID);
            id++;
            break;
        }

        repo.shutDown();
    }

    private void runQuery(Repository repo, String qStr, String idStr) throws IOException {

        Long t1, t2, t3;
        int results = 0;

        RepositoryConnection conn = repo.getConnection();

        t1 = System.currentTimeMillis();
        t2 = t1;

        log.info("Executing query: " + qStr);

        TupleQuery query = conn.prepareTupleQuery(qStr);
        TupleQueryResult result = query.evaluate();

        while (result.hasNext()) {
            if (results == 0) {
                t2 = System.currentTimeMillis();
            }
            log.info("Found " + result.next());
            results++;
        }

        t3 = System.currentTimeMillis();
        conn.close();

        Long queryExecTime = t3 - t1;
        Long queryWaitTime = t2 - t1;

        log.info("Query Execution Time: " + queryExecTime + " ms");
        log.info("Query Waiting Time: " + queryWaitTime + " ms");
        log.info("Found " + results + " results.");

        writer.write( idStr + "," + queryExecTime + "," + queryWaitTime + "," + results + "\n");
    }

    private static Set<String> fileToSetOfStrings(String path) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        String line;
        Set<String> set = new HashSet<>();

        while ((line = bufferedReader.readLine()) != null) {
            set.add(line);
        }
        bufferedReader.close();

        return set;
    }

    public static void main(String[] args) throws IOException {

        String usage = "USAGE:" +
                "\n\t java " + Experiment.class + " [inputFile] [outputPath]" +
                "\n\t [endpointsFile] - path of file that contains the endpoints" +
                "\n\t [outputDir] - path to write results in .csv";

        if (args.length < 2) {
            throw new IllegalArgumentException(usage);
        }

        Experiment experiment = new Experiment();
        experiment.setEndpoints(fileToSetOfStrings(args[0]));
        experiment.setWriterPath(args[1]);
        experiment.run();
    }

}
