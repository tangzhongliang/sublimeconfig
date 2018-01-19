/*
 *  Copyright (C) 2013 RICOH Co.,LTD.
 *  All rights reserved.
 */
package jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.json;

public interface Encoder {
	
	public String encode(Object source) throws EncodedException;

}
