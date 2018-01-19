/*
 *  Copyright (C) 2015 RICOH Co.,LTD.
 *  All rights reserved.
 */
package jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.service.scanner;

import java.util.List;
import java.util.Map;

import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.ArrayElement;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.ResponseBody;

/*
 * @since SmartSDK V2.00
 */
public class GetJobListResponseBody extends ArrayElement<JobInfo> implements ResponseBody {

	GetJobListResponseBody(List<Map<String, Object>> list) {
		super(list);
	}

	@Override
	protected JobInfo createElement(Map<String, Object> values) {
		return new JobInfo(values);
	}

}
