package org.semagrow.geotools.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class PropertiesHelpers {

    /** Return array from properties file. Array must be defined as "key.0=value0", "key.1=value1", ... */

    public static List<String> getSystemStringProperties(String propPath, String key) {

        List<String> result = new ArrayList<>();

        try (InputStream input = PropertiesHelpers.class.getResourceAsStream(propPath)) {

            Properties prop = new Properties();
            prop.load(input);

            String value;
            for(int i = 0; (value = prop.getProperty(key + "." + i)) != null; i++) {
                result.add(value);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return result;
    }
}
