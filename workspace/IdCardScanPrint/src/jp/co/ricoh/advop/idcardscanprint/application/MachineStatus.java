package jp.co.ricoh.advop.idcardscanprint.application;

/*
 *  Copyright (C) 2013 RICOH Co.,LTD.
 *  All rights reserved.
 */


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;


import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.common.SmartSDKApplication;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.TrayName;

import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.client.BasicRestContext;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.ErrorResponseBody;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.ErrorResponseBody.Errors;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.ErrorResponseBody.ErrorsArray;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.GenericJsonDecoder;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.InvalidResponseException;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.Request;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.RequestHeader;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.RequestQuery;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.Response;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.Entry;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.log.printerlog.DeviceLog;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.log.printerlog.GetDeviceLogResponseBody;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.property.DeviceProperty;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.property.GetDeviceInfoResponseBody;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.property.GetDeviceInfoResponseBody.DeviceDescription;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.property.GetDeviceInfoResponseBody.Plotter;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.property.GetDeviceInfoResponseBody.Scanner;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.status.GetSuppliesResponseBody;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.status.GetTraysResponseBody;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.status.GetTraysResponseBody.InputTrays;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.status.GetTraysResponseBody.InputTraysArray;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.status.SystemStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 機器情報を取得する機能を提供する。<BR>
 * <p>
 * 機器情報取得サンプルアプリの WebAPI による情報取得ロジックをまとめている。
 * 本クラスでは、機器の情報を取得する以下のメソッドを提供する。
 * <p>
 * <ul>
 * <li>機器構成情報の取得 (getMachineInfo())</li>
 * <li>トレイ状態情報の取得 (getTrayStatus())</li>
 * <li>サプライ状態情報の取得 (getSuppliesStatus())</li>
 * <li>ジョブログの取得 (getJobLog())</li>
 * <li>ジョブログの続きの取得 (getContinuationJobLog())</li>
 * </ul>
 */
public class MachineStatus {
    /** Log用TAG. */
    private static final String TAG = "KC:AP:MachSta";
    
    /** 給紙トレイ状態 : 正常 */
    public static final String TRAY_STATUS_READY = "ready";
    /** 給紙トレイ状態 : エラー */
    public static final String TRAY_STATUS_ERROR = "error";
    
    /** 仕向け地 : 日本 */
    public static final String DEST_JAPAN = "japan";
    /** 仕向け地 : 北米 */
    public static final String DEST_NORTH_AMERICA = "north_america";
    
    private Context context;
    /** 機器構成情報 */
    private GetDeviceInfoResponseBody machineInfo = null; // 機器構成情報
    /** トレイ情報 */
    private GetTraysResponseBody trayinfo = null;   // トレイ情報
    /** トレイ取得タスク */
    //private TrayStatusTask trayStatusTask = null;
    /** 全トレイ両面禁止フラグ */
    private boolean isDpxForbiddenAllTrays = false;
    /** モノクロ機 */
    private static final String MONO = "mono";
   // private static final Map<PaperTray, InputTrays> EMPTY_MAP = new HashMap<PaperTray, InputTrays>();
    
    /**
     * MachineStatusを構築する。
     * @param context コンテキスト
     */
    public MachineStatus(Context context) {
        this.context = context;
    }
    
    /**
     * 機器情報取得を初期化する。
     */
    public void init() {
        /* 機器構成情報の取得 */
        LogC.i("KC:SEQ", "(16)  Get deviceInfo : start");
        this.machineInfo = getMachineInfo();
        LogC.i("KC:SEQ", "(16)  Get deviceInfo : end");
        
        /* トレイ情報の要求 */
        LogC.i("KC:SEQ", "(20)  Get trayInfo : start");
        trayinfo = getTrayStatus();
        LogC.i("KC:SEQ", "(20)  Get trayInfo : end");
    }
    
    /**
     * 機器情報取得の終了処理を行う。
     */
//    public void deinit() {
//        if (trayStatusTask != null) {
//            trayStatusTask.cancel(false);
//            trayStatusTask = null;
//        }
//    }
    
    /** カラー機判定 */
    public static enum MachineType {
        /** カラー機 */
        COLOR,
        /** モノクロ機 */
        MONO,
        /** 不明 */
        UNKNOWN,
    };
    
    /**
     * カラー機判別
     * 
     * @return COLOR_MACHINE:カラー機 / MONO_MACHINE:モノクロ機 / UNKNOWN_MACHINE:不明
     */
    public MachineType isColorSupported() {
        try {
            Plotter plotter = this.machineInfo.getPlotter();
            String p_colorSupported = plotter.getColorSupported();
            Scanner scanner = this.machineInfo.getScanner();
            String s_colorSupported = scanner.getColorSupported();
            if (MONO.equals(p_colorSupported) || MONO.equals(s_colorSupported)) {
                return MachineType.MONO;
            }
        } catch (NullPointerException e) {
            return MachineType.UNKNOWN;
        }
        return MachineType.COLOR;
    }
    
