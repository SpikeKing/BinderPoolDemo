package org.wangchenlong.binderpooldemo;

import android.os.RemoteException;

/**
 * 计算实现
 * <p/>
 * Created by wangchenlong on 16/6/17.
 */
public class ComputeImpl extends ICompute.Stub {
    @Override public int add(int a, int b) throws RemoteException {
        return a + b;
    }
}
