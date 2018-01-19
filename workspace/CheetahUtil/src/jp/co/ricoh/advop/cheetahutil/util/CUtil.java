package jp.co.ricoh.advop.cheetahutil.util;

import java.util.Date;

public class CUtil {

//	private static CUtil instance;
	
	private CUtil() {
	}
	static long prelongTime = 0;

	static long INTERVAL_TIME = 300;
//	public static CUtil instance() {
//	    if(instance == null) {
//	        instance = new CUtil();
//	    }
//		return instance;
//	}
	
    public static boolean isStringEmpty(String str) {
        if(str == null || str.trim().equals("")) {
            return true;
        }
        return false;
    }

    public static boolean isValidEmail(String target) {
        return !isStringEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }         
    
    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            LogC.w("sleep", e);
        }
    } 
    	
    public static boolean isCountinue(){
//    	if(prelongTime == 0){
//			prelongTime = (new Date()).getTime();     
//		}
    	long curTime = (new Date()).getTime();
    	LogC.d("-----------------interval time is " + (curTime - prelongTime));
    	
    	if((curTime - prelongTime) < INTERVAL_TIME) {    		
    		return false;
    	} else {
    		prelongTime = curTime;
    		return true;
    	}
    }
 
	public static void setPrelongTime(long prelongTime) {
		CUtil.prelongTime = prelongTime;
	}
//    public <E> List<E> createElementList() {
//		return new ArrayList<E>();
//	}
}
