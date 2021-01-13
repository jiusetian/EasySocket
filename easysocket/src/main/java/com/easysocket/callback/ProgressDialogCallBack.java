
package com.easysocket.callback;

import android.app.Dialog;
import android.content.DialogInterface;

import com.easysocket.entity.OriginReadData;
import com.easysocket.exception.RequestCancelException;
import com.easysocket.interfaces.callback.IProgressDialog;
import com.easysocket.interfaces.callback.ProgressCancelListener;


/**
 * 自定义带有加载进度框的回调
 */
public abstract class ProgressDialogCallBack extends SuperCallBack implements ProgressCancelListener {

    private IProgressDialog progressDialog;
    private Dialog mDialog;
    private boolean isShowProgress = true;

    /**
     * @param
     */
    public ProgressDialogCallBack(IProgressDialog progressDialog, String callbackId) {
        super(callbackId);
        this.progressDialog = progressDialog;
        init(false);
        onStart();
    }

    /**
     * 自定义加载进度框,可以设置是否显示弹出框，是否可以取消
     *
     * @param progressDialog dialog
     * @param isShowProgress 是否显示进度
     * @param isCancel       对话框是否可以取消
     * @param
     */
    public ProgressDialogCallBack(IProgressDialog progressDialog, boolean isShowProgress,
                                  boolean isCancel, String callbackId) {
        super(callbackId);
        this.progressDialog = progressDialog;
        this.isShowProgress = isShowProgress;
        init(isCancel);
        onStart();
    }

    /**
     * 初始化
     *
     * @param isCancel
     */
    private void init(boolean isCancel) {
        if (progressDialog == null) return;
        mDialog = progressDialog.getDialog();
        if (mDialog == null) return;
        mDialog.setCancelable(isCancel);
        if (isCancel) {
            mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    ProgressDialogCallBack.this.onCancelProgress();
                }
            });
        }
    }

    /**
     * 展示进度框
     */
    private void showProgress() {
        if (!isShowProgress) {
            return;
        }
        if (mDialog != null && !mDialog.isShowing()) {
            mDialog.show();
        }
    }

    /**
     * 取消进度框
     */
    private void dismissProgress() {
        if (!isShowProgress) {
            return;
        }
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onStart() {
        showProgress();
    }

    @Override
    public void onCompleted() {
        dismissProgress();
    }

    public abstract void onResponse(OriginReadData data);

    @Override
    public void onError(Exception e) {
        onCompleted();
    }

    @Override
    public void onCancelProgress() {
        onCompleted();
        onError(new RequestCancelException("网络请求被取消"));
    }

}
