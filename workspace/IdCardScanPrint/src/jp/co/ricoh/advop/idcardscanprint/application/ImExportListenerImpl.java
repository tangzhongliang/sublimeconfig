
package jp.co.ricoh.advop.idcardscanprint.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import jp.co.ricoh.advop.cheetahutil.impexport.ImExportListener;
import jp.co.ricoh.advop.cheetahutil.impexport.ImExportManager;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImExportListenerImpl implements ImExportListener {
    private static final String TAG = ImExportListenerImpl.class.getSimpleName();
    
    // module id
    private static final ModuleID MODULE_ID = new ModuleID("M2a_IDScnPrt", 90000200);

    // key
    private static final String KEY_SECRET_SETTING = "idcard_secret_setting";
    private static final String KEY_NORMAL_SETTING = "idcard_normal_setting";

    // temp file
    private static final String FILE_SECRET_SETTING = "IDCardSecret";
    private static final String FILE_NORMAL_SETTING = "IDCardNormal";
    
    
    /** インポート実行要求結果のI/F定義 */
    private static final String IMPORT_ITEM_RES = "jp.co.ricoh.advop.impexpservice.controllerneed.IMPORT_ITEM_RES";

    /** インポート応答上限時間(スキャナは5分としておく) */
    private static final int LIMIT_SECONDS = 300;

    /** エクスポート実行クラスのインスタンス */
    private DoExportResponse mDoExportResponse = null;
    /** インポート実行クラスのインスタンス */
    private DoImportResponse mDoImportResponse = null;

    private Application application;

    public ImExportListenerImpl(Application application) {
        this.application = application;
    }
    
    private int getTotalNum(Boolean isNeedSecret) {
        if (isNeedSecret) {
            return 2;
        } else {
            return 1;
        }
    }
    
    /**
     * 型定義ファイル要求受信を通知する。</BR>
     * 
     * @return NULL固定
     */
    @Override
    public DefinitionResponse onReceiveDefinitionRequest() {
        return null;
    }

    /**
     * エクスポート可否の問い合わせ受信を通知する。</BR> エクスポート対象アイテムがリソース（ImExportItem.RESOURCE）の場合、<BR>
     * 
     * @param isNeedSecretInfo 機密情報の有無(参照しない)
     * @param isNeedUniqueInfo 固有情報の有無(参照しない)
     * @param item エクスポート対象アイテム
     * @return エクスポート可否応答
     */
    @Override
    public ExportableResponse onReceiveExportableRequest(ImExportTarget target,
            boolean isNeedSecret, boolean isNeedUnique, ImExportItem item) {
        LogC.d("onReceiveExportableRequest Call");
        if (target != ImExportTarget.USER) {
            return null;
        }

        if (item == ImExportItem.RESOURCE) {
            return null;
        }

        // 応答を作る
        ExportableResponse response = new ExportableResponse();
        response.mId = MODULE_ID;
        response.ready = true;
        response.numItems = getTotalNum(isNeedSecret);
        LogC.d("onReceiveExportableRequest End");
        return response;
    }

    /**
     * エクスポート要求受信を通知する。</BR> エクスポート対象アイテムがリソース（ImExportItem.RESOURCE）の場合、<BR>
     * 
     * @param isNeedSecretInfo 機密情報の有無(参照しない)
     * @param isNeedUniqueInfo 固有情報の有無(参照しない)
     * @param item エクスポート対象アイテム
     * @return エクスポート応答
     */
    @Override
    public ExportResponse onReceiveExportRequest(ImExportTarget target,
            boolean isNeedSecret, boolean isNeedUnique, ImExportItem item) {
        LogC.d("onReceiveExportRequest Call");
        if (target != ImExportTarget.USER) {
            return null;
        }

        if (item == ImExportItem.RESOURCE) {
            return null;
        }

        ExportResponse response = new ExportResponse();

        // 応答を作る
        response.mId = MODULE_ID;
        response.ready = true;
        response.numItems = getTotalNum(isNeedSecret);

        // エクスポート応答処理をバックグラウンドで起動
        if (response.ready) {
            if (mDoExportResponse != null) {
                mDoExportResponse.cancel(false);
            }
            mDoExportResponse = new DoExportResponse();
            mDoExportResponse.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, isNeedSecret);//.execute(isNeedSecret);
        }
        LogC.d("onReceiveExportRequest End");
        return response;
    }

    /**
     * エクスポート完了を通知する。</BR> 何も処理しない。<BR>
     */
    @Override
    public void onReceiveExportFinished() {
        // do nothing
    }

    /**
     * インポート可否の問い合わせ受信を通知する。</BR> エクスポート対象アイテムがリソース（ImExportItem.RESOURCE）の場合、<BR>
     * 
     * @param isNeedSecret 機密情報の有無(参照しない)
     * @param isNeedUnique 固有情報の有無(参照しない)
     * @param item インポート対象アイテム
     * @return インポート応答
     */
    @Override
    public ImportableResponse onReceiveImportableRequest(boolean isNeedSecret,
            boolean isNeedUnique,
            ImExportItem item) {

        if (item == ImExportItem.RESOURCE) {
            return null;
        }

        ImportableResponse response = new ImportableResponse();

        // 応答を作る
        response.mId = MODULE_ID;
        response.ready = true;

        return response;
    }

    /**
     * インポート可否の問い合わせ受信を通知する。</BR> エクスポート対象アイテムがリソース（ImExportItem.RESOURCE）の場合、<BR>
     * 
     * @param filePath ファイルパス
     * @param isNeedSecret 機密情報の有無(参照しない)
     * @param isNeedUnique 固有情報の有無(参照しない)
     * @param item インポート対象アイテム
     * @return インポート応答
     */
    @Override
    public ImportResponse onReceiveImportRequest(String filePath, boolean isNeedSecret,
            boolean isNeedUnique,
            ImExportItem item) {

        if (item == ImExportItem.RESOURCE) {
            return null;
        }

        // エクスポート応答処理をバックグラウンドで起動
        if (mDoImportResponse != null) {
            mDoImportResponse.cancel(false);
        }
        mDoImportResponse = new DoImportResponse(isNeedSecret);
        mDoImportResponse.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filePath);//.execute(filePath);

        return null;
    }

    /**
     * インポート完了を通知する。</BR> 何も処理しない。</BR>
     */
    @Override
    public void onReceiveImportFinished() {
        // do nothing
    }

    /**
     * インポート/エクスポート結果を提供する。<BR>
     */
    private class ImExportResult {
        int id;
        int result;
        String key;

        ImExportResult(int id, int result, String key) {
            this.id = id;
            this.result = result;
            this.key = key;
        }
    }

    /**
     * エクスポート実行タスクを提供する。<BR>
     * 非同期でエクスポート応答を生成する。<BR>
     */
    private class DoExportResponse extends AsyncTask<Boolean, Void, Integer> {
        /** エクスポートの機密情報ファイル名 */
        private final String EXPORT_FILE_NAME = "export.csv";
        /** エクスポートの固有情報ファイル名 */
        private final String EXPORT_RESULT_FILE_NAME = "export_result.csv";
        
        /**
         * 初期値およびプログラム情報のエクスポート用ファイルを作成し、 エクスポート項目を送信する。<BR>
         */
        @Override
        protected Integer doInBackground(Boolean... params) {
            Boolean isNeedSecret = params[0];
            String path = application.getFilesDir().getPath().toString() + "/";
            LogC.d("CreateFilePath:" + path);
            try {
                FileOutputStream fileOutputStream = application.openFileOutput(EXPORT_FILE_NAME,
                        Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
                // エクスポート結果
                List<ImExportResult> resultList = new ArrayList<ImExportResult>();
                
                // 非機密情報
                String data = PreferencesUtil.getInstance().exportUnencrypted();
                LogC.d("export", data);
                createBinalyData(FILE_NORMAL_SETTING, data);
                resultList.add(writeOutPutStream(
                        1, 
                        "1", 
                        KEY_NORMAL_SETTING, 
                        path + FILE_NORMAL_SETTING, 
                        true, 
                        fileOutputStream));
                
                // 機密情報
                if (isNeedSecret) {
                    data = PreferencesUtil.getInstance().exportEncrypted();
                    LogC.d("export", data);
                    createBinalyData(FILE_SECRET_SETTING, data);
                    resultList.add(writeOutPutStream(
                            2, 
                            "1", 
                            KEY_SECRET_SETTING, 
                            path + FILE_SECRET_SETTING, 
                            true, 
                            fileOutputStream));
                }

                fileOutputStream.close();
                createImExportResultFile(application.getFilesDir(), EXPORT_RESULT_FILE_NAME, resultList);

                ImExportManager.sendExportItem(
                        application,
                        MODULE_ID,
                        path + EXPORT_FILE_NAME,
                        path + EXPORT_RESULT_FILE_NAME);
            } catch (FileNotFoundException e) {
                LogC.e(TAG, e);
            } catch (IOException e) {
                LogC.e(TAG, e);
            }
            return 0;
        }
        
        /**
         * CSVファイルの書き込みをする。<BR>
         * 
         * @param id プリファレンスID
         * @param binaly バイナリデータ有無
         * @param key 項目名
         * @param value 設定値
         * @param out 出力ファイル
         * @return エクスポート結果
         */
        private ImExportResult writeOutPutStream(final int id, final String binaly,
                final String key, String value, Boolean isSecret, FileOutputStream out) {
            StringBuffer sb = new StringBuffer();
            if (value == null) {
                value = "";
            }
            // プリファレンスID
            sb.append("\"" + Integer.toString(MODULE_ID.value + id)
                    + "\",");
            // 機密情報有無
            if (isSecret) {
                sb.append("\"1\","); 
            } else {
                sb.append("\"0\","); 
            }
            // 固有情報有無
            sb.append("\"0\","); 
            // 設定値バイナリデータ有無
            sb.append("\"" + binaly + "\",");
            // 項目名
            sb.append("\"" + key + "\",");
            // 設定値
            sb.append("\"" + value + "\"\r\n");
            try {
                out.write(sb.toString().getBytes());
            } catch (IOException e) {
                LogC.e(TAG, e);
            }
            return new ImExportResult(MODULE_ID.value + id, 0, key);
        }

        /**
         * バイナリデータを作成する。<BR>
         * 
         * @param fileName バイナリデータのファイル名
         * @param value 保存する値
         */
        private void createBinalyData(final String fileName, final String value) {
            if (value == null) {
                return;
            }
            FileOutputStream outputStream = null;
            try {
                outputStream = application.openFileOutput(fileName, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
                byte[] bytes = value.getBytes();
                outputStream.write(bytes);
                outputStream.close();
            } catch (FileNotFoundException e) {
                LogC.e(TAG, e);
            } catch (IOException e) {
                LogC.e(TAG, e);
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e1) {
                    }
                }
            }
        }

        /*
         * (非 Javadoc)
         * @see android.os.AsyncTask#onCancelled()
         */
        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    /**
     * インポート実行タスクを提供する。<BR>
     * 非同期でインポート応答を生成する。<BR>
     */
    private class DoImportResponse extends AsyncTask<String, Void, Integer> {
        /** インポート結果を保存するファイル名 */
        private final String IMPORT_RESULT_FILE_NAME = "import_result.csv";
        /** 正規表現：CSVコンマ */
        private static final String REGEX_CSV_COMMA = ",(?=(([^\"]*\"){2})*[^\"]*$)";
        /** 正規表現：ダブルクォーテーションの囲み文字 */
        private static final String REGEX_SURROUND_DOUBLEQUATATION = "^\"|\"$";
        /** 正規表現：ダブルクォーテーション */
        private static final String REGEX_DOUBLEQUOATATION = "\"\"";

        private boolean isNeedSecret;
        
        public DoImportResponse(boolean isNeedSecret) {
            this.isNeedSecret = isNeedSecret;
        }
        
        /**
         * CSVファイルからインポート情報を読み込み、プログラム情報登録する。<BR>
         * 
         * @param params パラメータ
         * @return 0固定
         */
        @Override
        protected Integer doInBackground(String... params) {

            String path = application.getFilesDir().getPath().toString() + "/";
            int successCount = 0;

            FileOutputStream fileOutputStream = null;
            BufferedReader bufferdInputStream = null;
            try {
                // csvファイルからデータを読み込む
                bufferdInputStream = new BufferedReader(new FileReader(params[0]));
                fileOutputStream = application.openFileOutput(IMPORT_RESULT_FILE_NAME, Context.MODE_WORLD_READABLE
                        | Context.MODE_WORLD_WRITEABLE);
                // インポート結果
                List<ImExportResult> resultList = new ArrayList<ImExportResult>();
                String line = null;
                List<String[]> lineList = new ArrayList<String[]>();
                while ((line = bufferdInputStream.readLine()) != null)
                {
                    String token[] = splitLineWithComma(line);
                    /* プリファレンスID */
                    int prefid;
                    try {
                        prefid = Integer.parseInt(token[0]);
                    } catch (NumberFormatException e) {
                        prefid = 0;
                    }

                    int itemid = prefid - MODULE_ID.value;
                    int num = getTotalNum(isNeedSecret);
                    if (itemid <= 0 || itemid > num) {
//                        LogC.d(TAG, "not valid itemId:" + itemid + " prefId:" + prefid);
                        continue;
                    }
                    LogC.d(TAG, line);
                    lineList.add(token);
                    successCount++;
                }
                // ここでインポート実行要求の結果を通知する
                sendImportResponse(successCount);

                // スキャナ項目のインポート処理
                for (String[] token : lineList) {
                    // for (String s: token) {
                    // LogC.d(TAG, "ImportItem:" + s);
                    // }
                    /* プリファレンスID */
                    int prefid;
                    try {
                        prefid = Integer.parseInt(token[0]);
                    } catch (NumberFormatException e) {
                        prefid = 0;
                    }

                    int itemid = prefid - MODULE_ID.value;
                    if (itemid == 1) {
                        String defaultData = getStringBinaryData(token[5]);
                        PreferencesUtil.getInstance().importUnencrypted(defaultData);
                    } else if (itemid == 2) {
                        String defaultData = getStringBinaryData(token[5]);
                        PreferencesUtil.getInstance().importEncrypted(defaultData);
                    }
                    ImExportResult result = new ImExportResult(Integer.parseInt(token[0]), 0,
                            token[4]);
                    resultList.add(result);
                }
                fileOutputStream.close();
                bufferdInputStream.close();
                createImExportResultFile(application.getFilesDir(), IMPORT_RESULT_FILE_NAME, resultList);

                ImExportManager.sendImportResponse(
                        application,
                        MODULE_ID,
                        path + IMPORT_RESULT_FILE_NAME,
                        successCount);
                return 0;
            } catch (FileNotFoundException e) {
                LogC.e(TAG, e);
            } catch (IOException e) {
                LogC.e(TAG, e);
                // TODO インポートでエラーになる暫定対応
            } catch (Exception e) {
                LogC.e(TAG, e);
            } finally {
                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (IOException e1) {
                }
                try {
                    if (bufferdInputStream != null) {
                        bufferdInputStream.close();
                    }
                } catch (IOException e1) {
                }
            }
            // エラー発生時の応答
            ImExportManager.sendImportResponse(
                    application,
                    MODULE_ID,
                    path + IMPORT_RESULT_FILE_NAME,
                    successCount);
            return 0;
        }
        
        /**
         * コンマ区で区切られた行までの文字列を取得する。<BR>
         * 
         * @param line 対象文字列
         * @return 変換結果
         */
        private String[] splitLineWithComma(String line) {
            String[] arr = null;

            try {
                Pattern cPattern = Pattern.compile(REGEX_CSV_COMMA);
                String[] cols = cPattern.split(line, -1);

                arr = new String[cols.length];
                for (int i = 0, len = cols.length; i < len; i++) {
                    String col = cols[i].trim();

                    Pattern sdqPattern =
                            Pattern.compile(REGEX_SURROUND_DOUBLEQUATATION);
                    Matcher matcher = sdqPattern.matcher(col);
                    col = matcher.replaceAll("");

                    Pattern dqPattern =
                            Pattern.compile(REGEX_DOUBLEQUOATATION);
                    matcher = dqPattern.matcher(col);
                    col = matcher.replaceAll("\"");

                    arr[i] = col;
                }
            } catch (Exception e) {
                LogC.e(TAG, e);
            }
            return arr;
        }

        /**
         * エクスポートしたバイナリデータをString形式で取得する。<BR>
         * 
         * @param path バイナリデータのフルパス
         * @return バイナリデータのString
         */
        private String getStringBinaryData(final String path) {
            if (path == null || path.length() == 0) {
                return null;
            }
            String rtn = null;
            FileInputStream input = null;
            try {
                File file = new File(path);
                input = new FileInputStream(new File(path));
                byte[] data = new byte[(int) file.length()];
                input.read(data);
                input.close();
                rtn = new String(data);
            } catch (FileNotFoundException e) {
                LogC.e(TAG, e);
            } catch (IOException e) {
                LogC.e(TAG, e);
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e1) {
                    }
                }
            }
            return rtn;
        }

       
        /**
         * インポート実行要求結果を通知する。<BR>
         * 
         * @param successCount
         */
        private void sendImportResponse(int successCount) {
            Intent resIntent = new Intent(IMPORT_ITEM_RES);
            resIntent.putExtra("mIdList", MODULE_ID.getStringValue());
            resIntent.putExtra("limitSeconds", LIMIT_SECONDS);
            resIntent.putExtra("totalNum", successCount);
            application.sendBroadcast(resIntent);
            LogC.d(TAG, resIntent.toString());
            LogC.d(TAG, resIntent.getExtras().toString());
        }

       
    }

    /**
     * インポート/エクスポート結果ファイルを生成する。<BR>
     * 
     * @param fileDir 生成するファイルのディレクトリ
     * @param fileName 生成するファイル名
     * @param resultList インポート/エクスポート結果リスト
     * @throws IOException ファイルへの書き込みに失敗
     */
    private void createImExportResultFile(File fileDir, String fileName,
            List<ImExportResult> resultList) throws IOException {
        BufferedWriter bw = null;
        try {
            fileDir.mkdirs();
            bw = new BufferedWriter(new OutputStreamWriter(application.openFileOutput(fileName,
                    Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE)));
            StringBuffer sb = new StringBuffer();
            for (ImExportResult result : resultList) {
                sb.setLength(0);
                /* プリファレンスID */
                sb.append("\"").append(result.id).append("\",");
                /* 失敗理由 */
                sb.append("\"").append(result.result).append("\",");
                /* 項目名 */
                sb.append("\"").append(result.key).append("\"");
                bw.write(sb.toString());
                bw.newLine();
            }
        } finally {
            if (bw != null) {
                bw.close();
            }
        }
    }
}
