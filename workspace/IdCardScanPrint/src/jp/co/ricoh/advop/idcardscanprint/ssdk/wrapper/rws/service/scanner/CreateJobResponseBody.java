/*
 *  Copyright (C) 2013-2015 RICOH Co.,LTD.
 *  All rights reserved.
 */
package jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.service.scanner;

import java.util.List;
import java.util.Map;

import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.ArrayElement;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.Element;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.ResponseBody;

public class CreateJobResponseBody extends Element implements ResponseBody {

	private static final String KEY_JOB_ID				= "jobId";
	private static final String KEY_MESSAGE_DETAILS		= "messageDetails";  // SmartSDK V2.00

	CreateJobResponseBody(Map<String, Object> values) {
		super(values);
	}

	/*
	 * jobId (String)
	 */
	public String getJobId() {
		return getStringValue(KEY_JOB_ID);
	}

	/*
	 * messageDetails (Array[Object])
	 * @since SmartSDK V2.00
	 */
	public MessageDetailArray getMessageDetails() {
		List<Map<String, Object>> value = getArrayValue(KEY_MESSAGE_DETAILS);
		if (value == null) {
			return null;
		}
		return new MessageDetailArray(value);
	}


	/*
	 * @since SmartSDK V2.00
	 */
	public static class MessageDetailArray extends ArrayElement<MessageDetail> {

		MessageDetailArray(List<Map<String, Object>> list) {
			super(list);
		}

		@Override
		protected MessageDetail createElement(Map<String, Object> values) {
			return new MessageDetail(values);
		}

	}

	/*
	 * @since SmartSDK V2.00
	 */
	public static class MessageDetail extends Element {

		private static final String KEY_MESSAGE					= "message";
		private static final String KEY_ADDITIONAL_INFO			= "additionalInfo";

		MessageDetail(Map<String, Object> values) {
			super(values);
		}

		/*
		 * message (String)
		 * @since SmartSDK V2.00
		 */
		public String getMessage() {
			return getStringValue(KEY_MESSAGE);
		}

		/*
		 * additionalInfo (Array[Object])
		 * @since SmartSDK V2.00
		 */
		public AdditionalInfoArray getAdditionalInfo() {
			List<Map<String, Object>> value = getArrayValue(KEY_ADDITIONAL_INFO);
			if (value == null) {
				return null;
			}
			return new AdditionalInfoArray(value);
		}

	}

	/*
	 * @since SmartSDK V2.00
	 */
	public static class AdditionalInfoArray extends ArrayElement<AdditionalInfo> {

		AdditionalInfoArray(List<Map<String, Object>> list) {
			super(list);
		}

		@Override
		protected AdditionalInfo createElement(Map<String, Object> values) {
			return new AdditionalInfo(values);
		}

	}

	/*
	 * @since SmartSDK V2.00
	 */
	public static class AdditionalInfo extends Element {

		private static final String KEY_ID						= "id";
		private static final String KEY_VALUE					= "value";

		AdditionalInfo(Map<String, Object> values) {
			super(values);
		}

		/*
		 * id (String)
		 * @since SmartSDK V2.00
		 */
		public String getId() {
			return getStringValue(KEY_ID);
		}

		/*
		 * value (Array[String])
		 * @since SmartSDK V2.00
		 */
		public List<String> getValue() {
			return getArrayValue(KEY_VALUE);
		}

	}

}
