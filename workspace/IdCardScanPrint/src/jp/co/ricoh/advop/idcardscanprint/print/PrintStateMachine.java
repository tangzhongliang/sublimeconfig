/*
 *  Copyright (C) 2013-2015 RICOH Co.,LTD.
 *  All rights reserved.
 */
package jp.co.ricoh.advop.idcardscanprint.print;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import jp.co.ricoh.advop.cheetahutil.util.BaseDialogOnClickListener;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;

import jp.co.ricoh.advop.cheetahutil.util.CUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.cheetahutil.widget.BaseDialog;
import jp.co.ricoh.advop.cheetahutil.widget.MultiButtonDialog;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.application.CheetahApplication;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.logic.JpegProcessManager;
import jp.co.ricoh.advop.idcardscanprint.logic.JobManager.EXE_RESULT;
import jp.co.ricoh.advop.idcardscanprint.logic.job.JobPrintAndDelete;
import jp.co.ricoh.advop.idcardscanprint.logic.job.LongJob;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.attribute.Severity;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.PrintFile;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.PrintJob;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.PrintUserCode;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.PrintException;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.PrintRequestAttributeSet;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.PrintResponseException;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PrintJobPrintingInfo;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PrintJobStateReason;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PrintJobStateReasons;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PrinterStateReason;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PrinterStateReasons;
import jp.co.ricoh.advop.idcardscanprint.ui.fragment.PrintProcessingFragment;
import jp.co.ricoh.advop.idcardscanprint.util.Const;

import java.io.File;
import java.util.Map;
import java.util.Set;
//import jp.co.ricoh.advop.idcardscanprint.ui.DialogUtil;

/**
 * プリントサンプルのジョブステートマシン
 *  起動時及びジョブ発行後の以下のダイアログの生成・表示・破棄はすべてこのステートマシンを通して実行します
 * ・お待ちくださいダイアログ
 * ・印刷中ダイアログ
 *
 * Job state machine of print sample application.
 * Display/update/hide processes of the following dialog/screen are always executed by this statemachine.
 *   - Please wait dialog
 *   - Printing dialog
 */
public class PrintStateMachine {

    private static Handler mHandler;
    private static CheetahApplication mApplication;
    private static Context mContext;
    private static PrintProcessingFragment mProgressDialog;
//    private static ProgressDialog mPleaseWaitDialog;
//    private static AlertDialog mBootFailedDialog;
    private static PrintJob currentPrintJob;
    private static  MultiButtonDialog mJobStoppedProcessDialog;
    private static PrintManager printManager;
    
    
    PrintStateMachine(Handler handler) {
        printManager = CHolder.instance().getPrintManager();
        mApplication = CHolder.instance().getApplication();
        mHandler = handler;
        mContext = mApplication;
    }
    
    public boolean isJobRunning() {
        if (mState == State.STATE_APP_INITIAL
            || mState == State.STATE_JOB_ABORTED
            || mState == State.STATE_JOB_CANCELED
            || mState == State.STATE_JOB_COMPLETED
            || mState == State.STATE_JOB_INITIAL) {
            return false;
        }
        return true;
    }

    /**
     * 状態遷移するためのイベント
     * State transition event.
     */
    public enum PrintEvent {
        CHANGE_APP_ACTIVITY_INITIAL,
        CHANGE_APP_ACTIVITY_STARTED,
        CHANGE_APP_ACTIVITY_START_FAILED,
        CHANGE_APP_ACTIVITY_DESTROYED,
        CHANGE_JOB_STATE_INITIAL,
        CHANGE_JOB_STATE_PRE_PROCESS,
        CHANGE_JOB_STATE_PRE_PENDING,
        CHANGE_JOB_STATE_PENDING,
        CHANGE_JOB_STATE_PROCESSING,
        CHANGE_JOB_STATE_PROCESSING_STOPPED,
        CHANGE_JOB_STATE_COMPLETED,
        CHANGE_JOB_STATE_ABORTED,
        CHANGE_JOB_STATE_CANCELED,
        REQUEST_JOB_CANCEL,
    }

    /**
     * 現在のステートマシンの状態
     * Current state of the state machine
     */
    private State mState = State.STATE_APP_INITIAL;

