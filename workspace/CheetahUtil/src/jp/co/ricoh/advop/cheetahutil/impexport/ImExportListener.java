/*
 *  Copyright (C) 2014 RICOH Co.,LTD.
 *  All rights reserved.
 */
package jp.co.ricoh.advop.cheetahutil.impexport;

/**
 * インポートエクスポートの通知で使用される。<BR>
 */
public interface ImExportListener {
    /**
     * エキスポート出力対象定義の列挙を表す。<BR>
     */
    public enum ImExportTarget {
        /** エクスポート出力対象：初期設定 */
        USER(1),
        /** エクスポート出力対象：サービス設定 */
        SERVICE(2),
        /** エクスポート出力対象：CC認証設定 */
        CC(3);
        /** エクスポート対象アイテムの値 */
        private int value;

        /**
         * 指定された値を保持するインポートエクスポートの通知リスナーを構築する。<BR>
         * @param n エクスポート対象アイテムの値
         */
        private ImExportTarget(int n) {
            this.value = n;
        }

        /**
         * エクスポート出力対象の値を取得する。<BR>
         * @return エクスポート出力対象の値
         */
        private int getIntValue() {
            return (value);
        }

        /**
         * 該当のエクスポート出力対象の値を取得する。<BR>
         * @param n エクスポート対象アイテムの値
         * @return 該当のエクスポート対象アイテムの値
         */
        public static ImExportTarget valueOf(final int n) {
            for (ImExportTarget d : values()) {
                if (d.getIntValue() == n) {
                    return d;
                }
            }
            return valueOf("USER"); // fail safe
        }
    }

	/**
	 * エキスポート対象アイテム（カラム）定義を表す。<BR>
	 */
	public enum ImExportItem {
	    /** エクスポート対象アイテムの定義：すべて */
		ALL(1),
		/** エクスポート対象アイテムの定義：リソース */
		RESOURCE(2),
		/** エクスポート対象アイテムの定義：リソース外 */
		NONRESOURCE(3);
		/** エクスポート対象アイテムの値 */
		private int value;

		/**
		 * 指定された値を保持するインポートエクスポートの通知リスナーを構築する。<BR>
		 * @param n エクスポート対象アイテムの値
		 */
		private ImExportItem(int n) {
			this.value = n;
		}

		/**
		 * エクスポート対象アイテムの値を取得する。<BR>
		 * @return エクスポート対象アイテムの値
		 */
		private int getIntValue() {
			return (value);
		}

		/**
		 * 該当のエクスポート対象アイテムの値を取得する。<BR>
		 * @param n エクスポート対象アイテムの値
		 * @return 該当のエクスポート対象アイテムの値
		 */
		public static ImExportItem valueOf(final int n) {
			for (ImExportItem d : values()) {
				if (d.getIntValue() == n) {
					return d;
				}
			}
			return valueOf("ALL"); // fail safe
		}
	}

	/**
	 * モジュールID定義を表す。<BR>
	 */
	public class ModuleID {
//	    /** コピーのモジュールID定義 */
//	    QUICK_COPY("M_SimpleCopy", 80000000),
//	    /** スキャナのモジュールID定義 */
//	    QUICK_SCAN("M_SimpleScan", 80100000),
//	    /** ファックスのモジュールID定義 */
//	    QUICK_FAX("M_SimpleFax", 80200000),
//	    /** その他のモジュールID定義 */
//	    RESERVED("", 80300000);
	    /** モジュール名 */
		public String name;
		/** モジュールID */
		public int value;

		/**
		 * 指定された値を保持するモジュールID定義を構築する。<BR>
		 * @param name モジュール名
		 * @param value モジュールID
		 */
		public ModuleID(final String name, final int value) {
		    this.name = name;
		    this.value = value;
        }

		/**
		 * モジュール名を取得する。<BR>
		 * @return モジュール名
		 */
		public String getStringValue() {
		    return this.name;
		}
	}

	/**
	 * 型定義ファイル応答を表す。<BR>
	 */
	public class DefinitionResponse {
	    /** モジュールID */
		public ModuleID mId;
		/** バージョン */
		public String version;
		/** リソースID */
		public Integer resourceID;
	}

	/**
	 * 型定義ファイル要求受信を通知する。<BR>
	 */
	public abstract DefinitionResponse onReceiveDefinitionRequest();

	/**
	 * エクスポート可否応答を表す。<BR>
	 */
	public class ExportableResponse {
	    /** モジュールID */
		public ModuleID mId;
		/** 実行可否 */
		public boolean ready;
		/** 項目数 */
		public int numItems;
	}

	   /**
     * エクスポート可否の問い合わせ受信を通知する。<BR>
     * @param isNeedSecret 機密情報の有無
     * @param isNeedUnique 固有情報の有無
     * @param item エクスポート対象アイテム
     * @return エクスポート可否応答
     */
    public abstract ExportableResponse
            onReceiveExportableRequest(ImExportTarget target, boolean isNeedSecret, boolean isNeedUnique, ImExportItem item);

	/**
	 * エクスポート応答を表す。<BR>
	 */
	public class ExportResponse {
	    /** モジュールID */
		public ModuleID mId;
		/** 実行可否 */
		public boolean ready;
		/** 項目数 */
		public int numItems;
	}

	   /**
     * エクスポート要求受信を通知する。<BR>
     * @param isNeedSecret 機密情報の有無
     * @param isNeedUnique 固有情報の有無
     * @param item エクスポート対象アイテム
     * @return エクスポート可否応答
     */
    public abstract ExportResponse
            onReceiveExportRequest(ImExportTarget target, boolean isNeedSecret, boolean isNeedUnique, ImExportItem item);

	/**
	 * エクスポート完了を通知する。<BR>
	 */
	public abstract void onReceiveExportFinished();

	/**
	 * インポート可否応答を表す。<BR>
	 */
	public class ImportableResponse {
	    /** モジュールID */
		public ModuleID mId;
		/** 実行可否 */
		public boolean ready;
	}

	/**
	 * インポート可否の問い合わせ受信を通知する。<BR>
	 * @param isNeedSecret 機密情報の有無
	 * @param isNeedUnique 固有情報の有無
	 * @param item インポート対象アイテム
	 * @return インポート可否応答
	 */
	public abstract ImportableResponse
			onReceiveImportableRequest(boolean isNeedSecret, boolean isNeedUnique, ImExportItem item);

	/**
	 * インポート応答を表す。<BR>
	 */
	public class ImportResponse {
	    /** モジュールID */
		public ModuleID mId;
		/** インポート項目数 */
        public int mTotalNum;
        /** 応答上限時間 */
        public int mLimitSeconds;
	}

	/**
	 * インポート可否の問い合わせ受信を通知する。<BR>
	 * @param filePath ファイルパス
	 * @param isNeedSecret 機密情報有無
	 * @param isNeedUnique 固有情報の有無
	 * @param item インポート対象アイテム
	 * @return インポート可否応答
	 */
	public abstract ImportResponse
			onReceiveImportRequest(String filePath, boolean isNeedSecret, boolean isNeedUnique, ImExportItem item);

	/**
	 * インポート完了を通知する。<BR>
	 */
	public abstract void onReceiveImportFinished();
}
