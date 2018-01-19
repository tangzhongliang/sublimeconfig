
package jp.co.ricoh.advop.idcardscanprint.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.ricoh.advop.cheetahutil.util.CUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.application.CheetahApplication;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.model.GlobalDataManager.FragmentState;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;
import jp.co.ricoh.advop.idcardscanprint.model.RuleFileNameType;
import jp.co.ricoh.advop.idcardscanprint.model.TimeFormat;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.TrayName;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.status.GetTraysResponseBody;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.status.GetTraysResponseBody.InputTrays;


public class Util {
    private static final String TAG = Util.class.getSimpleName();
    private static final CheetahApplication application = CHolder.instance().getApplication();
    
    private static String fileName = "";
    
    public static void setFileName(String name) {
        fileName = name;
    }

    public static String getFileName() {
        if (CUtil.isStringEmpty(fileName)) {
            fileName = getDefaultTime();
        } else {
            String separator = PreferencesUtil.getInstance().getSeparatorChar();
            List<Map<String, Object>> fileNameArray = PreferencesUtil.getInstance().getRuleFileName();
            if (fileNameArray == null) {
                fileNameArray = Const.DefaultFileNameList;
            }

            fileName = Util.replaceFileName(separator, fileNameArray, Util.getTime());
            //Util.setFileName(fileName);
        }
        LogC.d("get fileName is" + fileName);
        return fileName;
    }

    public static String getDefaultDest() {
        String dest = CHolder.instance().getMachineStatusData().getDestination();
        // Log.v("DEBUG", "getDefaultDest " + dest);
        if (dest.equals("north_america")) {
            return "NA";
        } else {
            return dest;
        }
    }

    private static Map<TrayName, InputTrays> GetTrayList() {
        final HashMap<TrayName, InputTrays> map = new HashMap<TrayName, InputTrays>();
       
        GetTraysResponseBody trayinfo = CHolder.instance().getMachineStatusData().getTrayStatus();
         if (trayinfo != null) {
            for (InputTrays tray : trayinfo.getInputTrays()) {
                        TrayName paperTray = TrayName.fromString(tray.getName());
                        map.put(paperTray, tray);
            }

         }      

        return map;
    }

    static public boolean isSupportAutoTray() {
        Map<TrayName, InputTrays> trayMap;
        trayMap = GetTrayList();

        if (trayMap != null) {
            if (trayMap.containsKey(TrayName.AUTO)) {
                return true;
            }
        }

        return false;
    }


    static private TrayName getSizeTray(String paperSize) {
        Map<TrayName, InputTrays> trayMap;
        trayMap = GetTrayList();
        TrayName tray = null;

        if (trayMap != null) {
            if (trayMap.containsKey(TrayName.AUTO)) {
                return TrayName.AUTO;
            }
        }
        
        for (java.util.Map.Entry<TrayName, InputTrays> entry : trayMap.entrySet()) {
            InputTrays value = entry.getValue();
            if (value != null && value.getPaperSize() != null && value.getPaperSize().indexOf(paperSize) != -1) {
                tray = entry.getKey();
                if (value.getPaperRemain() > 0) {
                    tray = entry.getKey();
                    break;
                }
            }
        }

        return tray;

    }

    static public TrayName getCurrentTray() {

        TrayName tray = TrayName.TRAY1;
        if (getDefaultDest().equalsIgnoreCase("na")) {
            tray = getSizeTray("na_letter");
        } else {
            tray = getSizeTray("iso_a4");
        }

        return tray;
    }

    /**
     * Check MFP support tray
     * 
     * @param model : the {@link MaterialCopyModel}
     */
    static public int initializeSupportedAms(FragmentState state) {
        int backImageResId = -1;
        LogC.d("current model name is " + CHolder.instance().getMachineStatusData().getModelNameInfo());
        switch (state) {
            case ScanSide1:
                
                if (CHolder.instance().getMachineStatusData().getModelNameInfo().equalsIgnoreCase(Const.LEFFE_C1B_MODEL)) {
                    // leffe-c1b image
                    backImageResId = R.drawable.id1_guide1_lef;
                } else {
                    // if (getDefaultAMS().equalsIgnoreCase("LEF")) {
                    if (getDefaultDest().equalsIgnoreCase("na")) {
                        backImageResId = R.drawable.id1_guide1_letter;
                    } else {
                        backImageResId = R.drawable.id1_guide1;
                    }
                }

                break;
            case ScanSide2:
                // if (getDefaultAMS().equalsIgnoreCase("LEF")) {
                if (CHolder.instance().getMachineStatusData().getModelNameInfo().equalsIgnoreCase(Const.LEFFE_C1B_MODEL)) {
                    // leffe-c1b image
                    backImageResId = R.drawable.id1_guide2_lef;
                } else {
                    if (getDefaultDest().equalsIgnoreCase("na")) {
                        backImageResId = R.drawable.id1_guide2_letter;
                    } else {
                        backImageResId = R.drawable.id1_guide2;
                    }
                }

                break;
            default:
                break;
        }

        return backImageResId;
    }

    public static int[] getTime() {

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int date = c.get(Calendar.DATE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);

        int[] time = new int[] {
                year, month, date, hour, minute, second
        };

        return time;
    }

    public static String timeFormat(int time) {
        if (time > 0 && time < 10) {
            return "0" + time;
        } else {
            return "" + time;
        }
    }
    