    /**
     * アプリの状態を示します。
     * アプリの状態に応じた処理を定義します。
     * State definition.
     */
    public enum State {
        /**
         * 初期状態
         * Initial state
         */
        STATE_APP_INITIAL {
            @Override
            public State getNextState(PrintEvent event, Object param) {
                switch (event) {
                    case CHANGE_APP_ACTIVITY_INITIAL:
                        //showPleaseWaitDialog(mContext);
                        return STATE_APP_INITIAL;
                    case CHANGE_APP_ACTIVITY_STARTED:
                        //closePleaseWaitDialog();
                        return STATE_JOB_INITIAL;
                    case CHANGE_APP_ACTIVITY_START_FAILED:
                        //closePleaseWaitDialog();
                        //showBootFailedDialog();
                        return STATE_APP_INITIAL;
                    default:
                        return super.getNextState(event,param);
                }
            }
        },
        /**
         * ジョブ初期状態
         * Job initial state
         */
        STATE_JOB_INITIAL {
            @Override
            public State getNextState(PrintEvent event, Object param) {
                switch(event) {
                    case CHANGE_JOB_STATE_PRE_PROCESS:
                        if(param == null) return STATE_JOB_INITIAL;
                        if(!(param instanceof PrintSettingDataHolder)) return STATE_JOB_INITIAL;

                        new StartPrintJobTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (PrintSettingDataHolder) param);//execute((PrintSettingDataHolder) param);

                        return STATE_JOB_PRE_PROCESS;

                    case CHANGE_JOB_STATE_INITIAL:
                        return STATE_JOB_INITIAL;
                    default:
                        return super.getNextState(event, param);
                }
            }

            @Override
            public void entry(Object... params) {
                printManager.getPrintSysAlertDisplay().initAlertDialogDisplayed();
            }
        },

        /**
         * 印刷前の属性設定状態
         * The state before the job is started after attribute set.
         */
        STATE_JOB_PRE_PROCESS {
            @Override
            public State getNextState(PrintEvent event, Object param) {
                switch ( event) {
                    case CHANGE_JOB_STATE_PRE_PENDING :
                        return STATE_JOB_PRE_PENDING;

                    case CHANGE_JOB_STATE_INITIAL:
                        //ジョブ発行に失敗した場合
                        //failed to start job
                        closePrintingDialog();
                        closePrintStoppedProcessDialog();
                        return STATE_JOB_INITIAL;
                    case CHANGE_JOB_STATE_PENDING:
                        return STATE_JOB_PENDING;
                    case CHANGE_JOB_STATE_PROCESSING:
                        return STATE_JOB_PROCESSING;
                    default:
                        return super.getNextState(event, param);
                }
            }

            @Override
            public void entry(Object... params) {
                //印刷開始ダイアログの表示
                //show the print start dialog
                showPrintingDialog(mContext);
                setPrintingDialogCancelable(false);
            }
        },

