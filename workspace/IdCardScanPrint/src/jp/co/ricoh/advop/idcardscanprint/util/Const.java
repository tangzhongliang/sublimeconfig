package jp.co.ricoh.advop.idcardscanprint.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;
import jp.co.ricoh.advop.idcardscanprint.model.RuleFileNameType;
import jp.co.ricoh.advop.idcardscanprint.model.TimeFormat;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PrintJobStateReason;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PrinterStateReason;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.ScanJobStateReason;

public class Const {
    /**
     * アプリケーションの種別
     * システム警告ダイアログの設定に使用します。
     * Application type
     * Used for setting system warning dialog.
     */
    public final static String ALERT_DIALOG_APP_TYPE_SCANNER = "SCANNER";
    public final static String ALERT_DIALOG_APP_TYPE_PRINTER = "PRINTER";
    
    public class SettingShow {
        public static final int SCAN_SIDE1 = 0;
        public static final int SCAN_SIDE2 = 1;
        public static final int PREVIEW = 2;
        
    }

    public final static String SCAN_FILE_PATH = "/mnt/hdd/"
            + CHolder.instance().getApplication().getPackageName() + "/tmp/";

    public final static String COMBILE_FILE_NAME = "combined.jpg";
    public final static String SEND_FILE_PDF = ".pdf";
    public final static String SEND_FILE_TIFF = ".tif";
    public final static String SEND_FILE_JPG = ".jpg";

    public final static String SMTP_OPTION_FALSE = "false";
    public final static String SMTP_OPTION_TRUE = "true";
    public static final Map<String, Integer> SMTP_SERVER_MAP = new LinkedHashMap<String, Integer>();

    public final static String ENCODING_OPTION_USASCII = "US-ASCII";
    public final static String ENCODING_OPTION_SHIFTJIS = "Shift_JIS";
    public final static String ENCODING_OPTION_EUCJP = "EUC-JP";
    
    public static final Map<String, Integer> ENCODING_MAP = new LinkedHashMap<String, Integer>();
    
    public static final List<Integer> ENCODING_LIST = new ArrayList<Integer>();
    public static final List<Integer> SMTP_SERVER_LIST = new ArrayList<Integer>();
    
    static {
        SMTP_SERVER_MAP.put(SMTP_OPTION_TRUE, R.string.smtp_true);
        SMTP_SERVER_MAP.put(SMTP_OPTION_FALSE, R.string.smtp_false);   
        ENCODING_MAP.put(ENCODING_OPTION_USASCII, R.string.encoding_usascii);
        ENCODING_MAP.put(ENCODING_OPTION_SHIFTJIS, R.string.encoding_shiftjis);    
        ENCODING_MAP.put(ENCODING_OPTION_EUCJP, R.string.encoding_eucjp);      
        SMTP_SERVER_LIST.add(R.string.smtp_true);
        SMTP_SERVER_LIST.add(R.string.smtp_false);
        ENCODING_LIST.add(R.string.encoding_usascii);
        ENCODING_LIST.add(R.string.encoding_shiftjis);
        ENCODING_LIST.add(R.string.encoding_eucjp);
    }
    

   
    public final static Map<ScanJobStateReason, Integer> SCAN_ERROR_MAP = new LinkedHashMap<ScanJobStateReason, Integer>();
    public final static Map<PrinterStateReason, Integer> PRINT_ERROR_MAP = new LinkedHashMap<PrinterStateReason, Integer>();
    public final static Map<String, Integer> JOB_ERROR_MAP = new HashMap<String, Integer>();
    static {
        PRINT_ERROR_MAP.put(PrinterStateReason.COVER_OPEN, R.string.PRINT_COVER_OPEN);
        PRINT_ERROR_MAP.put(PrinterStateReason.DEVELOPER_EMPTY, R.string.PRINT_DEVELOPER_EMPTY);
        PRINT_ERROR_MAP.put(PrinterStateReason.DEVELOPER_LOW, R.string.PRINT_DEVELOPER_LOW);
        PRINT_ERROR_MAP.put(PrinterStateReason.INPUT_TRAY_MISSING, R.string.PRINT_INPUT_TRAY_MISSING);
        PRINT_ERROR_MAP.put(PrinterStateReason.INTERPRETER_RESOURCE_UNAVAILABLE, R.string.print_fail);
        PRINT_ERROR_MAP.put(PrinterStateReason.MARKER_WASTE_ALMOST_FULL, R.string.PRINT_MARKER_WASTE_ALMOST_FULL);
        PRINT_ERROR_MAP.put(PrinterStateReason.MARKER_WASTE_FULL, R.string.PRINT_MARKER_WASTE_FULL);
        PRINT_ERROR_MAP.put(PrinterStateReason.MEDIA_EMPTY, R.string.PRINT_MEDIA_EMPTY);
        PRINT_ERROR_MAP.put(PrinterStateReason.MEDIA_JAM, R.string.PRINT_MEDIA_JAM);
//        PRINT_ERROR_MAP.put(PrinterStateReason.OPC_LIFE_OVER, R.string.PRINT_OPC_LIFE_OVER);
//        PRINT_ERROR_MAP.put(PrinterStateReason.OPC_NEAR_EOL, R.string.PRINT_OPC_NEAR_EOL);
        PRINT_ERROR_MAP.put(PrinterStateReason.OTHER, R.string.PRINT_OTHER);
        PRINT_ERROR_MAP.put(PrinterStateReason.OUTPUT_AREA_FULL, R.string.PRINT_OUTPUT_AREA_FULL);
        PRINT_ERROR_MAP.put(PrinterStateReason.OUTPUT_TRAY_MISSING, R.string.PRINT_OUTPUT_TRAY_MISSING);
        PRINT_ERROR_MAP.put(PrinterStateReason.PAUSED, R.string.PRINT_PAUSED);
        //PRINT_ERROR_MAP.put(PrinterStateReason.STOPPED_PARTLY, R.string.PRINT_STOPPED_PARTLY);
        PRINT_ERROR_MAP.put(PrinterStateReason.TONER_EMPTY, R.string.PRINT_TONER_EMPTY);
        PRINT_ERROR_MAP.put(PrinterStateReason.TONER_LOW, R.string.PRINT_TONER_LOW);
        PRINT_ERROR_MAP.put(PrinterStateReason.COMMUNICATION_LOG_FULL, R.string.PRINT_COMMUNICATION_LOG_FULL);        
    }
    