    /**
     * 仕向け地の取得
     * @return 仕向け地の文字列。{@link MachineStatus#DEST_JAPAN}, {@link MachineStatus#DEST_NORTH_AMERICA},
     */
    public String getDestination() {
        try {
            DeviceDescription dd = this.machineInfo.getDeviceDescription();
            return dd.getDestination();
        } catch (NullPointerException e) {
            return "";
        }
    }
    
    /**
     * get model name
     * @return model name
     */
    public String getModelNameInfo() {
        try {
            DeviceDescription dd = this.machineInfo.getDeviceDescription();
            return dd.getModelName();
        } catch (NullPointerException e) {
            return "";
        }        
    }
    /**
     * 給紙トレイ情報取得。
     * @return  給紙トレイと給紙トレイ情報のマップ。取得できない場合は空のマップを返す。
     */
//    public Map<PaperTray, InputTrays> getSupportedTrayStatus() {
//        if (trayinfo != null) {
//            isDpxForbiddenAllTrays = true;
//            
//            HashMap<PaperTray, InputTrays> map = new HashMap<PaperTray, InputTrays>();
//            for (InputTrays tray : trayinfo.getInputTrays()) {
//                PaperTray paperTray = PaperTray.fromString(tray.getName());
//                map.put(paperTray, tray);
//                
//                Boolean dpxForbidden = tray.getDpxForbidden();  //両面禁止トレイ
//                if (dpxForbidden != null){
//                    if (!dpxForbidden) {
//                        isDpxForbiddenAllTrays = false;
//                    }
//                }
//            }
//            return map;
//        }
//        return EMPTY_MAP;
//    }
    
    /**
     * 全トレイ両面禁止フラグの取得
     * @return true:全トレイ両面禁止 false:全トレイ両面禁止でない
     */
    public boolean getIsDpxForbiddenAllTrays() {
        return isDpxForbiddenAllTrays;
    }
    
    /**
     * WebAPIにトレイ情報を要求する。
     * トレイ情報に変更があった場合、CHANGE_TRAY_STATUSをブロードキャストする
     */
//    public void requestTrayStatus() {
//        if (trayStatusTask == null) {
//            trayStatusTask = new TrayStatusTask();
//            trayStatusTask.execute();
//        }
//    }
    
    /**
     * 給紙トレイ状態イベント通知を処理する
     * @param trayEvent 給紙トレイ状態イベントが通知する文字列データ。
     */
//    public void handleTrayEvent(String trayEvent) {
//        Log.i(TAG, "handleTrayEvent: " + trayEvent);
//        if (TextUtils.isEmpty(trayEvent)) {
//            return;
//        }
//        Map<String, Object> decoded = GenericJsonDecoder.decodeToMap(trayEvent);
//        @SuppressWarnings("unchecked")
//        Map<String, Object> data = (Map<String, Object>) decoded.get("data");
//        if (data == null) {
//            return;
//        }
//        
//        /* トレイ情報更新 */
//        trayinfo = new GetTraysResponseBody(data);
//        
//        /* トレイ情報変更を通知する */
//        context.sendBroadcast(new Intent(InternalAction.CHANGE_TRAY_STATUS));
//    }
    
    /**
     * 給紙トレイ情報を取得する。<BR>
     */
//    private class TrayStatusTask extends AsyncTask<Void, Void, GetTraysResponseBody> {
//
//        @Override
//        protected GetTraysResponseBody doInBackground(Void... arg0) {
//            Log.i(TAG, "request TrayInfo.");
//            return getTrayStatus();
//        }
//
//        @Override
//        protected void onPostExecute(GetTraysResponseBody result) {
//            Log.i(TAG, "GetTrayResponseBody[" + result + "]");
//            super.onPostExecute(result);
//            trayStatusTask = null;
//            
//            /* トレイ情報変化 */
//            boolean change = false;
//            change |= (result == null) && (trayinfo != null);
//            change |= (result != null) && (trayinfo == null);
//            if (result != null && trayinfo != null) {
//                String next = result.toString();
//                String prev = trayinfo.toString();
//                change |= !next.equals(prev);
//            }
//            trayinfo = result;
//            
//            /* トレイ情報変更を通知する */
//            if (change) {
//                context.sendBroadcast(new Intent(InternalAction.CHANGE_TRAY_STATUS));
//            }
//        }
//    }
    
