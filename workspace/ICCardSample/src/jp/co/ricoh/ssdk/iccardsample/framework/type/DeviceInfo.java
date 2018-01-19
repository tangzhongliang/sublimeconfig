package jp.co.ricoh.ssdk.iccardsample.framework.type;

import java.util.ArrayList;
import java.util.List;

public class DeviceInfo {

	/** Device ID */
	private final String deviceId;

	/** List of support types supported by the device */
	private final List<String> supportTypes;

	/** Constructor */
	public DeviceInfo(String deviceId, List<String> supportTypes) {
		this.deviceId = deviceId;
		this.supportTypes = supportTypes;
	}

	/**
	 * デバイスの ID を取得します。
	 * Obtains the device ID
	 * 
	 * @return デバイスの ID
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * デバイスが対応するサポート種別を取得します。
	 * Obtains the list of support types supported by the device 
	 * 
	 * @return デバイスが対応するサポート種別
	 *         list of support types supported by the device
	 */
	public List<String> getSupportTypes() {
		return new ArrayList<String>(supportTypes);
	}
}
