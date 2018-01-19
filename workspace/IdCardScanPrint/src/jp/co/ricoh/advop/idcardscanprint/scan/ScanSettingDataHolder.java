/*
 *  Copyright (C) 2013-2015 RICOH Co.,LTD.
 *  All rights reserved.
 */

package jp.co.ricoh.advop.idcardscanprint.scan;

import android.util.Log;

import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;
import jp.co.ricoh.advop.idcardscanprint.model.ScanDataSetting;
import jp.co.ricoh.advop.idcardscanprint.model.SettingItemData;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.attribute.MediaSizeName;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.ScanService;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.HashScanRequestAttributeSet;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.ScanRequestAttributeSet;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.FileSetting;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.FileSetting.CompressionLevel;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.FileSetting.FileFormat;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.AutoCorrectJobSetting;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.JobMode;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.OriginalPreview;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.OriginalSide;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.OriginalSize;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.ScanColor;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.ScanDevice;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.ScanResolution;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.SendStoredFileSetting;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.StoreLocalSetting;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.WaitTimeForNextOriginal;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.supported.FileSettingSupported;
import jp.co.ricoh.advop.idcardscanprint.util.Util;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.FileSetting.CompressionMethod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * スキャン設定情報クラスです。 Scan setting data class.
 */
public class ScanSettingDataHolder {
    public static String TAG = ScanSettingDataHolder.class.getSimpleName();

    /**
     * 読取カラーの表示文字列IDと設定値のマップです。 Map of scan color display string ID and setting
     * values.
     */
    private final static List<SettingItemData> mAllColorList;

    /**
     * 読取カラー設定可能値の表示文字列IDのリストです。 List of display string ID for the available
     * scan color setting values.
     */
    private List<SettingItemData> mSupportedColorLabelList;

    /**
     * 選択中の読取カラー設定値の表示文字列IDです。 Display string ID of the selected scan color
     * setting value.
     */
    private int mSelectedColorLabel;

    private int tempSelectedColorLabel;

    /**
     * ファイル形式の表示文字列IDと設定値のマップです。 Map of file format display string ID and
     * setting values.
     */
    private final static List<SettingItemData> mAllFileFormatList;

    /**
     * ファイル設定の設定可能値の表示文字列IDのリストです。 List of display string ID for the available
     * file setting values.
     */
    private List<SettingItemData> mSupportedFileSettingLabelList;

    /**
     * 選択中のファイル設定値の表示文字列IDです。 Display string ID of the selected file setting
     * value.
     */
    private int mSelectedFileSettingLabel;

    private int tempSelectedFileSettingLabel;

    /**
     * mail file name
     */
    private String mailFileName;

    /**
     * ジョブモードの表示文字列IDと設定値のマップです。 Map of job mode display string ID and setting
     * values.
     */
    private final static LinkedHashMap<Integer, JobMode> mAllJobModeMap;

    /**
     * マルチページ設定の表示文字列IDと設定値のマップです。 Map of multipage setting display string ID
     * and setting values.
     */
    private final static LinkedHashMap<Integer, Boolean> mAllMultiPageMap;

    /**
     * 原稿面の表示文字列IDと設定値のマップです。 Map of scan side display string ID and setting
     * values.
     */
    private final static LinkedHashMap<Integer, OriginalSide> mAllSideMap;

    /**
     * プレビュー表示設定の表示文字列IDと設定値のマップです。 Map of preview setting display string ID and
     * setting values.
     */
    private final static LinkedHashMap<Integer, OriginalPreview> mAllPreviewMap;

    /**
     * ジョブモード設定可能値の表示文字列IDのリストです。 List of display string ID for the available
     * job mode setting values.
     */
    private List<Integer> mSupportedJobModeLabelList;

    /**
     * 原稿面設定可能値の表示文字列IDのリストです。 List of display string ID for the available scan
     * side setting values.
     */
    private List<Integer> mSupportedSideLabelList;

    /**
     * プレビュー表示設定可能値の表示文字列IDのリストです。 List of display string ID for the available
     * preview setting values.
     */
    private List<Integer> mSupportedPreviewLabelList;

