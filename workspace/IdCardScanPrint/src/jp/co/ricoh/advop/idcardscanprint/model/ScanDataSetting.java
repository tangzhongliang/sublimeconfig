
package jp.co.ricoh.advop.idcardscanprint.model;

import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.ScanColor;

public class ScanDataSetting {

    private ScanColor scanColor;
    private String scanFileType;

    public ScanDataSetting(ScanColor scanColor, String scanFileType) {
        this.scanColor = scanColor;
        this.scanFileType = scanFileType;
    }

    public ScanColor getScanColor() {
        return scanColor;
    }

    public void setScanColor(ScanColor scanColor) {
        this.scanColor = scanColor;
    }

    public String getScanFileType() {
        return scanFileType;
    }

    public void setScanFileType(String scanFileType) {
        this.scanFileType = scanFileType;
    }
}
