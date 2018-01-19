package jp.co.ricoh.advop.idcardscanprint.logic.job;

import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.logic.JobManager.EXE_RESULT;
import jp.co.ricoh.advop.idcardscanprint.logic.JobManager.ExeCallback;

public class JobPrintAndDelete extends LongJob {

    public JobPrintAndDelete(ExeCallback exeCallback) {
        super(exeCallback);
    }
    
    @Override
    public void onStart() {
        if (jobData.isNeedPDFPrint()) {
            // TODO convert jpg to PDF
        }
        LogC.d("start print file!");
        CHolder.instance().getPrintManager().startPrint();
    }
    
    public void printFinished() {
        // TODO del
        
        complete(EXE_RESULT.SUCCESSED);
    }
}
