package com.socker_server.iowork;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：
 */
public class EasyWriter implements IWriter {

    /**
     * 输出流
     */
    private OutputStream outputStream;

    /**
     * 写入数据的线程
     */
    private Thread writerThread;
    /**
     * 需要写入的数据
     */
    private LinkedBlockingDeque<byte[]> packetsToSend = new LinkedBlockingDeque<>();
    /**
     * 是否关闭线程
     */
    private boolean isShutdown;

    private Socket socket;

    public EasyWriter(OutputStream outputStream, Socket socket) {
        this.outputStream = outputStream;
        this.socket = socket;
    }

    @Override
    public void openWriter() {
        writerThread = new Thread(writerTask, "writer thread");
        isShutdown = false;
        writerThread.start();
    }

    @Override
    public void setOption(Object o) {

    }


    /**
     * io写任务
     */
    private Runnable writerTask = new Runnable() {
        @Override
        public void run() {
            //只要socket处于连接的状态，就一直活动
            while (socket.isConnected() && !isShutdown) {
                try {
                    byte[] sender = packetsToSend.take();
                    System.out.println("send message");
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
            try {
                int packageSize = 100; //每次发送的数据包的大小
                int remainingCount = sendBytes.length;
                ByteBuffer writeBuf = ByteBuffer.allocate(packageSize); //分配一个内存缓存
                writeBuf.order(ByteOrder.BIG_ENDIAN);
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
                closeWriter();
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
        isShutdown = true;
        if (writerThread != null && writerThread.isAlive() && !writerThread.isInterrupted()) {
            writerThread.interrupt();
        }
    }
}
