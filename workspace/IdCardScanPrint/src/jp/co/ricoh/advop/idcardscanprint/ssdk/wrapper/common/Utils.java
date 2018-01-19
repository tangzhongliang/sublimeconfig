/*
 *  Copyright (C) 2013 RICOH Co.,LTD.
 *  All rights reserved.
 */
package jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.client.RestResponse;

/**
 * 各Functionで用いる共通処理をまとめたクラス
 */
public final class Utils {
	
	public static <K, V> Map<K, V> createElementMap() {
		return new LinkedHashMap<K, V>();
	}
	
	public static <E> List<E> createElementList() {
		return new ArrayList<E>();
	}
	
	public static InvalidResponseException createInvalidResponseException(RestResponse response, Map<String, Object> body){
		ErrorResponseBody responseBody = null;
		if (body != null) {
			responseBody = new ErrorResponseBody(body);
		}
		return new InvalidResponseException(response, responseBody);
	}
	
	private Utils() {}

}
