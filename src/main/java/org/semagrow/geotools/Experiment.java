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
import java.util.*;

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
        writer.write("Endpoint,Query,ExecTime,WaitTime,Results");

        for (String endpoint: endpoints) {
            runQueries(endpoint, false);
            runQueries(endpoint, true);
        }

        writer.close();
    }

    private void runQueries(String endpoint, boolean thematic) throws IOException {

        String equalsPolygon = "equalsPolygon";
        String containsPoint = "containsPoint";
        String hasLeDistance = "hasLeDistance";
        String withinPolygon = "withinPolygon";

        QueryInfo equalsPolygonQuery = new QueryInfo(endpoint, equalsPolygon, thematic);
        QueryInfo containsPointQuery = new QueryInfo(endpoint, containsPoint, thematic);
        QueryInfo hasLeDistanceQuery = new QueryInfo(endpoint, hasLeDistance, thematic);
        QueryInfo withinPolygonQuery = new QueryInfo(endpoint, withinPolygon, thematic);

        Repository repo = new SPARQLRepository(endpoint);
        repo.init();

        log.info("Using endpoint: " + endpoint);

        for (String g : PropertiesHelpers.getSystemStringProperties(propFile, "INV_POLYGON")) {
            RunInfo info = runQuery(repo, Queries.equalsPolygonQuery(g, thematic));
            writer.write(equalsPolygonQuery.serailize() + "," + info.serailize());
        }
        for (String g : PropertiesHelpers.getSystemStringProperties(propFile, "INNER_POINT")) {
            RunInfo info = runQuery(repo, Queries.containsPointQuery(g, thematic));
            writer.write(containsPointQuery.serailize() + "," + info.serailize());
        }
        for (String g : PropertiesHelpers.getSystemStringProperties(propFile, "LUCAS_POINT")) {
            RunInfo info = runQuery(repo, Queries.hasDistanceQuery(g, thematic));
            writer.write(hasLeDistanceQuery.serailize() + "," + info.serailize());
        }
        for (String g : PropertiesHelpers.getSystemStringProperties(propFile, "AUSTRIA_ADM")) {
            RunInfo info = runQuery(repo, Queries.withinPolygon(g, thematic));
            writer.write(withinPolygonQuery.serailize() + "," + info.serailize());
        }

        repo.shutDown();
    }

    private RunInfo runQuery(Repository repo, String qStr) {

        Long t1, t2, t3;
        int results = 0;

        RepositoryConnection conn = repo.getConnection();

        t1 = System.currentTimeMillis();
        t2 = t1;

        log.info("Executing query: " + qStr.replace("\n"," "));

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

        return new RunInfo(queryExecTime, queryWaitTime, results);
    }

    private class QueryInfo {
        public String endoint;
        public String queryDesc;
        public boolean thematic;

        public QueryInfo(String endoint, String queryDesc, boolean thematic) {
            this.endoint = endoint;
            this.queryDesc = queryDesc;
            this.thematic = thematic;
        }
        public String serailize() {
            return endoint + "," + queryDesc + "," + thematic;
        }
    }

    private class RunInfo {
        public long queryExecTime;
        public long queryWaitTime;
        public int results;

        public RunInfo(long queryExecTime, long queryWaitTime, int results) {
            this.queryExecTime = queryExecTime;
            this.queryWaitTime = queryWaitTime;
            this.results = results;
        }
        public String serailize() {
            return queryWaitTime + "," + queryWaitTime + "," + results;
        }
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
