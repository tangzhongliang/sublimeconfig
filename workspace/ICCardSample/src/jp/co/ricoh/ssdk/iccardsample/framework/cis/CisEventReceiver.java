package jp.co.ricoh.ssdk.iccardsample.framework.cis;

import java.util.ArrayList;
import java.util.List;

import jp.co.ricoh.ssdk.iccardsample.Const;
import jp.co.ricoh.ssdk.iccardsample.framework.type.CardInfo;
import jp.co.ricoh.ssdk.iccardsample.framework.type.DeviceInfo;
import jp.co.ricoh.ssdk.iccardsample.framework.type.PluginInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class CisEventReceiver extends BroadcastReceiver {
	private static final String TAG = Const.TAG;

	private static final String ACTION_IC_CARD_ATTACHED 					= "jp.co.ricoh.isdk.sdkservice.card.IC_CARD_ATTACHED";
	private static final String ACTION_IC_CARD_DETACHED 					= "jp.co.ricoh.isdk.sdkservice.card.IC_CARD_DETACHED";
	private static final String ACTION_NOTIFY_READER_STATE_CHANGED 			= "jp.co.ricoh.isdk.sdkservice.card.NOTIFY_READER_STATE_CHANGED";
	private static final String ACTION_NOTIFY_PLUGIN_STATE_CHANGED 			= "jp.co.ricoh.isdk.sdkservice.card.NOTIFY_PLUGIN_STATE_CHANGED";
	private static final String ACTION_REQ_PIN_CODE 						= "jp.co.ricoh.isdk.sdkservice.card.REQ_PIN_CODE";
	private static final String ACTION_NOTIFY_PIN_CODE_RESULT 				= "jp.co.ricoh.isdk.sdkservice.card.NOTIFY_PIN_CODE_RESULT";
	private static final String ACTION_NOTIFY_EXCLUSIVE_LOCK_STATE_CHANGED 	= "jp.co.ricoh.isdk.sdkservice.card.NOTIFY_EXCLUSIVE_LOCK_STATE_CHANGED";
	private static final String ACTION_IC_CARD_INFO 						= "jp.co.ricoh.isdk.sdkservice.card.IC_CARD_INFO";

	private static final String KEY_SUPPORT_TYPE 	= "SUPPORT_TYPE";
	private static final String KEY_SUPPORT_TYPES 	= "SUPPORT_TYPES";
	private static final String KEY_CARD_TYPE 		= "CARD_TYPE";
	private static final String KEY_CARD_ID 		= "CARD_ID";
	private static final String KEY_DEVICE_ID 		= "DEVICE_ID";
	private static final String KEY_PLUGGED			= "PLUGGED";
	private static final String KEY_PACKAGE_NAME	= "PACKAGE_NAME";
	private static final String KEY_AVAILABLE		= "AVAILABLE";
	private static final String KEY_REQUEST_ID		= "REQUEST_ID";
	private static final String KEY_IS_CANCELLED	= "IS_CANCELLED";
	private static final String KEY_RESULT			= "RESULT";
	private static final String KEY_ERROR_CODE		= "ERROR_CODE";
	private static final String KEY_LOCK_STATE		= "LOCK_STATE";
	private static final String KEY_INFO_TYPE		= "INFO_TYPE";
	private static final String KEY_INFO			= "INFO";
	private static final String KEY_ACCESS_TOKEN	= "ACCESS_TOKEN";
	//private static final String KEY_PIN_CODE		= "PIN_CODE";

	/** Event listener */
	private ICisEventListener listener;

	/** Constructor */
	public CisEventReceiver(ICisEventListener listener) {
		if (listener==null) {
			throw new IllegalArgumentException();
		}
		this.listener = listener;
	}

	/**
	 * このブロードキャストレシーバーが使用するインテントフィルタを取得します。
	 * Obtains the intent filter for this broadcast receiver.
	 *
	 * @return インテントフィルタ
	 *         Intent filter
	 */
	public IntentFilter getFilter() {
		IntentFilter filter	= new IntentFilter();
		filter.addAction(ACTION_IC_CARD_ATTACHED);
		filter.addAction(ACTION_IC_CARD_DETACHED);
		filter.addAction(ACTION_IC_CARD_INFO);
		filter.addAction(ACTION_NOTIFY_EXCLUSIVE_LOCK_STATE_CHANGED);
		filter.addAction(ACTION_NOTIFY_PIN_CODE_RESULT);
		filter.addAction(ACTION_NOTIFY_PLUGIN_STATE_CHANGED);
		filter.addAction(ACTION_NOTIFY_READER_STATE_CHANGED);
		filter.addAction(ACTION_REQ_PIN_CODE);
		return filter;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.i(TAG, "onReceive(" + action + ")");

		if (ACTION_IC_CARD_ATTACHED.equals(action)) {
			String supportType = intent.getStringExtra(KEY_SUPPORT_TYPE);
			String cardType = intent.getStringExtra(KEY_CARD_TYPE);
			byte[] cardId = intent.getByteArrayExtra(KEY_CARD_ID);
			String deviceId = intent.getStringExtra(KEY_DEVICE_ID);
			CardInfo cardInfo = new CardInfo(supportType, cardType, cardId);
			listener.onCardAttached(cardInfo, deviceId);

		} else if (ACTION_IC_CARD_DETACHED.equals(action)) {
			String supportType = intent.getStringExtra(KEY_SUPPORT_TYPE);
			String cardType = intent.getStringExtra(KEY_CARD_TYPE);
			byte[] cardId = intent.getByteArrayExtra(KEY_CARD_ID);
			String deviceId = intent.getStringExtra(KEY_DEVICE_ID);
			CardInfo cardInfo = new CardInfo(supportType, cardType, cardId);
			listener.onCardDetached(cardInfo, deviceId);

		} else if (ACTION_NOTIFY_READER_STATE_CHANGED.equals(action)) {
			String deviceId = intent.getStringExtra(KEY_DEVICE_ID);
			List<String> types = intent.getStringArrayListExtra(KEY_SUPPORT_TYPES);
			boolean plugged = intent.getBooleanExtra(KEY_PLUGGED, false);
			DeviceInfo deviceInfo = new DeviceInfo(deviceId, types);
			listener.onDeviceStateChanged(deviceInfo, plugged);

		} else if (ACTION_NOTIFY_PLUGIN_STATE_CHANGED.equals(action)) {
			String pluginPackage = intent.getStringExtra(KEY_PACKAGE_NAME);
			ArrayList<String> supportTypes = intent.getStringArrayListExtra(KEY_SUPPORT_TYPES);
			boolean available = intent.getBooleanExtra(KEY_AVAILABLE, false);
			PluginInfo pluginInfo = new PluginInfo(pluginPackage, supportTypes);
			listener.onPluginStateChanged(pluginInfo, available);

		} else if (ACTION_REQ_PIN_CODE.equals(action)) {
			String requestId = intent.getStringExtra(KEY_REQUEST_ID);
			listener.onPinRequired(requestId);

		} else if (ACTION_NOTIFY_PIN_CODE_RESULT.equals(action)) {
			//String packageName = intent.getStringExtra(KEY_PACKAGE_NAME);
			String requestId = intent.getStringExtra(KEY_REQUEST_ID);
			//String pin = intent.getStringExtra(KEY_PIN_CODE);
			boolean isCancelled = intent.getBooleanExtra(KEY_IS_CANCELLED, false);
			boolean result = intent.getBooleanExtra(KEY_RESULT, false);
			int errorCode = intent.getIntExtra(KEY_ERROR_CODE, -1);
			listener.onPinVerified(result, isCancelled, requestId, errorCode);

		} else if (ACTION_NOTIFY_EXCLUSIVE_LOCK_STATE_CHANGED.equals(action)) {
			int lockState = intent.getIntExtra(KEY_LOCK_STATE, -1);
			String token = intent.getStringExtra(KEY_ACCESS_TOKEN);
			int errorCode = intent.getIntExtra(KEY_ERROR_CODE, -1);
			listener.onExclusiveLockStateChanged(lockState, token, errorCode);

		} else if (ACTION_IC_CARD_INFO.equals(action)) {
			//String packageName = intent.getStringExtra(KEY_PACKAGE_NAME);
			String token = intent.getStringExtra(KEY_ACCESS_TOKEN);
			String infoType = intent.getStringExtra(KEY_INFO_TYPE);
			boolean result = intent.getBooleanExtra(KEY_RESULT, false);
			byte[] info = intent.getByteArrayExtra(KEY_INFO);
			int errorCode = intent.getIntExtra(KEY_ERROR_CODE, -1);
			listener.onCardInfoReceived(result, token, infoType, info, errorCode);

		}
	}
}
