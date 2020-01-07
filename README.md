# EasySocket

 博客地址：https://blog.csdn.net/liuxingrong666/article/details/91579548

EasySocket的初衷是希望通过对传输数据的处理使得Socket编程更加简单、方便，EasySocket在实现了基本功能的基础上，还实现了Socket层面的请求回调功能。传统的Socket框架客户端发出一个请求信息，然后服务器返回一个应答信息，但是我们无法识别这个应答信息是对应哪个请求的，而EasySocket实现了将每个请求跟应答的一一对接，从而在Socket层面实现了请求回调功能

### EasySocket特点：

   1、采用链式调用一键发送数据，根据自己的需求配置参数，简单易用，灵活性高
   
   2、EasySocket不但实现了包括TCP的连接和断开、数据的发送和接收、心跳保活、重连机制等功能，还实现了Socket层面的请求回调功能
   
   3、消息结构使用的协议为：包头+包体，其中包体存储要发送的数据实体，而包头则存储包体的数据长度，这种结构方式便于数据的解析，很好地解决了Socket通信中消息的断包和粘包问题

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

    implementation 'com.github.jiusetian:EasySocket:{visionCode}'

}

### 二、EasySocket的基本功能使用
       
一般在项目的Application中对EasySocket进行全局化配置，下面是一个最简单的配置

    /**
     * 初始化EasySocket
     */
    private void initEasySocket() {
 
        //socket配置为默认值
        EasySocketOptions options = new EasySocketOptions.Builder()
                .build();
 
        //初始化EasySocket
        EasySocket.getInstance()
                .ip("192.168.4.52") //IP地址，测试的时候可以使用本地IP地址
                .port(9999) //端口
                .options(options); //连接的配置
 
        //创建一个socket连接
        EasySocket.getInstance().buildConnection();
    }

这里主要设置了IP和端口，其他的配置参数都使用了默认值，来看看框架的简单使用

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
        public void onSocketConnFail(SocketAddress socketAddress, IsReconnect isReconnect) {
            super.onSocketConnFail(socketAddress, isReconnect);
        }
 
        /**
         * socket断开连接
         * @param socketAddress
         * @param isReconnect 是否需要重连
         */
        @Override
        public void onSocketDisconnect(SocketAddress socketAddress, IsReconnect isReconnect) {
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

演示发送一个消息

    /**
     * 发送一个的消息，
     */
    private void sendMessage() {
        TestMsg testMsg =new TestMsg();
        testMsg.setMsgId("no_singer_msg");
        testMsg.setFrom("android");
        //发送
        EasySocket.getInstance().upObject(testMsg);
    }

执行结果如下：

	发送的数据={"from":"android","msgId":"no_singer_msg"} 


	socket监听器收到数据={"from":"server","msgId":"no_singer_msg"} 


可以看到注册的监听器监收到了服务器的响应消息

    
如果是只是测试的话，可以运行本项目提供的服务端程序socket_server，在Android studio要先将服务端程序添加配置上去，具体怎么操作可以参考我的博客，地址：https://blog.csdn.net/liuxingrong666/article/details/91579548


### 三、EasySocket启动心跳机制

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


### 四、EasySocket的请求回调功能

EasySocket的最大特点收到实现了消息的回调功能，即当发送一个带有回调标识的消息给服务器的时候，我们可以准确地接收到这个消息对应的响应消息，示例如下

    /**
     * 发送一个有回调的消息
     */
    private void sendCallbackMsg() {
 
        CallbackSender sender = new CallbackSender();
        sender.setMsgId("singer_msg");
        sender.setFrom("android");
        EasySocket.getInstance().upCallbackMessage(sender)
                .onCallBack(new SimpleCallBack<CallbackResponse>(sender) {
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

	发送的数据={"from":"android","msgId":"singer_msg","singer":"ZOLDZSWBPRR21I0ZVMR6"}

	回调消息=SingerResponse{from='server', msgId='singer_msg', singer='ZOLDZSWBPRR21I0ZVMR6'}

可以看到，发送消息的时候有一个数据singer是消息的回调标识，socket接收到的响应消息也是带有singer标识，而且是同一个值，正是这个singer才让我们可以识别到响应消息对应的是哪个发送消息

回调功能的基本原理也很简单，每次客户端发送消息的时候都会随机生成一个字符串作为此消息的唯一标识，本框架用singer作为回调标识，服务端方面在响应有singer标识的消息的时候，将这个singer标识返回给客户端就OK 了，至于客户端是怎么处理的，大家可以看看项目的源码


此外还封装了一个带进度框的请求，非常实用，使用方法如下

                CallbackSender sender = new CallbackSender();
                sender.setFrom("android");
                sender.setMsgId("delay_msg");
                EasySocket.getInstance()
                        .upCallbackMessage(sender)
                        .onCallBack(new ProgressDialogCallBack<String>(progressDialog, true, true, sender) {
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

### 五、EasySocket的配置信息说明（EasySocketOptions）

    /**
     * 框架是否是调试模式
     */
    private static boolean isDebug = true;
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
    private IReaderProtocol readerProtocol;
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
    private EasySocketFactory socketFactory;
    /**
     * 获取请求消息唯一标识singer的工厂，默认为DefaultCallbackSingerFactory
     */
    private CallbackSingerFactory callbackSingerFactory;
 
    /**
     * 请求超时时间，单位毫秒
     */
    private long requestTimeout;
    /**
     * 是否开启请求超时检测
     */
    private boolean isOpenRequestTimeout;
    
GitHub代码的Demo中还有socket服务端的测试代码，大家可以用本地IP地址对本框架进行测试，欢迎点评交流