        /**
         * ジョブ実行後の状態
         * The state after the job is started.
         */
        STATE_JOB_PRE_PENDING {
            @Override
            public State getNextState(PrintEvent event, Object param) {
                switch (event) {
                    case CHANGE_JOB_STATE_PENDING:
                        return STATE_JOB_PENDING;
                    case CHANGE_JOB_STATE_PROCESSING:
                        return STATE_JOB_PROCESSING;
                    case CHANGE_APP_ACTIVITY_DESTROYED:
                        return WAITING_JOB_CANCEL; 
                    case REQUEST_JOB_CANCEL:
                        return WAITING_JOB_CANCEL;
                    case CHANGE_JOB_STATE_ABORTED:
                        return STATE_JOB_ABORTED;
                    default:
                        return super.getNextState(event, param);
                }
            }

        },
        /**
         * ジョブ待機中状態
         * Job pending
         */
        STATE_JOB_PENDING {
            @Override
            public State getNextState(PrintEvent event, Object param) {
                switch (event) {
                    case CHANGE_JOB_STATE_PROCESSING:
                        return STATE_JOB_PROCESSING;
                    case REQUEST_JOB_CANCEL:
                        return WAITING_JOB_CANCEL;
                    case CHANGE_JOB_STATE_PROCESSING_STOPPED:
                        return STATE_JOB_PROCESSING_STOPPED;

                    case CHANGE_APP_ACTIVITY_DESTROYED:
                        return WAITING_JOB_CANCEL;
                    case CHANGE_JOB_STATE_ABORTED:
                        return STATE_JOB_ABORTED;
                    default:
                        return super.getNextState(event,param);
                }
            }
        },
        /**
         * ジョブ実行中状態
         * Job processing
         */
        STATE_JOB_PROCESSING {
            @Override
            public State getNextState(PrintEvent event, Object param) {
                switch (event) {
                    case CHANGE_JOB_STATE_ABORTED:
                        return STATE_JOB_ABORTED;

                    case CHANGE_JOB_STATE_CANCELED:
                        return STATE_JOB_CANCELED;

                    case CHANGE_JOB_STATE_COMPLETED:
                        return STATE_JOB_COMPLETED;

                    case CHANGE_JOB_STATE_PROCESSING:
                        return STATE_JOB_PROCESSING;

                    case REQUEST_JOB_CANCEL:
                        return WAITING_JOB_CANCEL;

                    case CHANGE_JOB_STATE_PROCESSING_STOPPED:
                        return STATE_JOB_PROCESSING_STOPPED;

                    case CHANGE_APP_ACTIVITY_DESTROYED:
                        return WAITING_JOB_CANCEL;

                    default:
                        return super.getNextState(event,param);
                }
            }

            @Override
            public void entry(Object... params) {
                //Show print start dialog
                if(params[0] instanceof PrintJobPrintingInfo) {
                    PrintJobPrintingInfo printingInfo = (PrintJobPrintingInfo) params[0];
                    if(printingInfo == null) return;

                    //String printedMessage = String.format(mContext.getResources().getString(R.string.dlg_printing_message_printing), printingInfo.getPrintedCount());

                    //updatePrintingDialog(mContext, printedMessage);
                    closePrintStoppedProcessDialog();
                    setPrintingDialogCancelable(true);
                }
            }
        },
        /**
         * ジョブ一時停止中状態*
         * Job pausing
         */
        STATE_JOB_PROCESSING_STOPPED{
            @Override
            public State getNextState(PrintEvent event, Object param) {
                switch (event) {
                    case CHANGE_JOB_STATE_ABORTED:
                        return STATE_JOB_ABORTED;

                    case CHANGE_JOB_STATE_CANCELED:
                        return STATE_JOB_CANCELED;

                    case CHANGE_JOB_STATE_COMPLETED:
                        return STATE_JOB_COMPLETED;

                    case CHANGE_JOB_STATE_PROCESSING:
                        return STATE_JOB_PROCESSING;

                    case REQUEST_JOB_CANCEL:
                        return WAITING_JOB_CANCEL;
                    case CHANGE_APP_ACTIVITY_DESTROYED:
                        return WAITING_JOB_CANCEL;

                    default:
                        return super.getNextState(event,param);
                }
            }

            @Override
            public void entry(Object... params) {
                if(params[0] == null) return;
                String message = "";
//                PrinterStateReasons reasons = (PrinterStateReasons)params[0];
//                StringBuilder sb = new StringBuilder();
//                for(PrinterStateReason reason : reasons.getReasons()) {
//                    //sb.append(reason.toString());
//                    LogC.e("STATE_JOB_PROCESSING_STOPPED:print job error " + reason);
//                    
//                    if(Const.PRINT_ERROR_MAP.get(reason) != null) {
//                        sb.append(CHolder.instance().getActivity().getString(Const.PRINT_ERROR_MAP.get(reason)));
//                    } else {
//                        sb.append(CHolder.instance().getActivity().getString(R.string.print_fail));                    
//                    } 
//                    sb.append("\n");
//                }
          
                // add PrintJobStateReasons
                //message += "\n";
                message += printManager.getStateMachine().getReasonsMessage(params[0]);
                setPrintingDialogCancelable(false);
                // Update job pausing dialog
//                updatePrintingDialog(mContext,
//                        mContext.getResources().getString(
//                                R.string.dlg_printing_message_printing_stopped) + "\n" + sb.toString());
                showPrintProcessJobStop(CHolder.instance().getActivity(), CHolder.instance().getActivity().getString(R.string.dlg_printing_message_printing_stopped)+ "\n" + message);
            }
        },

