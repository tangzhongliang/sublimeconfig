
package jp.co.ricoh.advop.idcardscanprint.util;

import android.R.color;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.model.SettingItemData;

public class SettingItemAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<SettingItemData> colorList;
    private Context context;
    private int select;
    private int disable;
    
    public SettingItemAdapter(Context context, List<SettingItemData> colorList) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.colorList = colorList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return colorList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.view_color_mode, null);
            holder = new ViewHolder();
            holder.selectIcon = (ImageView) convertView.findViewById(R.id.iv_cm_item_lasticon);
            holder.tx = (TextView) convertView.findViewById(R.id.tx_cm_item);
            holder.colorIcon = (ImageView) convertView.findViewById(R.id.iv_cm_item);
            holder.bg = (ImageView) convertView.findViewById(R.id.bg_iv);
            convertView.setTag(holder);
        } else {

            holder = (ViewHolder) convertView.getTag();
        }
        String colorLabel = context.getString(colorList.get(position).getTextId());
        holder.tx.setText(colorLabel);
        holder.colorIcon.setImageDrawable(context.getResources().getDrawable(
                colorList.get(position).getImageId()));
        
        if (select == position) {
//            convertView.setBackground(context.getResources().getDrawable(R.drawable.bt_com_01_w));
            holder.selectIcon.setSelected(true);
            holder.bg.setSelected(true);
        } else {
//            convertView.setBackground(context.getResources().getDrawable(R.drawable.bt_com_01_n));
            holder.selectIcon.setSelected(false);
            holder.bg.setSelected(false);
        }

        return convertView;
    }
    
    public void setSelect(int select) {
        this.select = select;
    }

    public final class ViewHolder {
        public ImageView bg;
        public ImageView selectIcon;
        public TextView tx;
        public ImageView colorIcon;
    }
}
