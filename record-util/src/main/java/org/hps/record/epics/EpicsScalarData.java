package org.hps.record.epics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.lcsim.event.EventHeader;
import org.lcsim.event.GenericObject;

/**
 * This is an API for reading and writing EPICS scalar data to LCIO events,
 * as well as parsing the scalar data from a CDATA section of an EVIO string
 * data bank.  The {@link #read(EventHeader)} method should be used to create
 * one of these objects from an LCIO event.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
*/
public final class EpicsScalarData {
    
    // Used in collection parameter map as name of the key list.
    public static final String EPICS_SCALAR_NAMES = "EPICS_SCALAR_NAMES";
    
    // Default collection name in the LCIO event.
    public static final String DEFAULT_COLLECTION_NAME = "EpicsScalarData";
    
    // Dummy collection parameter maps to try and make LCIO happy.
    static final Map<String, int[]> DUMMY_INT_MAP = new HashMap<String, int[]>();    
    static final Map<String, float[]> DUMMY_FLOAT_MAP = new HashMap<String, float[]>();
    
    // The map of scalar keys to values.
    private Map<String, Double> dataMap = new LinkedHashMap<String, Double>();
    
    // EPICS key descriptions from
    // https://confluence.slac.stanford.edu/display/hpsg/EVIO+Data+Format           
    private final static Map<String, String> DESCRIPTIONS = new HashMap<String, String>();
    static {
        DESCRIPTIONS.put("MBSY2C_energy",       "Beam energy according to Hall B BSY dipole string");
        DESCRIPTIONS.put("PSPECIRBCK",          "Pair Spectrometer Current Readback");
        DESCRIPTIONS.put("HPS:LS450_2:FIELD",   "Frascati probe field");
        DESCRIPTIONS.put("HPS:LS450_1:FIELD",   "Pair Spectrometer probe field");
        DESCRIPTIONS.put("MTIRBCK",             "Frascati Current Readback");
        DESCRIPTIONS.put("VCG2C21 2C21",        "Vacuum gauge pressure");
        DESCRIPTIONS.put("VCG2C21A",            "2C21A Vacuum gauge pressure");
        DESCRIPTIONS.put("VCG2C24A",            "2C24A Vacuum gauge pressure");
        DESCRIPTIONS.put("VCG2H00A",            "2H00 Vacuum gauge pressure");
        DESCRIPTIONS.put("VCG2H01A",            "2H01 Vacuum gauge pressure");
        DESCRIPTIONS.put("VCG2H02A",            "2H02 Vacuum gauge pressure");
        DESCRIPTIONS.put("scaler_calc1",        "Faraday cup current");
        DESCRIPTIONS.put("scalerS12b",          "HPS-Left beam halo count");
        DESCRIPTIONS.put("scalerS13b",          "HPS-Right beam halo count");
        DESCRIPTIONS.put("scalerS14b",          "HPS-Top beam halo count");
        DESCRIPTIONS.put("scalerS15b",          "HPS-SC beam halo count");
        DESCRIPTIONS.put("hallb_IPM2C21A_XPOS", "Beam position X at 2C21");
        DESCRIPTIONS.put("hallb_IPM2C21A_YPOS", "Beam position Y at 2C21");
        DESCRIPTIONS.put("hallb_IPM2C21A_CUR",  "Current at 2C21");
        DESCRIPTIONS.put("hallb_IPM2C24A_XPOS", "Beam position X at 2C24");
        DESCRIPTIONS.put("hallb_IPM2C24A_YPOS", "Beam position Y at 2C24");
        DESCRIPTIONS.put("hallb_IPM2C24A_CUR",  "Current at 2C24");
        DESCRIPTIONS.put("hallb_IPM2H00_XPOS",  "Beam position X at 2H00");
        DESCRIPTIONS.put("hallb_IPM2H00_YPOS",  "Beam position Y at 2H00");
        DESCRIPTIONS.put("hallb_IPM2H00_CUR",   "Current at 2H00");
        DESCRIPTIONS.put("hallb_IPM2H02_YPOS",  "Beam position X at 2H02");
        DESCRIPTIONS.put("hallb_IPM2H02_XPOS",  "Beam position Y at 2H02");
    };
       
    /**
     * Write this object's data into a GenericObject collection in the LCIO event using 
     * the default collection name.
     * @param event The LCIO event.
     */
    public void write(EventHeader event) {
        write(event, DEFAULT_COLLECTION_NAME);
    }
    
