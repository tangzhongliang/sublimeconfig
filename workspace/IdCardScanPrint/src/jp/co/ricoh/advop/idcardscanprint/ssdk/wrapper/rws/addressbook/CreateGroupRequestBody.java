/*
 *  Copyright (C) 2013 RICOH Co.,LTD.
 *  All rights reserved.
 */
package jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook;

import java.util.HashMap;
import java.util.Map;

import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.RequestBody;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.json.EncodedException;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.json.JsonUtils;

public class CreateGroupRequestBody extends Group implements RequestBody {

	private static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";
	
	public CreateGroupRequestBody() {
		super(new HashMap<String, Object>());
	}
	public CreateGroupRequestBody(Map<String, Object> value) {
		super(value);
	}

	@Override
	public String getContentType() {
		return CONTENT_TYPE_JSON;
	}

	@Override
	public String toEntityString() {
		try {
			return JsonUtils.getEncoder().encode(values);
		} catch (EncodedException e) {
		    LogC.w(e);
			return "{}";
		}
	}

}
