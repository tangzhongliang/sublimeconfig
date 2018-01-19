package jp.co.ricoh.advop.idcardscanprint.print;

import android.os.AsyncTask;

import jp.co.ricoh.advop.cheetahutil.util.CUIUtil;
import jp.co.ricoh.advop.cheetahutil.util.CUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.application.CheetahApplication;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.attribute.Severity;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.PrintServiceAttributeSet;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PrinterState;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PrinterStateReason;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PrinterStateReasons;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.ScanServiceAttributeSet;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.OccuredErrorLevel;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.ScannerState;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.ScannerStateReasons;
import jp.co.ricoh.advop.idcardscanprint.ui.activity.SplashActivity;
import jp.co.ricoh.advop.idcardscanprint.util.Const;

import java.util.Set;

public class PrintSysAlertDisplay {

    /**
     * システム警告画面が表示されているかのフラグ
     * Flag to indicate if system warning screen is displayed
     */
    private volatile boolean mAlertDialogDisplayed = false;
    
    private PrintManager printManager;
    private CheetahApplication application;
    
    private boolean dispErrorOnlyInJobRunning = true;
    
    public PrintSysAlertDisplay() {
        printManager = CHolder.instance().getPrintManager();
        application = CHolder.instance().getApplication();
        dispErrorOnlyInJobRunning = true;
    }
    
    private boolean needDisp() {
        if (dispErrorOnlyInJobRunning) {
            return printManager.isJobRunning();
        }
        return true;
    }
    
    public void initAlertDialogDisplayed() {
        this.mAlertDialogDisplayed = false;
    }

    public void showOnResume() {
        new AsyncTask<Void, Void, PrintServiceAttributeSet>() {

            @Override
            protected PrintServiceAttributeSet doInBackground(Void... params) {
                return printManager.getPrintService().getAttributes();
            }

            @Override
            protected void onPostExecute(PrintServiceAttributeSet attributes) {
                PrinterState state = (PrinterState)attributes.get(PrinterState.class);
                PrinterStateReasons reasons = (PrinterStateReasons)attributes.get(PrinterStateReasons.class);

                String reasonString = makeAlertStateReasonString(reasons);

                if (!CUtil.isStringEmpty(reasonString)) {
                    
                    if (!needDisp()) {
                        return;
                    }
                    
                    String stateString = makeAlertStateString(state);
                    LogC.d("mApplication.displayAlertDialog: " + stateString + " ||| " + reasonString);

                    application.displayAlertDialog(Const.ALERT_DIALOG_APP_TYPE_PRINTER, stateString, reasonString);

                    mAlertDialogDisplayed = true;
                }
            }
            
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);//.execute();
    }

    /**
     * システム警告画面の表示有無を判断し、必要な場合は表示要求を行う非同期タスクです。
     * The asynchronous task to judge to display system warning screen and to request to display the screen if necessary.
     */
    class AlertDialogDisplayTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {       
            // show legacy error
            PrintServiceAttributeSet attributes = printManager.getPrintService().getAttributes();
            PrinterState state = (PrinterState)attributes.get(PrinterState.class);
            PrinterStateReasons reasons = (PrinterStateReasons)attributes.get(PrinterStateReasons.class);

            String reasonString = makeAlertStateReasonString(reasons);

            if (!CUtil.isStringEmpty(reasonString)) {
                if (isCancelled()) {
                    return null;
                }
                
                if (!needDisp()) {
                    return null;
                }
                
                String stateString = makeAlertStateString(state);
                LogC.d("mApplication.displayAlertDialog: " + stateString + " ||| " + reasonString);

                application.displayAlertDialog(Const.ALERT_DIALOG_APP_TYPE_PRINTER, stateString, reasonString);

                mAlertDialogDisplayed = true;
            }
            return null;
        }

    }
    
    public void showOnServiceAttributeChange(PrinterState state, PrinterStateReasons reasons) {
        // show legacy error
        String reasonString = makeAlertStateReasonString(reasons);

        if (!CUtil.isStringEmpty(reasonString)) {
            String stateString = makeAlertStateString(state);
            
            LogC.d("mApplication.displayAlertDialog: " + stateString + " ||| " + reasonString);

            if (!needDisp()) {
                return;
            }

            if (mAlertDialogDisplayed) {
                application.updateAlertDialog(Const.ALERT_DIALOG_APP_TYPE_PRINTER, stateString, reasonString);
            } else if (CUIUtil.isForegroundApp(application)) {
                application.displayAlertDialog(Const.ALERT_DIALOG_APP_TYPE_PRINTER, stateString, reasonString);
                mAlertDialogDisplayed = true;
            }

        } else {
            // Error -> Normal
            if (mAlertDialogDisplayed) {
                String activityName = CUIUtil.getTopActivityClassName(application);
                if (activityName == null) {
                    activityName = SplashActivity.class.getName(); //TODO
                }
                application.hideAlertDialog(Const.ALERT_DIALOG_APP_TYPE_PRINTER, activityName);
                mAlertDialogDisplayed = false;
            }
        }
    } 

    private String makeAlertStateString(PrinterState state) {
        String stateString = "";
        if (state != null) {
            stateString = state.toString();
        }
        return stateString;
    }
    
    private String makeAlertStateReasonString(PrinterStateReasons reasons) {
        String reasonString = null;
        if (reasons != null) {
            Set<PrinterStateReason> set = reasons.printerStateReasonSet(Severity.ERROR);
            if (set != null) {
                for (PrinterStateReason reason : set) {
                    if (reason != null) {
                        reasonString = reason.toString();
                        if (!CUtil.isStringEmpty(reasonString)) {
                            break;
                        }
                    }
                }
            }
        }
        return reasonString;
    }

}
