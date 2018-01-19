package jp.co.ricoh.ssdk.iccardsample.framework.cis;

import java.util.ArrayList;
import java.util.List;

import jp.co.ricoh.ssdk.iccardsample.Const;
import jp.co.ricoh.ssdk.iccardsample.framework.type.CardInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class CisAccessor {
	private static final String TAG = Const.TAG;

	private static final String PERMISSION_APP_CMD 						= "jp.co.ricoh.isdk.sdkservice.common.SdkService.APP_CMD_PERMISSION";

	private static final String ACTION_REGISTER_IC_CARD 				= "jp.co.ricoh.isdk.sdkservice.card.REGISTER_IC_CARD";
	private static final String ACTION_UNREGISTER_IC_CARD 				= "jp.co.ricoh.isdk.sdkservice.card.UNREGISTER_IC_CARD";
	private static final String ACTION_EXCLUSIVE_LOCK_IC_CARD_INFO 		= "jp.co.ricoh.isdk.sdkservice.card.EXCLUSIVE_LOCK_IC_CARD_INFO";
	private static final String ACTION_EXCLUSIVE_UNLOCK_IC_CARD_INFO 	= "jp.co.ricoh.isdk.sdkservice.card.EXCLUSIVE_UNLOCK_IC_CARD_INFO";
	private static final String ACTION_RES_PIN_CODE 					= "jp.co.ricoh.isdk.sdkservice.card.RES_PIN_CODE";
	private static final String ACTION_GET_IC_CARD_INFO 				= "jp.co.ricoh.isdk.sdkservice.card.GET_IC_CARD_INFO";

	private static final String KEY_SUPPORT_TYPE 		= "SUPPORT_TYPE";
	private static final String KEY_SUPPORT_TYPES 		= "SUPPORT_TYPES";
	private static final String KEY_ALLOW_SUPPORT_TYPES = "ALLOW_SUPPORT_TYPES";
	private static final String KEY_CARD_TYPE 			= "CARD_TYPE";
	private static final String KEY_CARD_ID 			= "CARD_ID";
	private static final String KEY_PACKAGE_NAME		= "PACKAGE_NAME";
	private static final String KEY_REQUEST_ID			= "REQUEST_ID";
	private static final String KEY_IS_CANCELLED		= "IS_CANCELLED";
	private static final String KEY_RESULT				= "RESULT";
	private static final String KEY_ERROR_CODE			= "ERROR_CODE";
	private static final String KEY_INFO_TYPE			= "INFO_TYPE";
	private static final String KEY_ACCESS_TOKEN		= "ACCESS_TOKEN";
	private static final String KEY_PIN_CODE			= "PIN_CODE";

	/**
	 * アプリケーション登録結果ハンドラのインタフェース
	 * Application registration result handler interface
	 */
	public static interface RegisterResultHandler {
		public void handleRegisterResult(boolean result, List<String> supportTypes, int errorCode);
	}

	/**
	 * アプリ登録解除結果ハンドラのインタフェース
	 * Application unregistration result handler interface
	 */
	public static interface UnregisterResultHandler {
		public void handleUnregisterResult(boolean result, int errorCode);
	}

	/** Application context */
	private Context context;

	/** Constructor */
	public CisAccessor(Context context) {
		this.context = context;
	}

	/**
	 * ICカード利用アプリを登録します。
	 * Registers a card application
	 *
	 * @param  supportTypes サポート種別
	 *                      List of support types of the card
	 * @param  handler      登録結果を処理するハンドラ
	 *                      Register result handler
	 */
	public void register(ArrayList<String> supportTypes, final RegisterResultHandler handler) {
		Log.i(TAG, "send register: " + supportTypes);

		Intent intent = new Intent(ACTION_REGISTER_IC_CARD);
		intent.putExtra(KEY_PACKAGE_NAME, context.getPackageName());
		intent.putExtra(KEY_SUPPORT_TYPES, supportTypes);
    	context.sendOrderedBroadcast(intent, PERMISSION_APP_CMD, new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				Bundle bundle = getResultExtras(true);
				boolean result = bundle.getBoolean(KEY_RESULT, false);
				ArrayList<String> supportTypes = bundle.getStringArrayList(KEY_ALLOW_SUPPORT_TYPES);
				int errorCode = bundle.getInt(KEY_ERROR_CODE, -1);
				handler.handleRegisterResult(result, supportTypes, errorCode);
			}
    	}, null, 0, null, null);
	}

	/**
	 * 登録したICカード利用アプリを解除します。
	 * 未登録状態で本Ｉ／Ｆを呼び出しても何も起きません。
	 * Clears card application registration.
	 *
	 * @param  handler     登録解除結果を処理するハンドラ
	 *                     Unregister result handler
	 */
	public void unregister(final UnregisterResultHandler handler) {
		Log.i(TAG, "send unregister");

		Intent intent = new Intent(ACTION_UNREGISTER_IC_CARD);
		intent.putExtra(KEY_PACKAGE_NAME, context.getPackageName());
    	context.sendOrderedBroadcast(intent, PERMISSION_APP_CMD, new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				Bundle bundle = getResultExtras(true);
				boolean result = bundle.getBoolean(KEY_RESULT, false);
				int errorCode = bundle.getInt(KEY_ERROR_CODE, -1);
				handler.handleUnregisterResult(result, errorCode);
			}
    	}, null, 0, null, null);
	}

	/**
	 * 指定されたカード種別に対するアクセスを排他（ロック）します。
	 * Lock accesses from other applications.
	 *
	 * @param  cardInfo    カード情報
	 *                     Card Information
	 */
	public void lock(CardInfo cardInfo) {
		Log.i(TAG, "send lock");

		Intent intent = new Intent(ACTION_EXCLUSIVE_LOCK_IC_CARD_INFO);
		intent.putExtra(KEY_PACKAGE_NAME, context.getPackageName());
		intent.putExtra(KEY_SUPPORT_TYPE, cardInfo.getSupportType());
		intent.putExtra(KEY_CARD_TYPE, cardInfo.getCardType());
		intent.putExtra(KEY_CARD_ID, cardInfo.getCardId());
    	context.sendBroadcast(intent, PERMISSION_APP_CMD);
	}

	/**
	 * 指定されたカード種別に対するアクセス排他を解除（アンロック）します。
	 * Unlock the access lock of specified card type.
	 *
	 * @param  accessToken ロック時に取得したアクセストークン
	 *                     Access token received by onExclusiveLockStateChanged()
	 */
	public void unlock(String accessToken) {
		Log.i(TAG, "send unlock");
		Intent intent = new Intent(ACTION_EXCLUSIVE_UNLOCK_IC_CARD_INFO);
		intent.putExtra(KEY_PACKAGE_NAME, context.getPackageName());
		intent.putExtra(KEY_ACCESS_TOKEN, accessToken);
    	context.sendBroadcast(intent, PERMISSION_APP_CMD);
	}

	private void verifyPin(String requestId, String pin, boolean isCancelled) {
		Log.i(TAG, "send verify pin: " + requestId + " isCancelled: " + isCancelled);

		Intent intent = new Intent(ACTION_RES_PIN_CODE);
		intent.putExtra(KEY_PACKAGE_NAME, context.getPackageName());
		intent.putExtra(KEY_REQUEST_ID, requestId);
		intent.putExtra(KEY_PIN_CODE, pin);
		intent.putExtra(KEY_IS_CANCELLED, isCancelled);
    	context.sendBroadcast(intent, PERMISSION_APP_CMD);
	}

	/**
	 * 指定されたＰＩＮでＰＩＮ認証を行います。
	 * onPinRequired() を受信した際に、本Ｉ／ＦにてＰＩＮ認証を行います。
	 * 本Ｉ／ＦによるＰＩＮ認証がＯＫの場合、ＯＫ応答後に onCardAttached() が呼び出されます。
	 * Requests for PIN code authentication to the application.
	 * onCardAttached() can be called by passing a correct PIN code with this method.
	 *
	 * @param  requestId   プラグインにより割り振られた要求ＩＤ
	 *                     Request ID received by onPinRequired()
	 * @param  pin         ＰＩＮ
	 */
	public void verifyPin(String requestId, String pin) {
		verifyPin(requestId, pin, false);
	}

	/**
	 * PIN認証をキャンセルします。
	 * Cancels the PIN code authentication
	 *
	 * @param  requestId   プラグインにより割り振られた要求ＩＤ
	 *                     Request ID received by onPinRequired()
	 */
	public void cancelVerifyPin(String requestId) {
		verifyPin(requestId, null, true);
	}


	/**
	 * カードから任意の情報を取得します。
	 * Requests card information
	 * The application specifies an access token received by onExclusiveLockStateChanged() to request card information.
	 *
	 * @param  accessToken アクセストークン
	 *                     Access token received by onExclusiveLockStateChanged()
	 * @param  infoType    情報種別
	 *                     Type of information to obtain
	 */
	public void getInfo(String accessToken, String infoType) {
		Log.i(TAG, "send getInfo: " + infoType);
		Intent intent = new Intent(ACTION_GET_IC_CARD_INFO);
		intent.putExtra(KEY_PACKAGE_NAME, context.getPackageName());
		intent.putExtra(KEY_ACCESS_TOKEN, accessToken);
		intent.putExtra(KEY_INFO_TYPE, infoType);
    	context.sendBroadcast(intent, PERMISSION_APP_CMD);
	}

}
