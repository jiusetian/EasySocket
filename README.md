 # EasySocket

 博客地址：https://blog.csdn.net/liuxingrong666/article/details/91579548

EasySocket的初衷是希望使Socket编程变得更加简单、快捷，因此项目在实现了Socket基本功能的基础上，还实现了TCP层面的请求回调功能。传统的Socket框架客户端发出一个请求信息，然后服务器返回一个应答信息，但是我们无法识别这个应答信息是对应哪个请求的，而EasySocket实现了将每个请求跟应答的一一对接，从而在Socket层面实现了请求回调功能

### EasySocket特点：

   1、采用链式调用一键发送数据，根据自己的需求配置参数，简单易用，灵活性高
   
   2、EasySocket不但实现了包括TCP的连接和断开、数据的发送和接收、心跳保活、重连机制等功能，还实现了Socket层面的请求回调功能
   
   3、消息结构使用的协议为：包头+包体，其中包体存储要发送的消息实体，而包头则存储消息的长度、类型等，这种结构方式便于数据的解析，很好地解决了Socket通信中消息的断包和粘包问题

   4、EasySocket只需简单的配置即可启动心跳检测功能

### 一、EasySocket的Android Studio配置

#### 所需权限

uses-permission android:name="android.permission.INTERNET"
uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"

#### Gradle配置

1、在根目录的build.gradle文件中添加配置

allprojects {

    repositories {

        ...
	maven { url 'https://jitpack.io' }

    }

}

2、Module的build.gradle文件中添加依赖配置

  dependencies { 
   
    implementation 'com.github.jiusetian:EasySocket:最新版本号' 
  }

### 二、EasySocket的基本功能使用
       
1、创建Socket连接

一般在项目的Application中对EasySocket进行全局化配置，下面是一个最简单的配置

        /**
         * 初始化EasySocket
         */
        private void initEasySocket() {
     
            //socket配置
            EasySocketOptions options = new EasySocketOptions.Builder()
                    .setSocketAddress(new SocketAddress("192.168.3.9", 9999)) //主机地址
                    // 强烈建议定义一个消息协议，方便解决 socket黏包、分包的问题
                    // .setReaderProtocol(new DefaultMessageProtocol()) // 默认的消息协议
                    .build();
     
            //初始化EasySocket
            EasySocket.getInstance()
                    .options(options) //项目配置
                    .buildConnection();//创建一个socket连接
        }

这里主要设置了IP和端口，其他的配置参数都使用了默认值，然后创建一个Socket连接


2、注册Socket的监听

定义一个socket行为的监听器，如下

    /**
     * socket行为监听
     */
    private ISocketActionListener socketActionListener = new SocketActionListener() {
        /**
         * socket连接成功
         * @param socketAddress
         */
        @Override
        public void onSocketConnSuccess(SocketAddress socketAddress) {
            super.onSocketConnSuccess(socketAddress);
            LogUtil.d("连接成功");
        }
 
        /**
         * socket连接失败
         * @param socketAddress
         * @param isReconnect 是否需要重连
         */
        @Override
        public void onSocketConnFail(SocketAddress socketAddress, Boolean isReconnect) {
            super.onSocketConnFail(socketAddress, isReconnect);
        }
 
        /**
         * socket断开连接
         * @param socketAddress
         * @param isReconnect 是否需要重连
         */
        @Override
        public void onSocketDisconnect(SocketAddress socketAddress, Boolean isReconnect) {
            super.onSocketDisconnect(socketAddress, isReconnect);
        }
 
        /**
         * socket接收的数据
         * @param socketAddress
         * @param originReadData
         */
        @Override
        public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {
            super.onSocketResponse(socketAddress, originReadData);
            LogUtil.d("socket监听器收到数据=" + originReadData.getBodyString());
 
        }
    };

注册监听

            //监听socket相关行为
            EasySocket.getInstance().subscribeSocketAction(socketActionListener);


3、发送Socket消息

演示发送一个消息

    /**
     * 发送一个的消息，
     */
    private void sendMessage() {
        TestMsg testMsg =new TestMsg();
        testMsg.setMsgId("test_msg");
        testMsg.setFrom("android");
        //发送
        EasySocket.getInstance().upObject(testMsg);
    }

