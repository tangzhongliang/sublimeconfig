package jp.co.ricoh.advop.idcardscanprint.model;

import java.util.HashMap;
import java.util.Map;

public enum TimeFormat {

    YYYY_MM_DD_HH_MM_SS("YYYYMMDDHHMMSS"),
    DD_MM_YYYY_HH_MM_SS("DDMMYYYYHHMMSS"),
    MM_DD_YYYY_HH_MM_SS("MMDDYYYYHHMMSS");
    
    private String value;
    private TimeFormat(String value) {
        this.value = value;
    }
    
    private static volatile Map<String, TimeFormat> directory = null;
    private static Map<String, TimeFormat> getDirectory() {
        if(directory == null) {
            Map<String, TimeFormat> type = new HashMap<String, TimeFormat>();
            for (TimeFormat timeFormat:values()) {
                type.put(timeFormat.toString(), timeFormat);
            }
            directory = type;
        }
        return directory;
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    public static TimeFormat fromString(String value) {
        return getDirectory().get(value);
    }
}