    public static String getTime(String format,int[] time2) {
        String time = "";

        if (format.equals(TimeFormat.YYYY_MM_DD_HH_MM_SS.toString())) {
//            for (int i = 0; i < time2.length; i++) {
//                time = time + Util.timeFormat(time2[i]);
//            }
            time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
            
        } else if (format.equals(TimeFormat.DD_MM_YYYY_HH_MM_SS.toString())) {
//            time = Util.timeFormat(time2[2]) + Util.timeFormat(time2[1])
//                    + Util.timeFormat(time2[0]) + Util.timeFormat(time2[3])
//                    + Util.timeFormat(time2[4]) + Util.timeFormat(time2[5]);
            time = new SimpleDateFormat("ddMMyyyyHHmmss", Locale.getDefault()).format(new Date());
        } else if (format.equals(TimeFormat.MM_DD_YYYY_HH_MM_SS.toString())) {
//            time = Util.timeFormat(time2[1]) + Util.timeFormat(time2[2])
//                    + Util.timeFormat(time2[0]) + Util.timeFormat(time2[3])
//                    + Util.timeFormat(time2[4]) + Util.timeFormat(time2[5]);
            time = new SimpleDateFormat("MMddyyyyHHmmss", Locale.getDefault()).format(new Date());
        }

        return time;
    }

    public static String getDefaultTime() {

//        String time = "";
//        int[] time2 = Util.getTime();
//        for (int i = 0; i < time2.length; i++) {
//            time = time + Util.timeFormat(time2[i]);
//        }
        
        String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        
        return time;
    }
    
    public static String gainRuleFileName(String str) {
        if (RuleFileNameType.USER_NAME.toString().equals(str)) {
            return application.getString(Const.USER_NAME);
        } else if (RuleFileNameType.TIME.toString().equals(str)) {
            return application.getString(Const.TIME);
        } else if (RuleFileNameType.USER_INPUT.toString().equals(str)) {
            return application.getString(Const.INPUTWORD);
        }
        return application.getString(Const.SETTING_NOTHING);
    }
    
    public static String gainFileName(String type, String value, int[] time) {
        String userN = CHolder.instance().getApplication().getSystemStateMonitor().getLoginUserName();
        
        if(RuleFileNameType.USER_NAME.toString().equals(type)) {
            return userN;
        } else if(RuleFileNameType.TIME.toString().equals(type)){
             return Util.getTime(value,time);
        }else if(RuleFileNameType.USER_INPUT.toString().equals(type)){
            return value;
        }else {
            return null;
        }
        
    }
    
    public static String replaceFileName(String c, List<Map<String, Object>> fileNameArray, int[] time) {
        String fnStr = "";
//        String rfnStr = "";
        for (int i = 0; i < fileNameArray.size(); i++) {
            Map<String, Object> map = fileNameArray.get(i);
            String type = null;
            if(map.get(Const.KEY_TYPE) != null){
                type = map.get(Const.KEY_TYPE).toString();
            }
            String value = null;
            if(map.get(Const.KEY_VALUE) != null){
                value = map.get(Const.KEY_VALUE).toString();
            }
            //String value = (String) map.get(Const.KEY_VALUE);

            String s = Util.gainFileName(type, value, time);
            if (s != null) {
                fnStr = fnStr + c + s;
            }

//            String str = Util.gainRuleFileName(value);
//            if (str != null && !str.equals(application.getString(Const.SETTING_NOTHING))) {
//                rfnStr = rfnStr + c + "[" + str + "]";
//            }
        }
        if (fnStr.length() > 0) {
            fnStr = fnStr.substring(1, fnStr.length());
            return fnStr;
        }

//        if (rfnStr.length() > 0) {
//            rfnStr = rfnStr.substring(1, rfnStr.length());
//        }
        return null;
    }
    
    public static boolean checkedContent(String item, String range) {
        LogC.d("range :" + range);
        Pattern pattern = Pattern.compile(range);
        Matcher matcher = pattern.matcher(item);        
        return matcher.matches();
    }
    
    public static String delIllegalWord(String src, String illegalWord, String range) {
        
        String dString = "";
        if(src != null) {
            for (int i = 0; i < illegalWord.length() && !checkedContent(src, range); i++) {
                String c = illegalWord.substring(i, i+1);//.charAt(i);
                //Log.d("illegal word", c);
                src = src.replace(c, "");            
            }
            dString = src;
        }
        return dString;       
    }
    
    public static boolean fileIsExists(String fileName) {             
        try{                    
            File f=new File(fileName);      
            if(!f.exists()){                          
                return false;               
            }                    
        }catch (Exception e) {    
            LogC.e("open file is error happen!");
            return false;           
        }            
        return true;     
    }
    
    
    public static String getFileNameNoSuffix(String pathAndName) {
        int start = pathAndName.lastIndexOf("/");
        int end = pathAndName.lastIndexOf(".");
        if (start != -1) {
            return pathAndName.substring(start + 1, end);
        } else {
            LogC.e("Get file name fail.");
            return null;
        }

    }
    
    public static String getSuffix(String filename) {
        int dix = filename.lastIndexOf('.');
        if (dix < 0) {
            return "";
        } else {
            return filename.substring(dix + 1);
        }
    }
        
}