执行结果如下：

	发送的数据={"from":"android","msgId":"test_msg"} 


	socket监听器收到数据={"from":"server","msgId":"no_singer_msg"}


可以看到注册的监听器监收到了服务器的响应消息


4、断开Socket连接

    //断开当前的Socket连接，参数false表示当前断开不需要自动重连
    EasySocket.getInstance().disconnect(false);
     
    //连接Socket
    EasySocket.getInstance().connect();
    
    
5、销毁连接

    EasySocket.getInstance().destroyConnection();
    
destroyConnection()代表销毁整个连接状态，跟disconnect()不一样，如果此时要进行socket连接需要重新创建一个连接
    
    
测试的话，可以运行本项目提供的服务端程序socket_server，在Android studio要先将服务端程序添加配置上去，具体怎么操作可以参考我的博客，地址：https://blog.csdn.net/liuxingrong666/article/details/91579548


### 三、EasySocket消息协议的定义

之前框架严格定义了消息结构必须是Header+Body，收到很多网友的反馈，很多人想让框架兼容所有的消息格式，其实兼容是可以做到的，我不做消息格式的打包，直接发送即可，接收也是每一次socket读取的数据算一个消息。我强烈不建议这么做，因为这种形式当消息出现黏包和分包的时候，就会出现解析错误

但是太多人希望框架可以兼容所有消息格式，所以我还是满足了大家，所以现在框架默认情况下是兼容所有消息格式的，如果要定义消息格式，只需要设置消息协议即可

其实网络传输中消息协议的格式一般采用：“消息头+消息体”，EasySocket消息格式的基本框架也是如此，即 Header+Body，header一般保存消息的长度、类型等信息，body一般保存消息体

因为每个人的协议可能都不一样，所以框架不可能去统一格式，EasySocket提供了一个协议接口，如下

       public interface IMessageProtocol {

        /**
         * 获取包头的长度
         */
        int getHeaderLength();

        /**
         * 获取数据包体的长度，根据协议这个长度应该写在包头中，在读取数据的时候会用到
         */
        int getBodyLength(byte[] header, ByteOrder byteOrder);

        /**
         * 封装消息格式，返回socket发送的byte数组
         * 根据自己定义的消息格式来实现
         * @param sender
         * @return
         */
        byte[] pack(ISender sender);
       }

实现自己的消息协议需要实现接口的这三个方法，其中pack是定义如何打包消息，还有一定要定义好包头的长度和消息体的长度，可以参考框架默认的消息协议，如下

     public class DefaultMessageProtocol implements IMessageProtocol {
        @Override
        public int getHeaderLength() {
            return 4; // 包头的长度，用来保存body的长度值
        }

        @Override
        public int getBodyLength(byte[] header, ByteOrder byteOrder) {
            if (header == null || header.length < getHeaderLength()) {
                return 0;
            }
            ByteBuffer bb = ByteBuffer.wrap(header);
            bb.order(byteOrder);
            return bb.getInt(); // body的长度以int的形式写在header那里
        }

        @Override
        public byte[] pack(ISender sender) {
            // 默认为utf-8 Charset.forName(EasySocket.getInstance().getOptions().getCharsetName())
            byte[] body = new Gson().toJson(sender).getBytes(Charset.forName(EasySocket.getInstance().getOptions().getCharsetName()));
            // 消息头的长度，指多少个byte
            int headerLength = getHeaderLength();
            ByteBuffer bb = ByteBuffer.allocate(headerLength + body.length);
            bb.order(ByteOrder.BIG_ENDIAN);
            bb.putInt(body.length); // header，保存body的length
            bb.put(body); // body
            return bb.array();
        }
    }


### 四、EasySocket启动心跳机制

