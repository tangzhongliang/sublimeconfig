package jp.co.ricoh.advop.idcardscanprint.print;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Editable;

import jp.co.ricoh.advop.cheetahutil.util.CUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.cheetahutil.widget.UserCodeButtonDialog;
import jp.co.ricoh.advop.idcardscanprint.application.CheetahApplication;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.logic.JobManager.EXE_RESULT;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.common.impl.AsyncConnectState;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.PrintFile;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.PrintFile.PDL;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.PrintJob;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.PrintService;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.PrintJobAttributeSet;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.PrintServiceAttributeSet;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PrintColor;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PrintJobPrintingInfo;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PrintJobState;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PrintJobStateReasons;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PrinterStateReasons;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.UserCodeRestrictedFunc;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.event.PrintJobAttributeEvent;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.event.PrintJobAttributeListener;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.impl.service.FunctionMessageDispatcher;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.InvalidResponseException;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.service.printer.AuthRestriction;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.service.printer.PrintConfiguration;

import java.util.List;


public class PrintManager {
    private final static String TAG = PrintManager.class.getSimpleName();
    private CheetahApplication application;

    private PrintService mPrintService;
    private PrintJob mPrintJob;
    private PrintJobAttributeListener mJobAttributeListener;

    private PrintStateMachine mStateMachine;
    
    /**
     * 印刷に必要な設定値
     * Print settings.
     */
    private PrintSettingDataHolder printSettingDataHolder;

    /**
     * 機器に設定可能な印刷プロパティの一覧
     * Map of the supported print properties
     */
    private PrintSettingSupportedHolder mSettingSupportedHolders;
    
    /**
     * 印刷に実現するために必要なイベントを受け取るためのリスナー
     * Listener to receive print service attribute event.
     */
    private PrintServiceAttributeListenerImpl mServiceAttributeListener;


    private PrintSysAlertDisplay printSysAlertDisplay;

    private UserCodeRestrictedFunc  userCodeRestrictedFunc;
    private boolean needUserCodeAuth = false;
    private String userCode = null;
    
    public PrintManager() {
        application = CHolder.instance().getApplication();
    }
    
    public void onAppInit() {
        mStateMachine = new PrintStateMachine(new Handler());
        //mSettingSupportedHolders = new HashMap<PrintFile.PDL, PrintSettingSupportedHolder>();
        mPrintService = PrintService.getService();
        printSettingDataHolder = new PrintSettingDataHolder();
        printSysAlertDisplay = new PrintSysAlertDisplay();
    }
    
    public void onAppDestroy() {
    }
    
    public boolean isJobRunning() {
        return mStateMachine.isJobRunning();
    }
    
