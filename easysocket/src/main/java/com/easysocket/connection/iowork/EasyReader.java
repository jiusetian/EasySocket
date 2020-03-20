package com.easysocket.connection.iowork;

import com.easysocket.config.EasySocketOptions;
import com.easysocket.connection.action.IOAction;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.exception.SocketReadExeption;
import com.easysocket.interfaces.conn.IConnectionManager;
import com.easysocket.interfaces.conn.ISocketActionDispatch;
import com.easysocket.interfaces.io.IMessageProtocol;
import com.easysocket.interfaces.io.IReader;

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
        int headerLength = messageProtocol.getHeaderLength(); //默认的包头长度是4个字节
        ByteBuffer headBuf = ByteBuffer.allocate(headerLength); //读取数据包头的缓存
        headBuf.order(socketOptions.getReadOrder());

        //读取数据的header=====>>>
        try {
            //有余留数据
            if (remainingBuf != null) {
                //flip方法：buffer的容量不变，将limit移动到buffer的数据大小索引位置，将position置为0，这样就可以从0这个位置开始读取数据，直到limit大小的位置，limit就是buffer保存数据的大小
                remainingBuf.flip();
                int length = Math.min(remainingBuf.remaining(), headerLength);
                //将长度为length的数据写入headBuf中
                headBuf.put(remainingBuf.array(), 0, length);
                if (length < headerLength) { //不够一个header
                    //there are no data left
                    remainingBuf = null;
                    //再从stream中读取header剩下的长度
                    readHeaderFromSteam(headBuf, headerLength - length);
                } else { //读取header之后还有余留缓存
                    remainingBuf.position(headerLength); //移动指针位置
                }
            } else { //没有余留数据
                readHeaderFromSteam(headBuf, headBuf.capacity());
            }

            //将header赋值到原始数据中
            originalData.setHeaderData(headBuf.array());


            // 开始读取body数据=====>>>
            int bodyLength = messageProtocol.getBodyLength(originalData.getHeaderData(), socketOptions.getReadOrder());
            if (bodyLength > 0) {
                if (bodyLength > socketOptions.getMaxResponseDataMb() * 1024 * 1024) { //是否大于最大的读取数
                    throw new SocketReadExeption("服务器返回的单次数据的大小已经超过了规定的最大值，为了防止内存溢出，请规范好相关协议");
                }
                ByteBuffer byteBuffer = ByteBuffer.allocate(bodyLength);
                byteBuffer.order(socketOptions.getReadOrder());
                //有余留数据未读取
                if (remainingBuf != null) {
                    int bodyStartPosition = remainingBuf.position();
                    int length = Math.min(remainingBuf.remaining(), bodyLength);
                    //从余留的数据中读取
                    byteBuffer.put(remainingBuf.array(), bodyStartPosition, length);
                    //移动position位置
                    remainingBuf.position(bodyStartPosition + length);

                    //表示余留数据的大于大于或等于body数据的大小
                    if (length == bodyLength) {
                        if (remainingBuf.remaining() > 0) { //还有数据余留
                            ByteBuffer temp = ByteBuffer.allocate(remainingBuf.remaining());
                            temp.order(socketOptions.getReadOrder());
                            temp.put(remainingBuf.array(), remainingBuf.position(), remainingBuf.remaining());
                            remainingBuf = temp;
                        } else { //there are no data left
                            remainingBuf = null;
                        }

                        //将读取到body数据赋值给原始数据
                        originalData.setBodyData(byteBuffer.array());
                        //分发数据
                        actionDispatch.dispatchAction(IOAction.ACTION_READ_COMPLETE, originalData);
                        //此次读取结束，return
                        return;
                    }
                    //没有数据余留了
                    else { //there are no data left in buffer and some data pieces in channel
                        remainingBuf = null;
                    }
                }
                //继续从stream中读
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
            //LogUtil.d("接收的数据=" + originalData.getBodyString());
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
            int value = inputStream.read(bytes); //从输入流中读取相应长度的数据
            if (value == -1) {
                connectionManager.disconnect(new Boolean(true)); //断开重连
                throw new SocketReadExeption("读取数据的包头失败，在" + value + "位置断开了，可能是因为socket跟服务器断开了连接");

            }
            headBuf.put(bytes);
        }
    }

    private void readBodyFromStream(ByteBuffer byteBuffer) throws IOException {
        //body大小是缓存buffer是否还有剩余空间
        while (byteBuffer.hasRemaining()) {
            try {
                byte[] bufArray = new byte[socketOptions.getMaxReadBytes()]; //从服务器中单次读取的缓存数据大小
                int len = inputStream.read(bufArray);
                if (len == -1) {
                    break;
                }
                int remaining = byteBuffer.remaining();
                if (len > remaining) { //读多了
                    byteBuffer.put(bufArray, 0, remaining);
                    remainingBuf = ByteBuffer.allocate(len - remaining);
                    remainingBuf.order(socketOptions.getReadOrder());
                    remainingBuf.put(bufArray, remaining, len - remaining);
                } else { //还没够或者刚刚好
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
