package jp.co.ricoh.advop.cheetahutil.util;

import android.os.AsyncTask;

public abstract class CancelableAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> implements Cancelable {
	
}
