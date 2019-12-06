# EasySocket

 博客地址：https://blog.csdn.net/liuxingrong666/article/details/91579548

EasySocket的初衷是希望通过对传输数据的处理使得socket编程更加简单、方便，传统的socket框架客户端发出一个请求信息，然后服务器返回一个应答信息，但是我们无法识别这个应答信息是对应哪个请求的，而EasySocket可以将每一个请求信息和应答信息实现一一对接，从而在socket层面实现了请求回调的功能。

### EasySocket特点：

   1、采用链式调用一键发送数据，根据自己的需求配置参数，简单易用，灵活性高
   
   2、EasySocket分为简单使用和高级使用，简单使用是实现socket的普通功能，包括TCP的连接和断开、数据的发送和接收、心跳机制等等，高级使用实现了socket请求的回调功能和心跳自动检测

   3、消息结构使用（包头+包体）的协议，其中包体存储要发送的数据实体，而包头则存储包体的数据长度，这种结构方式方便于数据的解析，解决了TCP通信中断包、粘包等问题；

   4、Socket层面的数据传输回调功能，使得每一个请求信息和应答信息实现无缝对接。

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

### 二、EasySocket的简单配置
       
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
                .options(options) //连接的配置
                .buildConnection(); //创建一个socket连接
    }

其他的配置参数都使用了默认值，这里主要设置了IP和端口，这种配置是不具备回调功能和智能心跳检查功能的，但也满足了socket开发的基本需求，来看看框架的简单使用

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
            LogUtil.d("socket接收的数据->" + originReadData.getBodyString());
 
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
    
如果是只是测试的话，可以将IP地址设置为本地IP，然后在Android studio中启动项目的服务器测试软件，选择java test 运行就可以了

执行结果如下：

	发送的数据={"from":"android","msgId":"no_singer_msg"} 


	socket接收的数据->{"from":"server","msgId":"no_singer_msg"}


可以看到注册的监听器监收到了服务器的响应消息


### 三、EasySocket的高级使用

EasySocket的区别于其他Socket框架的主要特点是具备数据回调功能和智能心跳检测，但这两个功能默认是关闭的，需要自己手动配置，看例子如下

       /**
     * 初始化具有回调功能的socket
     */
    private void initCallbackSocket() {
        //心跳实例
        ClientHeartBeat clientHeartBeat = new ClientHeartBeat();
        clientHeartBeat.setMsgId("heart_beat");
        clientHeartBeat.setFrom("client");
 
        //socket配置
        EasySocketOptions options = new EasySocketOptions.Builder()
                .setActiveHeart(true) //启动心跳检测功能
                .setEnableCallback(true) //启动消息的回调功能
                .setClientHeart(clientHeartBeat) //设置心跳对象
                .build();
 
        //初始化EasySocket
        EasySocket.getInstance()
                .ip("192.168.3.9") //IP地址
                .port(9999) //端口
                .options(options) //连接的配置
                .buildConnection(); //创建一个socket连接
 
    }
     

和普通使用不同的在于，配置中分别启动了心跳检测功能和消息的回调功能，同时心跳的自动检测需要一个全局的心跳包实例，作为给服务器发送的一个心跳包

这里需要声明的是，本框架的消息回调功能是需要服务器软件的配合的，基本原理就是每次客户端发送消息的时候都会随机生成一个字符串，而在服务器响应消息的时候需要将这个字符串返回来给客户端，只有这样客户端才知道当前响应的消息是对哪个消息的响应


### 四、EasySocket的回调功能演示

经过上面的配置，首先实现心跳包的自动发送和接收并且检测连接状态，同时也实现了消息的回调功能，即当发送一个带有回调标识的消息给服务器的话，我们可以准确地接收到这个消息对应的响应消息，示例如下

    /**
     * 发送一个有回调的消息
     */
    private void sendSingerMsg() {
 
        SingerSender sender=new SingerSender();
        sender.setMsgId("singer_msg");
        sender.setFrom("android");
        EasySocket.getInstance().upObject(sender)
                .onCallBack(new SimpleCallBack<SingerResponse>(sender) {
                    @Override
                    public void onResponse(SingerResponse response) {
                        LogUtil.d("回调消息="+response.toString());
                    }
                });
 
    }
    
执行结果如下：

	发送的数据={"from":"android","msgId":"singer_msg","singer":"PZSE51SLQMOJ1ZZFO8MA"}

	回调消息=SingerResponse{from='server', msgId='singer_msg', singer='PZSE51SLQMOJ1ZZFO8MA'} 

可以看到，发送消息的时候有一个数据singer就是消息的回调标识，socket接收到的响应消息也带有singer标识，而且是同一个值，正是这个singer才让我们可以识别到哪个响应消息对应哪个发送消息

此外还封装了一个带进度框的请求，非常实用，使用方法如下：

                MyCallbackSender sender = new MyCallbackSender();
                sender.setFrom("android");
                sender.setMsgId("my_request");
                EasySocket.getInstance()
                        .upObject(sender)
                        .onCallBack(new ProgressDialogCallBack<String>(progressDialog, true, true, sender) {
                            @Override
                            public void onResponse(String s) {
                                LogUtil.d("请求返回的消息=" + s);
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

以上演示了EasySocket的基本使用方法，欢迎start。

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
    /**
     * 客户端心跳包
     */
    private BaseClientHeart clientHeart;
    /**
     * 是否开启心跳功能，默认关闭
     */
    private boolean isActiveHeart;
    /**
     * 是否启动socket的回调功能，默认关闭
     */
    private boolean isEnableCallback;

GitHub代码的Demo中还有socket服务端的测试代码，大家可以用本地IP地址对本框架进行测试，欢迎点评交流。