    static {
        SCAN_ERROR_MAP.put(ScanJobStateReason.MEMORY_OVER, R.string.SCAN_MEMORY_OVER);
        SCAN_ERROR_MAP.put(ScanJobStateReason.RESOURCES_ARE_NOT_READY, R.string.SCAN_RESOURCES_ARE_NOT_READY);
        SCAN_ERROR_MAP.put(ScanJobStateReason.INTERNAL_ERROR, R.string.SCAN_INTERNAL_ERROR);
        SCAN_ERROR_MAP.put(ScanJobStateReason.ORIGINAL_SET_ERROR, R.string.SCAN_ORIGINAL_SET_ERROR);
        SCAN_ERROR_MAP.put(ScanJobStateReason.TIMEOUT, R.string.SCAN_TIMEOUT);
        SCAN_ERROR_MAP.put(ScanJobStateReason.EXCEEDED_MAX_EMAIL_SIZE, R.string.SCAN_EXCEEDED_MAX_EMAIL_SIZE);
        SCAN_ERROR_MAP.put(ScanJobStateReason.EXCEEDED_MAX_PAGE_COUNT, R.string.SCAN_EXCEEDED_MAX_PAGE_COUNT);
        SCAN_ERROR_MAP.put(ScanJobStateReason.CHARGE_UNIT_LIMIT, R.string.SCAN_CHARGE_UNIT_LIMIT);
        SCAN_ERROR_MAP.put(ScanJobStateReason.PREPARING_JOB_START, R.string.SCAN_PREPARING_JOB_START);
        SCAN_ERROR_MAP.put(ScanJobStateReason.SCANNER_JAM, R.string.SCAN_SCANNER_JAM);
        SCAN_ERROR_MAP.put(ScanJobStateReason.WAIT_FOR_NEXT_ORIGINAL, R.string.SCAN_WAIT_FOR_NEXT_ORIGINAL);
        SCAN_ERROR_MAP.put(ScanJobStateReason.WAIT_FOR_NEXT_ORIGINAL_AND_CONTINUE, R.string.SCAN_WAIT_FOR_NEXT_ORIGINAL_AND_CONTINUE);
        SCAN_ERROR_MAP.put(ScanJobStateReason.CANNOT_DETECT_ORIGINAL_SIZE, R.string.SCAN_CANNOT_DETECT_ORIGINAL_SIZE);
        SCAN_ERROR_MAP.put(ScanJobStateReason.EXCEEDED_MAX_DATA_CAPACITY, R.string.SCAN_EXCEEDED_MAX_DATA_CAPACITY);
        SCAN_ERROR_MAP.put(ScanJobStateReason.NOT_SUITABLE_ORIGINAL_ORIENTATION, R.string.SCAN_NOT_SUITABLE_ORIGINAL_ORIENTATION);
        SCAN_ERROR_MAP.put(ScanJobStateReason.TOO_SMALL_SCAN_SIZE, R.string.SCAN_TOO_SMALL_SCAN_SIZE);
        SCAN_ERROR_MAP.put(ScanJobStateReason.WAIT_FOR_ORIGINAL_PREVIEW_OPERATION, R.string.SCAN_WAIT_FOR_ORIGINAL_PREVIEW_OPERATION);
        SCAN_ERROR_MAP.put(ScanJobStateReason.USER_REQUEST, R.string.SCAN_USER_REQUEST);        
    }
    