    // run async
    public EXE_RESULT intiPrintService(AsyncTask task) {

        mServiceAttributeListener = new PrintServiceAttributeListenerImpl();
        AsyncConnectState addListenerResult = null;
        AsyncConnectState getAsyncConnectStateResult = null;
        
        /*
         * UIスレッドのバックグラウンドで実行されるメソッドです。
         * [処理内容]
         *   (1)プリントサービスのイベントを受信するリスナーを設定します。
         *      機器が利用可能になるか、キャンセルが押されるまでリトライします。
         *   (2)非同期イベントの接続確認を行います。
         *      接続可能になるか、キャンセルが押されるまでリトライします。
         *   (3)接続に成功した場合は、プリントサービスから各設定の設定可能値を取得します。
         *
         * Runs in the background on the UI thread.
         * [Processes]
         *   (1) Sets the listener to receive print service events.
         *       This task repeats until the machine becomes available or cancel button is touched.
         *   (2) Confirms the asynchronous connection.
         *       This task repeats until the connection is confirmed or cancel button is touched.
         *   (3) After the machine becomes available and connection is confirmed,
         *       obtains job setting values.
         */ //(1)
        while (true) {
            if(task.isCancelled()) {
                return EXE_RESULT.CANCELED;
            }
            addListenerResult = mPrintService.addPrintServiceAttributeListener(mServiceAttributeListener);

            if (addListenerResult == null) {
                CUtil.sleep(100);
                continue;
            }

            if (addListenerResult.getState() == AsyncConnectState.STATE.CONNECTED) {
                break;
            }

            if (addListenerResult.getErrorCode() == AsyncConnectState.ERROR_CODE.NO_ERROR){
                // do nothing
            } else if (addListenerResult.getErrorCode() == AsyncConnectState.ERROR_CODE.BUSY) {
                CUtil.sleep(10000);
            } else if (addListenerResult.getErrorCode() == AsyncConnectState.ERROR_CODE.TIMEOUT){
                // do nothing
            } else if (addListenerResult.getErrorCode() == AsyncConnectState.ERROR_CODE.INVALID){
                return checkInitResult(addListenerResult, getAsyncConnectStateResult);
            } else {
                // unknown state
                return checkInitResult(addListenerResult, getAsyncConnectStateResult);
            }
        }

        if (addListenerResult.getState() != AsyncConnectState.STATE.CONNECTED) {
            return checkInitResult(addListenerResult, getAsyncConnectStateResult);
        }

        //(2)
        while (true) {
            if(task.isCancelled()) {
                return EXE_RESULT.CANCELED;
            }
            getAsyncConnectStateResult = mPrintService.getAsyncConnectState();

            if (getAsyncConnectStateResult == null) {
                CUtil.sleep(100);
                continue;
            }

            if (getAsyncConnectStateResult.getState() == AsyncConnectState.STATE.CONNECTED) {
                break;
            }

            if (getAsyncConnectStateResult.getErrorCode() == AsyncConnectState.ERROR_CODE.NO_ERROR){
                // do nothing
            } else if (getAsyncConnectStateResult.getErrorCode() == AsyncConnectState.ERROR_CODE.BUSY) {
                CUtil.sleep(10000);
            } else if (getAsyncConnectStateResult.getErrorCode() == AsyncConnectState.ERROR_CODE.TIMEOUT){
                // do nothing
            } else if (getAsyncConnectStateResult.getErrorCode() == AsyncConnectState.ERROR_CODE.INVALID){
                return checkInitResult(addListenerResult, getAsyncConnectStateResult);
            } else {
                // unknown state
                return checkInitResult(addListenerResult, getAsyncConnectStateResult);
            }
        }

        //(3)
        if (addListenerResult.getState() == AsyncConnectState.STATE.CONNECTED
                && getAsyncConnectStateResult.getState() == AsyncConnectState.STATE.CONNECTED) {

            List<PrintFile.PDL> supportedPDL = mPrintService.getSupportedPDL();
            if(supportedPDL == null) return EXE_RESULT.FAILED;
            
            // only jpg print, use tiff pdl
            if (supportedPDL.contains(PDL.TIFF)) {
                putPrintSettingSupportedHolder(PDL.TIFF, new PrintSettingSupportedHolder(mPrintService, PDL.TIFF));
            } else if (supportedPDL.contains(PDL.PDF)) {
                CHolder.instance().getJobData().setNeedPDFPrint(true);
                putPrintSettingSupportedHolder(PDL.PDF, new PrintSettingSupportedHolder(mPrintService, PDL.PDF));
            }
            //if (mSettingSupportedHolders.size() <= 0) {
            if (mSettingSupportedHolders == null) {
                LogC.w("not support PDL.TIFF and PDL.PDF");
                for (PDL pdl : supportedPDL) {
                    LogC.w(pdl.toString());
                }
                return EXE_RESULT.FAILED;
            }

            if (!LogC.runInEMU) {
                try {
                    userCode = null;
                    PrintConfiguration configuration = FunctionMessageDispatcher.getInstance().getPrintConfiguration();
                    needUserCodeAuth = configuration.getUserCodeRequired();
                    userCodeRestrictedFunc = configuration.getUserCodeFunc();
                } catch (InvalidResponseException e) {
                    LogC.e("get print user code auth failed!", e);
                    mStateMachine.procPrintEvent(PrintStateMachine.PrintEvent.CHANGE_APP_ACTIVITY_START_FAILED);
                    return EXE_RESULT.ERR_INIT;
                }
            }
            
            boolean ret = printSettingDataHolder.init(mPrintService);
            if (!ret) {
                return EXE_RESULT.ERR_INIT;
            } 
        }

        return checkInitResult(addListenerResult, getAsyncConnectStateResult);
    }    
    
    private EXE_RESULT checkInitResult(AsyncConnectState addListenerResult, AsyncConnectState getAsyncConnectStateResult) {

        if (addListenerResult != null 
                && getAsyncConnectStateResult != null 
                && addListenerResult.getState() == AsyncConnectState.STATE.CONNECTED
                && getAsyncConnectStateResult.getState() == AsyncConnectState.STATE.CONNECTED) {
            mStateMachine.procPrintEvent(PrintStateMachine.PrintEvent.CHANGE_APP_ACTIVITY_STARTED);;
            return EXE_RESULT.SUCCESSED;
        }
        else {
            if (addListenerResult != null ) {
                LogC.d(TAG, "addPrinterServiceAttributeListener:" + addListenerResult.getState() + "," + addListenerResult.getErrorCode());
            }
            if (getAsyncConnectStateResult != null ) {
                LogC.d(TAG, "getAsyncConnectState:" + getAsyncConnectStateResult.getState() + "," + getAsyncConnectStateResult.getErrorCode());
            }
            // the connection is invalid.
            LogC.d(TAG, "init Printer service failed");
            mStateMachine.procPrintEvent(PrintStateMachine.PrintEvent.CHANGE_APP_ACTIVITY_START_FAILED);
            return EXE_RESULT.FAILED;
        }
    }
    
