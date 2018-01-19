package jp.co.ricoh.ssdk.iccardsample.app;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import jp.co.ricoh.ssdk.iccardsample.R;
import jp.co.ricoh.ssdk.iccardsample.framework.cis.CisAccessor;
import jp.co.ricoh.ssdk.iccardsample.framework.cis.CisEventReceiver;
import jp.co.ricoh.ssdk.iccardsample.framework.cis.ICisEventListener;
import jp.co.ricoh.ssdk.iccardsample.framework.type.CardInfo;
import jp.co.ricoh.ssdk.iccardsample.framework.type.DeviceInfo;
import jp.co.ricoh.ssdk.iccardsample.framework.type.PluginInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String[] supportTypes = { "IsoDep", "MifareClassic",
			"MifareUltralight", "NfcA", "NfcB", "NfcF", "NfcV", "KSR" };

	private Handler handler = new Handler();

	private CisEventReceiver cisEventReceiver;

	private CisAccessor cisAccessor = new CisAccessor(this);

	/** Attached card information */
	private CardInfo attachedCard;

	/** Card exclusive locked flag */
	private boolean isLocked = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Register event listener
		cisEventReceiver = new CisEventReceiver(new CisEventListener());
		registerReceiver(cisEventReceiver, cisEventReceiver.getFilter());

		// Set button click listener
		View.OnClickListener clickListener = new MyOnClickListener();
		Button btnRegister = (Button) findViewById(R.id.btn_register);
		Button btnUnregister = (Button) findViewById(R.id.btn_unregister);
		Button btnGetInfo = (Button) findViewById(R.id.btn_get_info);
		btnRegister.setOnClickListener(clickListener);
		btnUnregister.setOnClickListener(clickListener);
		btnGetInfo.setOnClickListener(clickListener);
	}

	private class MyOnClickListener implements View.OnClickListener {
		/** Array of checked items */
		boolean[] checkedArray;

		@Override
		public void onClick(View view) {
			final TextView txtTitle = (TextView) findViewById(R.id.txt_title);
			final Button btnRegister = (Button) findViewById(R.id.btn_register);
			final Button btnUnregister = (Button) findViewById(R.id.btn_unregister);
			final Button btnGetInfo = (Button) findViewById(R.id.btn_get_info);

			switch (view.getId()) {
			case R.id.btn_register:
				checkedArray = new boolean[supportTypes.length];

				new AlertDialog.Builder(MainActivity.this)
						.setTitle(R.string.alert_title_register_app)
						.setMultiChoiceItems(
								supportTypes,
								null,
								new DialogInterface.OnMultiChoiceClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which, boolean isChecked) {
										checkedArray[which] = isChecked;
									}
								})
						.setPositiveButton(R.string.alert_btn_register,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										ArrayList<String> checkedTypes = new ArrayList<String>();
										for (int i = 0; i < checkedArray.length; ++i) {
											if (checkedArray[i]) {
												checkedTypes
														.add(supportTypes[i]);
											}
										}
										cisAccessor
												.register(
														checkedTypes,
														new CisAccessor.RegisterResultHandler() {
															@Override
															public void handleRegisterResult(
																	boolean result,
																	List<String> supportTypes,
																	int errorCode) {
																String msg = getString(R.string.alert_title_res_register_app)
																		+ getString(
																				R.string.alert_msg_res_register_result,
																				result)
																		+ getString(
																				R.string.alert_msg_res_register_support_types,
																				supportTypes)
																		+ getString(
																				R.string.alert_msg_res_register_error_code,
																				errorCode);
																showToast(msg);
																addLog(msg);

																if (result) {
																	btnUnregister
																			.setEnabled(true);
																	btnRegister
																			.setEnabled(false);
																	txtTitle.setText(R.string.act_announce_please_attache);
																}
															}
														});
									}
								})
						.setNegativeButton(R.string.alert_common_cancel, null)
						.show();
				break;

			case R.id.btn_unregister:
				cisAccessor
						.unregister(new CisAccessor.UnregisterResultHandler() {
							@Override
							public void handleUnregisterResult(boolean result,
									int errorCode) {
								String msg = getString(R.string.alert_title_res_unregister_app)
										+ getString(
												R.string.alert_msg_res_unregister_result,
												result)
										+ getString(
												R.string.alert_msg_res_unregister_error_code,
												errorCode);
								showToast(msg);
								addLog(msg);

								if (result) {
									btnUnregister.setEnabled(false);
									btnRegister.setEnabled(true);
									btnGetInfo.setEnabled(false);
									txtTitle.setText(R.string.act_announce_please_register);
								}
							}
						});
				break;
			case R.id.btn_get_info:
				// 諠�蝣ｱ蜿門ｾ怜燕縺ｫ蠢�縺唔C繧ｫ繝ｼ繝画賜莉悶Ο繝�繧ｯ繧定｡後≧
				// 諠�蝣ｱ蜿門ｾ怜�ｦ逅�縺ｯonExclusiveLockStateChnaged()縺ｧ陦後≧
				// It is always required to request to lock the card before a
				// card application
				// requests for card information.
				// See onExclusiveLockStateChanged()
				cisAccessor.lock(attachedCard);
				break;
			default:
				// nothing to do
				break;
			}
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(cisEventReceiver);
	}

	private class CisEventListener implements ICisEventListener {

		TextView txtTitle = (TextView) findViewById(R.id.txt_title);
		TextView txtCardState = (TextView) findViewById(R.id.txt_val_card_state);
		TextView txtCardId = (TextView) findViewById(R.id.txt_val_card_id);
		TextView txtCardType = (TextView) findViewById(R.id.txt_val_card_type);
		TextView txtSupportType = (TextView) findViewById(R.id.txt_val_support_type);
		TextView txtDeviceId = (TextView) findViewById(R.id.txt_val_device_id);
		Button btnGetInfo = (Button) findViewById(R.id.btn_get_info);

		@Override
		public void onCardAttached(CardInfo cardInfo, String deviceId) {
			txtTitle.setText(R.string.act_announce_card_attached);
			txtCardState.setText(R.string.act_txt_val_attached);
			txtCardId.setText(byte2hex(cardInfo.getCardId()));
			txtCardType.setText(cardInfo.getCardType());
			txtSupportType.setText(cardInfo.getSupportType());
			txtDeviceId.setText(deviceId);
			attachedCard = cardInfo;
			btnGetInfo.setEnabled(true);

			String msg = getString(R.string.alert_title_card_attached)
					+ getString(R.string.alert_msg_card_card_id,
							byte2hex(cardInfo.getCardId()))
					+ getString(R.string.alert_msg_card_card_type,
							cardInfo.getCardType())
					+ getString(R.string.alert_msg_card_support_type,
							cardInfo.getSupportType())
					+ getString(R.string.alert_msg_card_device_id, deviceId);
			showToast(msg);
			addLog(msg);
		}

		@Override
		public void onCardDetached(CardInfo cardInfo, String deviceId) {
			txtTitle.setText(R.string.act_announce_please_attache);
			txtCardState.setText(R.string.act_txt_val_detached);
			txtCardId.setText(byte2hex(cardInfo.getCardId()));
			txtCardType.setText(cardInfo.getCardType());
			txtSupportType.setText(cardInfo.getSupportType());
			txtDeviceId.setText(deviceId);

			String msg = getString(R.string.alert_title_card_detached)
					+ getString(R.string.alert_msg_card_card_id,
							byte2hex(cardInfo.getCardId()))
					+ getString(R.string.alert_msg_card_card_type,
							cardInfo.getCardType())
					+ getString(R.string.alert_msg_card_support_type,
							cardInfo.getSupportType())
					+ getString(R.string.alert_msg_card_device_id, deviceId);
			showToast(msg);
			addLog(msg);
		}

		@Override
		public void onPluginStateChanged(PluginInfo pluginInfo,
				boolean available) {
			String msg = getString(R.string.alert_title_plugin_state_changed)
					+ getString(R.string.alert_msg_plugin_id,
							pluginInfo.getPackageName())
					+ getString(R.string.alert_msg_plugin_support_types,
							pluginInfo.getSupportTypes())
					+ getString(R.string.alert_msg_plugin_available, available);
			showToast(msg);
			addLog(msg);
		}

		@Override
		public void onDeviceStateChanged(DeviceInfo deviceInfo, boolean plugged) {
			String msg = getString(R.string.alert_title_device_state_changed)
					+ getString(R.string.alert_msg_device_id,
							deviceInfo.getDeviceId())
					+ getString(R.string.alert_msg_device_support_types,
							deviceInfo.getSupportTypes())
					+ getString(R.string.alert_msg_device_plugged, plugged);
			showToast(msg);
			addLog(msg);
		}

		@Override
		public void onPinRequired(final String requestId) {
			LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
			View view = inflater.inflate(R.layout.dialog_textbox, null);
			TextView textView = (TextView) view.findViewById(R.id.txt_msg);
			textView.setText(R.string.alert_msg_pin_required);
			final EditText editText = (EditText) view
					.findViewById(R.id.edit_input);

			// Show PIN input dialog
			new AlertDialog.Builder(MainActivity.this)
					.setTitle(R.string.alert_title_pin_required)
					.setView(view)
					.setPositiveButton(R.string.alert_common_ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									cisAccessor.verifyPin(requestId, editText
											.getText().toString());
								}
							})
					.setNegativeButton(R.string.alert_common_cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									cisAccessor.cancelVerifyPin(requestId);
								}
							}).show();

			String msg = getString(R.string.alert_title_pin_required)
					+ getString(R.string.alert_msg_pin_request_id, requestId);
			showToast(msg);
			addLog(msg);
		}

		@Override
		public void onPinVerified(boolean result, boolean isCancelled,
				String requestId, int errorCode) {
			String msg = getString(R.string.alert_title_pin_verified)
					+ getString(R.string.alert_msg_pin_result, result)
					+ getString(R.string.alert_msg_pin_cancelled, isCancelled)
					+ getString(R.string.alert_msg_pin_error_code, errorCode);
			showToast(msg);
			addLog(msg);
		}

		@Override
		public void onExclusiveLockStateChanged(int lockState,
				String accessToken, int errorCode) {
			EditText editInfoType = (EditText) findViewById(R.id.edit_info_type);
			if (lockState == LockState.LOCK_START) {
				isLocked = true;
				cisAccessor.getInfo(accessToken, editInfoType.getText()
						.toString());
			} else if (lockState == LockState.LOCK_END) {
				isLocked = false;
			} else if (lockState == LockState.LOCK_ERROR) {
				isLocked = false;
			}

			String msg = getString(R.string.alert_title_lock_state_changed)
					+ getString(R.string.alert_msg_lock_state, lockState)
					+ getString(R.string.alert_msg_lock_access_token,
							accessToken)
					+ getString(R.string.alert_msg_lock_error_code, errorCode);
			showToast(msg);
			addLog(msg);
		}

		@Override
		public void onCardInfoReceived(boolean result, String accessToken,
				String infoType, byte[] info, int errorCode) {
			// Card exclusive lock unlock request
			if (isLocked) {
				cisAccessor.unlock(accessToken);
			}

			String strInfo;
			try {
				strInfo = (info == null) ? null : (new String(info, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				strInfo = null;
			}

			String msg = getString(R.string.alert_title_res_info)
					+ getString(R.string.alert_msg_res_info_result, result)
					+ getString(R.string.alert_msg_res_info_access_token,
							accessToken)
					+ getString(R.string.alert_msg_res_info_info_type, infoType)
					+ getString(R.string.alert_msg_res_info_info, strInfo)
					+ getString(R.string.alert_msg_res_info_error_code,
							errorCode);
			showToast(msg);
			addLog(msg);
		}
	}

	/**
	 * Convert byte array to hex string.
	 * 
	 * @param data
	 *            Byte array
	 * @return hex string
	 */
	private String byte2hex(byte[] data) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			String str = Integer.toHexString(data[i] & 0xff);
			if (str.length() == 1) {
				sb.append("0");
			}
			sb.append(str);
		}
		return sb.toString();
	}

	/**
	 * 繝ｭ繧ｰ陦ｨ遉ｺ繧ｨ繝ｪ繧｢縺ｫ繝ｭ繧ｰ繧定ｿｽ蜉�縺励∪縺吶�� 譛�譁ｰ1000譁�蟄励∪縺ｧ縺瑚｡ｨ遉ｺ縺輔ｌ縺ｾ縺吶�� Add log
	 * message to the log display area Latest 1000 characters are displayed.
	 * 
	 * @param message
	 *            繝ｭ繧ｰ縺ｮ譁�蟄怜�� log massage
	 */
	private void addLog(final String message) {
		this.handler.post(new Runnable() {
			public void run() {
				TextView textLog = (TextView) findViewById(R.id.text_log);
				StringBuilder builder = new StringBuilder(message);

				builder.append("----------------------------\n");
				builder.append(textLog.getText().toString() + "\n");

				if (builder.length() > 1000) {
					textLog.setText(builder.substring(0, 1000));
				} else {
					textLog.setText(builder.toString());
				}
			}
		});

	}

	/**
	 * Show Toast
	 */
	void showToast(final String message) {
		this.handler.post(new Runnable() {
			public void run() {
				Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT)
						.show();
			}
		});
	}
}
