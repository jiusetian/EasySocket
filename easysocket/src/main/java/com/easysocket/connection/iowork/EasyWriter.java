package com.easysocket.connection.iowork;

import com.easysocket.config.EasySocketOptions;
import com.easysocket.interfaces.conn.IConnectionManager;
import com.easysocket.interfaces.conn.ISocketActionDispatch;
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
     * 写数据线程
     */
    private Thread writerThread;
    /**
     * 是否停止写数据
     */
    private boolean isStop;
    /**
     * 需要写入的数据
     */
    private LinkedBlockingDeque<byte[]> packetsToSend = new LinkedBlockingDeque<>();

    public EasyWriter(IConnectionManager connectionManager, ISocketActionDispatch actionDispatch) {
        this.connectionManager = connectionManager;
        socketOptions = connectionManager.getOptions();
        outputStream = connectionManager.getOutStream();
        this.actionDispatch = actionDispatch;
    }

    @Override
    public void openWriter() {
        if (!isStop) {
            isStop=false;
            writerThread = new Thread(writerTask, "writer thread");
            writerThread.start();
        }
    }

    @Override
    public void setOption(EasySocketOptions socketOptions) {
        this.socketOptions = socketOptions;
    }

    /**
     * io写任务
     */
    private Runnable writerTask = new Runnable() {
        @Override
        public void run() {
            //循环写数据到socket
            while (!isStop) {
                try {
                    byte[] sender = packetsToSend.take();
                    write(sender);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void write(byte[] sendBytes) {
        if (sendBytes != null) {
            LogUtil.d("发送数据=" + new String(sendBytes, Charset.forName("utf-8")));
            try {
                int packageSize = socketOptions.getMaxWriteBytes(); //每次可以发送的最大数据
                int remainingCount = sendBytes.length;
                ByteBuffer writeBuf = ByteBuffer.allocate(packageSize);
                writeBuf.order(socketOptions.getReadOrder());
                int index = 0;
                //如果发送的数据大于单次可发送的最大数据，则分多次发送
                while (remainingCount > 0) {
                    int realWriteLength = Math.min(packageSize, remainingCount);
                    writeBuf.clear(); //清空缓存
                    writeBuf.rewind(); //将position位置移到0
                    writeBuf.put(sendBytes, index, realWriteLength);
                    //
                    writeBuf.flip();
                    byte[] writeArr = new byte[realWriteLength];
                    writeBuf.get(writeArr);
                    outputStream.write(writeArr);
                    outputStream.flush(); //强制写入缓存中残留数据
                    index += realWriteLength;
                    remainingCount -= realWriteLength;
                }
            } catch (Exception e) {
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
            isStop = true;
            writerThread.interrupt();
            writerThread = null;
        }
    }
}
