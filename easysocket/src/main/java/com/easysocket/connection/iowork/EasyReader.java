package com.easysocket.connection.iowork;

import com.easysocket.config.EasySocketOptions;
import com.easysocket.connection.action.IOAction;
import com.easysocket.entity.OriginReadData;
import com.easysocket.exception.ReadRecoverableExeption;
import com.easysocket.exception.ReadUnrecoverableException;
import com.easysocket.interfaces.config.IMessageProtocol;
import com.easysocket.interfaces.conn.IConnectionManager;
import com.easysocket.interfaces.conn.ISocketActionDispatch;
import com.easysocket.interfaces.io.IReader;
import com.easysocket.utils.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：
 */
public class EasyReader implements IReader<EasySocketOptions> {
    /**
     * 输入流
     */
    private InputStream inputStream;
    /**
     * 读取原始数据的缓存空间
     */
    private ByteBuffer originBuf;
    /**
     * socket行为分发器
     */
    private ISocketActionDispatch actionDispatch;
    /**
     * 连接器
     */
    private IConnectionManager connectionManager;
    /**
     * 连接参数
     */
    private EasySocketOptions socketOptions;

    /**
     * 读数据时，余留数据的缓存
     */
    private ByteBuffer remainingBuf;
    /**
     * 读数据线程
     */
    private Thread readerThread;
    /**
     * 是否停止线程
     */
    private boolean stopThread;

    public EasyReader(IConnectionManager connectionManager, ISocketActionDispatch actionDispatch) {
        this.actionDispatch = actionDispatch;
        this.connectionManager = connectionManager;
        socketOptions = connectionManager.getOptions();
    }

    @Override
    public void read() throws IOException, ReadRecoverableExeption, ReadUnrecoverableException {
        OriginReadData originalData = new OriginReadData();
        IMessageProtocol messageProtocol = socketOptions.getMessageProtocol();
        // 消息协议为null，则直接读原始消息，不建议这样使用，因为会发生黏包、分包的问题
        if (messageProtocol == null) {
            readOriginDataFromSteam(originalData);
            return;
        }

        // 定义了消息协议
        int headerLength = messageProtocol.getHeaderLength(); // 包头长度
        ByteBuffer headBuf = ByteBuffer.allocate(headerLength); // 包头数据的buffer
        headBuf.order(socketOptions.getReadOrder());

        /*1、读 header=====>>>*/
        if (remainingBuf != null) { // 有余留
            // flip方法：一般从Buffer读数据前调用，将limit设置为当前position，将position设置为0，在读数据时，limit代表可读数据的有效长度
            remainingBuf.flip();
            // 读余留数据的长度
            int length = Math.min(remainingBuf.remaining(), headerLength);
            // 读入余留数据
            headBuf.put(remainingBuf.array(), 0, length);

            if (length < headerLength) { // 余留数据小于一个header
                // there are no data left
                remainingBuf = null;
                // 从stream中读剩下的header数据
                readHeaderFromSteam(headBuf, headerLength - length);
            } else {
                // 移动开始读数据的指针
                remainingBuf.position(headerLength);
            }
        } else { // 无余留
            // 从stream读取一个完整的 header
            readHeaderFromSteam(headBuf, headBuf.capacity());
        }

        // 保存header
        originalData.setHeaderData(headBuf.array());

        /*2、读 body=====>>>*/
        int bodyLength = messageProtocol.getBodyLength(originalData.getHeaderData(), socketOptions.getReadOrder());
        if (bodyLength > 0) {
            if (bodyLength > socketOptions.getMaxResponseDataMb() * 1024 * 1024) {
                throw new ReadUnrecoverableException("服务器返回的单次数据超过了规定的最大值，可能你的Socket消息协议不对，一般消息格式" +
                        "为：Header+Body，其中Header保存消息长度和类型等，Body保存消息内容，请规范好你的协议");
            }
            // 分配空间
            ByteBuffer bodyBuf = ByteBuffer.allocate(bodyLength);
            bodyBuf.order(socketOptions.getReadOrder());

            // 有余留
            if (remainingBuf != null) {
                int bodyStartPosition = remainingBuf.position();

                int length = Math.min(remainingBuf.remaining(), bodyLength);
                // 读length大小的余留数据
                bodyBuf.put(remainingBuf.array(), bodyStartPosition, length);
                // 移动position位置
                remainingBuf.position(bodyStartPosition + length);

                // 读的余留数据刚好等于一个body
                if (length == bodyLength) {
                    if (remainingBuf.remaining() > 0) { // 余留数据未读完
                        ByteBuffer temp = ByteBuffer.allocate(remainingBuf.remaining());
                        temp.order(socketOptions.getReadOrder());
                        temp.put(remainingBuf.array(), remainingBuf.position(), remainingBuf.remaining());
                        remainingBuf = temp;
                    } else { // there are no data left
                        remainingBuf = null;
                    }

                    // 保存body
                    originalData.setBodyData(bodyBuf.array());

                    LogUtil.d("Socket收到数据-->" + originalData.getBodyString());
                    // 分发数据
                    actionDispatch.dispatchAction(IOAction.ACTION_READ_COMPLETE, originalData);

                    /*return读取结束*/
                    return;

                } else { // there are no data left in buffer and some data pieces in channel
                    remainingBuf = null;
                }
            }
            // 无余留，则从stream中读
            readBodyFromStream(bodyBuf);
            // 保存body到originalData
            originalData.setBodyData(bodyBuf.array());

        } else if (bodyLength == 0) { // 没有body数据
            originalData.setBodyData(new byte[0]);
            if (remainingBuf != null) {
                // the body is empty so header remaining buf need set null
                if (remainingBuf.hasRemaining()) {
                    ByteBuffer temp = ByteBuffer.allocate(remainingBuf.remaining());
                    temp.order(socketOptions.getReadOrder());
                    temp.put(remainingBuf.array(), remainingBuf.position(), remainingBuf.remaining());
                    remainingBuf = temp;
                } else {
                    remainingBuf = null;
                }
            }
        } else if (bodyLength < 0) {
            throw new ReadUnrecoverableException("数据body的长度不能小于0");
        }

        LogUtil.d("Socket收到数据-->" + originalData.getBodyString());
        // 分发
        actionDispatch.dispatchAction(IOAction.ACTION_READ_COMPLETE, originalData);

    }


