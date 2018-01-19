
package jp.co.ricoh.advop.idcardscanprint.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.print.PrintSettingDataHolder;
import jp.co.ricoh.advop.cheetahutil.util.EncryptUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.scan.ScanSettingDataHolder;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.Copies;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PrintColor;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.ScanColor;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.GenericJsonDecoder;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.json.EncodedException;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.json.JsonUtils;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.Entry;
import jp.co.ricoh.advop.idcardscanprint.util.Const;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreferencesUtil {

    private static final String TAG = PreferencesUtil.class.getSimpleName();

    private static final String DES_KEY = "IdCardEncrypt";

    private static final String KEY_DESTINATIONS_SETTINGS = "destinationsSetting";
    private static final String KEY_SMTP_SERVER_SETTING = "smtpServerSetting";
    /**
     * the key of scan setting data
     */
    private static final String KEY_SCAN_SETTING = "scanSetting";
    private static final String KEY_SCAN_COLOR_SETTING = "scanColorSetting";
    private static final String KEY_SCAN_FILE_TYPE_SETTING = "scanFileTypeSetting";

    /**
     * the key of print setting data
     */
    private static final String KEY_PRINT_SETTING = "printSetting";
    private static final String KEY_PRINT_COLOR_SETTING = "printColorSetting";
    private static final String KEY_PRINT_COUNT_SETTING = "printCountSetting";

    /**
     * the key of subject setting data
     */
    private static final String KEY_SUBJECT = "SUBJECT";

    /**
     * the key about the rule of the file name
     */
    private static final String KEY_RULE_FILE_NAME_SETTING = "ruleFileNameSetting";

    /**
     * the key about separator char
     */
    private static final String KEY_SEPARATOR_CHAR = "keySeparatorChar";
    /**
     * the key of sender data
     */
    private static final String KEY_ENTRY_SENDER_SELECT = "senderData";
    /**
     * the key of domain
     */
    private static final String KEY_DOMAIN = "domain";

    private static PreferencesUtil ourInstance;
    private SharedPreferences preferences;
    private Editor editor;

    private Map<String, Object> allPreferences;

    private ScanSettingDataHolder scanSettingDataHolder;
    // private ScanDataSetting scanDataSetting;
    // private SettingItemData scanColor;
    // private SettingItemData scanFileType;
    private PrintSettingDataHolder printSettingDataHolder;
    // private PrintDataSetting printDataSetting;
    // private SettingItemData printColor;
    // private Copies printCopies;

//    private SmtpServerSetting smtpServerSetting;

    public static PreferencesUtil getInstance() {

        if (ourInstance == null) {
            ourInstance = new PreferencesUtil(CHolder.instance().getApplication());
        }

        return ourInstance;
    }

    private PreferencesUtil(Context context) {

        scanSettingDataHolder = CHolder.instance().getScanManager().getScanSettingDataHolder();
        printSettingDataHolder = CHolder.instance().getPrintManager().getPrintSettingDataHolder();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();

        this.allPreferences = new HashMap<String, Object>();
        
        fillAllKeys();
    }
    
    public void clear() {
        this.allPreferences = new HashMap<String, Object>();
    }
    
    public void fillAllKeys() {
        if (!preferences.contains(KEY_DESTINATIONS_SETTINGS)) {
            editor.putString(KEY_DESTINATIONS_SETTINGS, null);
        }

        if (!preferences.contains(KEY_SCAN_SETTING)) {
            editor.putString(KEY_SCAN_SETTING, null);
        }

        if (!preferences.contains(KEY_PRINT_SETTING)) {
            editor.putString(KEY_PRINT_SETTING, null);
        }

        if (!preferences.contains(KEY_SUBJECT)) {
            editor.putString(KEY_SUBJECT, null);
        }

        if (!preferences.contains(KEY_DOMAIN)) {
            editor.putString(KEY_DOMAIN, null);
        }

        if (!preferences.contains(KEY_SMTP_SERVER_SETTING)) {
            setSmtpServer(new SmtpServerSetting().getSmtpMap());
        }

        if (!preferences.contains(KEY_RULE_FILE_NAME_SETTING)) {
            editor.putString(KEY_RULE_FILE_NAME_SETTING, null);
        }

        if (!preferences.contains(KEY_SEPARATOR_CHAR)) {
            editor.putString(KEY_SEPARATOR_CHAR, ".");
        }

        if (!preferences.contains(KEY_ENTRY_SENDER_SELECT)) {
            editor.putString(KEY_ENTRY_SENDER_SELECT, null);
        }
        
        editor.commit();
    }

    public String exportUnencrypted() {
        Map<String, ?> allData = preferences.getAll();
        allData.remove(KEY_DESTINATIONS_SETTINGS);

        for (Map.Entry<String, ?> entry : allData.entrySet()) {
            LogC.d("exportUnencrypted " + entry.getKey() + ":" + entry.getValue());
        }
        
        String json = null;
        try {
            json = JsonUtils.getEncoder().encode(allData);
            return json;
        } catch (EncodedException e) {
            LogC.e("Error in encoding preferences!");
            return null;
        }
    }

    public boolean importUnencrypted(String json) {
        LogC.d("importUnencrypted", json);
        try {
            allPreferences.clear();

            String destJson = preferences.getString(KEY_DESTINATIONS_SETTINGS, null);
            editor.clear();
            editor.putString(KEY_DESTINATIONS_SETTINGS, destJson);
            
            Map<String, ?> allData = GenericJsonDecoder.decodeToMap(json);
            for (Map.Entry<String, ?> entry : allData.entrySet()) {
                String temp = (String)entry.getValue(); // TODO editor.put int bool will failed
                LogC.d("importUnencrypted " + entry.getKey() + ":" + temp);
                editor.putString(entry.getKey(), temp);
            }
            editor.commit();
            return true;
        } catch (Exception e) {
            LogC.e("importUnencrypted", e);
            return false;
        }
    }

    public String exportEncrypted() {
        LogC.i("exportEncrypted");
        Map<String, String> encryptedData = new HashMap<String, String>();
        String destinations = preferences.getString(KEY_DESTINATIONS_SETTINGS, null);
        encryptedData.put(KEY_DESTINATIONS_SETTINGS, destinations);
        
//        for (Map.Entry<String, ?> entry : encryptedData.entrySet()) {
//            LogC.d("exportUnencrypted " + entry.getKey() + ":" + entry.getValue());
//        }
        
        String json = null;
        try {
            json = JsonUtils.getEncoder().encode(encryptedData);
            return json;
        } catch (EncodedException e) {
            LogC.e("Error in encoding preferences!");
            return null;
        }
    }

    public boolean importEncrypted(String json) {
        LogC.i("importEncrypted");
//        LogC.d("importEncrypted all", json);
        try {
            allPreferences.clear();
            
            Map<String, ?> allData = GenericJsonDecoder.decodeToMap(json);
            for (Map.Entry<String, ?> entry : allData.entrySet()) {
                String temp = (String)entry.getValue();
//                LogC.d("importEncrypted " + entry.getKey() + ":" + temp);
                editor.putString(entry.getKey(), temp);
            }
            editor.commit();
            return true;
        } catch (Exception e) {
            LogC.e("importEncrypted", e);
            return false;
        }
    }

    public void setDestinations(ArrayList<Entry> entryList) {

        for (Entry entry : entryList) {
            if (entry != null && entry.getFolderAuthData() != null
                    && entry.getFolderAuthData().getLoginPassword() != null) {
                String password = entry.getFolderAuthData().getLoginPassword();
                String encrypt = EncryptUtil.encryptByDES(DES_KEY, password);
                entry.getFolderAuthData().setLoginPassword(encrypt);
            }
        }

        // JSONArray jsonArray = new JSONArray(entryList);
        // String jsonString = jsonArray.toString();
        String jsonString = null;
        try {
            jsonString = JsonUtils.getEncoder().encode(entryList);
        } catch (EncodedException e) {
            LogC.e("setDestinations", e);
        }

        editor.putString(KEY_DESTINATIONS_SETTINGS, jsonString);
        editor.commit();

        for (Entry entry : entryList) {
            if (entry != null && entry.getFolderAuthData() != null
                    && entry.getFolderAuthData().getLoginPassword() != null) {
                String encrypt = entry.getFolderAuthData().getLoginPassword();
                String password = EncryptUtil.decryptByDES(DES_KEY, encrypt);
                entry.getFolderAuthData().setLoginPassword(password);
            }
        }
        allPreferences.put(KEY_DESTINATIONS_SETTINGS, entryList);
    }

    public ArrayList<Entry> getDestinations() {
        ArrayList<Entry> entryList = (ArrayList<Entry>) this.allPreferences
                .get(KEY_DESTINATIONS_SETTINGS);
        if (entryList != null) {
            return entryList;
        }

        String jsonString = preferences.getString(KEY_DESTINATIONS_SETTINGS, null);
        //LogC.d("getDestinations", jsonString);
        try {
            
            entryList = new ArrayList<Entry>();
            if(jsonString == null) {
                return null;
            }
            List<Map<String, Object>> list = GenericJsonDecoder.decodeToList(jsonString);
            for (Map<String, Object> map : list) {
                if (map == null) {
                    entryList.add(null);
                    continue;
                }
                Entry entry = new Entry(map);
                if (entry != null && entry.getFolderAuthData() != null
                        && entry.getFolderAuthData().getLoginPassword() != null) {
                    String encrypt = entry.getFolderAuthData().getLoginPassword();
                    String password = EncryptUtil.decryptByDES(DES_KEY, encrypt);
                    entry.getFolderAuthData().setLoginPassword(password);
                }
                entryList.add(entry);
            }
            allPreferences.put(KEY_DESTINATIONS_SETTINGS, entryList);
            return entryList;
        } catch (Exception e) {
            LogC.e("getDestinations", e);
            return null;
        }
    }

    /**
     * ***********************************************************scan data
     * setting
     * 
     * @return
     */
    public ScanDataSetting getScanDataSetting() {
        ScanDataSetting scanDataSetting = (ScanDataSetting) allPreferences.get(KEY_SCAN_SETTING);
        if (scanDataSetting == null) {
            String jsonString = preferences.getString(KEY_SCAN_SETTING, null);
            if (jsonString != null && jsonString.length() > 0) {
                try {
                    Map<String, String> map = GenericJsonDecoder.decodeToMap(jsonString);
                    ScanColor scanColor = ScanColor.fromString(map.get(KEY_SCAN_COLOR_SETTING));
                    String scanFileType = map.get(KEY_SCAN_FILE_TYPE_SETTING);

                    return new ScanDataSetting(scanColor, scanFileType);
                } catch (Exception e) {
                    LogC.e(TAG, " gain ScanDataSetting happen exception");
                    return null;
                }
            }
        }

        return scanDataSetting;
    }

    public void setScanDataSetting(ScanDataSetting scanDataSetting) {
        allPreferences.put(KEY_SCAN_SETTING, scanDataSetting);

        String jsonString = null;
        ScanColor scanColor = scanDataSetting.getScanColor();
        String scanFileType = scanDataSetting.getScanFileType();

        Map<String, String> map = new HashMap<String, String>();
        map.put(KEY_SCAN_COLOR_SETTING, scanColor.getValue().toString());
        map.put(KEY_SCAN_FILE_TYPE_SETTING, scanFileType);

        try {
            jsonString = JsonUtils.getEncoder().encode(map);
        } catch (EncodedException e) {
            LogC.e(TAG, " save ScanDataSetting fail");
        }

        editor.putString(KEY_SCAN_SETTING, jsonString);
        editor.commit();
    }

    public SettingItemData getSelectedColorValue() {
        ScanDataSetting scanDataSetting = getScanDataSetting();
        if (scanDataSetting != null) {
            return scanSettingDataHolder.getColorFromID(scanSettingDataHolder
                    .getColorFromEnum(scanDataSetting.getScanColor()));
        } else {
            return scanSettingDataHolder.getColorFromID(scanSettingDataHolder
                    .getDefaultSupportedColor());
        }

    }

    public SettingItemData getSelectedFileFormatValue() {
        ScanDataSetting scanDataSetting = getScanDataSetting();
        if (scanDataSetting != null) {
            return scanSettingDataHolder.getFileFormatFromID(scanSettingDataHolder
                    .getFileFormatFromString(scanDataSetting.getScanFileType()));
        } else {
            return scanSettingDataHolder.getFileFormatFromID(scanSettingDataHolder
                    .getDefaultSupportedFileTyp());
        }

    }

    /**
     * ***********************************************************print data
     * setting
     * 
     * @return
     */
    public PrintDataSetting getPrintDataSetting() {
        PrintDataSetting printDataSetting = (PrintDataSetting) allPreferences
                .get(KEY_PRINT_SETTING);
        if (printDataSetting == null) {

            String jsonString = preferences.getString(KEY_PRINT_SETTING, null);
            if (jsonString != null && jsonString.length() > 0) {
                try {
                    Map<String, String> map = GenericJsonDecoder.decodeToMap(jsonString);

                    PrintColor printColor = PrintColor.fromString(map.get(KEY_PRINT_COLOR_SETTING));
                    Integer printCount = Integer.valueOf(map.get(KEY_PRINT_COUNT_SETTING));
                    return new PrintDataSetting(printColor, printCount);
                } catch (Exception e) {
                    LogC.e(TAG, " gain PrintDataSetting happen exception");
                    return null;
                }
            }
        }
        return printDataSetting;
    }

    public void setPrintDataSetting(PrintDataSetting printDataSetting) {
        allPreferences.put(KEY_PRINT_SETTING, printDataSetting);

        String jsonString = null;
        PrintColor printColor = printDataSetting.getPrintColor();
        Integer printCount = printDataSetting.getPrintCount();

        Map<String, String> map = new HashMap<String, String>();
        map.put(KEY_PRINT_COLOR_SETTING, printColor.getValue().toString());
        map.put(KEY_PRINT_COUNT_SETTING, String.valueOf(printCount));

        try {
            jsonString = JsonUtils.getEncoder().encode(map);
        } catch (EncodedException e) {
            LogC.e(TAG, " save PrintDataSetting fail");
        }

        editor.putString(KEY_PRINT_SETTING, jsonString);
        editor.commit();
    }

    /**
     * 現在の印刷カラー設定値を取得します。 Get the current print color setting value.
     * 
     * @return
     */
    public SettingItemData getSelectedPrintColorValue() {

        PrintDataSetting printDataSetting = getPrintDataSetting();
        if (printDataSetting != null) {
            return printSettingDataHolder.getSelectedPrintColorValue(printSettingDataHolder
                    .getColorFromEnum(printDataSetting.getPrintColor()));
        } else {
            return printSettingDataHolder.getSelectedPrintColorValue(printSettingDataHolder
                    .getDefaultSupportedColor());
        }

    }

    public Copies getSelectedPrintPageValue() {

        PrintDataSetting printDataSetting = getPrintDataSetting();
        if (printDataSetting != null) {
            return printSettingDataHolder.getSelectedPrintPageValue(printDataSetting
                    .getPrintCount());
        } else {
            return printSettingDataHolder.getSelectedPrintPageValue(1);
        }

    }

    /**
     * subject setting
     * 
     * @return
     */
    public String getSubject() {

        String fileName = (String) allPreferences.get(KEY_SUBJECT);
        if (fileName == null) {
            fileName = preferences.getString(KEY_SUBJECT, null);
        }
        return fileName;
    }

    public void setSubject(String subject) {
        allPreferences.put(KEY_SUBJECT, subject);
        editor.putString(KEY_SUBJECT, subject);
        editor.commit();
    }

    public String getDomain() {
        String domain = (String) allPreferences.get(KEY_DOMAIN);
        if (domain == null) {
            domain = preferences.getString(KEY_DOMAIN, "");
        }
        return domain;
    }

    public void setDomain(String domain) {
        allPreferences.put(KEY_DOMAIN, domain);
        editor.putString(KEY_DOMAIN, domain);
        editor.commit();
    }

    public Map<String, String> getSmtpServer() {
        LogC.d("-----get smtp server from all preference!--------------");
        Map<String, String> entryList = (Map<String, String>) this.allPreferences.get(KEY_SMTP_SERVER_SETTING);
        if (entryList != null) {
            //LogC.d("get smtp server from all preference!" + entryList.get(SmtpServerSetting.KEY_PASSWORD));
            Map<String, String> dataMap = new HashMap<String, String>();
            dataMap.putAll(entryList);
//            String encrypt = dataMap.get(SmtpServerSetting.KEY_PASSWORD);
//            if (encrypt != null) {
//                String password = EncryptUtil.decryptByDES(DES_KEY, encrypt);
//                dataMap.put(SmtpServerSetting.KEY_PASSWORD, password);
                //LogC.d("getSmtpServer from allPreferences:" + dataMap.get(SmtpServerSetting.KEY_PASSWORD));
//            }
            return dataMap;
        }

        String jsonString = preferences.getString(KEY_SMTP_SERVER_SETTING, null);
        if (jsonString != null) {
            try {
                Map<String, String> list = GenericJsonDecoder.decodeToMap(jsonString);
                String encrypt = list.get(SmtpServerSetting.KEY_PASSWORD);
                if (list != null && encrypt != null) {
                    String password = EncryptUtil.decryptByDES(DES_KEY, encrypt);
                    list.put(SmtpServerSetting.KEY_PASSWORD, password);
                    allPreferences.put(KEY_SMTP_SERVER_SETTING, list);
                    LogC.d("getSmtpServer from file");
                }
                
                return list;
            } catch (Exception e) {
                LogC.e("", e);
            }
        }
        return new SmtpServerSetting().getSmtpMap();

    }

    public void setSmtpServer(Map<String, String> entryMap) {
        Map<String, String> dataMap = new HashMap<String, String>();
        
        if (entryMap != null) {
            dataMap.putAll(entryMap);
            this.allPreferences.put(KEY_SMTP_SERVER_SETTING, dataMap);
            String password = entryMap.get(SmtpServerSetting.KEY_PASSWORD);// .getFolderAuthData().getLoginPassword();
            if (password != null) {//
                String encrypt = EncryptUtil.encryptByDES(DES_KEY, password);
                entryMap.put(SmtpServerSetting.KEY_PASSWORD, encrypt);
                LogC.d("encrypt password");
            }
            
        }

        //this.allPreferences.put(KEY_SMTP_SERVER_SETTING, entryMap);
        
        String jsonString = null;
        try {
            jsonString = JsonUtils.getEncoder().encode(entryMap);
        } catch (EncodedException e) {
            LogC.e("", e);
        }

        editor.putString(KEY_SMTP_SERVER_SETTING, jsonString);
        editor.commit();
    }

    public ArrayList<Map<String, Object>> getRuleFileName() {

        ArrayList<Map<String, Object>> fileNameList = (ArrayList<Map<String, Object>>) allPreferences.get(KEY_RULE_FILE_NAME_SETTING);
        if (fileNameList == null) {
            String jsonStr = preferences.getString(KEY_RULE_FILE_NAME_SETTING, null);
            if (jsonStr != null && jsonStr.length() > 0) {
                try {
                    fileNameList = new ArrayList<Map<String, Object>>(); 
                    List<Map<String, String>> list = GenericJsonDecoder.decodeToList(jsonStr);
                    for (int i = 0; i < list.size(); i++) {
                        Map<String, String> map1 = list.get(i);

                        Map<String, Object> map2 = new HashMap<String, Object>();
                        map2.put(Const.KEY_TYPE, RuleFileNameType.fromString(map1.get(Const.KEY_TYPE)));
                        map2.put(Const.KEY_VALUE, map1.get(Const.KEY_VALUE));
                        fileNameList.add(map2);
                    }

                } catch (Exception e) {
                    LogC.e("", e);
                    return null;
                }
            } else {

                Map<String, Object> map1 = new HashMap<String, Object>();
                map1.put(Const.KEY_TYPE, null);
                map1.put(Const.KEY_VALUE, null);

                fileNameList = new ArrayList<Map<String, Object>>();
                fileNameList.add(map1);
                fileNameList.add(map1);
                fileNameList.add(map1);
                fileNameList.add(map1);
                fileNameList.add(map1);
                fileNameList.add(map1);
            }
        }
        
        ArrayList<Map<String, Object>> fileRuleList = new ArrayList<Map<String, Object>>();
        
        if(fileNameList != null) {
            
            for (int i = 0; i < fileNameList.size(); i++) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.putAll(fileNameList.get(i));
                fileRuleList.add(map);
            }            
            
        }
        return fileRuleList;      
        
        
    }

    public void setRuleFileName(List<Map<String, Object>> fileNameList) {

        allPreferences.put(KEY_RULE_FILE_NAME_SETTING, fileNameList);

        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (int i = 0; i < fileNameList.size(); i++) {
            Map<String, Object> map1 = fileNameList.get(i);

            Map<String, String> map2 = new HashMap<String, String>();
            if(map1.get(Const.KEY_TYPE) != null){
                map2.put(Const.KEY_TYPE, map1.get(Const.KEY_TYPE).toString());
            }else {
                map2.put(Const.KEY_TYPE, null);
            }
            map2.put(Const.KEY_VALUE, (String) map1.get(Const.KEY_VALUE));
            list.add(map2);
        }

        String jsonString = null;
        try {
            jsonString = JsonUtils.getEncoder().encode(list);
        } catch (EncodedException e) {
            LogC.e("", e);
        }
        editor.putString(KEY_RULE_FILE_NAME_SETTING, jsonString);
        editor.commit();
    }

    public String getSeparatorChar() {
        String c = (String) allPreferences.get(KEY_SEPARATOR_CHAR);
        if (c == null) {
            c = preferences.getString(KEY_SEPARATOR_CHAR, null);
            if (c == null) {
                c = ".";
            }
        }
        return c;
    }

    public void setSeparatorChar(String c) {
        allPreferences.put(KEY_SEPARATOR_CHAR, c);
        editor.putString(KEY_SEPARATOR_CHAR, c);
        editor.commit();
    }

    /**
     * setEntryDestinations getEntryDestinations
     */
    public void setEntrySender(Entry entryData) {

        allPreferences.put(KEY_ENTRY_SENDER_SELECT, entryData);
        
        String jsonString = null;
        try {
            jsonString = JsonUtils.getEncoder().encode(entryData);
        } catch (EncodedException e) {
            LogC.e("setEntrySender", e);
        }

        editor.putString(KEY_ENTRY_SENDER_SELECT, jsonString);
        editor.commit();
    }

    public Entry getEntrySender() {
 
        Entry entry = (Entry) this.allPreferences.get(KEY_ENTRY_SENDER_SELECT);
        if (entry != null) {
            return entry;
        }
        String jsonString = preferences.getString(KEY_ENTRY_SENDER_SELECT, null);

        if (jsonString != null) {
            Map<String, Object> map = GenericJsonDecoder.decodeToMap(jsonString);
            Entry selectedEntry = new Entry(map);

            if (selectedEntry != null) {
                return selectedEntry;
            }
        }

        return null;

    }
}