    /**
     * 選択中のジョブモード設定値の表示文字列IDです。 Display string ID of the selected job mode
     * setting value.
     */
    private int mSelectedJobModeLabel;

    /**
     * 選択中の原稿面設定値の表示文字列IDです。 Display string ID of the selected scan side setting
     * value.
     */
    private int mSelectedSideLabel;

    /**
     * 選択中のプレビュー表示設定値の表示文字列IDです。 Display string ID of the selected preview
     * setting value.
     */
    private int mSelectedPreviewLabel;

    /**
     * 各マップの初期化を行います。 [処理内容] (1)ジョブモード設定のマップの初期化 (2)読取カラー設定のマップの初期化
     * (3)ファイル形式設定のマップの初期化 (4)マルチページ設定のマップの初期化 (5)原稿面設定のマップの初期化
     * (6)プレビュー表示設定のマップの初期化 Initializes maps. [Processes] (1) Initializes the
     * map for job mode setting (2) Initializes the map for scan color setting
     * (3) Initializes the map for file setting (4) Initializes the map for
     * multipage setting (5) Initializes the map for scan side setting (6)
     * Initializes the map for preview setting
     */

    static {

        // (1)
        mAllJobModeMap = new LinkedHashMap<Integer, JobMode>() {
            {
                // put(R.string.txid_scan_b_jobmode_scan_and_send,
                // JobMode.SCAN_AND_SEND);
                // put(R.string.txid_scan_b_jobmode_scan_and_store_local,
                // JobMode.SCAN_AND_STORE_LOCAL);
                // put(R.string.txid_scan_b_jobmode_send_stored_file,
                // JobMode.SEND_STORED_FILE);
            }

        };

        // (2)

        mAllColorList = new ArrayList<SettingItemData>();

        mAllColorList.add(new SettingItemData(ScanColor.AUTO_COLOR,
                R.drawable.icon_setting_color_01,
                R.string.txid_scan_b_top_auto_color_select));
        mAllColorList.add(new SettingItemData(ScanColor.COLOR_TEXT_PHOTO,
                R.drawable.icon_setting_color_02,
                R.string.txid_scan_b_top_full_color_text_photo));
        mAllColorList.add(new SettingItemData(ScanColor.GRAYSCALE,
                R.drawable.icon_setting_color_03,
                R.string.txid_scan_b_top_mono_text_photo));

        // mAllColorMap = new LinkedHashMap<Integer, SettingItemData>() {
        // {
        // // put(R.string.txid_scan_b_top_mono_text,
        // // ScanColor.MONOCHROME_TEXT);
        //
        // put(R.string.txid_scan_b_top_mono_text_photo, new SettingItemData(
        // ScanColor.GRAYSCALE, R.drawable.icon_setting_color_03,
        // R.string.txid_scan_b_top_mono_text_photo));
        //
        // // put(R.string.txid_scan_b_top_mono_text_lineart,
        // // ScanColor.MONOCHROME_TEXT_LINEART);
        //
        // // put(R.string.txid_scan_b_top_mono_photo,
        // // ScanColor.MONOCHROME_PHOTO);
        //
        // // put(R.string.txid_scan_b_top_gray_scale,
        // // ScanColor.GRAYSCALE);
        //
        // put(R.string.txid_scan_b_top_full_color_text_photo, new
        // SettingItemData(
        // ScanColor.COLOR_TEXT_PHOTO, R.drawable.icon_setting_color_02,
        // R.string.txid_scan_b_top_mono_text_photo));
        //
        // // put(R.string.txid_scan_b_top_full_color_glossy_photo,
        // // ScanColor.COLOR_GLOSSY_PHOTO);
        //
        // put(R.string.txid_scan_b_top_auto_color_select, new SettingItemData(
        // ScanColor.AUTO_COLOR, R.drawable.icon_setting_color_01,
        // R.string.txid_scan_b_top_mono_text_photo));
        // }
        // };

        // (3)
        mAllFileFormatList = new ArrayList<SettingItemData>();

        mAllFileFormatList.add(new SettingItemData(
                "pdf", R.drawable.icon_setting_filetype_03,
                R.string.txid_scan_b_top_file_pdf));
        mAllFileFormatList.add(new SettingItemData(
                "tiff", R.drawable.icon_setting_filetype_02,
                R.string.txid_scan_b_top_file_tiff));
        mAllFileFormatList.add(new SettingItemData(
                "jpg", R.drawable.icon_setting_filetype_01,
                R.string.txid_scan_b_top_file_jpg));

        // mAllFileFormatMap = new LinkedHashMap<Integer, SettingItemData>() {
        // {
        // put(R.string.txid_scan_b_top_file_jpg, new SettingItemData(
        // FileFormat.TIFF_JPEG, R.drawable.icon_setting_filetype_01,
        // R.string.txid_scan_b_top_file_jpg));
        // // put(R.string.txid_scan_b_top_file_mtiff,
        // // FileFormat.TIFF_JPEG);
        // put(R.string.txid_scan_b_top_file_tiff, new SettingItemData(
        // FileFormat.TIFF_JPEG, R.drawable.icon_setting_filetype_02,
        // R.string.txid_scan_b_top_file_jpg));
        // // put(R.string.txid_scan_b_top_file_mpdf, FileFormat.PDF);
        // put(R.string.txid_scan_b_top_file_pdf, new SettingItemData(
        // FileFormat.TIFF_JPEG, R.drawable.icon_setting_filetype_03,
        // R.string.txid_scan_b_top_file_jpg));
        // }
        // };

        // (4)
        mAllMultiPageMap = new LinkedHashMap<Integer, Boolean>() {
            {
                // put(R.string.txid_scan_b_top_file_stiff_jpg, false);
                // put(R.string.txid_scan_b_top_file_mtiff, true);
                // put(R.string.txid_scan_b_top_file_spdf, false);
                // put(R.string.txid_scan_b_top_file_mpdf, true);
            }
        };

        // (5)
        mAllSideMap = new LinkedHashMap<Integer, OriginalSide>() {
            {
                // put(R.string.txid_scan_b_top_one_sided,
                // OriginalSide.ONE_SIDE);
                // put(R.string.txid_scan_b_top_top_to_top,
                // OriginalSide.TOP_TO_TOP);
                // put(R.string.txid_scan_b_top_top_to_bottom,
                // OriginalSide.TOP_TO_BOTTOM);
                // put(R.string.txid_scan_b_top_spread, OriginalSide.SPREAD);
            }
        };

        // (6)
        mAllPreviewMap = new LinkedHashMap<Integer, OriginalPreview>() {
            {
                // put(R.string.txid_scan_b_other_preview_on,
                // OriginalPreview.ON);
                // put(R.string.txid_scan_b_other_preview_off,
                // OriginalPreview.OFF);
            }
        };
    }

