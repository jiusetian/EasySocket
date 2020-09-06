package com.socker_server.iowork;

import com.socker_server.HandlerIO;
import com.socker_server.ServerConfig;
import com.socker_server.entity.IMessageProtocol;
import com.socker_server.entity.OriginReadData;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：
 */
public class EasyReader implements IReader {
    /**
     * 输入流
     */
    private InputStream inputStream;

    /**
     * 读取数据时，没读完的残留数据缓存
     */
    private ByteBuffer remainingBuf;
    /**
     * 读数据线程
     */
    private Thread readerThread;

    private Socket socket;

    private boolean isShutdown;
    /**
     * 处理接收到数据
     */
    private HandlerIO handlerIO;

    public EasyReader(InputStream inputStream, Socket socket,HandlerIO handlerIO) {
        this.inputStream = inputStream;
        this.socket = socket;
        this.handlerIO=handlerIO;
    }

    @Override
    public void read() {
        OriginReadData originalData = new OriginReadData();
        IMessageProtocol readerProtocol = ServerConfig.getInstance().getMessageProtocol();
        // 如果消息协议为null，则直接读取原始消息，不建议这样使用，因为会发生黏包、分包的问题
        if (readerProtocol == null) {
            readOriginDataFromSteam(originalData);
            return;
        }

        int headerLength = readerProtocol.getHeaderLength(); //默认的包头长度是4个字节
        ByteBuffer headBuf = ByteBuffer.allocate(headerLength); //读取数据包头的缓存
        headBuf.order(ByteOrder.BIG_ENDIAN);

        //首先读取数据的header=====>>>
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
            }
            //没有余留数据
            else {
                readHeaderFromSteam(headBuf, headBuf.capacity());
            }
            //将header赋值到原始数据中
            originalData.setHeaderData(headBuf.array());

            // 开始读取body数据=====>>>
            int bodyLength = readerProtocol.getBodyLength(originalData.getHeaderData(), ByteOrder.BIG_ENDIAN);

            if (bodyLength > 0) {
                if (bodyLength > 5 * 1024 * 1024) { //是否大于最大的读取数
                    throw new RuntimeException("服务器返回的单次数据的大小已经超过了规定的最大值，为了防止内存溢出，请规范好相关协议");
                }
                ByteBuffer byteBuffer = ByteBuffer.allocate(bodyLength);
                byteBuffer.order(ByteOrder.BIG_ENDIAN);
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
                            temp.order(ByteOrder.BIG_ENDIAN);
                            temp.put(remainingBuf.array(), remainingBuf.position(), remainingBuf.remaining());
                            remainingBuf = temp;
                        } else { //there are no data left
                            remainingBuf = null;
                        }

                        //将读取到body数据赋值给原始数据
                        originalData.setBodyData(byteBuffer.array());
                        //分发数据
                        handlerIO.handReceiveMsg(originalData.getBodyString());
                        //actionDispatch.dispatchAction(IOAction.ACTION_READ_COMPLETE, originalData);
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
                originalData.setBodyData(byteBuffer.array()); //将body数据赋值
            }
            //数据body长度为0
            else if (bodyLength == 0) {
                originalData.setBodyData(new byte[0]);
                if (remainingBuf != null) {
                    //the body is empty so header remaining buf need set null
                    if (remainingBuf.hasRemaining()) {
                        ByteBuffer temp = ByteBuffer.allocate(remainingBuf.remaining());
                        temp.order(ByteOrder.BIG_ENDIAN);
                        temp.put(remainingBuf.array(), remainingBuf.position(), remainingBuf.remaining());
                        remainingBuf = temp;
                    } else {
                        remainingBuf = null;
                    }
                }
            } else if (bodyLength < 0) {
                throw new RuntimeException("读取失败，读取到的数据长度小于0，可能是读取的过程中跟socket跟服务器断开了连接");
            }
            //将读取到一个完整数据发布出去
            handlerIO.handReceiveMsg(originalData.getBodyString());
            //actionDispatch.dispatchAction(IOAction.ACTION_READ_COMPLETE, originalData);
        } catch (Exception e) {
            e.printStackTrace();
            closeReader();
        }
    }

    @Override
    public void openReader() {
        isShutdown = false;
        readerThread = new Thread(readerTask, "reader thread");
        readerThread.start();
    }

    /**
     * 读取数据任务类
     */
    private Runnable readerTask = new Runnable() {
        @Override
        public void run() {
            while (socket.isConnected() && !isShutdown) {
                read();
            }
        }
    };

    // 直接读取原始数据，适合于所有数据格式
    private void readOriginDataFromSteam(OriginReadData readData) {
        try {
            byte[] bufArray = new byte[4096]; // 从服务器单次读取的最大数据
            int len = inputStream.read(bufArray);
            if (len == -1) { // no more data
                return;
            }
            ByteBuffer data=ByteBuffer.allocate(len);
            data.put(bufArray,0,len);
            readData.setBodyData(data.array());
            System.out.println("字符串大小"+readData.getBodyString().length());
            // 分发数据
            handlerIO.handReceiveMsg(readData.getBodyString().trim());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readHeaderFromSteam(ByteBuffer headBuf, int readLength) throws IOException {
        for (int i = 0; i < readLength; i++) {
            byte[] bytes = new byte[1];
            int value = inputStream.read(bytes); //从输入流中读取相应长度的数据
            if (value == -1) {
             }
            headBuf.put(bytes);
        }
    }

    private void readBodyFromStream(ByteBuffer byteBuffer) throws IOException {
        //body大小是缓存buffer是否还有剩余空间
        while (byteBuffer.hasRemaining()) {
            try {
                byte[] bufArray = new byte[50]; //从服务器中单次读取的缓存数据大小
                int len = inputStream.read(bufArray);
                if (len == -1) {
                    break;
                }
                int remaining = byteBuffer.remaining();
                if (len > remaining) { //读多了
                    byteBuffer.put(bufArray, 0, remaining);
                    remainingBuf = ByteBuffer.allocate(len - remaining);
                    remainingBuf.order(ByteOrder.BIG_ENDIAN);
                    remainingBuf.put(bufArray, remaining, len - remaining);
                } else { //还没够或者刚刚好
                    byteBuffer.put(bufArray, 0, len);
                }
            } catch (Exception e) {
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
    public void setOption(Object o) {
    }

    private void shutDownThread() {
        isShutdown = true;
        if (readerThread != null && readerThread.isAlive() && !readerThread.isInterrupted()) {
            readerThread.interrupt();
        }
    }
}