        /**
         * ジョブキャンセル待ち
         * Job waiting to be canceled
         */
        WAITING_JOB_CANCEL {
            @Override
            public State getNextState(PrintEvent event, Object param) {
                switch (event){
                    case CHANGE_JOB_STATE_INITIAL:
                        return STATE_JOB_INITIAL;
                    case CHANGE_JOB_STATE_CANCELED:
                        return STATE_JOB_CANCELED;
                    case CHANGE_JOB_STATE_ABORTED:
                        return STATE_JOB_ABORTED;
                    case CHANGE_JOB_STATE_PROCESSING:
                        return STATE_JOB_PROCESSING;
                    case CHANGE_JOB_STATE_COMPLETED:
                        return STATE_JOB_COMPLETED;
                    default:
                        return super.getNextState(event, param);
                }
            }

            @Override
            public void entry(Object... params) {
                new CancelPrintJobTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);//.execute();
            }

        },

        /**
         * ジョブ完了状態
         * Job completed
         */
        STATE_JOB_COMPLETED {
            @Override
            public State getNextState(PrintEvent event, Object param) {
                switch (event) {
                    case CHANGE_JOB_STATE_INITIAL:
                        return STATE_JOB_INITIAL;

                    default:
                        return super.getNextState(event,param);
                }
            }

            @Override
            public void entry(Object... params) {
                //close printing dialog
                closePrintingDialog();
                closePrintStoppedProcessDialog();
                LongJob job = CHolder.instance().getJobData().getCurrentJob();
                if (job != null) {
                    ((JobPrintAndDelete)job).printFinished();
                }
            }
        },
        /**
         * ジョブ中断状態
         * Job aborted
         */
        STATE_JOB_ABORTED {
            @Override
            public State getNextState(PrintEvent event, Object param) {
                switch (event) {
                    case CHANGE_JOB_STATE_INITIAL:
                        return STATE_JOB_INITIAL;

                    default:
                        return super.getNextState(event,param);
                }
            }

            @Override
            public void entry(Object... params) {
                //close printing dialog
                closePrintingDialog();
                closePrintStoppedProcessDialog();
                // show toast message with aborted reason
                String message = "";//CHolder.instance().getActivity().getString(R.string.print_fail);
                if ((params != null) && (params.length > 0)) {
//                    if (params[0] instanceof PrinterStateReasons) {
//                        PrinterStateReasons reasons = (PrinterStateReasons)params[0];
//                        StringBuilder sb = new StringBuilder();
//                        sb.append(message);
//                        sb.append(System.getProperty("line.separator"));
//                        //sb.append(reasons.getReasons().toString());
//                        for(PrinterStateReason reason : reasons.getReasons()) {
//                            //sb.append(reason.toString());
//                            LogC.e("STATE_JOB_ABORTED print job error " + reason);
//                            
//                            if(Const.PRINT_ERROR_MAP.get(reason) != null) {
//                                sb.append(CHolder.instance().getActivity().getString(Const.PRINT_ERROR_MAP.get(reason)));
//                            } 
//                           // sb.append("\n");
//                        }
//
//                        message = sb.toString();
//                    }
                    //add PrintJobStateReasons
                    //message += "\n";
                    message += printManager.getStateMachine().getReasonsMessage(params[0]);
                }
                //showToastMessage(mContext, message);
                
                showPrintJobStop(CHolder.instance().getActivity(), message);
                
                LongJob.forceComplete(EXE_RESULT.ERR_PRINT);
            }
        },
        /**
         * ジョブキャンセル状態
         * Job canceled
         */
        STATE_JOB_CANCELED {
            @Override
            public State getNextState(PrintEvent event, Object param) {
                switch (event) {
                    case CHANGE_JOB_STATE_INITIAL:
                        return STATE_JOB_INITIAL;

                    default:
                        return super.getNextState(event,param);
                }
            }

            @Override
            public void entry(Object... params) {
                //close printing dialog
                closePrintingDialog();
                closePrintStoppedProcessDialog();
                LongJob.forceComplete(EXE_RESULT.ERR_PRINT);
            }
        } ;

        /******************************************************************
         * Stateの共通処理
         * 呼び出し順番は、getNextState()=>exit()=>entry()になります。
         *
         * Common methods.
         * These methods are called in the following order.
         * ・getNextState()
         * ・exit()
         * ・entry()
         ******************************************************************/

        /**
         * 状態遷移
         * Obtains the next methods.
         */
        public State getNextState(final PrintEvent event, final Object param) {
            switch (event){
                default:
                    return null;
            }
        }

        /**
         * 入場メソッド
         * Entry method
         */
        public void entry(final Object ... params) {

        }

        /**
         * 退場メソッド
         * Exit method
         */
        public void exit(final Object ... params) {

        }
    }

    /**
     * 状態遷移を行います。
     * Changes states.
     */
    public void procPrintEvent(final PrintEvent event) {
        procPrintEvent(event, null);
    }

    /**
     * 状態遷移を行います。
     * Changes states.
     */
    public void procPrintEvent(final PrintEvent event, final Object prm) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                State newState = mState.getNextState(event, prm);
                if (newState != null) {
                    LogC.i(getClass().getSimpleName(), "#evtp :" + event + " state:" + mState + " > " + newState);
                    mState.exit(prm);
                    mState = newState;
                    mState.entry(prm);
                }
            }
        });
    }


    /******************************************************************
     * アクションメソッド
     * Action method.
     ******************************************************************/

    /**
     * PleaseWait画面の表示
     * Displays please wait dialog
     */