    /**
     * コンストラクタです。 各設定値の文字列を初期化します。 Constructor. Initializes the display string
     * of the setting values.
     */
    public ScanSettingDataHolder() {
        mSupportedJobModeLabelList = new ArrayList<Integer>();
        mSupportedColorLabelList = new ArrayList<SettingItemData>();
        mSupportedFileSettingLabelList = new ArrayList<SettingItemData>();
        mSupportedSideLabelList = new ArrayList<Integer>();
        mSupportedPreviewLabelList = new ArrayList<Integer>();
    }

    /**
     * ScanServiceから各設定の設定可能値一覧を取得します。
     * 指定されたSmartSDKのAPIバージョンでサポートされていない設定値は除去します。 [処理内容] (1)ジョブモード設定可能値を取得する
     * (2)読取カラー設定可能値を取得する (3)ファイル形式設定可能値とマルチページ設定可能値を取得する (4)原稿面設定可能値を取得する
     * (5)プレビュー設定可能値を取得する Obtains the list of available setting values from
     * ScanService. Removes the unsupported values on the specified SmartSDK API
     * version from the list. [Processes] (1) Obtains the available setting
     * values for job mode setting. (2) Obtains the available setting values for
     * scan color setting. (3) Obtains the available setting values for file
     * setting and multipage setting. (4) Obtains the available setting values
     * for scan side setting. (5) Obtains the available setting values for
     * preview setting.
     */
    public Boolean init(ScanService scanService) {

        FileSettingSupported fileSettingSupported = (FileSettingSupported) scanService
                .getSupportedAttributeValues(FileSetting.class);
        if (fileSettingSupported != null) {
            List<FileFormat> fileFormatList = fileSettingSupported.getFileFormatList();
            // TODO lack of condition (color) to FileType setting
            if (!fileFormatList.contains(FileFormat.TIFF_JPEG)) {
                return false;
            }
        } else {
            return false;
        }

        // (1)
        @SuppressWarnings("unchecked")
        List<JobMode> jobModeList = (List<JobMode>) scanService
                .getSupportedAttributeValues(JobMode.class);
        List<JobMode> localJobModeList = new ArrayList<JobMode>();
        if (jobModeList != null) {
            if (jobModeList.contains(JobMode.SCAN_AND_SEND)) {
                localJobModeList.add(JobMode.SCAN_AND_SEND);
            }
            if (jobModeList.contains(JobMode.SCAN_AND_STORE_LOCAL)) {
                if (scanService.getSupportedAttributeValues(StoreLocalSetting.class) != null) {
                    localJobModeList.add(JobMode.SCAN_AND_STORE_LOCAL);
                }
            }
            if (jobModeList.contains(JobMode.SEND_STORED_FILE)) {
                if (scanService.getSupportedAttributeValues(SendStoredFileSetting.class) != null) {
                    localJobModeList.add(JobMode.SEND_STORED_FILE);
                }
            }
        }
        setSupportedJobModeList(localJobModeList);

        // (2)
        @SuppressWarnings("unchecked")
        List<ScanColor> colorList = (List<ScanColor>) scanService
                .getSupportedAttributeValues(ScanColor.class);
        setSupportedColorList(colorList);
        List<SettingItemData> colorLabelList = getColorLabelList();
        if (colorLabelList == null || colorLabelList.size() == 0) {
            return false;
        }

        // (3)
        setSupportedFileSettingList();

        // (4)
        @SuppressWarnings("unchecked")
        List<OriginalSide> originalSideList = (List<OriginalSide>) scanService
                .getSupportedAttributeValues(OriginalSide.class);
        setSupportedSideList(originalSideList);

        // (5)
        @SuppressWarnings("unchecked")
        List<OriginalPreview> originalPreviewList = (List<OriginalPreview>) scanService
                .getSupportedAttributeValues(OriginalPreview.class);
        setSupportedPreviewList(originalPreviewList);

        // ScanDataSetting scanDataSetting =
        // CHolder.instance().getPreferences().getScanDataSetting();
        // if (scanDataSetting != null) {
        // ScanColor scanColor = scanDataSetting.getScanColor();
        // String scanFileFormat = scanDataSetting.getScanFileType();
        // mSelectedColorLabel = getColorFromEnum(scanColor);
        // mSelectedFileSettingLabel = getFileFormatFromString(scanFileFormat);
        // } else {
        // mSelectedColorLabel = getDefaultSupportedColor();
        // mSelectedFileSettingLabel = getDefaultSupportedFileTyp();
        // }
        // mailFileName = CHolder.instance().getPreferences().getFileName();
        return true;
    }

