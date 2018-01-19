package jp.co.ricoh.advop.idcardscanprint.logic;

import jp.co.ricoh.advop.idcardscanprint.logic.job.LongJob;

public class JobData {
    private LongJob currentJob = null;
    
    private String pathFrontImg;
    private String pathBackImg;
    private String pathCombinedJPG;
    private String pathSendFile;
    private String pathPrintFile;
    private Integer imgRotate;
    private boolean needPDFPrint = false;
    private boolean jobCancel = false;
    
    public boolean isJobCancel() {
        return jobCancel;
    }
    public void setJobCancel(boolean jobCancel) {
        this.jobCancel = jobCancel;
    }
    public LongJob getCurrentJob() {
        return currentJob;
    }
    public void setCurrentJob(LongJob currentJob) {
        this.currentJob = currentJob;
    }
    public String getPathFrontImg() {
        return pathFrontImg;
    }
    public void setPathFrontImg(String pathFrontImg) {
        this.pathFrontImg = pathFrontImg;
    }
    public String getPathCombinedJPG() {
        return pathCombinedJPG;
    }
    public void setPathCombinedJPG(String pathCombinedJPG) {
        this.pathCombinedJPG = pathCombinedJPG;
    }
    public String getPathSendFile() {
        return pathSendFile;
    }
    public void setPathSendFile(String pathSendFile) {
        this.pathSendFile = pathSendFile;
    }
    public String getPathBackImg() {
        return pathBackImg;
    }
    public void setPathBackImg(String pathBackImg) {
        this.pathBackImg = pathBackImg;
    }
    public boolean isNeedPDFPrint() {
        return needPDFPrint;
    }
    public void setNeedPDFPrint(boolean needPDFPrint) {
        this.needPDFPrint = needPDFPrint;
    }

    public Integer getImgRotate() {
        return imgRotate;
    }
    public void setImgRotate(Integer imgRotate) {
        this.imgRotate = imgRotate;
    }
    public String getPathPrintFile() {
        return pathPrintFile;
    }
    public void setPathPrintFile(String pathPrintFile) {
        this.pathPrintFile = pathPrintFile;
    }
    
}
