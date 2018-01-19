package jp.co.ricoh.advop.idcardscanprint.scan;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Editable;

import jp.co.ricoh.advop.cheetahutil.util.CUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.cheetahutil.widget.UserCodeButtonDialog;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.application.CheetahApplication;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.logic.JobManager.EXE_RESULT;
import jp.co.ricoh.advop.idcardscanprint.scan.ScanStateMachine.ScanEvent;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.common.SmartSDKApplication;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.common.impl.AsyncConnectState;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.ScanJob;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.ScanService;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.ScanJobAttributeSet;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.OccuredErrorLevel;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.ScanJobScanningInfo;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.ScanJobSendingInfo;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.ScanJobState;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.ScanJobStateReason;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.ScanJobStateReasons;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.ScannerRemainingMemory;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.ScannerState;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.ScannerStateReason;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.ScannerStateReasons;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.event.ScanJobAttributeEvent;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.event.ScanJobAttributeListener;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.event.ScanJobEvent;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.event.ScanJobListener;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.event.ScanServiceAttributeEvent;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.event.ScanServiceAttributeListener;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.impl.service.FunctionMessageDispatcher;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.InvalidResponseException;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.service.scanner.AuthRestriction;
import jp.co.ricoh.advop.idcardscanprint.ui.activity.SplashActivity;
import jp.co.ricoh.advop.idcardscanprint.util.Const;

