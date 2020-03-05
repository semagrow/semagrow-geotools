package org.semagrow.geotools.helpers;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class CRS {

    private static final IRI EPSG_4326;

    private static final ValueFactory vf;

    static {
        vf = SimpleValueFactory.getInstance();

        EPSG_4326 = vf.createIRI("http://www.opengis.net/def/crs/EPSG/0/4326");
    }
}
