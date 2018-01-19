/*
 *  Copyright (C) 2014 RICOH Co.,LTD.
 *  All rights reserved.
 */
package jp.co.ricoh.advop.idcardscanprint.application.resource.common;

import java.util.HashMap;

import jp.co.ricoh.advop.cheetahutil.util.LogC;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;

import com.larvalabs.svgandroid.SVGParser;

/**
 * 画像リソースに関するアクセス機能を提供する。<BR/>
 */
public class ImageResource {
    /** ログ出力用タグ情報 */
	private static final String TAG = "SS:AP:ImgRes";
	/** SVG画像のキャッシュ(ロードに時間がかかるため) */
	private static final HashMap<Integer, Drawable> mSvgCatsh = new HashMap<Integer, Drawable>();

	/** SVG画像をキャッシュする対象データ(メモリを使うため、使用回数が多いものに絞る) */
//	private static final ArrayList<Integer> SVG_CATSH_TARGET = new ArrayList<Integer>();
//	static {
//        SVG_CATSH_TARGET.add(R.raw.sim_icon_filemenu_resolution);
//        SVG_CATSH_TARGET.add(R.raw.icon_value_readable_01);
//        SVG_CATSH_TARGET.add(R.raw.icon_value_readable_02);
//        SVG_CATSH_TARGET.add(R.raw.icon_value_readable_03);
//        SVG_CATSH_TARGET.add(R.raw.icon_value_readable_04);
//        SVG_CATSH_TARGET.add(R.raw.icon_paperorient_oriinalland);
//        SVG_CATSH_TARGET.add(R.raw.icon_paperorient_orijinalport);
//        SVG_CATSH_TARGET.add(R.raw.icon_value_color_01);
//        SVG_CATSH_TARGET.add(R.raw.icon_value_color_02);
//        SVG_CATSH_TARGET.add(R.raw.icon_value_color_03);
//        SVG_CATSH_TARGET.add(R.raw.icon_value_color_05);
//        SVG_CATSH_TARGET.add(R.raw.icon_service_folder);
//        SVG_CATSH_TARGET.add(R.raw.icon_service_foldergroup);
//        SVG_CATSH_TARGET.add(R.raw.icon_service_mail);
//        SVG_CATSH_TARGET.add(R.raw.icon_service_mailgroup);
//        SVG_CATSH_TARGET.add(R.raw.icon_service_mailsecure);
//        SVG_CATSH_TARGET.add(R.raw.icon_value_paper_duplex01);
//        SVG_CATSH_TARGET.add(R.raw.icon_value_paper_duplex02);
//        SVG_CATSH_TARGET.add(R.raw.icon_value_paper_duplex03);
//        SVG_CATSH_TARGET.add(R.raw.icon_value_paper_book01);
//	};

	/**
	 * リソースから画像を取得する。<br/>
	 * SVG画像、通常の画像の両方に対応。<br/>
	 * <br/>
	 * @param resources リソース
	 * @param resId 画像のリソースID
	 * @return Drawableデータ。 失敗時はnull。
	 */
	public static Drawable getImageDrawable(View view, Resources resources, int resId) {
        if (view != null) {
            view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        } else {
            LogC.w(TAG,
                    "getImageDrawable view==null. view must be set setLayerType LAYER_TYPE_SOFTWARE.");
        }
		/* キャッシュにあれば返す */
		if (mSvgCatsh.containsKey(resId)) {
			return mSvgCatsh.get(resId);
		}
		/* キャッシュになければ取得する */
		Drawable drawable = null;
		try {
			/* SVG画像生成 */
			drawable = SVGParser.getSVGFromResource(resources, resId).createPictureDrawable();
//			if (SVG_CATSH_TARGET.contains(resId)) {
				mSvgCatsh.put(resId, drawable);
//			}
		}
		catch (Exception e) {
		}
		if (drawable == null) {
			drawable = resources.getDrawable(resId);
		}
		return drawable;
	}

	/**
	 * キャッシュしたSVGデータを破棄する。<br/>
	 */
	public static void clearCatsh() {
		LogC.i(TAG, "Clear image catsh");
		mSvgCatsh.clear();
	}

	/**
	 * セレクタを生成する。<br/>
	 * @param resources リソース
	 * @param resIdn 通常時のリソースID
	 * @param resIdw 押下時のリソースID
	 * @return セレクタ
	 */
    public static StateListDrawable createSelector(View view, Resources resources, int resIdn,
            int resIdw) {
        Drawable drawableN = getImageDrawable(view, resources, resIdn);
        Drawable drawableW = getImageDrawable(view, resources, resIdw);
		StateListDrawable selector = new StateListDrawable();
		selector.addState( new int[]{ -android.R.attr.state_pressed }, drawableN);
		selector.addState( new int[]{ android.R.attr.state_pressed }, drawableW);
		return selector;
	}

}
