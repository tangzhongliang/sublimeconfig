package jp.co.ricoh.ssdk.iccardsample.framework.type;

import java.util.ArrayList;
import java.util.List;

public class PluginInfo {

	/** 
	 * プラグインのパッケージ名 
	 * Package name of the plugin
	 */
	private final String packageName;

	/** 
	 * プラグインが対応するサポート種別
	 * List of support types supported by the plugin
	 */
	private final List<String> supportTypes;

	/** Constructor */
	public PluginInfo(String packageName, List<String> supportTypes) {
		this.packageName = packageName;
		this.supportTypes = supportTypes;
	}

	/**
	 * プラグインのパッケージ名を取得します。
	 * Obtains the package name of the plugin
	 * 
	 * @return プラグインのパッケージ名
	 *         Package name of the plugin
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * プラグインが対応するサポート種別を取得します。
	 * Obtains the list of support types supported by the plugin
	 * 
	 * @return プラグインが対応するサポート種別
	 *         List of support types supported by the plugin
	 */
	public List<String> getSupportTypes() {
		return new ArrayList<String>(supportTypes);
	}
}
