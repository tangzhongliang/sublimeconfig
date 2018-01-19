
package jp.co.ricoh.advop.idcardscanprint.model;

import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PrintColor;

public class PrintDataSetting {
    private PrintColor printColor;
    private Integer printCount;

    public PrintDataSetting(PrintColor printColor, Integer printCount) {
        this.printColor = printColor;
        this.printCount = printCount;
    }

    public PrintColor getPrintColor() {
        return printColor;
    }

    public void setPrintColor(PrintColor printColor) {
        this.printColor = printColor;
    }

    public Integer getPrintCount() {
        return printCount;
    }

    public void setPrintCount(Integer printCount) {
        this.printCount = printCount;
    }

}