    /**
     * 機器構成情報を取得します。<BR>
     * <ol>
     * <li>WebAPI実行のための Request を作成します。</li>
     * <li>クライアントクラスを利用して、作成した Request を実行します。</li>
     * <li>実行応答 (response) から情報を取得します。</li>
     * </ol>
     * 
     * @return 機器構成情報。取得できない場合はnullを返す。
     */
    public GetDeviceInfoResponseBody getMachineInfo() {
        // 1
        // 1-1
        final RequestHeader reqHeader = getRequestHeader();
        // 1-2
        final Request request = new Request();
        // 1-3
        request.setHeader(reqHeader);

        // 2
        // 2-1
        final BasicRestContext context = new BasicRestContext();
        // 2-2
        final DeviceProperty deviceProperty = new DeviceProperty(context);
        Response<GetDeviceInfoResponseBody> response = null;
        // 2-3
        try {
            response = deviceProperty.getDeviceInfo(request);
        } catch (InvalidResponseException ire) {
            logOutputErrorResponse(ire);
            return null;
        } catch (IOException ioe) {
            logOutputErrorResponse(ioe);
            return null;
        } catch (Exception e) {
            logOutputErrorResponse(e);
            return null;
        }

        // 3
        final GetDeviceInfoResponseBody body = response.getBody();

        return body;

    }

    /**
     * トレイ情報を取得します。<BR>
     * <ol>
     * <li>WebAPI実行のための Request を作成します。</li>
     * <li>クライアントクラスを利用して、作成した Request を実行します。</li>
     * <li>実行応答 (response) から情報を取得します。</li>
     * </ol>
     * 
     * @return トレイ情報応答
     */
    public GetTraysResponseBody getTrayStatus() {
        // 1
        // 1-1
        final RequestHeader reqHeader = getRequestHeader();
        // 1-2
        final Request request = new Request();
        // 1-3
        request.setHeader(reqHeader);

        // 2
        // 2-1
        final BasicRestContext context = new BasicRestContext();
        // 2-2
        final SystemStatus systemStatus = new SystemStatus(context);
        Response<GetTraysResponseBody> response = null;
        // 2-3
        try {
            response = systemStatus.getTrays(request);
        } catch (InvalidResponseException ire) {
            logOutputErrorResponse(ire);
            return null;
        } catch (IOException ioe) {
            logOutputErrorResponse(ioe);
            return null;
        }

        // 3
        final GetTraysResponseBody body = response.getBody();

        return body;

    }

    /**
     * 機器サプライ情報を取得します。
     * <ol>
     * <li>WebAPI実行のための Request を作成します。</li>
     * <li>クライアントクラスを利用して、作成した Request を実行します。</li>
     * <li>実行応答 (response) から情報を取得します。</li>
     * </ol>
     * 
     * @return サプライ情報応答
     */
    public GetSuppliesResponseBody getSuppliesStatus() {
        // 1
        // 1-1
        final RequestHeader reqHeader = getRequestHeader();
        // 1-2
        final Request request = new Request();
        // 1-3
        request.setHeader(reqHeader);

        // 2
        // 2-1
        final BasicRestContext context = new BasicRestContext();
        // 2-2
        final SystemStatus systemStatus = new SystemStatus(context);
        Response<GetSuppliesResponseBody> response = null;
        // 2-3
        try {
            response = systemStatus.getSupplies(request);
        } catch (InvalidResponseException ire) {
            logOutputErrorResponse(ire);
            return null;
        } catch (IOException ioe) {
            logOutputErrorResponse(ioe);
            return null;
        }

        // 3
        final GetSuppliesResponseBody body = response.getBody();

        return body;

    }

    /**
     * ジョブログを取得します。
     * <ol>
     * <li>WebAPI実行のための Request を作成します。</li>
     * <li>クライアントクラスを利用して、作成した Request を実行します。</li>
     * <li>実行応答 (response) から情報を取得します。</li>
     * </ol>
     * 
     * <p>
     * ジョブログ取得では、WebAPI の実行に管理者ユーザー権限が必要となるため、
     * 該当属性値を引数として受け取り、1-1 で設定しています。
     * また、1-4 でジョブログ検索条件を Query として設定しています。
     * 本サンプルアプリケーションでは、未取得のジョブログのうち古い10件を取得する条件を固定値で設定しています。
     * </p>
     * 
     * @return ジョブログ応答
     */
    public GetDeviceLogResponseBody getJobLog() {
        // 1
        // 1-1
        final RequestHeader reqHeader = getRequestHeader();
        // 1-2
        final Request request = new Request();
        // 1-3
        request.setHeader(reqHeader);
        // 1-4
        final RequestQuery reqQuery = getJobLogSearchCondition();
        request.setQuery(reqQuery);

        // 2
        // 2-1
        final BasicRestContext context = new BasicRestContext();
        // 2-2
        final DeviceLog deviceLog = new DeviceLog(context);
        Response<GetDeviceLogResponseBody> response = null;
        // 2-3
        try {
            response = deviceLog.getJobLog(request);
        } catch (InvalidResponseException ire) {
            logOutputErrorResponse(ire);
            return null;
        } catch (IOException ioe) {
            logOutputErrorResponse(ioe);
            return null;
        }

        // 3
        final GetDeviceLogResponseBody body = response.getBody();

        return body;

    }

