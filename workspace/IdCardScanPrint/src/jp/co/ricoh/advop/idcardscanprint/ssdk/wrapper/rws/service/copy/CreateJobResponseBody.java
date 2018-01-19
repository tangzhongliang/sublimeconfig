/*
 *  Copyright (C) 2013 RICOH Co.,LTD.
 *  All rights reserved.
 */
package jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.service.copy;

import java.util.Map;

import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.Element;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.ResponseBody;

public class CreateJobResponseBody extends Element implements ResponseBody {

	private static final String KEY_JOB_ID	= "jobId";

	CreateJobResponseBody(Map<String, Object> values) {
		super(values);
	}

	/*
	 * jobId (String)
	 */
	public String getJobId() {
		return getStringValue(KEY_JOB_ID);
	}

}