    public int getDefaultSupportedColor() {
        List<SettingItemData> colorLabelList = getColorLabelList();
        for (int i = 0; i < colorLabelList.size(); i++) {
            if (colorLabelList.get(i).getTextId() == R.string.txid_scan_b_top_auto_color_select) {
                return colorLabelList.get(i).getTextId();
            }
        }
        if(colorLabelList.size() == 0){
            return R.string.txid_scan_b_top_auto_color_select;
        }else {
            return colorLabelList.get(0).getTextId();
        }
    }

    public int getDefaultSupportedFileTyp() {
        List<SettingItemData> ftList = getFileSettingLabelList();
        for (int i = 0; i < ftList.size(); i++) {
            if (ftList.get(i).getTextId() == R.string.txid_scan_b_top_file_pdf) {
                return ftList.get(i).getTextId();
            }
        }
        if (ftList.size() == 0) {
            return R.string.txid_scan_b_top_file_pdf;
        }else {
            return ftList.get(0).getTextId();
        }
    }

    /**
     * ジョブモード設定可能値の表示文字列IDリストを作成します。 Creates the list of display string ID for
     * the available job mode setting values.
     * 
     * @param jobModeList ジョブモード設定可能値のリスト List of available job mode setting
     *            values
     */
    private void setSupportedJobModeList(List<JobMode> jobModeList) {
        mSupportedJobModeLabelList.clear();
        if (jobModeList != null) {
            Set<Map.Entry<Integer, JobMode>> entrySet = mAllJobModeMap.entrySet();
            Iterator<Map.Entry<Integer, JobMode>> it = entrySet.iterator();
            while (it.hasNext())
            {
                Map.Entry<Integer, JobMode> entry = it.next();
                if (jobModeList.contains(entry.getValue())) {
                    mSupportedJobModeLabelList.add(entry.getKey());
                }
            }
        }
    }

