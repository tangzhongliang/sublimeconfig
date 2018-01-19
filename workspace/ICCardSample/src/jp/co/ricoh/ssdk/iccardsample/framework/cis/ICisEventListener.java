package jp.co.ricoh.ssdk.iccardsample.framework.cis;

import jp.co.ricoh.ssdk.iccardsample.framework.type.CardInfo;
import jp.co.ricoh.ssdk.iccardsample.framework.type.DeviceInfo;
import jp.co.ricoh.ssdk.iccardsample.framework.type.PluginInfo;


public interface ICisEventListener {
	
	/** Lock state */
	public static final class LockState {
		/** Start exclusive lock */
    	public static final int LOCK_START = 0;
    	/** End exclusive lock */
    	public static final int LOCK_END = 1;
    	/** Exclusive lock error */
    	public static final int LOCK_ERROR = 2;
	}

	/**
	 * カードのアタッチを通知します。
	 * Called when a card has been attached. 
	 * 
	 * @param  cardInfo   カード情報
	 *                    Card information
	 * @param  deviceId   アタッチを検知したデバイスのID
	 *                    ID of the device in which card attached event is detected.
	 */
	public void onCardAttached(CardInfo cardInfo, String deviceId);

	/**
	 * カードのデタッチを通知します。
	 * Called when a card has been detached. 
	 * 
	 * @param  cardInfo   カード情報
	 *                    Card information
	 * @param  deviceId   アタッチを検知したデバイスのID
	 *                    ID of the device in which card attached event is detected.
	 */
	public void onCardDetached( CardInfo cardInfo, String deviceId);
	
	/**
	 * プラグインの使用可／不可状態が変化した際に通知されます。
	 * Called when a card plug-ins availability has been changed.
	 * 
	 * @param  pluginInfo プラグイン情報
	 *                    Plugin information
	 * @param  available  true: プラグイン使用可、false: プラグイン使用不可
	 *                    true: Available, false: Unavailable
	 */
	public void onPluginStateChanged(PluginInfo pluginInfo, boolean available);

	/**
	 * デバイスの接続状態が変化した際に通知されます。
	 * Called when a connection state of card reader has been changed.
	 * 
	 * @param  deviceInfo デバイス情報
	 *                    Device information
	 * @param  plugged    true: 接続されている、false: 切断されている
	 *                    true: Plugged, false: Unplugged
	 */
	public void onDeviceStateChanged(DeviceInfo deviceInfo, boolean plugged);

	/**
	 * ＰＩＮ認証が必要な際に通知されます。
	 * Called when PIN authentication has been required.
	 * 
	 * @param  requestId ＰＩＮ要求のリクエストＩＤ
	 *                    Request ID of PIN authentication request
	 */
	public void onPinRequired(String requestId);
	
	/**
	 * PIN認証結果が通知されます。
	 * Called when the PIN authentication result has been notified.
	 * 
	 * @param result      PIN認証結果、もしくはPIN認証キャンセルに対する結果
	 *                    Result of the PIN authentication request or PIN authentication cancellation request
	 * @param isCancelled PIN認証キャンセルに対する結果の場合はtrue
	 *                    True when it is the responce of PIN authentication cancellation request
	 * @param requestId   ＰＩＮ要求のリクエストＩＤ
	 *                    Request ID of PIN authentication request
	 * @param errorCode   エラーコード
	 *                    Error Code
	 */
	public void onPinVerified(boolean result, boolean isCancelled, String requestId, int errorCode);
	
	/**
	 * 排他ロック状態変化を通知します。
	 * Called when the card exclusive lock state has been changed
	 * 
	 * @param lockState     ロック状態
	 *                      Lock state
	 * @parama accessToken アクセストークン。ロック状態がLOCK_STARTの場合のみ通知されます。
	 *                     Access token. Set only when Lock state is LOCK_START.
	 * @param errorCode    エラーコード
	 *                     Error Code
	 */
	public void onExclusiveLockStateChanged(int lockState, String accessToken, int errorCode);
	
	/**
	 * ICカード情報取得に対する応答の通知です。
	 * Notifies the result of card information request
	 * 
	 * @param result      取得結果
	 *                    true: Success, false: failed
	 * @param accessToken アクセストークン
	 *                    Access token obtained for exclusive lock.
	 * @param infoType    情報種別
	 *                    Information type
	 * @param info        取得要求した情報。取得結果がfalseの時はnull
	 *                    Information. Set only if result is true.
	 * @param errorCode   エラーコード
	 *                    Error code
	 */
	public void onCardInfoReceived(boolean result, String accessToken, String infoType, byte[] info, int errorCode);
}
