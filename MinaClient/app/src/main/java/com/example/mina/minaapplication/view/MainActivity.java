package com.example.mina.minaapplication.view;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mina.minaapplication.R;
import com.example.mina.minaapplication.mina.ByteArrayCodecFactory;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Mina客户端
 */
public class MainActivity extends Activity {
    /**
     * 线程池，避免阻塞主线程，与服务器建立连接使用，创建一个只有单线程的线程池，尽快执行线程的线程池
     */
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();


    /**
     * 连接对象
     */
    private NioSocketConnector mConnection;
    /**
     * session对象
     */
    private IoSession mSession;
    /**
     * 连接服务器的地址
     */
    private InetSocketAddress mAddress;

    private ConnectFuture mConnectFuture;


    public static final int UPADTE_TEXT = 1;
    /**
     * 服务端返回的信息
     */
    private TextView tvShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvShow = findViewById(R.id.tv_show);
        initConfig();
        connect();
        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {//发送消息数据


            @Override
            public void onClick(View view) {
                if (mConnectFuture != null && mConnectFuture.isConnected()) {//与服务器连接上
                    mConnectFuture.getSession().write("{\"id\":11,\"name\":\"ccc\"}");//发送json字符串
                }

            }
        });
    }

    /**
     * 初始化Mina配置信息
     */
    private void initConfig() {
        mAddress = new InetSocketAddress("192.168.0.1", 20000);//连接地址,此数据可改成自己要连接的IP和端口号
        mConnection = new NioSocketConnector();// 创建连接
        // 设置读取数据的缓存区大小
        SocketSessionConfig socketSessionConfig = mConnection.getSessionConfig();
        socketSessionConfig.setReadBufferSize(2048);
        socketSessionConfig.setIdleTime(IdleStatus.BOTH_IDLE, 4);//设置4秒没有读写操作进入空闲状态
        mConnection.getFilterChain().addLast("logging", new LoggingFilter());//logging过滤器
        mConnection.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ByteArrayCodecFactory(Charset.forName("UTF-8"))));//自定义解编码器
        mConnection.setHandler(new DefaultHandler());//设置handler
        mConnection.setDefaultRemoteAddress(mAddress);//设置地址


    }

    /**
     * 创建连接
     */

    private void connect() {

        FutureTask<Void> futureTask = new FutureTask<>(new Callable<Void>() {
            @Override
            public Void call() {//

                try {
                    while (true) {
                        mConnectFuture = mConnection.connect();
                        mConnectFuture.awaitUninterruptibly();//一直等到他连接为止
                        mSession = mConnectFuture.getSession();//获取session对象
                        if (mSession != null && mSession.isConnected()) {
                            Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        Thread.sleep(3000);//每隔三秒循环一次
                    }

                } catch (Exception e) {//连接异常


                }
                return null;
            }
        });
        executorService.execute(futureTask);//执行连接线程
    }


    /**
     * Mina处理消息的handler,从服务端返回的消息一般在这里处理
     */
    private class DefaultHandler extends IoHandlerAdapter {


        @Override
        public void sessionOpened(IoSession session) throws Exception {
            super.sessionOpened(session);

        }

        /**
         * 接收到服务器端消息
         *
         * @param session
         * @param message
         * @throws Exception
         */
        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            Log.e("tag", "接收到服务器端消息：" + message.toString());

            Message message1 = new Message();
            message1.what = UPADTE_TEXT;
            message1.obj = message;
            handler.sendMessage(message1);
        }


        @Override
        public void sessionIdle(IoSession session, IdleStatus status) throws Exception {//客户端进入空闲状态.
            super.sessionIdle(session, status);

        }
    }

    /**
     * 更新UI
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPADTE_TEXT:
                    String message = (String) msg.obj;
                    tvShow.setText(message);
                    break;
            }
        }
    };
}
