package jp.co.ricoh.advop.cheetahutil.util;

import android.content.Context;
import android.content.SharedPreferences;

/** 
 * String, Integer, Boolean, Float, Long  could be accepted
 * @author lijianwei 
 * 
 */  
public class SharedPreferencesUtils {  
    /** 
     * saved file name 
     */  
    private static final String FILE_NAME = "share_date";  
    private static final String Server_message = "server_message";  
    private static final String Setting_data="setting_data";
      
    /** 
     * save data
     * @param context 
     * @param key 
     * @param object  
     */  
    public static void setParam(Context context , String key, Object object){  
          
        String type = object.getClass().getSimpleName();  
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);  
        SharedPreferences.Editor editor = sp.edit();  
          
        if("String".equals(type)){  
            editor.putString(key, (String)object);  
        }  
        else if("Integer".equals(type)){  
            editor.putInt(key, (Integer)object);  
        }  
        else if("Boolean".equals(type)){  
            editor.putBoolean(key, (Boolean)object);  
        }  
        else if("Float".equals(type)){  
            editor.putFloat(key, (Float)object);  
        }  
        else if("Long".equals(type)){  
            editor.putLong(key, (Long)object);  
        }  
          
        editor.commit();  
    }  
      
      
    /** 
     * get data
     * @param context 
     * @param key 
     * @param defaultObject 
     * @return 
     */  
    public static Object getParam(Context context , String key, Object defaultObject){  
        String type = defaultObject.getClass().getSimpleName();  
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);  
          
        if("String".equals(type)){  
            return sp.getString(key, (String)defaultObject);  
        }  
        else if("Integer".equals(type)){  
            return sp.getInt(key, (Integer)defaultObject);  
        }  
        else if("Boolean".equals(type)){  
            return sp.getBoolean(key, (Boolean)defaultObject);  
        }  
        else if("Float".equals(type)){  
            return sp.getFloat(key, (Float)defaultObject);  
        }  
        else if("Long".equals(type)){  
            return sp.getLong(key, (Long)defaultObject);  
        }  
          
        return null;  
    }  
}  
