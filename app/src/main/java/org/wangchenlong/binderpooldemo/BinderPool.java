package org.wangchenlong.binderpooldemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.concurrent.CountDownLatch;

/**
 * 连接池实现
 * <p/>
 * Created by wangchenlong on 16/6/17.
 */
public class BinderPool {

    private static final String TAG = "DEBUG-WCL: " + BinderPool.class.getSimpleName();

    public static final int BINDER_COMPUTE = 0;
    public static final int BINDER_SECURITY_CENTER = 1;

    // 编译器每次都需要从主存中读取
    private IBinderPool mBinderPool;
    private static volatile BinderPool sInstance;
    private Context mContext;

    private CountDownLatch mCountDownLatch; // 同步机制

    private BinderPool(Context context) {
        mContext = context.getApplicationContext();
        connectBinderPoolService();
    }

    // 单例
    public static BinderPool getInstance(Context context) {
        if (sInstance == null) {
            synchronized (BinderPool.class) {
                if (sInstance == null) {
                    sInstance = new BinderPool(context);
                }
            }
        }
        return sInstance;
    }

    // 连接服务池
    private synchronized void connectBinderPoolService() {
        mCountDownLatch = new CountDownLatch(1); // 只保持一个绑定服务
        Intent service = new Intent(mContext, BinderPoolService.class);
        mContext.bindService(service, mBinderPoolConnection, Context.BIND_AUTO_CREATE);
        try {
            mCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 失效重联机制, 当Binder死亡时, 重新连接
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override public void binderDied() {
            Log.e(TAG, "Binder失效");
            mBinderPool.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mBinderPool = null;
            connectBinderPoolService();
        }
    };

    // Binder的服务连接
    private ServiceConnection mBinderPoolConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder service) {
            mBinderPool = IBinderPool.Stub.asInterface(service);
            try {
                mBinderPool.asBinder().linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mCountDownLatch.countDown();
        }

        @Override public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * 查询Binder
     *
     * @param binderCode binder代码
     * @return Binder
     */
    public IBinder queryBinder(int binderCode) {
        IBinder binder = null;
        try {
            if (mBinderPool != null) {
                binder = mBinderPool.queryBinder(binderCode);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return binder;
    }

    /**
     * Binder池实现
     */
    public static class BinderPoolImpl extends IBinderPool.Stub {
        public BinderPoolImpl() {
            super();
        }

        @Override public IBinder queryBinder(int binderCode) throws RemoteException {
            IBinder binder = null;
            switch (binderCode) {
                case BINDER_COMPUTE:
                    binder = new ComputeImpl();
                    break;
                case BINDER_SECURITY_CENTER:
                    binder = new SecurityCenterImpl();
                    break;
                default:
                    break;
            }
            return binder;
        }
    }
}
