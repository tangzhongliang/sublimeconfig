package jp.co.ricoh.advop.cheetahutil.impexport;

import java.util.ArrayList;

import jp.co.ricoh.advop.cheetahutil.impexport.ImExportListener.DefinitionResponse;
import jp.co.ricoh.advop.cheetahutil.impexport.ImExportListener.ExportResponse;
import jp.co.ricoh.advop.cheetahutil.impexport.ImExportListener.ExportableResponse;
import jp.co.ricoh.advop.cheetahutil.impexport.ImExportListener.ImExportItem;
import jp.co.ricoh.advop.cheetahutil.impexport.ImExportListener.ImExportTarget;
import jp.co.ricoh.advop.cheetahutil.impexport.ImExportListener.ImportResponse;
import jp.co.ricoh.advop.cheetahutil.impexport.ImExportListener.ImportableResponse;
import jp.co.ricoh.advop.cheetahutil.impexport.ImExportListener.ModuleID;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * インポートエクスポート操作を提供する。<BR>
 * BroadcastReceiver を継承した Receiver クラスを内部で保持し、
 * {@link Receiver#onReceive(Context, Intent)} でブロードキャストの伝達を受信する。<BR>
 */
public class ImExportManager {
    /** ログ出力用タグ情報 */
	private static final String TAG = ImExportManager.class.getSimpleName();
	/** インポート/エクスポート対象パッケージへのリソースファイル要求のI/F定義 */
	private static final String IMPEXP_GET_RESOURCE = "jp.co.ricoh.advop.impexpservice.controllerneed.IMPEXP_GET_RESOURCE";
	/** エクスポート対象パッケージへのエクスポート開始要求のI/F定義 */
	private static final String EXPORT_PRE_COMFIRM = "jp.co.ricoh.advop.impexpservice.controllerneed.EXPORT_PRE_COMFIRM";
	/** エクスポート対象パッケージへのエクスポート実行要求のI/F定義 */
	private static final String EXPORT_COMFIRM = "jp.co.ricoh.advop.impexpservice.controllerneed.EXPORT_COMFIRM";
    /** エクスポート応答上限時間(スキャナは2分としておく) */
    private static final int EXPORT_LIMIT_SECONDS = 120;
    /** エクスポート結果のI/F定義 */
	private static final String EXPORT_ITEM = "jp.co.ricoh.advop.impexpservice.controllerneed.EXPORT_ITEM";
    /** インポート対象パッケージへのエクスポート開始要求のI/F定義 */
	private static final String IMPORT_PRE_COMFIRM = "jp.co.ricoh.advop.impexpservice.controllerneed.IMPORT_PRE_COMFIRM";
    /** インポート対象パッケージへのエクスポート実行要求のI/F定義 */
	private static final String IMPORT_ITEM = "jp.co.ricoh.advop.impexpservice.controllerneed.IMPORT_ITEM";
	/** インポート実行要求結果のI/F定義 */
	private static final String IMPORT_ITEM_RES = "jp.co.ricoh.advop.impexpservice.controllerneed.IMPORT_ITEM_RES";
    /** インポート結果のI/F定義 */
	private static final String IMPORT_RETURN = "jp.co.ricoh.advop.impexpservice.controllerneed.IMPORT_RETURN";
    /** インポート/エクスポート終了通知のI/F定義 */
	private static final String NOTIFY_END = "jp.co.ricoh.advop.impexpservice.controllerneed.NOTIFY_END";
	/** イベントリスナー */
	private static ImExportListener mEventListner;

	/**
	 * 指定されたリスナーを登録する。<BR>
	 * @param listener リスナー
	 */
	public static void setEventListener(ImExportListener listener) {
		mEventListner = listener;
	}

	/**
	 * インポートエクスポート要求の受信を提供する。<BR>
	 * BroadcastReceiver を継承し、機器からのイベントを受信する機能を利用する。<BR>
     * <BR>
     * [対象アクション]<BR>
     * ・インポート/エクスポート対象パッケージへのリソースファイル要求<BR>
     * <dd>{@link ImExportManager#IMPEXP_GET_RESOURCE}</dd>
     * ・エクスポート対象パッケージへのエクスポート開始要求<BR>
     * <dd>{@link ImExportManager#EXPORT_PRE_COMFIRM}</dd>
     * ・エクスポート対象パッケージへのエクスポート実行要求<BR>
     * <dd>{@link ImExportManager#EXPORT_COMFIRM}</dd>
     * ・インポート対象パッケージへのエクスポート開始要求<BR>
     * <dd>{@link ImExportManager#IMPORT_PRE_COMFIRM}</dd>
     * ・インポート対象パッケージへのエクスポート実行要求<BR>
     * <dd>{@link ImExportManager#IMPORT_ITEM}</dd>
     * ・インポート/エクスポート終了通知<BR>
     * <dd>{@link ImExportManager#NOTIFY_END}</dd>
     * <BR>
	 */
	public final static class Receiver extends BroadcastReceiver {
	    /**
	     * 戻り値に文字列の値を設定する。<BR>
	     * @param key キー
	     * @param value 値
	     */
		private void addResultString(String key, String value) {
			Bundle result = getResultExtras(false);
			if (result == null) {
				result = new Bundle();
			}

			ArrayList<String> list = result.getStringArrayList(key);
			if (list == null) {
				list = new ArrayList<String>();
			}
			list.add(value);
			result.putStringArrayList(key, list);

			setResultExtras(result);
		}

		/**
		 * 戻り値に数値の値を設定する。<BR>
		 * @param key キー
		 * @param value 値
		 */
		private void addResultInteger(String key, Integer value) {
			Bundle result = getResultExtras(false);
			if (result == null) {
				result = new Bundle();
			}

			ArrayList<Integer> list = result.getIntegerArrayList(key);
			if (list == null) {
				list = new ArrayList<Integer>();
			}
			list.add(value);
			result.putIntegerArrayList(key, list);

			setResultExtras(result);
		}

		/**
		 * インポートエクスポート要求に関する非同期イベントを通知する。<BR>
		 * @param context Context
		 * @param intent Intent
		 */
		@Override
		public void onReceive(Context context, Intent intent) {
			LogC.i(TAG, "onReceive:" + intent.getAction());
			Application application = (Application) context.getApplicationContext();

			if (mEventListner != null) {
				Bundle bundle = intent.getExtras();
				String action = intent.getAction();
				if (action.equals(IMPEXP_GET_RESOURCE)) {
					DefinitionResponse response =
							mEventListner.onReceiveDefinitionRequest();
					if (response != null) {
						addResultString("mIdList", response.mId.getStringValue());
						addResultString("version", response.version);
						addResultString("packageName", application.getPackageName());
						addResultInteger("resourceID", response.resourceID);
						LogC.i(TAG, response.toString());
						LogC.i(TAG, getResultExtras(false).toString());
					}
				} else if (action.equals(EXPORT_PRE_COMFIRM)) {
					ExportableResponse response =
							mEventListner.onReceiveExportableRequest(
							        ImExportTarget.valueOf(bundle.getInt("exportTarget")),
									bundle.getBoolean("isNeedSercretInfo"),
									bundle.getBoolean("isNeedUniqueInfo"),
									ImExportItem.valueOf(bundle.getInt("target")));
					if (response != null) {
						addResultString("mIdList", response.mId.getStringValue());
						addResultInteger("canExport", response.ready ? 1 : 0);
						addResultInteger("totalNum", response.numItems);
						LogC.i(TAG, response.toString());
						LogC.i(TAG, getResultExtras(false).toString());
					}
				} else if (action.equals(EXPORT_COMFIRM)) {
					ExportResponse response =
							mEventListner.onReceiveExportRequest(
							        ImExportTarget.valueOf(bundle.getInt("exportTarget")),
									bundle.getBoolean("isNeedSercretInfo"),
									bundle.getBoolean("isNeedUniqueInfo"),
									ImExportItem.valueOf(bundle.getInt("target")));
					if (response != null) {
						addResultString("mIdList", response.mId.getStringValue());
						addResultInteger("totalNum", response.numItems);
						addResultInteger("limitSeconds", EXPORT_LIMIT_SECONDS);
						LogC.i(TAG, response.toString());
						LogC.i(TAG, getResultExtras(false).toString());
					}
				} else if (action.equals(IMPORT_PRE_COMFIRM)) {
					ImportableResponse response =
							mEventListner.onReceiveImportableRequest(
									bundle.getBoolean("isNeedSercretInfo"),
									bundle.getBoolean("isNeedUniqueInfo"),
									ImExportItem.valueOf(bundle.getInt("target")));
					if (response != null) {
						addResultString("mIdList", response.mId.getStringValue());
						addResultInteger("canExport", response.ready ? 1 : 0);
						LogC.i(TAG, response.toString());
						LogC.i(TAG, getResultExtras(false).toString());
					}
				} else if (action.equals(IMPORT_ITEM)) {
					ImportResponse response =
							mEventListner.onReceiveImportRequest(
									bundle.getString("filePath"),
									bundle.getBoolean("isNeedSercretInfo"),
									bundle.getBoolean("isNeedUniqueInfo"),
									ImExportItem.valueOf(bundle.getInt("target")));
					if (response != null) {
	                      Intent resIntent = new Intent(IMPORT_ITEM_RES);
	                        resIntent.putExtra("mIdList", response.mId.getStringValue());
	                        resIntent.putExtra("limitSeconds", response.mLimitSeconds);
	                        resIntent.putExtra("totalNum", response.mTotalNum);
	                        application.sendBroadcast(resIntent);
	                        LogC.i(TAG, resIntent.toString());
	                        LogC.i(TAG, resIntent.getExtras().toString());
					}
				} else if (action.equals(NOTIFY_END)) {
					switch (bundle.getInt("actionKind")) {
					case 1:
						mEventListner.onReceiveImportFinished();
						break;
					case 2:
						mEventListner.onReceiveExportFinished();
						break;
					default:
						break;
					}
				}
			}
		}
	};

	/**
	 * エクスポートする項目を送信する。<BR>
	 * @param application アプリケーション
	 * @param mId モジュールID
	 * @param exportFileName 機密情報の有無
	 * @param resultFileName 固有情報の有無
	 */
	public static void sendExportItem(Application application, ModuleID mId, String exportFileName, String resultFileName) {
		Intent intent = new Intent(EXPORT_ITEM);
		intent.putExtra("mId", mId.getStringValue());
		intent.putExtra("filePath", exportFileName);
		intent.putExtra("resultFilePath", resultFileName);
		application.sendBroadcast(intent);
		LogC.i(TAG, intent.toString());
		LogC.i(TAG, intent.getExtras().toString());
	}

	/**
	 * インポートする項目を送信する。<BR>
	 * @param application アプリケーション
	 * @param mId モジュールID
	 * @param resultFileName ファイル名
	 * @param successCount インポート成功数
	 */
	public static void sendImportResponse(Application application, ModuleID mId, String resultFileName, int successCount) {
	    if (resultFileName == null) {
	        resultFileName = "";
	    }
		Intent intent = new Intent(IMPORT_RETURN);
		intent.putExtra("mId", mId.getStringValue());
		intent.putExtra("resultFilePath", resultFileName);
		intent.putExtra("successCount", successCount);
		application.sendBroadcast(intent);
		LogC.i(TAG, intent.toString());
		LogC.i(TAG, intent.getExtras().toString());
	}
}
