package com.android.phone;

import com.android.internal.telephony.CommandException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.WindowManager;

import java.util.ArrayList;
import com.mediatek.xlog.Xlog;

interface  TimeConsumingPreferenceListener {
    public void onStarted(Preference preference, boolean reading);
    public void onUpdate(TimeConsumingPreferenceListener tcp, boolean flag);
    public void onFinished(Preference preference, boolean reading);
    public void onError(Preference preference, int error);
    public void onException(Preference preference, CommandException exception);
}

public class TimeConsumingPreferenceActivity extends PreferenceActivity
                        implements TimeConsumingPreferenceListener, DialogInterface.OnClickListener,
                        DialogInterface.OnCancelListener {
    private static final String LOG_TAG = "Settings/TimeConsumingPreferenceActivity";
    private final boolean DBG = true;

    private static final int BUSY_READING_DIALOG = 100;
    private static final int BUSY_SAVING_DIALOG = 200;

    static final int EXCEPTION_ERROR = 300;
    static final int RESPONSE_ERROR = 400;
    static final int RADIO_OFF_ERROR = 500;
    static final int FDN_CHECK_FAILURE = 600;
    static final int PASSWORD_ERROR = 700;
    static final int FDN_FAIL = 800;

    private final ArrayList<String> mReadBusyList=new ArrayList<String> ();
    private final ArrayList<String> mSaveBusyList=new ArrayList<String> ();
    
    protected boolean mIsForeground = false;
    /*the two line code following is add by lemon mtk54102*/
    private TimeConsumingPreferenceListener mTCPL = null;
    protected boolean mIsUpdate = false;

    @Override
    protected Dialog onCreateDialog(int id) {
	mIsUpdate = false;
        if (id == BUSY_READING_DIALOG || id == BUSY_SAVING_DIALOG) {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle(getText(R.string.updating_title));
            dialog.setIndeterminate(true);

            switch(id) {
                case BUSY_READING_DIALOG:
                    dialog.setCancelable(true);
                    dialog.setOnCancelListener(this);
                    dialog.setMessage(getText(R.string.reading_settings));
                    return dialog;
                case BUSY_SAVING_DIALOG:
                    dialog.setCancelable(false);
                    dialog.setMessage(getText(R.string.updating_settings));
                    return dialog;
            }
            return null;
        }

        if (id == RESPONSE_ERROR || id == RADIO_OFF_ERROR || id == EXCEPTION_ERROR
                || id == PASSWORD_ERROR || id == FDN_FAIL || id == FDN_CHECK_FAILURE) {
            AlertDialog.Builder b = new AlertDialog.Builder(this);

            int msgId;
            int titleId = R.string.error_updating_title;

            switch (id) {
                case RESPONSE_ERROR:
                    msgId = R.string.response_error;
                    // Set Button 2, tells the activity that the error is
                    // recoverable on dialog exit.
                    b.setNegativeButton(R.string.close_dialog, this);
                    break;
                case RADIO_OFF_ERROR:
                    msgId = R.string.radio_off_error;
                    // Set Button 3
                    b.setNeutralButton(R.string.close_dialog, this);
                    break;
                case PASSWORD_ERROR:
                    msgId =com.android.internal.R.string.passwordIncorrect;
                    b.setNeutralButton(R.string.close_dialog, this);
                    break;
                case FDN_FAIL:
                    msgId = com.mediatek.R.string.fdnFailMmi;
                    b.setNeutralButton(R.string.close_dialog, this);
                    break;
                case FDN_CHECK_FAILURE:
                    msgId = R.string.fdn_only_error;
                    // Set Button 2
                    b.setNegativeButton(R.string.close_dialog, this);
                    break;
                case EXCEPTION_ERROR:
                default:
                    msgId = R.string.exception_error;
                    // Set Button 3, tells the activity that the error is
                    // not recoverable on dialog exit.
                    b.setNeutralButton(R.string.close_dialog, this);
                    break;
            }

            b.setTitle(getText(titleId));
            b.setMessage(getText(msgId));
            b.setCancelable(false);
            AlertDialog dialog = b.create();

            // make the dialog more obvious by blurring the background.
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

            return dialog;
        }
        return null;
    }

    @Override
    public void onResume() {
    	Xlog.d(LOG_TAG, "onResume");
        super.onResume();
     
        mIsForeground = true;
        
    	if(mReadBusyList.size() == 1) {
            showDialog(BUSY_READING_DIALOG);
            Xlog.d(LOG_TAG, "showDialog(BUSY_READING_DIALOG)");
    	} 
    	
    	if(mSaveBusyList.size() == 1) {
            showDialog(BUSY_SAVING_DIALOG);
            Xlog.d(LOG_TAG, "showDialog(BUSY_SAVING_DIALOG)");
    	}
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsForeground = false;
    	Xlog.d(LOG_TAG, "onPause");
    }

    public void onClick(DialogInterface dialog, int which) {
    	Xlog.d(LOG_TAG, "onClick");
        dialog.dismiss();
	if(mIsUpdate 
	    && mTCPL instanceof GsmUmtsCallForwardOptions){
            Xlog.d(LOG_TAG, "update call forward settings");
	    mIsUpdate = false;
            ((GsmUmtsCallForwardOptions) mTCPL).refreshSettings(true);
	}
    }

    public void onUpdate(TimeConsumingPreferenceListener tcp, boolean flag){
	    mIsUpdate = flag;
	    mTCPL = tcp;
    }

    public void onStarted(Preference preference, boolean reading) {
        if (DBG) dumpState();
        if (DBG) Xlog.d(LOG_TAG, "onStarted, preference=" + preference.getKey()
                + ", reading=" + reading);

        if (reading) {
        	mReadBusyList.add(preference.getKey());
        	
        	if((mReadBusyList.size() == 1) && mIsForeground) {
                showDialog(BUSY_READING_DIALOG);
                Xlog.d(LOG_TAG, "showDialog(BUSY_READING_DIALOG)");
        	}

            

        } else {
        	mSaveBusyList.add(preference.getKey());
        	
        	if((mSaveBusyList.size() == 1) && mIsForeground) {
                showDialog(BUSY_SAVING_DIALOG);
                Xlog.d(LOG_TAG, "showDialog(BUSY_SAVING_DIALOG)");
        	}

        }

    }

    public void onFinished(Preference preference, boolean reading) {
        if (DBG) dumpState();
        if (DBG) Xlog.d(LOG_TAG, "onFinished, preference=" + preference.getKey()
                + ", reading=" + reading);


        if (reading) {
            mReadBusyList.remove(preference.getKey());
            
            if(mReadBusyList.isEmpty()) {
            	removeDialog(BUSY_READING_DIALOG);
                Xlog.d(LOG_TAG, "removeDialog(BUSY_READING_DIALOG)");
            }
            
        } else {
            mSaveBusyList.remove(preference.getKey());
            
            if(mSaveBusyList.isEmpty()) {
            	removeDialog(BUSY_SAVING_DIALOG);
                Xlog.d(LOG_TAG, "removeDialog(BUSY_SAVING_DIALOG)");
            }

        }
    }


	public void removeDialog(){
        removeDialog(BUSY_SAVING_DIALOG);
        removeDialog(BUSY_READING_DIALOG);
	}

    public void onError(Preference preference, int error) {
        if (DBG) dumpState();
        if (DBG) Xlog.d(LOG_TAG, "onError, preference=" + preference.getKey() + ", error=" + error);

        if (mIsForeground) {
        	try {
            showDialog(error);
        	}catch (Throwable e)
        	{
        		Xlog.d(LOG_TAG, "Catch the throwable: " + e.toString());
        	}
        }
    }

    public void onException(Preference preference, CommandException exception) {
    	Xlog.d(LOG_TAG, "onException");
        if (exception.getCommandError() == CommandException.Error.FDN_CHECK_FAILURE) {
            onError(preference, FDN_CHECK_FAILURE);
        } else {
            preference.setEnabled(false);
            onError(preference, EXCEPTION_ERROR);
        }
    }
    public void onCancel(DialogInterface dialog) {
        if (DBG) dumpState();
        finish();
    }


    void dumpState() {

        for (String key : mReadBusyList) {
            Xlog.d(LOG_TAG, "mReadBusyList: key=" + key);
        }
        
        for (String key : mSaveBusyList) {
            Xlog.d(LOG_TAG, "mSaveBusyList: key=" + key);
        }

    }
}