    /**
     * <p>
     * Read data into this object from an LCIO event using the default collection name.
     * <p>
     * This is the primary method for users to read the EPICS data into their Drivers
     * in the {@link org.lcsim.util.Driver#process(EventHeader)} method.
     * @param event The LCIO event. 
     * @return The EPICS data from the event.
     */
    public static EpicsScalarData read(EventHeader event) {
        if (event.hasCollection(GenericObject.class, EpicsScalarData.DEFAULT_COLLECTION_NAME)) {
            return read(event, DEFAULT_COLLECTION_NAME);
        } else {
            return null;
        }
    }
    
    /**
     * Get a double value from the key.
     * @return The value from the key.
     */
    public Double getValue(String name) {
        return dataMap.get(name);
    }
    
    /**
     * Get the description of a named EPICS scalar.
     * @param name The name of the scalar.
     */
    public static String getDescription(String name) {
        return DESCRIPTIONS.get(name);
    }
    
    /**
     * Get the static list of default, available EPICs scalar names.
     * <p>
     * This could be different than the names which were actually 
     * written into the collection header.  For this, use the method
     * {@link #getUsedNames()}.
     * 
     * @return The list of default EPICS scalar names. 
     */
    public static Set<String> getDefaultNames() {
        return DESCRIPTIONS.keySet();
    }
    
    /**
     * Get the list of used EPICS scalars in this object.
     * <p>
     * This could potentially be different than the list of
     * default names from {@link #getDefaultNames()} but it
     * will usually be the same.
     * 
     * @return The list of used EPICS scalar names.
     */
    public Set<String> getUsedNames() {
        return dataMap.keySet();
    }
    
    /**
     * Write this object into an LCIO event under the given collection name.
     * @param event The LCIO event.
     * @param collectionName The name of the collection in the event.
     */
    void write(EventHeader event, String collectionName) {
        List<GenericObject> collection = new ArrayList<GenericObject>();
        EpicsGenericObject object = toGenericObject();
        collection.add(object);
        Map<String, String[]> stringMap = new HashMap<String, String[]>();
        stringMap.put(EPICS_SCALAR_NAMES, object.keys);
        event.put(collectionName, collection, GenericObject.class, 0, DUMMY_INT_MAP, DUMMY_FLOAT_MAP, stringMap);
    }    
      
    /**
     * Parse a raw data string from the EVIO data bank and
     * turn it into a list of keys and values within this object. 
     * @param rawData The raw data in the form of a string.
     */
    void fromString(String rawData) {
        String lines[] = rawData.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.length() == 0) {
                continue;
            }
            String[] data = trimmed.split("  ");
            Double value = Double.parseDouble(data[0]);
            String key = data[1];
            dataMap.put(key, value);
        }
    }

    /**
     * Convert this object into a {@link org.lcsim.event.GenericObject} 
     * that can be written into an LCIO collection.
     * @return The GenericObject representing this data.
     */
    EpicsGenericObject toGenericObject() {
        EpicsGenericObject newObject = new EpicsGenericObject();
        newObject.keys = new String[dataMap.size()];
        newObject.values = new double[dataMap.size()];
        int index = 0;
        for (String key : dataMap.keySet()) {
            newObject.keys[index] = key;
            newObject.values[index] = dataMap.get(key);
            index++;
        }
        return newObject;
    }

    /**
     * Given a list of names, read the double values from the 
     * {@link org.lcsim.event.GenericObject} into the data map
     * of this object.
     * @param object The GenericObject with the scalar values.
     * @param names The list of names.
     */
    void fromGenericObject(GenericObject object, String[] names) {
        for (int index = 0; index < names.length; index++) {                      
            dataMap.put(names[index], object.getDoubleVal(index)); 
        }
    }
        
    /**
     * Read data into this object from a collection in the LCIO event
     * with the given collection name.
     * @param event The LCIO event.
     * @param collectionName The collection name.
     * @return The EPICS data from the LCIO event.
     */
    static EpicsScalarData read(EventHeader event, String collectionName) {
        List<GenericObject> collection = event.get(GenericObject.class, collectionName);
        @SuppressWarnings("rawtypes")
        Map stringMap = event.getMetaData(collection).getStringParameters();
        String[] keys = (String[]) stringMap.get(EPICS_SCALAR_NAMES);
        EpicsScalarData data = new EpicsScalarData();
        data.fromGenericObject(collection.get(0), keys);
        return data;
    }
        
    /**
     * Convert this object to a string.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Entry<String, Double> entry :  dataMap.entrySet()) {
            sb.append(entry.getKey() + " " + entry.getValue() + '\n');
        }
        return sb.toString();
    }
}