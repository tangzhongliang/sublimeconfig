/*
 *  Copyright (C) 2013 RICOH Co.,LTD.
 *  All rights reserved.
 */
package jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.event;

public interface PrintJobListener {
    void jobCanceled(PrintJobEvent event);
    void jobCompleted(PrintJobEvent event);
    void jobAborted(PrintJobEvent event);
    void jobProcessing(PrintJobEvent event);
    void jobPending(PrintJobEvent event);
    void jobProcessingStop(PrintJobEvent event);
}
