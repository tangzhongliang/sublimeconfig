/*
 *  Copyright (C) 2013 RICOH Co.,LTD.
 *  All rights reserved.
 */
package jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard;

import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.ScanRequestAttribute;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.supported.MaxMinSupported;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.RangeElement;

public final class OriginalSizeCustomX implements ScanRequestAttribute {

	private static final String SCAN_ORIGINAL_CUSTOM_SIZE_X = "originalSizeCustomX";

	private final int sizeX;

	public OriginalSizeCustomX(int x) {
		this.sizeX = x;
	}

    @Override
    public String toString() {
        return Integer.toString(sizeX);
    }

	@Override
	public Class<?> getCategory() {
		return OriginalSizeCustomX.class;
	}

	@Override
	public String getName() {
		return SCAN_ORIGINAL_CUSTOM_SIZE_X;
	}

	@Override
	public Object getValue() {
		return this.sizeX;
	}


	public static MaxMinSupported getSupportedValue(RangeElement values) {
		return MaxMinSupported.getMaxMinSupported(values);
	}
}