    /**
     * 读数据任务
     */
    private Runnable readerTask = new Runnable() {
        @Override
        public void run() {
            try {
                while (!stopThread) {
                    read();
                }
            } catch (ReadUnrecoverableException unrecoverableException) {
                // 读异常
                unrecoverableException.printStackTrace();
                // 停止线程
                stopThread = true;
                release();
            } catch (ReadRecoverableExeption readRecoverableExeption) {
                readRecoverableExeption.printStackTrace();
                // 重连
                connectionManager.disconnect(true);

            } catch (IOException e) {
                e.printStackTrace();
                // 重连
                connectionManager.disconnect(true);
            }
        }
    };


    private void readHeaderFromSteam(ByteBuffer headBuf, int readLength) throws ReadRecoverableExeption, IOException {
        for (int i = 0; i < readLength; i++) {
            byte[] bytes = new byte[1];
            // 从输入流中读数据，无数据时会阻塞
            int value = inputStream.read(bytes);
            // -1代表读到了文件的末尾，一般是因为服务器断开了连接
            if (value == -1) {
                throw new ReadRecoverableExeption("读数据失败，可能是因为socket跟服务器断开了连接");
            }
            headBuf.put(bytes);
        }
    }

    private void readOriginDataFromSteam(OriginReadData readData) throws ReadRecoverableExeption, IOException {
        // 用 全局originBuf避免重复创建字节数组
        int len = inputStream.read(originBuf.array());
        // no more data
        if (len == -1) {
            throw new ReadRecoverableExeption("读数据失败，可能因为socket跟服务器断开了连接");
        }
        // bytes复制
        byte[] data = new byte[len];
        originBuf.get(data, 0, len);
        readData.setBodyData(data);
        LogUtil.d("Socket收到数据-->" + readData.getBodyString());
        // 分发数据
        actionDispatch.dispatchAction(IOAction.ACTION_READ_COMPLETE, readData);
        // 相当于把指针重新指向positon=0
        originBuf.clear();
    }

    private void readBodyFromStream(ByteBuffer byteBuffer) throws ReadRecoverableExeption, IOException {
        // while循环直到byteBuffer装满数据
        while (byteBuffer.hasRemaining()) {
            byte[] bufArray = new byte[socketOptions.getMaxReadBytes()]; // 从服务器单次读取的最大值
            int len = inputStream.read(bufArray);
            if (len == -1) { // no more data
                throw new ReadRecoverableExeption("读数据失败，可能是因为socket跟服务器断开了连接");
            }
            int remaining = byteBuffer.remaining();
            if (len > remaining) { // 从stream读的数据超过byteBuffer的剩余空间
                byteBuffer.put(bufArray, 0, remaining);
                // 将多余的数据保存到remainingBuf中缓存，等下一次读取
                remainingBuf = ByteBuffer.allocate(len - remaining);
                remainingBuf.order(socketOptions.getReadOrder());
                remainingBuf.put(bufArray, remaining, len - remaining);
            } else { // 从stream读的数据小于或等于byteBuffer的剩余空间
                byteBuffer.put(bufArray, 0, len);
            }
        }
    }

    @Override
    public void openReader() {
        init();
        if (readerThread == null || !readerThread.isAlive()) {
            readerThread = new Thread(readerTask, "reader thread");
            stopThread = false;
            readerThread.start();
        }
    }

    @Override
    public void closeReader() {
        try {
            // 关闭线程释放资源
            shutDownThread();
            release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 释放资源
    private void release() {
        if (originBuf != null) {
            originBuf = null;
        }
        if (remainingBuf != null) {
            remainingBuf = null;
        }
        if (readerThread != null && !readerThread.isAlive()) {
            readerThread = null;
        }

        try {
            if (inputStream != null)
                inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            inputStream = null;
        }
    }

    // 初始化
    private void init() {
        inputStream = connectionManager.getInputStream();
        // 没有定义消息协议
        if (socketOptions.getMessageProtocol() == null) {
            originBuf = ByteBuffer.allocate(1024 * 4);
        }
    }

    @Override
    public void setOption(EasySocketOptions socketOptions) {
        this.socketOptions = socketOptions;
    }

    // 关闭读数据线程
    private void shutDownThread() throws InterruptedException {
        if (readerThread != null && readerThread.isAlive() && !readerThread.isInterrupted()) {
            stopThread = true;
            readerThread.interrupt();
            readerThread.join();
        }
    }
}
