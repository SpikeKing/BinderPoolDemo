package org.wangchenlong.binderpooldemo;

import android.os.RemoteException;

/**
 * 加密算法的实现
 * <p/>
 * Created by wangchenlong on 16/6/17.
 */
public class SecurityCenterImpl extends ISecurityCenter.Stub {

    private static final char SECRET_CODE = 'w';

    @Override public String encrypt(String content) throws RemoteException {
        char[] chars = content.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] ^= SECRET_CODE;
        }
        return new String(chars);
    }

    @Override public String decrypt(String password) throws RemoteException {
        return encrypt(password);
    }
}
