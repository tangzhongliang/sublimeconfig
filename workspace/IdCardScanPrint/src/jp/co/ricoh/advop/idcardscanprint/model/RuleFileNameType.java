package jp.co.ricoh.advop.idcardscanprint.model;


import java.util.HashMap;
import java.util.Map;

public enum RuleFileNameType {

    SETTING_NIL("setting_nil"),
    USER_NAME("user_name"),
    TIME("time"),
    USER_INPUT("user_input");
    
    private String value;
    
    private RuleFileNameType(String value) {
        this.value = value;
    }
    
    private static volatile Map<String, RuleFileNameType> directory = null;
    private static Map<String, RuleFileNameType> getDirectory() {
        if(directory == null) {
            Map<String, RuleFileNameType> type = new HashMap<String, RuleFileNameType>();
            for (RuleFileNameType ruleFileNameType:values()) {
                type.put(ruleFileNameType.toString(), ruleFileNameType);
            }
            directory = type;
        }
        return directory;
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    public static RuleFileNameType fromString(String value) {
        return getDirectory().get(value);
    }
}