    public void destroyPrintService() {
        mStateMachine.procPrintEvent(PrintStateMachine.PrintEvent.CHANGE_APP_ACTIVITY_DESTROYED);
        if(mServiceAttributeListener != null) {
            mPrintService.removePrintServiceAttributeListener(mServiceAttributeListener);
        }
    }
    
    /**
     * プリントサービスを取得します。
     * obtains the print service
     */
    public PrintService getPrintService() {
        return mPrintService;
    }

    public PrintSysAlertDisplay getPrintSysAlertDisplay() {
        return printSysAlertDisplay;
    }

    /**
     * プリント情報をセットします。
     * Sets the print setting object.
     * @param pdl
     * @param holder
     */
    public void putPrintSettingSupportedHolder(PrintFile.PDL pdl, PrintSettingSupportedHolder holder) {
        //mSettingSupportedHolders.put(pdl, holder);
        mSettingSupportedHolders = holder;
        //TODO  add pdl
        printSettingDataHolder.setSelectedPDL(pdl);
    }

    public PrintSettingDataHolder getPrintSettingDataHolder() {
        return printSettingDataHolder;
    }

    /**
     * 印刷を開始します。
     * Start print.
     * @param holder
     */
    public void startPrint() {
        mStateMachine.procPrintEvent(PrintStateMachine.PrintEvent.CHANGE_JOB_STATE_INITIAL, printSettingDataHolder);
        mStateMachine.procPrintEvent(PrintStateMachine.PrintEvent.CHANGE_JOB_STATE_PRE_PROCESS, printSettingDataHolder);
    }

    /**
     * プリントジョブを初期化します。
     * Initializes the print job.
     */
    public void initialJob() {
        if( mPrintJob != null) {
            if(mJobAttributeListener != null) {
                mPrintJob.removePrintJobAttributeListener(mJobAttributeListener);
            }
        }

        mPrintJob = new PrintJob();
        mJobAttributeListener = new PrintJobAttributeListenerImpl();

        mPrintJob.addPrintJobAttributeListener(mJobAttributeListener);

    }

    /**
     * プリントジョブを取得します。
     * Obtains the print job.
     */
    public PrintJob getPrintJob() {
        initialJob();
        return mPrintJob;
    }

    /**
     * このアプリのステートマシンを取得します。
     * Obtains the statemachine.
     */
    public PrintStateMachine getStateMachine() {
        return this.mStateMachine;
    }

    /**
     * プリントジョブの設定データ保持クラスのインスタンスを取得します。
     * Obtains the instance for the class to save print setting data.
     */
    public PrintSettingSupportedHolder getSettingSupportedDataHolders() {
        return this.mSettingSupportedHolders;
    }
    
//    public PrintConfiguration getConfiguration() {
//        return configuration;
//    }
    
    public boolean needUserCodeAuth() {
        return needUserCodeAuth;
    }

    public UserCodeRestrictedFunc getUserCodeRestrictedFunc() {
        return userCodeRestrictedFunc;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }
    