//    private static void showPleaseWaitDialog(Context context) {
//        mPleaseWaitDialog = new ProgressDialog(context);
//        mPleaseWaitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        mPleaseWaitDialog.setMessage(context.getResources().getString(R.string.dlg_waiting_message));
//        mPleaseWaitDialog.setCancelable(false);
//        mPleaseWaitDialog.setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getString(R.string.btn_cancel),
//                new DialogInterface.BaseOnClickListener() {
//            @Override
//            public void onWork(DialogInterface dialog, int which) {
//                ((Activity)mContext).finish();
//            }
//        });
//        //DialogUtil.showDialog(mPleaseWaitDialog);
//    }

    /**
     * PleaseWait画面の消去
     * Hides please wait dialog
     */
//    private static void closePleaseWaitDialog() {
//        if(mPleaseWaitDialog == null) return;
//        mPleaseWaitDialog.dismiss();
//        mPleaseWaitDialog = null;
//    }

    /**
     * 初期化失敗ダイアログの表示
     * Displays boot failed dialog
     */
//    private static void showBootFailedDialog() {
//        if(mBootFailedDialog==null || mBootFailedDialog.isShowing()==false) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//            builder.setTitle(R.string.error_title);
//            builder.setMessage(R.string.error_cannot_connect);
//            builder.setCancelable(false);
//            builder.setNegativeButton(R.string.btn_close,
//                new DialogInterface.BaseOnClickListener() {
//                    @Override
//                    public void onWork(DialogInterface dialog, int which) {
//                        ((Activity)mContext).finish();
//                    }
//                });
//            mBootFailedDialog = builder.create();
//            //DialogUtil.showDialog(mBootFailedDialog);
//        }
//    }

    /**
     * 印刷中ダイアログの表示
     * Displays scanning dialog.
     * @param context メインアクテビティのコンテキスト
     *                Context of MainActivity
     */
    private static void showPrintingDialog(Context context) {
        
        mProgressDialog = new PrintProcessingFragment(CHolder.instance().getActivity());
//      
        
        mProgressDialog.show(CHolder.instance().getActivity().getFragmentManager(), "printting");
        //mProgressDialog.getDialog().setCancelable(false);
        mProgressDialog.setPrintStopCallback(new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
                LogC.d("stop print");
                
                mProgressDialog.setStopButtonEnable(false);
                mProgressDialog.setStopCanceled();
                printManager.getStateMachine().procPrintEvent(PrintEvent.REQUEST_JOB_CANCEL);
                
            }
        });
        
        mProgressDialog.setStopButtonEnable(false);
