package org.semagrow.geotools.partition;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;

import java.io.IOException;

public interface DatasetPartitioner {

    void setResourcePrefix(String resourcePrefix);

    void setGeometryPrefix(String geometryPrefix);

    void addDatasetPartition(String id, Geometry boundary, String oPath) throws IOException;

    void process(String iPath) throws IOException;
}
