/*
 *  Copyright (C) 2013 RICOH Co.,LTD.
 *  All rights reserved.
 */
package jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook;

import java.util.Map;

import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.ResponseBody;

public class UpdateEntryResponseBody extends Entry implements ResponseBody {

	UpdateEntryResponseBody(Map<String, Object> values) {
		super(values);
	}
	
}