    /**
     * 読取カラー設定可能値の表示文字列IDリストを作成します。 Creates the list of display string ID for
     * the available scan color setting values.
     * 
     * @param colorList 読取カラー設定可能値のリスト List of available scan color setting
     *            values
     */
    // private void setSupportedColorList(List<ScanColor> colorList) {
    // mSupportedColorLabelList.clear();
    // if (colorList != null) {
    // Set<Map.Entry<Integer, SettingItemData>> entrySet =
    // mAllColorMap.entrySet();
    // Iterator<Map.Entry<Integer, SettingItemData>> it = entrySet.iterator();
    // while (it.hasNext())
    // {
    // Map.Entry<Integer, SettingItemData> entry = it.next();
    // ScanColor scanColor = (ScanColor) (entry.getValue().getItemValue());
    // if (colorList.contains(scanColor)) {
    // mSupportedColorLabelList.add(entry.getKey());
    // }
    // }
    //
    // if
    // (!mSupportedColorLabelList.contains(R.string.txid_scan_b_top_full_color_text_photo))
    // {
    // if (colorList.contains(ScanColor.COLOR_GLOSSY_PHOTO)) {
    // mSupportedColorLabelList.add(R.string.txid_scan_b_top_full_color_text_photo);
    // }
    // }
    // }
    // }

    /**
     * ファイル設定可能値の表示文字列IDリストを作成します。 Creates the list of display string ID for the
     * available file setting values.
     * 
     * @param fileFormatList ファイル形式設定可能値のリスト List of available scan file format
     *            setting values
     * @param multiPageFormatList マルチページ設定可能値のリスト List of available scan
     *            multipage setting values
     */
    // private void setSupportedFileSettingList(List<FileFormat> fileFormatList,
    // List<Boolean> multiPageFormatList) {
    // mSupportedFileSettingLabelList.clear();
    // if (fileFormatList != null) {
    // Set<Map.Entry<Integer, SettingItemData>> entrySet1 =
    // mAllFileFormatMap.entrySet();
    // Iterator<Map.Entry<Integer, SettingItemData>> it1 = entrySet1.iterator();
    // while (it1.hasNext())
    // {
    // Map.Entry<Integer, SettingItemData> entry = it1.next();
    // if (fileFormatList.contains(entry.getValue().getItemValue())) {
    // mSupportedFileSettingLabelList.add(entry.getKey());
    // }
    // }
    // }
    // if (multiPageFormatList != null) {
    // Set<Map.Entry<Integer, Boolean>> entrySet2 = mAllMultiPageMap.entrySet();
    // Iterator<Map.Entry<Integer, Boolean>> it2 = entrySet2.iterator();
    // while (it2.hasNext())
    // {
    // Map.Entry<Integer, Boolean> entry = it2.next();
    // if (!multiPageFormatList.contains(entry.getValue())) {
    // mSupportedFileSettingLabelList.remove(entry.getKey());
    // }
    // }
    // }
    // }

