package com.easysocket.connection.iowork;

import com.easysocket.config.EasySocketOptions;
import com.easysocket.connection.action.SocketStatus;
import com.easysocket.interfaces.conn.ISocketActionDispatch;
import com.easysocket.interfaces.conn.IConnectionManager;
import com.easysocket.interfaces.io.IWriter;
import com.easysocket.utils.LogUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：
 */
public class EasyWriter implements IWriter<EasySocketOptions> {

    /**
     * 输出流
     */
    private OutputStream outputStream;

    /**
     * 连接管理器
     */
    private IConnectionManager connectionManager;
    /**
     * 连接参数
     */
    private EasySocketOptions socketOptions;
    /**
     * 行为分发
     */
    private ISocketActionDispatch actionDispatch;
    /**
     * 写入数据的线程
     */
    private Thread writerThread;
    /**
     * 需要写入的数据
     */
    private LinkedBlockingDeque<byte[]> packetsToSend = new LinkedBlockingDeque<>();

    public EasyWriter(IConnectionManager connectionManager, ISocketActionDispatch actionDispatch) {
        this.connectionManager = connectionManager;
        socketOptions=connectionManager.getOptions();
        outputStream = connectionManager.getOutStream();
        this.actionDispatch = actionDispatch;
    }

    @Override
    public void openWriter() {
        writerThread = new Thread(writerTask, "writer thread");
        writerThread.start();
    }

    @Override
    public void setOption(EasySocketOptions socketOptions) {
        this.socketOptions=socketOptions;
    }

    /**
     * io写任务
     */
    private Runnable writerTask = new Runnable() {
        @Override
        public void run() {
            //只要socket处于连接的状态，就一直活动
            while (connectionManager.getConnectionStatus() == SocketStatus.SOCKET_CONNECTED) {
                try {
                    byte[] sender = packetsToSend.take();
                    write(sender);
                } catch (InterruptedException e) {
                    //取数据异常
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void write(byte[] sendBytes) {
        if (sendBytes != null) {
            LogUtil.d("发送的数据="+new String(sendBytes, Charset.forName("utf-8")));
            try {
                int packageSize = socketOptions.getMaxWriteBytes(); //每次发送的数据包的大小
                int remainingCount = sendBytes.length;
                ByteBuffer writeBuf = ByteBuffer.allocate(packageSize); //分配一个内存缓存
                writeBuf.order(socketOptions.getReadOrder());
                int index = 0;
                //如果要发送的数据大小大于每次发送的数据包的大小， 则要分多次将数据发出去
                while (remainingCount > 0) {
                    int realWriteLength = Math.min(packageSize, remainingCount);
                    writeBuf.clear(); //清空缓存
                    writeBuf.rewind(); //将position位置移到0
                    writeBuf.put(sendBytes, index, realWriteLength);
                    writeBuf.flip(); //将position赋为0，limit赋为数据大小
                    byte[] writeArr = new byte[realWriteLength];
                    writeBuf.get(writeArr);
                    outputStream.write(writeArr);
                    outputStream.flush(); //强制缓存中残留的数据写入清空
                    index += realWriteLength;
                    remainingCount -= realWriteLength;
                }
            } catch (Exception e) {
                //写数据异常
                e.printStackTrace();
            }
        }
    }

    @Override
    public void offer(byte[] sender) {
        packetsToSend.offer(sender);
    }

    @Override
    public void closeWriter() {
        try {
            if (outputStream != null)
                outputStream.close();
            shutDownThread();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void shutDownThread() {
        if (writerThread != null && writerThread.isAlive() && !writerThread.isInterrupted()) {
            writerThread.interrupt();
        }
    }
}