Socket的连接监听一般用心跳包去检测，EasySocket启动心跳机制非常简单， 下面是实例代码

 
    //启动心跳检测功能
    private void startHeartbeat() {
        //心跳实例
        ClientHeartBeat clientHeartBeat = new ClientHeartBeat();
        clientHeartBeat.setMsgId("heart_beat");
        clientHeartBeat.setFrom("client");
        EasySocket.getInstance().startHeartBeat(clientHeartBeat, new HeartManager.HeartbeatListener() {
            @Override
            public boolean isServerHeartbeat(OriginReadData originReadData) {
                String msg = originReadData.getBodyString();
                try {
                    JSONObject jsonObject = new JSONObject(msg);
                    if ("heart_beat".equals(jsonObject.getString("msgId"))) {
                        LogUtil.d("收到服务器心跳");
                        return true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }
     

启动心跳机制关键要定义一个心跳包实例作为Socket发送给服务端的心跳，然后实现一个接口，用来识别当前收到的消息是否为服务器的心跳，这个要根据自己的现实情况来实现，其实也挺简单的


### 五、EasySocket的请求回调功能

EasySocket的最大特点是实现了消息的回调功能，即当发送一个带有回调标识的消息给服务器的时候，我们可以准确地接收到这个消息的响应。启用回调功能需要用户实现如下步骤

  
 1.自定义CallbakcIdKeyFactory

   因为回调消息需要携带一个消息的唯一标识，这里我们称之为callbackId，通常这个值会打包在Json格式的消息中，但是每个人的callbackId在json消息中对应的key值可能不一样，所以用户需要自定义CallbakcIdKeyFactory来告诉框架callbackId的key值是什么，如Demo中所示

    public class CallbackIdKeyFactoryImpl extends CallbakcIdKeyFactory {
     
        @Override
        public String getCallbackIdKey() {
            return "callbackId";
        }
    }
   这里我的callbackId值对应的key就是callbackId，即在json消息的key-value形式中是这样的：callbackId : callbackId的值，这样我们就知道怎么从消息中去获取callbackId值了
    
    
 2.配置CallbakcIdKeyFactory
    
   定义好了CallbakcIdKeyFactory，需要进行配置，如下
   
           //socket配置
           EasySocketOptions options = new EasySocketOptions.Builder()
                   .setSocketAddress(new SocketAddress("192.168.3.9", 9999)) //主机地址
                   .setCallbackIdKeyFactory(new CallbackIdKeyFactoryImpl())
                   .build();
                
   启用回调功能的时候，需要将定义好的CallbakcIdKeyFactory通过setCallbackIdKeyFactory方法配置上去
    
    
 3.自定义回调功能的消息
    
   本框架默认都是以Json格式来解析消息的，其他形式暂不支持，每个人的回调标识callbackId在消息类中对应的字段可能不一样，所以需要我们通过继承SuperCallbackSender和SuperCallbackResponse来定义自己的消息结构类，其中SuperCallbackSender是发送消息的父类，SuperCallbackResponse是接收消息的父类，Demo的例子如下
    
    //回调消息的发送类
    public class CallbackSender extends SuperCallbackSender {
     
        private String msgId;
        private String from;
        private String callbackId;
     
        public String getMsgId() {
            return msgId;
        }
     
        public void setMsgId(String msgId) {
            this.msgId = msgId;
        }
     
        public String getFrom() {
            return from;
        }
     
        public void setFrom(String from) {
            this.from = from;
        }
     
        @Override
        public String getCallbackId() {
            return callbackId;
        }
     
        @Override
        public void setCallbackId(String callbackId) {
            this.callbackId=callbackId;
        }
    }
     
    //回调消息的接收类
    public class CallbackResponse extends SuperCallbackResponse {
     
        private String from;
        /**
         * 消息ID
         */
        private String msgId;
     
        private String callbackId;
        
        public String getMsgId() {
            return msgId;
        }
     
        public void setMsgId(String msgId) {
            this.msgId = msgId;
        }
     
     
        public String getFrom() {
            return from;
        }
     
        public void setFrom(String from) {
            this.from = from;
        }
     
        @Override
        public String getCallbackId() {
            return callbackId;
        }
     
        @Override
        public void setCallbackId(String callbackId) {
            this.callbackId = callbackId;
        }
    }
    
    
 4.服务器方面的配合使用
    
  回调功能需要服务器方面的配合，因为回调消息携带了唯一标识即callbackId，所以服务器在响应消息的时候，需要将这个callbackId的值返回给客户端
    
  如果以上方面都准备好的话，那么就可以实现Socket的回调功能了，示例如下
                    
    /**
     * 发送一个有回调的消息
     */
    private void sendCallbackMsg() {
 
        CallbackSender sender = new CallbackSender();
        sender.setMsgId("singer_msg");
        sender.setFrom("android");
        EasySocket.getInstance().upCallbackMessage(sender)
                .onCallBack(new SimpleCallBack<CallbackResponse>(sender.getSigner()) {
                    @Override
                    public void onResponse(CallbackResponse response) {
                        LogUtil.d("回调消息=" + response.toString());
                    }
		    
		    @Override
                    public void onError(Exception e) {
                        super.onError(e);
                        e.printStackTrace();
                    }
                });
    }
    
执行结果如下：

   发送的数据=������V{"callbackId":"VOOOSRR8VI56I8MPGRML","from":"我来自android","msgId":"callback_msg"} 

   回调消息=SingerResponse{from='server', msgId='callback_msg', callbackId='VOOOSRR8VI56I8MPGRML'}

   可以看到，在发送的回调方法中收到了此消息的反馈

   此外还封装了一个带进度框的请求，非常实用，使用方法如下

                CallbackSender sender = new CallbackSender();
                sender.setFrom("android");
                sender.setMsgId("delay_msg");
                EasySocket.getInstance()
                        .upCallbackMessage(sender)
                        .onCallBack(new ProgressDialogCallBack<String>(progressDialog, true, true, sender.getCallbackId()) {
                            @Override
                            public void onResponse(String s) {
                                LogUtil.d("进度条回调消息=" + s);
                            }
			    
			    @Override
                            public void onError(Exception e) {
                                super.onError(e);
                                e.printStackTrace();
                            }
                        });
            
 
    //接口实现类，返回一个Dialog
    private IProgressDialog progressDialog=new IProgressDialog() {
        @Override
        public Dialog getDialog() {
            Dialog dialog=new Dialog(MainActivity.this);
            dialog.setTitle("正在加载...");
            return dialog;
        }
    };

以上演示了EasySocket的基本使用方法，欢迎start

### 六、EasySocket的配置信息说明（EasySocketOptions）
      
    /**
     * 框架是否是调试模式
     */
    private static boolean isDebug = true;
    /**
     * 主机地址
     */
    private SocketAddress socketAddress;
    /**
     * 备用主机地址
     */
    private SocketAddress backupAddress;
    /**
     * 写入Socket管道的字节序
     */
    private ByteOrder writeOrder;
    /**
     * 从Socket读取字节时的字节序
     */
    private ByteOrder readOrder;
    /**
     * 从socket读取数据时遵从数据包结构协议，在业务层进行定义
     */
    private IMessageProtocol messageProtocol;
    /**
     * 写数据时单个数据包的最大值
     */
    private int maxWriteBytes;
    /**
     * 读数据时单次读取最大缓存值，数值越大效率越高，但是系统消耗也越大
     */
    private int maxReadBytes;
    /**
     * 心跳频率/毫秒
     */
    private long heartbeatFreq;
    /**
     * 心跳最大的丢失次数，大于这个数据，将断开socket连接
     */
    private int maxHeartbeatLoseTimes;
    /**
     * 连接超时时间(毫秒)
     */
    private int connectTimeout;
    /**
     * 服务器返回数据的最大值（单位Mb），防止客户端内存溢出
     */
    private int maxResponseDataMb;
    /**
     * socket重连管理器
     */
    private AbsReconnection reconnectionManager;
    /**
     * 安全套接字相关配置
     */
    private SocketSSLConfig easySSLConfig;
    /**
     * socket工厂
     */
    private SocketFactory socketFactory;
    /**
     * 实现回调功能需要callbackID，而callbackID是保存在发送消息和返回消息中的，此工厂用来获取socket消息中
     * 保存callbackID值的键，即key，比如json格式中的key-value中的key
     */
    private CallbakcIdKeyFactory callbakcKeyFactory;
    /**
     * 请求超时时间，单位毫秒
     */
    private long requestTimeout;
    /**
     * 是否开启请求超时检测
     */
    private boolean isOpenRequestTimeout;
 
    /**
     * IO字符流的编码方式，默认utf-8
     */
    private String charsetName;
       
Demo中还有socket服务端的测试代码，大家可以用本地IP地址对本框架进行测试，欢迎点评交流

### 关于混淆请加上：
   -keep class com.easysocket.entity.basemsg.** { *; }
  
   -keep class com.easysocket.entity.OriginReadData