//        mProgressDialog = new ProgressDialog(context);
//        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//		mProgressDialog.setTitle(context.getResources().getString(R.string.dlg_printing_title));
//        mProgressDialog.setMessage(
//                context.getResources().getString(R.string.dlg_printing_message_send_file));
//        mProgressDialog.setCancelable(false);
//        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
//                context.getResources().getString(R.string.dlg_printing_button_cancel),
//                new DialogInterface.BaseOnClickListener() {
//                    @Override
//                    public void onWork(DialogInterface dialogInterface, int i) {
//                        printManager.getStateMachine()
//                                .procPrintEvent(PrintEvent.REQUEST_JOB_CANCEL);
//                    }
//                });
       // DialogUtil.showDialog(mProgressDialog);
    }

    /**
     * 印刷中ダイアログの文言更新
     * Updates the printing dialog.
     *
     * @param context メインアクテビティのコンテキスト
     *                Context of MainActivity
     * @param updateMessage ダイアログに表示する文言
     *                      String to display on the dialog
     */
//    private static void updatePrintingDialog(Context context, String updateMessage) {
//        if(mProgressDialog == null) return;
//        mProgressDialog.setMessage(updateMessage);
//    }
    
    private static void showPrintProcessJobStop(Context context, String message) {
        LogC.d("show showPrintProcessJobStop Stop");
        //final TitleButtonDialog mJobStoppedDialog = new TitleButtonDialog(CHolder.instance().getActivity());//(reason);
        
        //builder.setIcon(android.R.drawable.ic_dialog_alert);
        //LogC.d("message:" + message);
         mJobStoppedProcessDialog = MultiButtonDialog.createCancelDialog(CHolder.instance().getActivity(), message, new BaseDialogOnClickListener() {
            
            @Override
            public void onWork(BaseDialog dialog) {
                
                printManager.getStateMachine().procPrintEvent(PrintEvent.REQUEST_JOB_CANCEL);                
            }
        });

		mJobStoppedProcessDialog.show();
    }
    
    private static void closePrintStoppedProcessDialog() {
        if(mJobStoppedProcessDialog != null && mJobStoppedProcessDialog.isShowing()) {
            mJobStoppedProcessDialog.dismiss();
            mJobStoppedProcessDialog = null;
        }
    }
    
    private static void showPrintJobStop(Context context, String message) {
        LogC.d("show printJob Stop");
        //final TitleButtonDialog mJobStoppedDialog = new TitleButtonDialog(CHolder.instance().getActivity());//(reason);
        
        //builder.setIcon(android.R.drawable.ic_dialog_alert);
  
        MultiButtonDialog mJobStoppedDialog = MultiButtonDialog.createMsgDialog(CHolder.instance().getActivity(), message);
//        mJobStoppedDialog.setTxtTitle("Job stopped");
//        mJobStoppedDialog.setTxtMsg(message);                  
//        mJobStoppedDialog.setRightButtonVisible(View.VISIBLE);       
//        mJobStoppedDialog.setButtonRight("OK");       
//        mJobStoppedDialog.setRightButtonClick(new BaseOnClickListener() {
//            
//            @Override
//            public void onWork(View v) {
//                mJobStoppedDialog.dismiss();
//                
//            }
//        });
//        
        mJobStoppedDialog.show();
        
    }

    /**
     * 印刷中ダイアログの非表示
     * Close the printing dialog,
     */
    private static void closePrintingDialog() {
        if(mProgressDialog == null || mProgressDialog.getDialog() == null || !mProgressDialog.getDialog().isShowing()) return;
        mProgressDialog.dismissAllowingStateLoss();
        mProgressDialog = null;
    }

    /**
     * 印刷中ダイアログのキャンセルボタンの有無を設定する
     * Display/Hide the cancel button in the printing dialog
     * @param enable true:キャンセルボタン表示/false:キャンセルボタン非表示
     *               true:display false:hide
     */
    private static void setPrintingDialogCancelable(boolean enable) {
        if(mProgressDialog == null || mProgressDialog.getDialog() == null || !mProgressDialog.getDialog().isShowing()) return;

        //Button cancelButton = mProgressDialog.getButton(ProgressDialog.BUTTON_NEGATIVE);
        //if(cancelButton == null) return;

        //if(enable){
            mProgressDialog.setStopButtonEnable(enable);//.setVisibility(View.VISIBLE);
        //}else{
         //   cancelButton.setVisibility(View.INVISIBLE);
        //}
    }

    /**
     * Toastメッセージ表示
     * Display the toast message
     *
     * @param context メインアクテビティのコンテキスト
     *                Context of MainActivity
     * @param updateMessage Toastに表示する文言
     *                       String to display in the toast
     */
    private static void showToastMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /******************************************************************
     * 非同期タスク
     * Asynchronous task
     ******************************************************************/

    /**
     * PrintResponseExceptionのエラー情報を文字列化します。
     * フォーマットは以下の通りです。
     * Creates the string of the ScanResponseException error information.
     * The format is as below.
     *
     * base[separator]
     * [separator]
     * message_id: message[separator]
     * message_id: message[separator]
     * message_id: message
     *
     * @param e 文字列化対象のPrintResponseException
     *          ScanResponseException to be converted as a string
     * @param base メッセージ先頭文字列
     *             Starting string of the message
     * @return メッセージ文字列
     *         Message string
     */
    private String makeJobErrorResponceMessage(PrintResponseException e, String base) {
        if (base == null) {
            base = "";
        }
        StringBuilder sb = new StringBuilder(base);
        if (e.hasErrors()) {
            Map<String, String> errors = e.getErrors();
            if (errors.size() > 0) {
                //String separator = System.getProperty("line.separator");
                //sb.append(separator);

                for (Map.Entry<String, String> entry : errors.entrySet()) {
                    //sb.append(separator);
                    // message_id
                    //sb.append(entry.getKey());
                    // message (exists only)
                    LogC.e("Print Job Error Response id:" + entry.getKey());
                    if(Const.JOB_ERROR_MAP.get(entry.getKey()) != null) {
                        //LogC.e("Print Job Error Response id:" + entry.getKey());
                        sb.append(CHolder.instance().getActivity().getString(Const.JOB_ERROR_MAP.get(entry.getKey())));
                    } 
//                    String message = entry.getValue();
//                    if ((message != null) && (message.length() > 0)) {
//                        sb.append(": ");
//                        sb.append(message);
//                    }
                }
            }
        }
        
        if(CUtil.isStringEmpty(sb.toString())) {
            sb.append(CHolder.instance().getActivity().getString(R.string.print_fail));
        }
        return sb.toString();
    }

    /**
     * ジョブを開始するタスクです。
     * The asynchronous task to start the scan job.
     */
    static class StartPrintJobTask extends AsyncTask<PrintSettingDataHolder, Void, Boolean> {

        private String message = "";

        @Override
        protected Boolean doInBackground(PrintSettingDataHolder... holders) {

            PrintFile printFile;
            try {
               String filePathString = Const.SCAN_FILE_PATH + Const.COMBILE_FILE_NAME;
                
               File file = new File(filePathString);
               
                CHolder.instance().getJobData().setPathPrintFile(filePathString);
                if(file.exists() && file.isFile()) {
                    if (CHolder.instance().getJobData().isNeedPDFPrint()) {
                        // convert jpg to PDF
                        String pathPdf = Const.SCAN_FILE_PATH + "printTmp.pdf";
                        JpegProcessManager.JpegToPdf(filePathString, pathPdf);
                        CHolder.instance().getJobData().setPathPrintFile(pathPdf);
                    }
                }
                printFile = holders[0].getPrintFile();
            } catch (PrintException e) {                
                //Toast.makeText(mContext, "get print file fail", Toast.LENGTH_LONG).show();
                LogC.e("get print file error happen!");
                if (e instanceof PrintResponseException) {
                    message = printManager.getStateMachine().makeJobErrorResponceMessage((PrintResponseException)e, message);
                } else if(CUtil.isStringEmpty(message)) {
                    message = CHolder.instance().getActivity().getResources().getString(R.string.print_fail);// + e.getMessage();
                }
                
                return false;
            }

            PrintRequestAttributeSet attributeSet = holders[0].getPrintRequestAttributeSet();
            if(printFile == null || attributeSet == null) {
                //Toast.makeText(mContext, "printFile is " + printFile + "attributeSet is " + attributeSet, Toast.LENGTH_LONG).show();
                return false;
            }

            try {
                printManager.initialJob();
                currentPrintJob = printManager.getPrintJob();
                currentPrintJob.print(printFile, attributeSet, new PrintUserCode(printManager.getUserCode()));
                return true;
            } catch (PrintException e) { 
                LogC.e("start print job error happen!");
                if (e instanceof PrintResponseException) {
                    
                    message = printManager.getStateMachine().makeJobErrorResponceMessage((PrintResponseException)e, message);
                }else if(CUtil.isStringEmpty(message)) {
                    message =  CHolder.instance().getActivity().getResources().getString(R.string.print_fail);// + e.getMessage();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(!result) {
                //Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                MultiButtonDialog msgDlg = MultiButtonDialog.createMsgDialog(CHolder.instance().getActivity(), message);
                LogC.d("---------------StartPrintJobTask--------------");
                msgDlg.show();
                printManager.getStateMachine().procPrintEvent(PrintEvent.CHANGE_JOB_STATE_INITIAL);
                LongJob.forceComplete(EXE_RESULT.ERR_PRINT);
                return;
            }

            printManager.getStateMachine().procPrintEvent(PrintStateMachine.PrintEvent.CHANGE_JOB_STATE_PRE_PENDING);
        }
    }

    /**
     * 現在印刷中のジョブを中止するタスクです。
     * The task to cancel the job.
     */
    static class CancelPrintJobTask extends  AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean result = false;

            if(currentPrintJob == null) return false;
            try {
                result = currentPrintJob.cancelPrintJob(new PrintUserCode(printManager.getUserCode()));
            } catch (PrintException e) {
                LogC.e("CancelPrintJobTask", e);
            }
            if (!result) {
                LogC.e("CancelPrintJobTask failed");
            }
            return result;
        }
    }
    
    private String getReasonMessage(PrinterStateReason reason) {
        Integer ret =  Const.PRINT_ERROR_MAP.get(reason);
        if(ret == null) {
            ret = R.string.print_fail;
        }
        return mApplication.getResources().getString(ret);
    }
    
    private String getJobReasonMessage(PrintJobStateReason reason) {
        Integer ret =  Const.mJobReasonStringMap.get(reason);
        if(ret == null) {
            ret = R.string.print_fail;
        }
        return mApplication.getResources().getString(ret);
    }
    
    
    private String getReasonsMessage(Object prm) {        
        if(prm == null) {
            LogC.d("prm is null.");
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        if(prm instanceof PrinterStateReasons) {
            PrinterStateReasons reasons = (PrinterStateReasons)prm;
            Set<PrinterStateReason> set = reasons.printerStateReasonSet(Severity.ERROR);
            if(set != null && set.size() == 0) {
                set = reasons.getReasons();
            }
            for(PrinterStateReason reason : set) {
                sb.append(getReasonMessage(reason));
                sb.append("\n");
            }
            
            Set<PrinterStateReason> setTemp = reasons.getReasons();
            for(PrinterStateReason reason : setTemp) {
                LogC.d("", "PrinterStateReason " + reason.toString());
            }
        } else if(prm instanceof PrintJobStateReasons) {
            PrintJobStateReasons reasons = (PrintJobStateReasons)prm;
            for(PrintJobStateReason reason : reasons.getReasons()) {
                sb.append(getJobReasonMessage(reason));
                LogC.d("", "PrinterStateReason " + reason.toString());
                sb.append("\n");
            }
        } else {
            LogC.d("error is neither PrinterStateReason nor PrinterStateReason.");
            
            return null;
        }
        if(sb.length() > 1) {
            // del last \n
            sb.deleteCharAt(sb.length()-1);
        }
        
        return sb.toString();
    }

}
