
package jp.co.ricoh.advop.idcardscanprint.model;

public class SettingItemData {
    private Object itemValue;
    private int imageId;
    private int textId;

    public SettingItemData(Object itemValue, int imgId, int textId) {
        this.itemValue = itemValue;
        this.imageId = imgId;
        this.textId = textId;
    }

    public Object getItemValue() {
        return itemValue;
    }

    public void setItemValue(Object itemValue) {
        this.itemValue = itemValue;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getTextId() {
        return textId;
    }

    public void setTextId(int textId) {
        this.textId = textId;
    }

}