    /**
     * 原稿面設定可能値の表示文字列IDリストを作成します。 Creates the list of display string ID for the
     * available scan side setting values.
     * 
     * @param sideList 原稿面設定可能値のリスト List of available scan side setting values
     */
    private void setSupportedSideList(List<OriginalSide> sideList) {
        mSupportedSideLabelList.clear();
        if (sideList != null) {
            Set<Map.Entry<Integer, OriginalSide>> entrySet = mAllSideMap.entrySet();
            Iterator<Map.Entry<Integer, OriginalSide>> it = entrySet.iterator();
            while (it.hasNext())
            {
                Map.Entry<Integer, OriginalSide> entry = it.next();
                if (sideList.contains(entry.getValue())) {
                    mSupportedSideLabelList.add(entry.getKey());
                }
            }
        }
    }

    /**
     * プレビュー設定可能値の表示文字列IDリストを作成します。 Creates the list of display string ID for
     * the available preview setting values.
     * 
     * @param previewList プレビュー設定可能値のリスト List of available preview setting
     *            values
     */
    private void setSupportedPreviewList(List<OriginalPreview> previewList) {
        mSupportedPreviewLabelList.clear();
        if (previewList != null) {
            Set<Map.Entry<Integer, OriginalPreview>> entrySet = mAllPreviewMap.entrySet();
            Iterator<Map.Entry<Integer, OriginalPreview>> it = entrySet.iterator();
            while (it.hasNext())
            {
                Map.Entry<Integer, OriginalPreview> entry = it.next();
                if (previewList.contains(entry.getValue())) {
                    mSupportedPreviewLabelList.add(entry.getKey());
                }
            }
        }
    }

    /**
     * 選択中のジョブモード設定値の表示文字列IDを取得します。 Obtains the display string ID of the
     * selected job mode setting value.
     */
    public Integer getSelectedJobModeLabel() {
        return mSelectedJobModeLabel;
    }

    /**
     * 選択中のジョブモード設定値を取得します。 Obtains the selected job mode setting value.
     */
    public JobMode getSelectedJobModeValue() {
        return mAllJobModeMap.get(mSelectedJobModeLabel);
    }

    /**
     * 指定されたジョブモード設定値を選択状態にします。 Changes the selection state of the specified job
     * mode setting value to "selected".
     */
    public void setSelectedJobMode(Integer id) {
        if (mSupportedJobModeLabelList.contains(id)) {
            mSelectedJobModeLabel = id;
        }
    }

    /**
     * 選択中のマルチページ形式設定値を取得します。 Obtains the selected multipage setting value.
     */
    public Boolean getSelectedMultiPageValue() {
        // onlu single page;
        return false;
    }

    /**
     * 選択中の原稿面設定値の表示文字列IDを取得します。 Obtains the display string ID of the selected
     * scan side setting value.
     */
    public Integer getSelectedSideLabel() {
        return mSelectedSideLabel;
    }

    /**
     * 選択中の原稿面設定値を取得します。 Obtains the selected scan side setting value.
     */
    public OriginalSide getSelectedSideValue() {
        return mAllSideMap.get(mSelectedSideLabel);
    }

    /**
     * 指定された原稿面設定値を選択状態にします。 Changes the selection state of the specified scan
     * side setting value to "selected."
     * 
     * @param id
     */
    public void setSelectedSide(Integer id) {
        if (mSupportedSideLabelList.contains(id)) {
            mSelectedSideLabel = id;
        }
    }

    /**
     * 選択中のプレビュー表示設定値の表示文字列IDを取得します。 Obtains the display string ID of the
     * selected preview setting value.
     */
    public Integer getSelectedPreviewLabel() {
        return mSelectedPreviewLabel;
    }

    /**
     * 選択中のプレビュー表示設定値を取得します。 Obtains the selected preview setting value.
     */
    public OriginalPreview getSelectedPreviewValue() {
        return mAllPreviewMap.get(mSelectedPreviewLabel);
    }

    /**
     * 指定されたプレビュー表示設定値を選択状態にします。 Changes the selection state of the specified
     * preview setting value to "selected."
     * 
     * @param id
     */
    public void setSelectedPreview(Integer id) {
        if (mSupportedPreviewLabelList.contains(id)) {
            mSelectedPreviewLabel = id;
        }
    }

    /**
     * ジョブモード設定可能値のリストを取得します。 Obtains the display string ID of the job mode
     * setting value to select
     */
    public List<Integer> getJobModeLabelList() {
        return mSupportedJobModeLabelList;
    }