    public final static Map<PrintJobStateReason, Integer> mJobReasonStringMap = new LinkedHashMap<PrintJobStateReason, Integer>() {
        {
            put(PrintJobStateReason.COMPRESSION_ERROR,                 R.string.print_fail);
            put(PrintJobStateReason.DOCUMENT_FORMAT_ERROR,             R.string.print_fail);
            put(PrintJobStateReason.JOB_CANCELED_AT_DEVICE,            R.string.dlg_printing_message_printing_stopped);
            put(PrintJobStateReason.RESOURCES_ARE_NOT_READY,           R.string.print_fail);
            put(PrintJobStateReason.PERMISSION_DENIED,                 R.string.print_fail);
            put(PrintJobStateReason.PRINT_VOLUME_LIMIT,                R.string.print_fail);
            put(PrintJobStateReason.TIMEOUT,                           R.string.print_fail);
            put(PrintJobStateReason.JOB_CANCELED_BY_USER,              R.string.dlg_printing_message_printing_stopped);
            put(PrintJobStateReason.JOB_CANCELED_DURING_CREATING,      R.string.dlg_printing_message_printing_stopped);
            put(PrintJobStateReason.PREPARING_JOB_START,               R.string.print_fail);
        }
    };
    
    static {
        JOB_ERROR_MAP.put("error.login_failed", R.string.error_login_failed);
        JOB_ERROR_MAP.put("error.not_input_authentication", R.string.error_not_input_authentication);
        JOB_ERROR_MAP.put("error.not_authorized", R.string.error_not_authorized);
        JOB_ERROR_MAP.put("error.system_busy", R.string.error_system_busy);
        JOB_ERROR_MAP.put("error.permission_denied", R.string.error_permission_denied);
        JOB_ERROR_MAP.put("error.path_no_exist", R.string.error_path_no_exist);
        //JOB_ERROR_MAP.put("error.unsupported_job_setting", R.string.print_error_paper_size);
    
    }
  
    
    public static final String KEY_TYPE = "type";
    public static final String KEY_VALUE = "value";
            
    /**
     * 設定なし
     */
    public final static int SETTING_NOTHING = R.string.tx_init_setting_section_print_file_name_rule01;

    /**
     * ユーザ名
     */
    public final static int USER_NAME = R.string.tx_init_setting_section_print_file_name_rule02;

    /**
     * 日時
     */
    public final static int TIME = R.string.tx_init_setting_section_print_file_name_rule03;

    public final static int INPUTWORD = R.string.tx_init_setting_section_print_file_name_rule04;

    public final static int STRATUM1 = R.string.tx_init_setting_section_print_file_name_stratum01;
    public final static int STRATUM2 = R.string.tx_init_setting_section_print_file_name_stratum02;
    public final static int STRATUM3 = R.string.tx_init_setting_section_print_file_name_stratum03;
    public final static int STRATUM4 = R.string.tx_init_setting_section_print_file_name_stratum04;
    public final static int STRATUM5 = R.string.tx_init_setting_section_print_file_name_stratum05;
    public final static int STRATUM6 = R.string.tx_init_setting_section_print_file_name_stratum06;

    /**
     * ファイル名ルール設定
     */
    public final static int FILENAMESETTING = R.string.tx_init_setting_rule_file_name;
    
    public static List<Map<String, Object>> DefaultFileNameList = new ArrayList<Map<String, Object>>();
    static{
//            Map<String, Object> map = new HashMap<String, Object>();
//            map.put(Const.KEY_TYPE, RuleFileNameType.TIME);
//            map.put(Const.KEY_VALUE, TimeFormat.YYYY_MM_DD_HH_MM_SS.toString());
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put(Const.KEY_TYPE, null);
            map1.put(Const.KEY_VALUE, null);

            DefaultFileNameList.add(map1);
            DefaultFileNameList.add(map1);
            DefaultFileNameList.add(map1);
            DefaultFileNameList.add(map1);
            DefaultFileNameList.add(map1);
            DefaultFileNameList.add(map1);
    }
    
    public final static String FILE_NAME_ILLEGALWORD = "\\/:*?\"<>|";
    public final static String invaidPattern = "^[^\\/:*?\"<>|]+$";
    public final static String LEFFE_C1B_MODEL = "MP 305+";
}
