package jp.co.ricoh.advop.idcardscanprint.application.resource.common;

import android.view.View;
import android.view.animation.AlphaAnimation;

/**
 * キーの有効化/無効化を提供する。<BR>
 */
public class KeyAvailability {

    /**
     * キーの有効化/無効化を行う。<BR>
     * @param v 設定対象ビュー
     * @param isAvailable 設定する状態
     *   true:有効、false:無効
     */
    public static void setKeyAvailability(final View v, final boolean isAvailable){
        if(v == null || v.getVisibility() == View.INVISIBLE || v.getVisibility() == View.GONE){
            return;
        }

        if(v.isPressed() && isAvailable == false){
            v.setPressed(false);
        }
        v.setEnabled(isAvailable);

        if(isAvailable || v.isSelected()){
            AlphaAnimation animation = new AlphaAnimation(1.0F, 1.0F);
            animation.setDuration(0);
            animation.setFillBefore(false);
            animation.setFillAfter(true);
            v.startAnimation(animation);
        } else {
            AlphaAnimation animation = new AlphaAnimation(0.3F, 0.3F);
            animation.setDuration(0);
            animation.setFillBefore(false);
            animation.setFillAfter(true);
            v.startAnimation(animation);
        }
    }
}
