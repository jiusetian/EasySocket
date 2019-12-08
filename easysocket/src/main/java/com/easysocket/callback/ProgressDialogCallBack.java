
package com.easysocket.callback;

import android.app.Dialog;
import android.content.DialogInterface;

import com.easysocket.entity.exception.RequestCancelException;
import com.easysocket.entity.basemsg.BaseCallbackSender;
import com.easysocket.interfaces.callback.IProgressDialog;
import com.easysocket.interfaces.callback.ProgressCancelListener;
import com.google.gson.Gson;


/**
 * 描述：可以自定义带有加载进度框的回调
 * 1.可以自定义带有加载进度框的回调,是否需要显示，是否可以取消
 */
public abstract class ProgressDialogCallBack<T> extends SuperCallBack<T> implements ProgressCancelListener {
    private IProgressDialog progressDialog;
    private Dialog mDialog;
    private boolean isShowProgress = true;

    /**
     *
     * @param
     */
    public ProgressDialogCallBack(IProgressDialog progressDialog, BaseCallbackSender sender) {
        super(sender);
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
                                  boolean isCancel, BaseCallbackSender sender) {
        super(sender);
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
        if (mDialog != null) {
            if (!mDialog.isShowing()) {
                mDialog.show();
            }
        }
    }

    /**
     * 取消进度框
     */
    private void dismissProgress() {
        if (!isShowProgress) {
            return;
        }
        if (mDialog != null) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }
        }
    }

    @Override
    public void onStart() {
        openTimeoutTask(); //开始计时
        showProgress();
    }

    @Override
    public void onCompleted() {
        closeTimeoutTask();//停止计时
        dismissProgress();
    }

    @Override
    public void onSuccess(String s) {
        onCompleted();
        Class<?> clazz = getClazz();
        if (clazz.equals(String.class)) { //泛型是字符串类型
            onResponse((T) s);
        } else { //非string
            Gson gson = new Gson();
            T result = (T) gson.fromJson(s, clazz);
            onResponse(result);
        }
    }

    public abstract void onResponse(T t);

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