    /**
     * 原稿面設定可能値のリストを取得します。 Obtains the display string ID of the scan side
     * setting value to select
     */
    public List<Integer> getSideLabelList() {
        return mSupportedSideLabelList;
    }

    /**
     * プレビュー設定可能値のリストを取得します。 Obtains the display string ID of the preview
     * setting value to select
     */
    public List<Integer> getPreviewLabelList() {
        return mSupportedPreviewLabelList;
    }

    /*************************************************************
     * color setting about color setting
     * 
     * @param colorList
     */
    private void setSupportedColorList(List<ScanColor> colorList) {
        mSupportedColorLabelList.clear();
        if (colorList != null) {
            for (int i = 0; i < mAllColorList.size(); i++) {
                SettingItemData data = mAllColorList.get(i);

                if (colorList.contains(data.getItemValue())) {
                    mSupportedColorLabelList.add(data);
                }
            }

            if (!mSupportedColorLabelList
                    .contains(getColorFromID(R.string.txid_scan_b_top_full_color_text_photo))) {
                if (colorList.contains(ScanColor.COLOR_GLOSSY_PHOTO)) {
                    SettingItemData item = getColorFromID(R.string.txid_scan_b_top_full_color_text_photo);
                    if (item != null) {
                        mSupportedColorLabelList.add(item);
                    }

                }
            }

            List<SettingItemData> temSupportedColorLabelList = new ArrayList<SettingItemData>();
            for (int i = 0; i < mAllColorList.size(); i++) {
                SettingItemData data = mAllColorList.get(i);

                if (mSupportedColorLabelList.contains(data)) {
                    temSupportedColorLabelList.add(data);
                }
            }

            mSupportedColorLabelList = temSupportedColorLabelList;
        }
    }

    public SettingItemData getColorFromID(int id) {
        for (int i = 0; i < mAllColorList.size(); i++) {
            SettingItemData data = mAllColorList.get(i);
            if (data.getTextId() == id) {
                return data;
            }
        }
        return null;
    }

    public int getColorFromEnum(ScanColor scanColor) {
        for (int i = 0; i < mAllColorList.size(); i++) {
            SettingItemData data = mAllColorList.get(i);
            if ((ScanColor) data.getItemValue() == scanColor) {
                return data.getTextId();
            }
        }
        return -1;
    }

    /**
     * 読取カラー設定可能値のリストを取得します。 Obtains the display string ID of the scan color
     * setting value to select
     */
    public List<SettingItemData> getColorLabelList() {
        return mSupportedColorLabelList;
    }

    /**
     * 指定された読取カラー設定値を選択状態にします。 Changes the selection state of the specified scan
     * color setting value to "selected."
     * 
     * @param id
     */
    public void setSelectedColor(Integer id) {
        if (mSupportedColorLabelList.contains(getColorFromID(id))) {
            mSelectedColorLabel = id;
        }
    }

    /**
     * 選択中の読取カラー設定値の表示文字列IDを取得します。 Obtains the display string ID of the selected
     * scan color setting value.
     */
    public Integer getSelectedColorLabel() {
        return mSelectedColorLabel;
    }

    /**
     * 選択中の読取カラー設定値を取得します。 Obtains the selected scan color setting value.
     */
    public SettingItemData getSelectedColorValue() {
        return getColorFromID(mSelectedColorLabel);
    }

    public SettingItemData getSelectedColorValue(int mSelectedColorLabel) {
        return getColorFromID(mSelectedColorLabel);
    }

    public int getTempSelectedColorLabel() {
        return tempSelectedColorLabel;
    }

    public void setTempSelectedColorLabel(int tempSelectedColorLabel) {
        this.tempSelectedColorLabel = tempSelectedColorLabel;
    }

    /**************************************************************
     * file type ファイル設定可能値の表示文字列IDリストを作成します。 Creates the list of display string
     * ID for the available file setting values.
     * 
     * @param fileFormatList ファイル形式設定可能値のリスト List of available scan file format
     *            setting values
     */
    private void setSupportedFileSettingList() {
        mSupportedFileSettingLabelList.clear();
        for (SettingItemData data : mAllFileFormatList) {
            mSupportedFileSettingLabelList.add(data);
        }
    }

