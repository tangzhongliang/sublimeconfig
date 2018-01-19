/*
 *  Copyright (C) 2013 RICOH Co.,LTD.
 *  All rights reserved.
 */

package jp.co.ricoh.advop.idcardscanprint.print;

import android.util.Log;
import android.widget.Toast;

import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;
import jp.co.ricoh.advop.idcardscanprint.model.SettingItemData;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.attribute.MediaSizeName;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.PrintFile;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.PrintService;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.HashPrintRequestAttributeSet;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.PrintException;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.PrintRequestAttribute;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.PrintRequestAttributeSet;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.Copies;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PrintColor;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.TrayName;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PaperSize;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.supported.MaxMinSupported;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.Magnification;

import jp.co.ricoh.advop.idcardscanprint.ui.activity.PrintColorActivity;
import jp.co.ricoh.advop.idcardscanprint.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * 印刷に必要な各種設定値を定義するクラスです。 Print setting data class.
 */
public class PrintSettingDataHolder {

    private final static String TAG = PrintSettingDataHolder.class.getSimpleName();
    /**
     * 読取カラーの表示文字列IDと設定値のマップです。 Map of print color display string ID and setting
     * values.
     */
    private final static List<SettingItemData> mAllColorList;

    /**
     * 読取カラー設定可能値の表示文字列IDのリストです。 List of display string ID for the available
     * print color setting values.
     */
    private List<SettingItemData> mSupportedColorLabelList;
    
    private List<PaperSize> supportPaperSizeList;
    
    private int mSelectedPrintColor;
    private int tempSelectedPrintColor;

    private int mSelectedPrintPage;
    private int tempSelectedPrintPage;

    /**
     * the range of print count
     */
    private MaxMinSupported mmSupported;

    /**
     * 印刷ファイルのPDL PDL setting
     */
    private PrintFile.PDL mSelectedPDL;

    // /**
    // * ステープルの設定 Staple setting
    // */
    // private Staple mSelectedStapleValue;
    //
    // /**
    // * 印刷部数 Number of copies
    // */
    // private Copies mSelectedCopiesValue;
    //
    // /**
    // * 印刷ファイルの名称 File name
    // */
    // private String mSelectedPrintAssetFileName;

    static {

        mAllColorList = new ArrayList<SettingItemData>();
        mAllColorList.add(new SettingItemData(
                PrintColor.AUTO_COLOR, R.drawable.icon_setting_color_01,
                R.string.txid_scan_b_top_auto_color_select));

        mAllColorList.add(new SettingItemData(
                PrintColor.COLOR, R.drawable.icon_setting_color_02,
                R.string.txid_scan_b_top_full_color_text_photo));

        mAllColorList.add(new SettingItemData(
                PrintColor.MONOCHROME, R.drawable.icon_setting_color_03,
                R.string.txid_scan_b_top_mono_text_photo));

    }

    public PrintSettingDataHolder() {

        mSupportedColorLabelList = new ArrayList<SettingItemData>();
    }