    /**
     * ジョブログ検索条件クエリインスタンスを取得します。<br>
     * ここでは、未取得の10件を取得し取得済みにする、条件クエリを作成します。
     * <ol>
     * <li>クエリインスタンスを生成します。</li>
     * <li>検索件数条件に 10件を設定します。</li>
     * <li>未転送のもののみを取得する設定します。</li>
     * <li>取得したものを転送済みにする設定します。</li>
     * </ol>
     * @return 未取得の10件を取得するための検索条件クエリ
     */
    private RequestQuery getJobLogSearchCondition() {
        // 1
        final RequestQuery query = new RequestQuery();
        // 2
        query.put(QUERY_SEARCH_NUM, "10");
        // 3
        query.put(QUERY_STATE, "unforward");
        // 4
        query.put(QUERY_CHANGE_STATE, "forwarded");

        return query;
    }
    /** 検索件数条件クエリ名 */
    private static final String QUERY_SEARCH_NUM = "num";
    /** 転送有無による取得条件クエリ名 */
    private static final String QUERY_STATE = "state";
    /** 取得したものを転送済みとするクエリ名 */
    private static final String QUERY_CHANGE_STATE = "change_state";

    /**
     * 取得したログ情報に続きがある場合、このメソッドで続きを取得します。<br>
     * ログに続きがある場合、応答属性内に nextLink 属性が含まれます。<br>
     * この属性の値を取得し、本メソッドの nextLink 引数に指定して実行します。
     * <ol>
     * <li>WebAPI実行のための Request を作成します。</li>
     * <li>クライアントクラスを利用して、作成した Request を実行します。</li>
     * <li>実行応答 (response) から情報を取得します。</li>
     * </ol>
     * 
     * @param nextLink 続きを取得するためのURL(String)
     * @return ジョブログ応答
     */
    public GetDeviceLogResponseBody getContinuationJobLog(String nextLink) {

        // 1
        // 1-1
        final RequestHeader reqHeader = getRequestHeader();
        // 1-2
        final Request request = new Request();
        // 1-3
        request.setHeader(reqHeader);

        // 2
        // 2-1
        final BasicRestContext context = new BasicRestContext();
        // 2-2
        final DeviceLog deviceLog = new DeviceLog(context);
        Response<GetDeviceLogResponseBody> response = null;
        // 2-3
        try {
            response = deviceLog.getContinuationResponse(request, nextLink);
        } catch (InvalidResponseException ire) {
            logOutputErrorResponse(ire);
            return null;
        } catch (IOException ioe) {
            logOutputErrorResponse(ioe);
            return null;
        }

        // 3
        final GetDeviceLogResponseBody body = response.getBody();

        return body;

    }

    /**
     * WebAPI実行のためのヘッダーを取得します。<BR>
     * WebAPI実行のヘッダーにはアプリケーションのプロダクトID 属性を設定する必要があるため、<BR>
     * ここで共通化しています。
     * @return API実行のためのヘッダー
     */
    private RequestHeader getRequestHeader() {
        final RequestHeader header = new RequestHeader();
        header.put(RequestHeader.KEY_X_APPLICATION_ID, SmartSDKApplication.getProductId());

        return header;
    }

    /**
     * WebAPI の実行エラーをログ出力します。
     * @param e 例外
     */
    private void logOutputErrorResponse(Exception e) {

        if (e instanceof InvalidResponseException) {
            final InvalidResponseException ire = (InvalidResponseException)e;
            LogC.e("", "", ire);
            final StringBuffer buf = new StringBuffer();
            buf.append("Request failed.").append("\n");
            buf.append("  ").append("Error code = ").append(ire.getStatusCode()).append("\n");
            if (ire.hasBody()) {
                final ErrorResponseBody errorBody = ire.getBody();
                final ErrorsArray errorsArray = errorBody.getErrors();
                for (Iterator<Errors> itr = errorsArray.iterator();  itr.hasNext(); ) {
                    final Errors errors = itr.next();
                    buf.append("    ").append(errors.getMessageId()).append(" : ").append(errors.getMessage()).append("\n");
                }
            }
            LogC.e("", buf.toString());
        }
        else {
            LogC.e("", e.getMessage(), (Exception)e);
        }

    }
    
}