import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class ScanManager {
    private final static String TAG = ScanManager.class.getSimpleName();
    private CheetahApplication application;

    /**
     * スキャン設定
     * Scan setting
     */
    private ScanSettingDataHolder mScanSettingDataHolder;

    /**
     * スキャンサービス
     * Scan service
     */
    private ScanService mScanService;

    /**
     * スキャンジョブ
     * Scan job
     */
    private ScanJob mScanJob;

    /**
     * スキャンジョブ状態変化監視リスナー
     * Scan job listener
     */
    private ScanJobListener mScanJobListener;

    /**
     * スキャンジョブ属性変化監視リスナー
     * Scan job attribute listener
     */
    private ScanJobAttributeListener mScanJobAttrListener;

    /**
     * スキャンサービス状態リスナー
     * Scan service attribute listener
     */
    private ScanServiceAttributeListener mScanServiceAttrListener;

    /**
     * ステートマシン
     * Statemachine
     */
    private ScanStateMachine mStateMachine;

    /**
     * 読み取ったページ数
     * Number of pages scanned
     */
    private int scannedPages;
    
    private int remainMemory;

    /**
     * 次原稿受付までの最大待ち時間です。
     * 0を指定した場合は、待ち続けます。
     * Maximum waiting time for accept the next page.
     * This timeout value supports "0" which means "keep waiting forever".
     */
    private int timeOfWaitingNextOriginal = 0;
    
    private ScanSysAlertDisplay scanSysAlertDisplay;
    
    private boolean needUserCodeAuth;
    private String userCode = null;


    public ScanManager() {
        application = CHolder.instance().getApplication();
    }
    
    public void onAppInit() {
        mScanSettingDataHolder = new ScanSettingDataHolder();
        mStateMachine = new ScanStateMachine(new Handler());
        mScanService = ScanService.getService();
        scanSysAlertDisplay = new ScanSysAlertDisplay();
        mScanServiceAttrListener = new ScanServiceAttributeListenerImpl();
        initJobSetting();
    }
    
    public void onAppDestroy() {
        mScanSettingDataHolder = null;
        mScanService = null;
        mScanJob = null;
        mScanJobListener = null;
        mScanJobAttrListener = null;
        mStateMachine = null;
    }
    
    public boolean isJobRunning() {
        return mStateMachine.isJobRunning();
    }

    // run async
    public EXE_RESULT intiScanService(AsyncTask task) {
        // init scan service
        // from scan sample mainAct
        
        /*
         * 非同期でスキャンサービスとの接続を行います。
         * [処理内容]
         *   (1)スキャンサービスのイベントを受信するリスナーを設定します。
         *      機器が利用可能になるか、キャンセルが押されるまでリトライします。
         *   (2)非同期イベントの接続確認を行います。
         *      接続可能になるか、キャンセルが押されるまでリトライします。
         *   (3)接続に成功した場合は、スキャンサービスから各設定の設定可能値を取得します。
         *
         * Connects with the scan service asynchronously.
         * [Processes]
         *   (1) Sets the listener to receive scan service events.
         *       This task repeats until the machine becomes available or cancel button is touched.
         *   (2) Confirms the asynchronous connection.
         *       This task repeats until the connection is confirmed or cancel button is touched.
         *   (3) After the machine becomes available and connection is confirmed,
         *       obtains job setting values.
         */
        AsyncConnectState addListenerResult = null;
        AsyncConnectState getAsyncConnectStateResult = null;
        
        //(1)
        while (true) {
            if(task.isCancelled()) {
                return EXE_RESULT.CANCELED;
            }
            addListenerResult = mScanService.addScanServiceAttributeListener(mScanServiceAttrListener);

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
            getAsyncConnectStateResult = mScanService.getAsyncConnectState();

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
           if (!LogC.runInEMU) {
                try {
                    userCode = null;
                    EXE_RESULT ret = checkRestrict();
                    if (ret != EXE_RESULT.SUCCESSED) {
                        mStateMachine.procScanEvent(ScanEvent.ACTIVITY_BOOT_FAILED);
                        return ret;
                    }
                } catch (InvalidResponseException e) {
                    LogC.e("checkRestrict failed!", e);
                    mStateMachine.procScanEvent(ScanEvent.ACTIVITY_BOOT_FAILED);
                    return EXE_RESULT.ERR_INIT;
                }
            }
            
            boolean ret = mScanSettingDataHolder.init(mScanService);
            if (!ret) {
                return EXE_RESULT.ERR_INIT;
            } 
        }
        
        // check result
        return checkInitResult(addListenerResult, getAsyncConnectStateResult);
    }

//    /** キーカウンタ */
//    public static final String KEY_COUNTER = "key_counter";
//    /** キーカウンタ（機能） */
//    public static final String KEY_COUNTER_FUNC = "key_counter_func";
//    /** キーカード */
//    public static final String KEY_CARD = "key_card";
//    /** キーカード（機能） */
//    public static final String KEY_CARD_FUNC = "key_card_func";
    /** ユーザコード */
    public static final String USER_CODE = "user_code";
    /** ユーザコード（機能） */
    public static final String USER_CODE_FUNC = "user_code_func";
//    /** コインラック */
//    public static final String COIN_BOX = "coin_box";
//    /** コインラック（機能） */
//    public static final String COIN_BOX_FUNC = "coin_box_func";
//    public static final String COIN_BOX_PAPER_SIZE = "coin_box_paper_size";
//    public static final String COIN_BOX_PAPER_SIZE_FUNC = "coin_box_paper_size_func";
//    /** 個人認証 */
//    public static final String PERSONAL_AUTHENTICATION = "personal_authentication";
//    /** その他 */
//    public static final String THE_OTHERS = "the_others";
    private EXE_RESULT checkRestrict() throws InvalidResponseException {
        needUserCodeAuth = false;
        if(CHolder.instance().getApplication().getSystemStateMonitor().isMachineAdmin()) {
            return EXE_RESULT.SUCCESSED;
        }
        FunctionMessageDispatcher  dispatcher = FunctionMessageDispatcher.getInstance();
        AuthRestriction auth = dispatcher.getScanAuthRestriction("");
        if (!auth.getLoginPermission()) {        
            List<String> restrictionPanelId = auth.getRestrictionPanelId();
            if(restrictionPanelId != null && restrictionPanelId.size() > 0) {
                for (String panelID : restrictionPanelId) {
                    String panelInfo = dispatcher.createScanRestrictionPanelInfo(panelID);
                    if (CUtil.isStringEmpty(panelInfo)) {
                        continue;
                    }
                    if (panelInfo.equals(USER_CODE) || panelInfo.equals(USER_CODE_FUNC)) {
                        needUserCodeAuth = true;
                        break;
                    }
    //                if (panelInfo.equals(KEY_COUNTER) 
    //                        || panelInfo.equals(KEY_COUNTER_FUNC)
    //                        || panelInfo.equals(KEY_CARD)
    //                        || panelInfo.equals(KEY_CARD_FUNC)
    //                        || panelInfo.equals(COIN_BOX)
    //                        || panelInfo.equals(COIN_BOX_FUNC)
    //                        || panelInfo.equals(COIN_BOX_PAPER_SIZE)
    //                        || panelInfo.equals(COIN_BOX_PAPER_SIZE_FUNC)
    //                        || panelInfo.equals(THE_OTHERS)) {
    //                    return EXE_RESULT.ERR_RESTRICT_DEVICE_INSTALLED;
    //                }
                }
            }
        }
        
        if (dispatcher.isOptionalCounterExist()) {
            return EXE_RESULT.ERR_RESTRICT_DEVICE_INSTALLED;
        }
        return EXE_RESULT.SUCCESSED;
    }
    
    private EXE_RESULT checkInitResult(AsyncConnectState addListenerResult, AsyncConnectState getAsyncConnectStateResult) {

        if (addListenerResult != null 
                && getAsyncConnectStateResult != null 
                && addListenerResult.getState() == AsyncConnectState.STATE.CONNECTED
                && getAsyncConnectStateResult.getState() == AsyncConnectState.STATE.CONNECTED) {
            mStateMachine.procScanEvent(ScanEvent.ACTIVITY_BOOT_COMPLETED);
            return EXE_RESULT.SUCCESSED;
        }
        else {
            if (addListenerResult != null ) {
                LogC.d(TAG, "addScanServiceAttributeListener:" + addListenerResult.getState() + "," + addListenerResult.getErrorCode());
            }
            if (getAsyncConnectStateResult != null ) {
                LogC.d(TAG, "getAsyncConnectState:" + getAsyncConnectStateResult.getState() + "," + getAsyncConnectStateResult.getErrorCode());
            }
            // the connection is invalid.
            LogC.d(TAG, "init scan service failed");
            mStateMachine.procScanEvent(ScanEvent.ACTIVITY_BOOT_FAILED);
            return EXE_RESULT.FAILED;
        }
    }
    
    public void destroyScanService() {
        mStateMachine.procScanEvent(ScanEvent.ACTIVITY_DESTROYED);
        if (mScanServiceAttrListener != null) {
            mScanService.removeScanServiceAttributeListener(mScanServiceAttrListener);
        }
    }
    
    /**
     * スキャンジョブを初期化します。
     * Initializes the scan job.
     */
    public void initJobSetting() {
        //If a state change listener is registered to the current scan job, the listener is removed.
        if(mScanJob!=null) {
            if(mScanJobListener!=null) {
                mScanJob.removeScanJobListener(mScanJobListener);
            }
            if(mScanJobAttrListener!=null) {
                mScanJob.removeScanJobAttributeListener(mScanJobAttrListener);
            }
        }

        mScanJob = new ScanJob();
        mScanJobListener = new ScanJobListenerImpl();
        mScanJobAttrListener = new ScanJobAttributeListenerImpl();

        //Registers a new listener to the new scan job.
        mScanJob.addScanJobListener(mScanJobListener);
        mScanJob.addScanJobAttributeListener(mScanJobAttrListener);
        

    }

    /**
     * スキャンジョブの属性変化監視リスナー実装クラスです。
     * [処理内容]
     *    (1)読み取り情報があれば、ステートマシンに通知します。
     *    (2)送信情報があれば、ステートマシンに通知します。
     *
     * The class to implement the listener to monitor scan job state changes.
     * [Processes]
     *    (1) If scan information exists, the information is notified to the state machine.
     *    (2) If data transfer information exists, the information is notified to the state machine.
     */
    class ScanJobAttributeListenerImpl implements ScanJobAttributeListener {

        @Override
        public void updateAttributes(ScanJobAttributeEvent attributesEvent) {
            ScanJobAttributeSet attributes = attributesEvent.getUpdateAttributes();
            mStateMachine.procScanEvent(ScanEvent.UPDATE_JOB_STATE_PROCESSING, attributes);

            //(1)
            ScanJobScanningInfo scanningInfo = (ScanJobScanningInfo) attributes.get(ScanJobScanningInfo.class);
            if (scanningInfo != null && scanningInfo.getScanningState()==ScanJobState.PROCESSING) {
                String status = application.getString(R.string.txid_scan_g_running_scan) + " "
                        + String.format(application.getString(R.string.txid_scan_d_count), scanningInfo.getScannedCount());
                scannedPages = Integer.valueOf(scanningInfo.getScannedCount());
                mStateMachine.procScanEvent(ScanEvent.UPDATE_JOB_STATE_PROCESSING, status);
            }
            if (scanningInfo != null && scanningInfo.getScanningState() == ScanJobState.PROCESSING_STOPPED) {
                timeOfWaitingNextOriginal = Integer.valueOf(scanningInfo.getRemainingTimeOfWaitingNextOriginal());
            }

            //(2)
            ScanJobSendingInfo sendingInfo = (ScanJobSendingInfo) attributes.get(ScanJobSendingInfo.class);
            if (sendingInfo != null && sendingInfo.getSendingState()==ScanJobState.PROCESSING) {
                String status = application.getString(R.string.txid_sending);
                mStateMachine.procScanEvent(ScanEvent.UPDATE_JOB_STATE_PROCESSING, status);
            }
        }

    }

    /**
     * スキャンジョブの状態監視リスナーです。
     * The listener to monitor scan job state changes.
     */
    class ScanJobListenerImpl  implements ScanJobListener {

        @Override
        public void jobCanceled(ScanJobEvent event) {
            // set application state to normal
            application.setAppState(SplashActivity.class.getName(), SmartSDKApplication.APP_STATE_NORMAL, SmartSDKApplication.APP_STATE_NORMAL_MSG, SmartSDKApplication.APP_TYPE_SCANNER);
            
            mStateMachine.procScanEvent(ScanEvent.CHANGE_JOB_STATE_CANCELED);
        }

        @Override
        public void jobCompleted(ScanJobEvent event) {
            // set application state to normal
            application.setAppState(SplashActivity.class.getName(), SmartSDKApplication.APP_STATE_NORMAL, SmartSDKApplication.APP_STATE_NORMAL_MSG, SmartSDKApplication.APP_TYPE_SCANNER);
            
            mStateMachine.procScanEvent(ScanEvent.CHANGE_JOB_STATE_COMPLETED);
        }

        @Override
        public void jobAborted(ScanJobEvent event) {
            // set application state to normal
            application.setAppState(SplashActivity.class.getName(), SmartSDKApplication.APP_STATE_NORMAL, SmartSDKApplication.APP_STATE_NORMAL_MSG, SmartSDKApplication.APP_TYPE_SCANNER);
            
            ScanJobAttributeSet attributes = event.getAttributeSet();
            mStateMachine.procScanEvent(ScanEvent.CHANGE_JOB_STATE_ABORTED, attributes);
        }

        @Override
        public void jobProcessing(ScanJobEvent event) {
            // set application state to normal
            application.setAppState(SplashActivity.class.getName(), SmartSDKApplication.APP_STATE_NORMAL, SmartSDKApplication.APP_STATE_NORMAL_MSG, SmartSDKApplication.APP_TYPE_SCANNER);
            
            ScanJobAttributeSet attributes = event.getAttributeSet();
            mStateMachine.procScanEvent(ScanEvent.CHANGE_JOB_STATE_PROCESSING, attributes);
        }

        @Override
        public void jobPending(ScanJobEvent event) {
            // set application state to normal
            application.setAppState(SplashActivity.class.getName(), SmartSDKApplication.APP_STATE_NORMAL, SmartSDKApplication.APP_STATE_NORMAL_MSG, SmartSDKApplication.APP_TYPE_SCANNER);
            
            mStateMachine.procScanEvent(ScanEvent.CHANGE_JOB_STATE_PENDING);
        }

        /**
         * ジョブが一時停止状態になった際に呼び出されます。
         * 状態の理由が複数ある場合は、最初の１つのみを参照します。
         * [処理内容]
         *   (1)原稿ガラススキャン時、次原稿待ちの一時停止イベントだった場合
         *      -ステートマシンに次原稿待ちイベントを送信します。
         *   (2)プレビュー表示のための一時停止イベントだった場合
         *      -ステートマシンにプレビュー表示のイベントを送信します。
         *   (3)その他の理由による一時停止イベントだった場合
         *      -ここでは、ジョブをキャンセルします。
         *
         * Called when the job is in paused state.
         * If multiple reasons exist, only the first reason is checked.
         * [Processes]
         *   (1) For the pause event for waiting for the next document when using exposure glass
         *        - Sends the document waiting event to the state machine.
         *   (2) For the pause event for preview display
         *        - Sends the preview display event to the state machine.
         *   (3) For the pause event for other reasons
         *        - The job is cancelled in this case.
         */
        @Override
        public void jobProcessingStop(ScanJobEvent event) {
            ScanJobAttributeSet attributes = event.getAttributeSet();

            ScanJobStateReasons reasons = (ScanJobStateReasons) attributes.get(ScanJobStateReasons.class);
            if (reasons != null) {
                Set<ScanJobStateReason> reasonSet = reasons.getReasons();
                
                boolean isError = false;
                Iterator<ScanJobStateReason> iter = reasonSet.iterator();
                ScanJobStateReason jobStateReason = null;
                while(iter.hasNext()){
                    jobStateReason = iter.next();
                    switch(jobStateReason){
                        case SCANNER_JAM:
                        case MEMORY_OVER:
                        case EXCEEDED_MAX_EMAIL_SIZE:
                        case EXCEEDED_MAX_PAGE_COUNT:
                            isError = true;
                            break;
                        default :
                            isError = false;
                            break;
                    }
                    
                    if(isError){
                        break;
                    }                        
                }
                if(isError){
                    // set application state to error
                    application.setAppState(SplashActivity.class.getName(), SmartSDKApplication.APP_STATE_ERROR, jobStateReason.toString(), SmartSDKApplication.APP_TYPE_SCANNER);
                }
                else{
                    // set application state to normal
                    if(reasonSet.contains(ScanJobStateReason.WAIT_FOR_ORIGINAL_PREVIEW_OPERATION)) {
                        //TODO
                        //application.setAppState(PreviewActivity.class.getName(), SmartSDKApplication.APP_STATE_NORMAL, SmartSDKApplication.APP_STATE_NORMAL_MSG, SmartSDKApplication.APP_TYPE_SCANNER);
                    }
                    else{
                        application.setAppState(SplashActivity.class.getName(), SmartSDKApplication.APP_STATE_NORMAL, SmartSDKApplication.APP_STATE_NORMAL_MSG, SmartSDKApplication.APP_TYPE_SCANNER);
                    }
                }

                // show Preview window
                if (reasonSet.contains(ScanJobStateReason.WAIT_FOR_ORIGINAL_PREVIEW_OPERATION)) {
                    mStateMachine.procScanEvent(ScanEvent.CHANGE_JOB_STATE_STOPPED_PREVIEW);
                    return;
                }

                // show CountDown window
                if (reasonSet.contains(ScanJobStateReason.WAIT_FOR_NEXT_ORIGINAL_AND_CONTINUE)) {
                    mStateMachine.procScanEvent(ScanEvent.CHANGE_JOB_STATE_STOPPED_COUNTDOWN);
                    return;
                }

                // show Other reason job stopped window
                StringBuilder sb = new StringBuilder();
                for (ScanJobStateReason reason : reasonSet) {
                    //sb.append(reason.toString()).append("\n");
                    LogC.e("scan job error:" + reason);
                    if(Const.SCAN_ERROR_MAP.get(reason) != null) {
                        sb.append(CHolder.instance().getActivity().getString(Const.SCAN_ERROR_MAP.get(reason)));
                    } 
                }
                
                if(CUtil.isStringEmpty(sb.toString())) {
                   LogC.e("scan error other reason");
                   sb.append(CHolder.instance().getActivity().getString(R.string.scan_fail));                    
                }
                
                mStateMachine.procScanEvent(ScanEvent.CHANGE_JOB_STATE_STOPPED_OTHER, sb.toString());
                return;
            }
            
            // set application state to normal
            application.setAppState(SplashActivity.class.getName(), SmartSDKApplication.APP_STATE_NORMAL, SmartSDKApplication.APP_STATE_NORMAL_MSG, SmartSDKApplication.APP_TYPE_SCANNER);
                        
            // Unknown job stop reason
            mStateMachine.procScanEvent(ScanEvent.CHANGE_JOB_STATE_STOPPED_OTHER, "(unknown reason)");
        }
    }

    /**
     * スキャンサービスを取得します。
     * obtains the scan service
     */
    public ScanService getScanService() {
        return mScanService;
    }

    /**
     * スキャンジョブを取得します。
     * Obtains the scan job.
     */
    public ScanJob getScanJob() {
        return mScanJob;
    }

    /**
     * スキャンジョブの設定データ保持クラスのインスタンスを取得します。
     * Obtains the instance for the class to save scan setting data.
     */
    public ScanSettingDataHolder getScanSettingDataHolder() {
        return mScanSettingDataHolder;
    }

    /**
     * このアプリのステートマシンを取得します。
     * Obtains the statemachine.
     */
    public ScanStateMachine getStateMachine() {
        return mStateMachine;
    }
    
    /**
     * スキャンページ総数を取得します。
     * Obtains the number of total pages scanned.
     */
    public int getScannedPages() {
        return scannedPages;
    }
    
    public void setScannedPages(int pages){
        scannedPages = pages;
    }
    public int getRemainMemory() {
        return remainMemory;
    }

    /**
     * 次原稿までの最大待ち時間を取得します。
     * Obtains the maximum waiting time.
     * @return
     */
    public int getTimeOfWaitingNextOriginal() {
        return timeOfWaitingNextOriginal;
    }
    
    public ScanSysAlertDisplay getScanSysAlertDisplay() {
        return scanSysAlertDisplay;
    }
    
    public boolean needUserCodeAuth() {
        return needUserCodeAuth;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    /**
     * スキャナサービスの状態変更監視リスナーです。
     * [処理内容]
     *   (1)スキャンサービスの状態によって、スキャンサービス状態表示ラベルを書き換えます。
     *   (2)エラー画面の表示・更新・非表示要求を行います。
     * The listener class to monitor scan service attribute changes.
     * [Processes]
     *   (1) Rewrites the scan service state display label accordingly to the scan service state.
     *   (2) Requests to display/update/hide error screens.
     */
    class ScanServiceAttributeListenerImpl implements ScanServiceAttributeListener {

        @Override
        public void attributeUpdate(final ScanServiceAttributeEvent event) {
            ScannerState state = (ScannerState)event.getAttributes().get(ScannerState.class);
            ScannerStateReasons stateReasons = (ScannerStateReasons)event.getAttributes().get(ScannerStateReasons.class);
            OccuredErrorLevel errorLevel = (OccuredErrorLevel) event.getAttributes().get(OccuredErrorLevel.class);
            ScannerRemainingMemory memory = (ScannerRemainingMemory)event.getAttributes().get(ScannerRemainingMemory.class);
            if (memory != null) {
                remainMemory = memory.getRemainingMemory();
                LogC.d("remainMemory="+ remainMemory);
            }
            
            //(1)
            switch(state) {
            case IDLE :
                LogC.d(TAG, "ScannerState : IDLE");
                break;
            case MAINTENANCE:
                LogC.d(TAG, "ScannerState : MAINTENANCE");
                break;
            case PROCESSING:
                LogC.d(TAG, "ScannerState : PROCESSING");
                break;
            case STOPPED:
                LogC.d(TAG, "ScannerState : STOPPED");
                break;
            case UNKNOWN:
                LogC.d(TAG, "ScannerState : UNKNOWN");
                break;
            default:
                LogC.d(TAG, "ScannerState : never reach here ...");
                /* never reach here */
                break;
            }

            if( stateReasons != null ) {
                Set<ScannerStateReason> reasonSet = stateReasons.getReasons();
                for(ScannerStateReason reason : reasonSet) {
                    switch(reason) {
                        case COVER_OPEN:
                            LogC.d(TAG, "ScannerState : COVER_OPEN");
                            break;
                        case MEDIA_JAM:
                            LogC.d(TAG, "ScannerState : MEDIA_JAM");
                            break;
                        case PAUSED:
                            LogC.d(TAG, "ScannerState : PAUSED");
                            break;
                        case OTHER:
                            LogC.d(TAG, "ScannerState : OTHER");
                            break;
                        default:
                            /* never reach here */
                            break;
                    }
                }
            }

            //(2)
            scanSysAlertDisplay.showOnServiceAttributeChange(state, stateReasons, errorLevel);
        }
    }
    
    public interface ScanJobContinueCB{
        void scanJobContinue();
    }
    
    public void getScanAuthInfo(Context context, final ScanJobContinueCB cb){
        LogC.d("start auth scan user code");
        final UserCodeButtonDialog dialog = UserCodeButtonDialog.createUserCodeDialog(context, UserCodeButtonDialog.USER_CODE_SCAN);
        
        dialog.setDialogCB(dialog.new UserCodeCheckCallBack() {
            Editable input = null;
            
            @Override
            public void onCheckStart(Editable input) {
                this.input = input;
            }
            
            @Override
            public void run() {
                AuthRestriction auth = null;
                try {
                    auth = FunctionMessageDispatcher.getInstance().getScanAuthRestriction(input.toString());
                } catch (InvalidResponseException e) {
                    LogC.d(e.getMessage());
                }
                onCheckEnd(auth.getLoginErrorReason(), false);
                if (auth.getLoginPermission()) {
                    setUserCode(input.toString());
                    cb.scanJobContinue();
                }    
                LogC.d("check input user code "+(auth.getLoginPermission()?"success":"fail")+" error message is "+auth.getLoginErrorReason());
            }

            @Override
            public void setJobContinue(boolean isContinue) {
                // TODO Auto-generated method stub
                
            }
        });
        
        if (needUserCodeAuth()) {
            dialog.show();
        } else {
            cb.scanJobContinue();
        }
    }
}