    public Boolean init(PrintService printService) {

        List<PrintColor> colorList = (List<PrintColor>) printService.getSupportedAttributeValues(mSelectedPDL, PrintColor.class);
        setSupportedColorList(colorList);
        List<PaperSize> paperSizeList = (List<PaperSize>) printService.getSupportedAttributeValues(mSelectedPDL, PaperSize.class);
        setSupportPaperSizeList(paperSizeList);
        if (paperSizeList != null) {
            LogC.d("get capability paper size", paperSizeList.toString());
        }
        List<SettingItemData> colorLabelList = getColorLabelList();
        if (colorLabelList == null || colorLabelList.size() == 0) {
            return false;
        }
        // gain printer support print max and min copies;
        mmSupported = (MaxMinSupported) printService.getSupportedAttributeValues(mSelectedPDL,
                Copies.class);
        if (mmSupported == null) {
            LogC.e(TAG, "Get Supported Attribute Values is null");
        }
        return true;
        
        // TODO
        // PrintDataSetting printDataSetting =
        // CHolder.instance().getPreferences().getPrintDataSetting();
        // if (printDataSetting != null) {
        // PrintColor printColor = printDataSetting.getPrintColor();
        // int printCount = printDataSetting.getPrintCount();
        // mSelectedPrintColor = getColorFromEnum(printColor);
        // mSelectedPrintPage = printCount;
        // } else {
        // mSelectedPrintColor = getDefaultSupportedColor();
        // mSelectedPrintPage = 1;
        // }
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

    /***********************************************************
     * 公開メソッド public methods
     ***********************************************************/

    /**
     * 指定されたリソースから印刷ファイルのオブジェクトを生成します。 Create PrintFile object from the
     * specified resources.
     * 
     * @return
     * @throws PrintException
     */
    public PrintFile getPrintFile() throws PrintException {
        LogC.d("print file : " + CHolder.instance().getJobData().getPathPrintFile());
        PrintFile printfile = (new PrintFile.Builder())
                .printerFilePath(CHolder.instance().getJobData().getPathPrintFile())
                .pdl(mSelectedPDL).build();
        return printfile;
    }

    /**
     * 現在の設定値からプリント要求用の属性セットを生成します。 Create Print request attribute set from
     * current print settings.
     * 
     * @return
     */
    public PrintRequestAttributeSet getPrintRequestAttributeSet() {
        PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
        //attributeSet.add(AutoCorrectJobSetting.AUTO_CORRECT_ON);
        // if (mSelectedStapleValue != null)
        // attributeSet.add(mSelectedStapleValue);
        // if (mSelectedCopiesValue != null)
        // attributeSet.add(mSelectedCopiesValue);
        // if (getColorFromID(mSelectedPrintColor).getItemValue() != null)
        // attributeSet.add((PrintRequestAttribute)
        // getColorFromID(mSelectedPrintColor).getItemValue());
        Copies copies = PreferencesUtil.getInstance().getSelectedPrintPageValue();
        if (copies != null) {
            attributeSet.add(copies);
        }
        PrintColor printColor = (PrintColor) PreferencesUtil.getInstance()
                .getSelectedPrintColorValue().getItemValue();
        if (printColor != null) {
            attributeSet.add(printColor);
        }

        if (Util.getDefaultDest().equalsIgnoreCase("na")) {
            if (getSupportPaperSizeList() != null 
                   && getSupportPaperSizeList().contains(PaperSize.fromString(String.valueOf("na_letter")))) {
                
                LogC.d("getPrintRequestAttributeSet", "----contains na_leter");
                attributeSet.add(new PaperSize(MediaSizeName.NA_LETTER));
                
            } else if (getSupportPaperSizeList() != null 
                   && !getSupportPaperSizeList().contains(PaperSize.fromString(String.valueOf("na_letter")))
                   && getSupportPaperSizeList().contains(PaperSize.fromString(String.valueOf("na_letter_landscape")))){
                
                LogC.d("getPrintRequestAttributeSet", "----contains NA_LETTER_LANDSCAPE");
                attributeSet.add(new PaperSize(MediaSizeName.NA_LETTER_LANDSCAPE));
                
            } else {
                LogC.d("getPrintRequestAttributeSet", "NA not set paper size");
            }
        } else {
            if (getSupportPaperSizeList() != null 
                   && getSupportPaperSizeList().contains(PaperSize.fromString(String.valueOf("iso_a4")))) {
                
                attributeSet.add(new PaperSize(MediaSizeName.ISO_A4));
                LogC.d("getPrintRequestAttributeSet", "----contains ISO_A4");
                
            } else if (getSupportPaperSizeList() != null 
                    && !getSupportPaperSizeList().contains(PaperSize.fromString(String.valueOf("iso_a4")))
                    && getSupportPaperSizeList().contains(PaperSize.fromString(String.valueOf("iso_a4_landscape")))) {
                
                attributeSet.add(new PaperSize(MediaSizeName.ISO_A4_LANDSCAPE));
                LogC.d("getPrintRequestAttributeSet", "----contains ISO_A4_LANDSCAPE");
                
            } else {
                LogC.d("getPrintRequestAttributeSet", "NOT NA,not set paper size");
            }
        }

        //if (Util.isSupportAutoTray()) {
        //    attributeSet.add(TrayName.AUTO);
        //} else {
         attributeSet.add(Util.getCurrentTray());
        //}
        
        attributeSet.add(new Magnification("fitting"));
        
        
        return attributeSet;
    }

    /***********************************************************
     * 設定値のセッター Setter methods
     ***********************************************************/

    // /**
    // * ステープル設定値に指定された値をセットします。 Set the staple setting to the specified value.
    // *
    // * @param value
    // */
    // public void setSelectedStaple(Staple value) {
    // mSelectedStapleValue = value;
    // }
    //
    // /**
    // * 印刷部数設定値に指定された値をセットします。 Set the number of copies to the specified value.
    // *
    // * @param selectedCopiesValue
    // */
    // public void setSelectedCopiesValue(Copies selectedCopiesValue) {
    // mSelectedCopiesValue = selectedCopiesValue;
    // }
    //
    // /**
    // * 印刷ファイルの名称に指定された値をセットします。 Set the print file name to the specified
    // value.
    // *
    // * @param selectedPrintAssetFileName
    // */
    // public void setSelectedPrintAssetFileName(String
    // selectedPrintAssetFileName) {
    // mSelectedPrintAssetFileName = selectedPrintAssetFileName;
    // }

    /**
     * PDL設定に指定された値をセットします。 Set the PDL to the specified value.
     * 
     * @param pdl
     */
    public void setSelectedPDL(PrintFile.PDL pdl) {
        mSelectedPDL = pdl;
    }

    /***********************************************************
     * 設定値のゲッター Getter methods
     ***********************************************************/

    // /**
    // * 現在のステープルの設定値を取得します。 Obtains the current staple setting value.
    // *
    // * @return
    // */
    // public Staple getSelectedStaple() {
    // return mSelectedStapleValue;
    // }
    //
    // /**
    // * 現在の印刷部数の設定値を取得します。 Get the current number of pages.
    // *
    // * @return
    // */
    // public Copies getSelectedCopiesValue() {
    // return mSelectedCopiesValue;
    // }
    //
    // /**
    // * 現在の印刷ファイル名を取得します。 Get the current print file name.
    // *
    // * @return
    // */
    // public String getSelectedPrintAssetFileName() {
    // return mSelectedPrintAssetFileName;
    // }

    /**
     * 現在のPDL設定値を取得します。 Get the current PDL setting value.
     * 
     * @return
     */
    public PrintFile.PDL getSelectedPDL() {
        return mSelectedPDL;
    }

    /**
     * print color ___________________________________________________________
     */
    private void setSupportedColorList(List<PrintColor> colorList) {
        mSupportedColorLabelList.clear();
        if (colorList != null) {
            for (int i = 0; i < mAllColorList.size(); i++) {
                SettingItemData data = mAllColorList.get(i);

                if (colorList.contains(data.getItemValue())) {
                    mSupportedColorLabelList.add(data);
                }
            }
//TODO
            if (!mSupportedColorLabelList
                    .contains(getColorFromID(R.string.txid_scan_b_top_full_color_text_photo))) {
//                if (colorList.contains(ScanColor.COLOR_GLOSSY_PHOTO)) {
//                    SettingItemData item = getColorFromID(R.string.txid_scan_b_top_full_color_text_photo);
//                    if (item != null) {
//                        mSupportedColorLabelList.add(item);
//                    }
//
//                }
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

    public int getColorFromEnum(PrintColor printColor) {
        for (int i = 0; i < mAllColorList.size(); i++) {
            SettingItemData data = mAllColorList.get(i);
            if ((PrintColor) data.getItemValue() == printColor) {
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

    public void setSelectedPrintColor(int mSelectedPrintColor) {
        this.mSelectedPrintColor = mSelectedPrintColor;
    }

    /**
     * 現在の印刷カラー設定値を取得します。 Get the current print color setting value.
     * 
     * @return
     */
    public SettingItemData getSelectedPrintColorValue() {
        return getColorFromID(mSelectedPrintColor);
    }

    public SettingItemData getSelectedPrintColorValue(int mSelectedPrintColor) {
        return getColorFromID(mSelectedPrintColor);
    }

    public int getSelectedPrintColor() {
        return mSelectedPrintColor;
    }

    public int getTempSelectedPrintColor() {
        return tempSelectedPrintColor;
    }

    public void setTempSelectedPrintColor(int tempSelectedPrintColor) {
        this.tempSelectedPrintColor = tempSelectedPrintColor;
    }

    /**
     * print count __________________________________________________________
     */
    public int getSelectedPrintPage() {
        return mSelectedPrintPage;
    }

    public Copies getSelectedPrintPageValue() {
        return new Copies(mSelectedPrintPage);
    }

    public Copies getSelectedPrintPageValue(int mSelectedPrintPage) {
        return new Copies(mSelectedPrintPage);
    }

    public void setSelectedPrintPage(int mSelectedPrintPage) {
        this.mSelectedPrintPage = mSelectedPrintPage;
    }

    public int getTempSelectedPrintPage() {
        return tempSelectedPrintPage;
    }

    public void setTempSelectedPrintPage(int tempSelectedPrintPage) {
        this.tempSelectedPrintPage = tempSelectedPrintPage;
    }

    public MaxMinSupported getMmSupported() {
        return mmSupported;
    }

    public void setMmSupported(MaxMinSupported mmSupported) {
        this.mmSupported = mmSupported;
    }
    
    public List<PaperSize> getSupportPaperSizeList() {
        return supportPaperSizeList;
    }

    public void setSupportPaperSizeList(List<PaperSize> supportPaperSizeList) {
        this.supportPaperSizeList = supportPaperSizeList;
    }
}