    /**
     * ジョブ状態を監視するリスナークラスです。
     * The listener class to monitor scan job state changes.
     */
    class PrintJobAttributeListenerImpl implements PrintJobAttributeListener {
        /**
         * ジョブ状態が変化すると呼び出されるメソッドです。
         * ジョブ状態を示すイベントを受信し、受信したイベントに応じてステートマシンにイベントをポストします。
         * Called when the job state changes.
         * Receives the job state event and posts the appropriate event to the statemachine.
         */
        @Override
        public void updateAttributes(PrintJobAttributeEvent attributesEvent) {
            PrintJobAttributeSet attributeSet = attributesEvent.getUpdateAttributes();

            PrintJobState jobState = (PrintJobState)attributeSet.get(PrintJobState.class);
            LogC.d(getClass().getSimpleName(), "JobState[" + jobState + "]");
            if(jobState == null) return;
            Object reasons = null;
            PrintServiceAttributeSet serviceAttributeSet = null;
            switch (jobState) {
                case PENDING:
                    mStateMachine.procPrintEvent(
                            PrintStateMachine.PrintEvent.CHANGE_JOB_STATE_PENDING, null);
                    break;
                case PROCESSING:
                    PrintJobPrintingInfo printingInfo = (PrintJobPrintingInfo)attributeSet.get(
                            PrintJobPrintingInfo.class);

                    mStateMachine.procPrintEvent(
                            PrintStateMachine.PrintEvent.CHANGE_JOB_STATE_PROCESSING, printingInfo);
                    break;
                case PROCESSING_STOPPED:
                    serviceAttributeSet = mPrintService.getAttributes();
                    reasons = (PrintJobStateReasons)attributeSet.get(PrintJobStateReasons.class);
                    if(reasons == null) {
                        reasons = (PrinterStateReasons) serviceAttributeSet.get((PrinterStateReasons.class));
                    }

                    mStateMachine.procPrintEvent(
                            PrintStateMachine.PrintEvent.CHANGE_JOB_STATE_PROCESSING_STOPPED, reasons);
                    break;
                case ABORTED:
                    serviceAttributeSet = mPrintService.getAttributes();
                     reasons = (PrintJobStateReasons)attributeSet.get(PrintJobStateReasons.class);
                     if(reasons == null) {
                         reasons = (PrinterStateReasons) serviceAttributeSet.get((PrinterStateReasons.class));
                     }

                    mStateMachine.procPrintEvent(
                            PrintStateMachine.PrintEvent.CHANGE_JOB_STATE_ABORTED, reasons);
                    break;
                case CANCELED:
                    mStateMachine.procPrintEvent(
                            PrintStateMachine.PrintEvent.CHANGE_JOB_STATE_CANCELED, null);
                    break;
                case COMPLETED:
                    mStateMachine.procPrintEvent(
                            PrintStateMachine.PrintEvent.CHANGE_JOB_STATE_COMPLETED, null);
                    break;
                default:
                    break;
            }
        }
    }

    
    public interface PrintJobContinueCB{
        void printJobContinue();
    }
    
    public void getPrintAuthInfo(Context context, final PrintColor color, final PrintJobContinueCB cb){
        LogC.d("start auth print user code");
        final UserCodeButtonDialog dialog = UserCodeButtonDialog.createUserCodeDialog(context, UserCodeButtonDialog.USER_CODE_PRINT);
        
        dialog.setDialogCB(dialog.new UserCodeCheckCallBack() {
            Editable input = null;
            boolean isJobCon = false;
            @Override
            public void onCheckStart(Editable input) {
                this.input = input;
            }
            
            @Override
            public void run() {
                if(isJobCon) {
                    setUserCode(input.toString());
                    cb.printJobContinue();
                    return;
                }
                AuthRestriction auth = null;
                try {
                    auth = jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.impl.service
                            .FunctionMessageDispatcher.getInstance().getPrintAuthRestriction(input.toString(), color);
                } catch (InvalidResponseException e) {
                    LogC.d(e.getMessage());
                }
                
                onCheckEnd(auth.getLoginErrorReason(), userCodeRestrictedFunc == UserCodeRestrictedFunc.AUTO_REGISTER);
                if (auth.getLoginPermission()) {
                    setUserCode(input.toString());
                    cb.printJobContinue();
                } 
                LogC.d("check input user code "+(auth.getLoginPermission()?"success":"fail")+" error message is "+auth.getLoginErrorReason());
            }

            @Override
            public void setJobContinue(boolean isContinue) {
                isJobCon = isContinue;                
            }
        });
        
        if (needUserCodeAuth()) {
            
            new  AsyncTask<Object, Object, Boolean>(){
                
                boolean b;
                
                @Override
                protected Boolean doInBackground(Object... params) {
                    
                    try {
                        b = getUserCode() == null ?// print user code is null?
                                FunctionMessageDispatcher.getInstance().getPrintAuthRestriction(CHolder.instance().getScanManager().getUserCode()
                                        , color).getLoginPermission()// auth by scan user code
                                : FunctionMessageDispatcher.getInstance()
                                .getPrintAuthRestriction(getUserCode()
                                        , color).getLoginPermission();// auth by print user code
                                        
                    } catch (InvalidResponseException e) {
                        LogC.d(e.getMessage());
                    }
                    return b;
                }
                
                @Override
                protected void onPostExecute(Boolean result) {
                    super.onPostExecute(result);
                    if (result) {
                        if (getUserCode() == null) {
                            setUserCode(CHolder.instance().getScanManager().getUserCode());
                        }
                        cb.printJobContinue();
                    } else {
                        LogC.d("check print user code fail, then check by input");
                        dialog.show();
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);//.execute();
        } else {
            cb.printJobContinue();
        }
        
    }
}
