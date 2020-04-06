package com.easysocket.connection.iowork;

import com.easysocket.config.EasySocketOptions;
import com.easysocket.connection.action.IOAction;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.exception.SocketReadExeption;
import com.easysocket.interfaces.conn.IConnectionManager;
import com.easysocket.interfaces.conn.ISocketActionDispatch;
import com.easysocket.interfaces.io.IMessageProtocol;
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
     * 读取数据时，没读完的残留数据缓存
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
        inputStream = connectionManager.getInputStream();
        this.actionDispatch = actionDispatch;
        this.connectionManager = connectionManager;
        socketOptions = connectionManager.getOptions();
    }

    @Override
    public void read() {
        OriginReadData originalData = new OriginReadData();
        IMessageProtocol messageProtocol = socketOptions.getMessageProtocol();
        int headerLength = messageProtocol.getHeaderLength(); //默认包头为4个字节
        ByteBuffer headBuf = ByteBuffer.allocate(headerLength); //读取数据包头的buffer
        headBuf.order(socketOptions.getReadOrder());

        /*读取数据的header=====>>>*/
        try {
            if (remainingBuf != null) { //有余留数据
                //flip方法：一般在从Buffer读出数据前调用，将limit设置为当前position，然后将position设置为0，在读数据的时候，limit代表可读数据的有效长度
                remainingBuf.flip();
                //数据读取长度，如果余留数据不够一个header，则全部读取，如果大于一个header，则只读一个header的数据
                int length = Math.min(remainingBuf.remaining(), headerLength);
                //将长度为length的数据写入headBuf中
                headBuf.put(remainingBuf.array(), 0, length);

                if (length < headerLength) { //余留数据不够一个header
                    //there are no data left
                    remainingBuf = null;
                    //从stream中读取header剩下的长度
                    readHeaderFromSteam(headBuf, headerLength - length);
                } else { //读取header之后还有余留缓存
                    remainingBuf.position(headerLength); //移动指针位置
                }
            } else { //没有余留数据
                //直接从stream读取一个header的数据
                readHeaderFromSteam(headBuf, headBuf.capacity());
            }

            //将header数据赋值给原始数据
            originalData.setHeaderData(headBuf.array());

            /*开始读取body数据=====>>>*/
            //数据体的长度
            int bodyLength = messageProtocol.getBodyLength(originalData.getHeaderData(), socketOptions.getReadOrder());
            if (bodyLength > 0) {
                if (bodyLength > socketOptions.getMaxResponseDataMb() * 1024 * 1024) { //是否大于最大的读取数
                    throw new SocketReadExeption("服务器返回的单次数据超过了规定的最大值，可能你的Socket消息的数据格式不对，本项目默认的消息格式" +
                            "为：Header+Body，消息头Header保存消息的长度，一般长度为一个int，Body保存消息内容，请规范好相关协议");
                }
                ByteBuffer byteBuffer = ByteBuffer.allocate(bodyLength);
                byteBuffer.order(socketOptions.getReadOrder());

                //有余留数据
                if (remainingBuf != null) {
                    int bodyStartPosition = remainingBuf.position();

                    int length = Math.min(remainingBuf.remaining(), bodyLength);
                    //从余留的数据中读取length长度数据
                    byteBuffer.put(remainingBuf.array(), bodyStartPosition, length);
                    //移动position位置
                    remainingBuf.position(bodyStartPosition + length);

                    //表示余留数据大于或等于body数据
                    if (length == bodyLength) {
                        if (remainingBuf.remaining() > 0) { //读完还有数据余留，保存起来等下一次读取
                            ByteBuffer temp = ByteBuffer.allocate(remainingBuf.remaining());
                            temp.order(socketOptions.getReadOrder());
                            temp.put(remainingBuf.array(), remainingBuf.position(), remainingBuf.remaining());
                            remainingBuf = temp;
                        } else { //there are no data left
                            remainingBuf = null;
                        }

                        //将读取到body数据赋值给原始数据
                        originalData.setBodyData(byteBuffer.array());
                        LogUtil.d("接收的原始数据=" + originalData.getBodyString());
                        //分发数据
                        actionDispatch.dispatchAction(IOAction.ACTION_READ_COMPLETE, originalData);
                        //此次读取结束
                        return;
                    }
                    //没有数据余留了
                    else { //there are no data left in buffer and some data pieces in channel
                        remainingBuf = null;
                    }
                }
                //没有余留数据，从stream中读
                readBodyFromStream(byteBuffer);
                //将body数据存入originalData中
                originalData.setBodyData(byteBuffer.array());
            }
            //数据body长度为0
            else if (bodyLength == 0) {
                originalData.setBodyData(new byte[0]);
                if (remainingBuf != null) {
                    //the body is empty so header remaining buf need set null
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
                connectionManager.disconnect(new Boolean(true)); //断开重连
                throw new SocketReadExeption("读取失败，读取到的数据长度小于0，可能是读取的过程中socket跟服务器断开了连接");
            }
            //将读取到一个完整数据发布出去
            LogUtil.d("接收的原始数据=" + originalData.getBodyString());
            actionDispatch.dispatchAction(IOAction.ACTION_READ_COMPLETE, originalData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void openReader() {
        if (!stopThread) {
            readerThread = new Thread(readerTask, "reader thread");
            stopThread = false;
            readerThread.start();
        }
    }

    /**
     * 读取数据任务类
     */
    private Runnable readerTask = new Runnable() {
        @Override
        public void run() {
            while (!stopThread) {
                read();
            }
        }
    };


    private void readHeaderFromSteam(ByteBuffer headBuf, int readLength) throws IOException {
        for (int i = 0; i < readLength; i++) {
            byte[] bytes = new byte[1];
            int value = inputStream.read(bytes); //从输入流中读取数据，没数据的时候该方面被阻塞
            if (value == -1) {
                connectionManager.disconnect(new Boolean(true)); //断开重连
                throw new SocketReadExeption("读取数据的包头失败，在" + value + "位置断开了，可能是因为socket跟服务器断开了连接");

            }
            headBuf.put(bytes);
        }
    }

    private void readBodyFromStream(ByteBuffer byteBuffer) throws IOException {
        //byteBuffer是否还有剩余空间
        while (byteBuffer.hasRemaining()) {
            try {
                byte[] bufArray = new byte[socketOptions.getMaxReadBytes()]; //从服务器单次读取的最大数据
                int len = inputStream.read(bufArray);
                if (len == -1) { //no more data
                    break;
                }
                int remaining = byteBuffer.remaining();
                if (len > remaining) { //从stream读取的数据超过一个body的大小
                    //保存一个body的数据到byteBuffer中
                    byteBuffer.put(bufArray, 0, remaining);
                    //将多余的数据保存到remainingBuf中缓存，等下一次读取
                    remainingBuf = ByteBuffer.allocate(len - remaining);
                    remainingBuf.order(socketOptions.getReadOrder());
                    remainingBuf.put(bufArray, remaining, len - remaining);
                } else { //从stream读取的数据小于或等于一个body的大小
                    byteBuffer.put(bufArray, 0, len);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    @Override
    public void closeReader() {
        try {
            if (inputStream != null)
                inputStream.close();
            shutDownThread();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setOption(EasySocketOptions socketOptions) {
        this.socketOptions = socketOptions;
    }

    private void shutDownThread() {
        if (readerThread != null && readerThread.isAlive() && !readerThread.isInterrupted()) {
            stopThread = true;
            readerThread.interrupt();
            readerThread = null;
        }
    }
}
