package jp.co.ricoh.ssdk.iccardsample.framework.type;

public class CardInfo {
	
	/** カードのサポート種別 */
	private final String supportType;
	
	/** カード種別 */
	private final String cardType;
	
	/** カードID */
	private byte[] cardId;
	
	/**
	 * コンストラクタ
	 * Constructor
	 * 
	 * @param supportType サポート種別
	 * @param cardType    カード種別
	 * @param cardId      カードID
	 */
	public CardInfo(String supportType, String cardType, byte[] cardId) {
	    this.supportType = supportType;
	    this.cardType = cardType;
	    this.cardId = cardId;
	}
	
	/**
	 * サポート種別を取得します。
	 * Obtains the support type of the card
	 * 
	 * @return サポート種別
	 *         Support type
	 */
	public String getSupportType() {
		return supportType;
	}

	/**
	 * カード種別を取得します。
	 * Obtains the card type
	 * 
	 * @return カード種別
	 *         Card type
	 */
	public String getCardType() {
		return cardType;
	}
	/**
	 * カードIDを取得します。
	 * Obtains the card ID
	 * 
	 * @return カードID
	 *         Card ID
	 */
	public byte[] getCardId() {
		return cardId.clone();
	}

}