    /**
     * ファイル設定可能値のリストを取得します。 Obtains the display string ID of the file setting
     * value to select
     */
    public List<SettingItemData> getFileSettingLabelList() {
        return mSupportedFileSettingLabelList;
    }

    public SettingItemData getFileFormatFromID(int id) {
        for (int i = 0; i < mAllFileFormatList.size(); i++) {
            SettingItemData data = mAllFileFormatList.get(i);
            if (data.getTextId() == id) {
                return data;
            }
        }
        return null;
    }

    public int getFileFormatFromString(String fileFormat) {
        for (int i = 0; i < mAllFileFormatList.size(); i++) {
            SettingItemData data = mAllFileFormatList.get(i);
            if (fileFormat.equals((String) data.getItemValue())) {
                return data.getTextId();
            }
        }
        return -1;
    }

    /*
     * 指定されたファイル設定値を選択状態にします。 Changes the selection state of the specified file
     * setting value to "selected."
     * @param id
     */
    public void setSelectedFileSetting(Integer id) {
        if (mSupportedFileSettingLabelList.contains(getFileFormatFromID(id))) {
            mSelectedFileSettingLabel = id;
        }
    }

    /**
     * 選択中のファイル設定値の表示文字列IDを取得します。 Obtains the display string ID of the selected
     * scan color setting value.
     */
    public Integer getSelectedFileSettingLabel() {
        return mSelectedFileSettingLabel;
    }

    /**
     * 選択中のファイル形式設定値を取得します。 Obtains the selected scan color setting value.
     */
    public SettingItemData getSelectedFileFormatValue() {
        return getFileFormatFromID(mSelectedFileSettingLabel);
    }

    public SettingItemData getSelectedFileFormatValue(int mSelectedFileSettingLabel) {
        return getFileFormatFromID(mSelectedFileSettingLabel);
    }

    public int getTempSelectedFileSettingLabel() {
        return tempSelectedFileSettingLabel;
    }

    public void setTempSelectedFileSettingLabel(int tempSelectedFileSettingLabel) {
        this.tempSelectedFileSettingLabel = tempSelectedFileSettingLabel;
    }

    public ScanRequestAttributeSet getScanJobAttributes() {
        ScanRequestAttributeSet requestAttributes;
        requestAttributes = new HashScanRequestAttributeSet();
        requestAttributes.add(AutoCorrectJobSetting.AUTO_CORRECT_ON);
        requestAttributes.add(OriginalSize.AUTO);

        requestAttributes.add(ScanResolution.DPI_300);
        requestAttributes.add(JobMode.SCAN_AND_STORE_TEMPORARY);
        
        requestAttributes.add(new WaitTimeForNextOriginal(0)); // only scan 1 page

        ScanColor scanColor = (ScanColor) PreferencesUtil.getInstance().getSelectedColorValue()
                .getItemValue();
        if (scanColor == ScanColor.AUTO_COLOR) {
            scanColor = ScanColor.COLOR_TEXT_PHOTO;
        }

        requestAttributes.add(scanColor);
        requestAttributes.add(getSelectedSideValue());
        requestAttributes.add(getSelectedPreviewValue());

        // FileSetting
        FileSetting fileSetting = new FileSetting();
        fileSetting.setFileFormat(FileFormat.TIFF_JPEG);
        fileSetting.setMultiPageFormat(getSelectedMultiPageValue());
        fileSetting.setCompressionMethod(CompressionMethod.JPEG);
        fileSetting.setCompressionLevel(CompressionLevel.LEVEL1);
        requestAttributes.add(fileSetting);

        // original size
        if (Util.getDefaultDest().equalsIgnoreCase("na")) {
            requestAttributes.add(new OriginalSize(MediaSizeName.INVOICE_LANDSCAPE));
        } else {
            requestAttributes.add(new OriginalSize(MediaSizeName.ISO_A5_LANDSCAPE));
        }

        // scan device
        requestAttributes.add(ScanDevice.CONTACT_GLASS);
        return requestAttributes;
    }
}
