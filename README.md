# EasySocket

 博客地址：https://blog.csdn.net/liuxingrong666/article/details/91579548

EasySocket的初衷是希望通过对传输数据的处理使得socket编程更加简单、方便，传统的socket框架客户端发出一个请求信息，然后服务器返回一个应答信息，但是我们无法识别这个应答信息是对应哪个请求的，而EasySocket可以将每一个请求信息和应答信息实现一一对接，从而在socket层面实现了请求回调的功能。

### EasySocket特点：

   1、采用链式调用一键发送数据，根据自己的需求配置参数，简单易用，灵活性高
   
   2、EasySocket分为简单使用和高级使用，简单使用是实现socket的普通功能，包括TCP的连接和断开、数据的发送和接收、心跳机制等等，高级使用实现了socket请求的回调功能和智能心跳机制

   3、消息结构使用（包头+包体）的协议，其中包体存储要发送的数据实体，而包头则存储包体的数据长度，这种结构方式方便于数据的解析，解决了TCP通信中断包、粘包等问题；

   4、智能的心跳包保活机制，自动发送和接收心跳包，实时检测socket连接状态，断开自动重连机制；

   5、Socket层面的数据传输回调功能，使得每一个请求信息和应答信息实现无缝对接。

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
                    .ip("192.168.4.52") //IP地址
                    .port(9999) //端口
                    .options(options) //连接的配置
                    .buildConnection(); //创建一个socket连接
        }

Socket的相关参数都使用了默认值，主要设置了IP和端口，这种配置是不具备回调功能和智能心跳的，但也满足了socket的基本需求，来看看框架的简单使用

首先 定义一个socket行为的监听器，如下

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
                LogUtil.d("监听器接收的数据->" + originReadData.getBodyString());
     
            }
        };

注册监听

            //监听socket相关行为
            EasySocket.getInstance().subscribeSocketAction(socketActionListener);

 演示发送一个数据包，测试是不是能监听到返回的数据

     /**
     * 发送心跳包
     */
    private void sendHeartBeat() {
        ClientHeartBeat clientHeartBeat = new ClientHeartBeat();
        clientHeartBeat.setMsgId("heart_beat");
        clientHeartBeat.setFrom("client");
        //发送
        EasySocket.getInstance().upObject(clientHeartBeat);
    }

执行结果如下：

发送的数据=������C{"from":"client","msgId":"heart_beat","ack":"HXG1LVLZL1DIMGWULTOT"} 

接收的数据={"from":"server","msgId":"heart_beat","ack":"HXG1LVLZL1DIMGWULTOT"} 

监听器接收的数据->{"from":"server","msgId":"heart_beat","ack":"HXG1LVLZL1DIMGWULTOT"} 


可以看到确实监听到了服务器返回的心跳


### 三、EasySocket的高级配置

EasySocket的主要特点是具备数据回调功能和智能心跳管理，但这个需要更高级的配置才能使用，默认是不开启的，下面来看看高级配置

        /**
         * 初始化具有回调功能和智能心跳的EasySocket
         */
        private void initCallbackSocket() {
            //心跳包实例
            ClientHeartBeat clientHeartBeat = new ClientHeartBeat();
            clientHeartBeat.setMsgId("heart_beat");
            clientHeartBeat.setFrom("client");
     
            //socket配置
            EasySocketOptions options = new EasySocketOptions.Builder()
                    .setActiveHeart(true) //启动心跳管理器
                    .setClientHeart(clientHeartBeat) //设置全局心跳对象
                    .setActiveResponseDispatch(true) //启动消息的回调管理
                    .setAckFactory(new AckFactoryImpl()) //设置获取请求标识ack的factory
                    .build();
     
            //初始化EasySocket
            EasySocket.getInstance()
                    .ip("192.168.4.52") //IP地址
                    .port(9999) //端口
                    .options(options) //连接的配置
                    .buildConnection(); //创建一个socket连接
     
        }

要想实现心跳包的自动发送和接收，需要在初始化的时候启动心跳管理器，并且设置一个心跳包实例。

上面AckFactoryImpl是高级配置的关键，这是一个获取回调标识ack的工厂类，需要使用者自己定义，其中它的抽象类是这样的

    public abstract class AckFactory {
        public abstract String createCallbackAck(OriginReadData originReadData);
    }

能够拿到所谓的回调标识ack是EasySocket实现回调功能的关键，每一个由客户端向服务器发送的信息都会携带一个随机生成的20位的字符串，我们称之为ack，所以服务器接收的每一个信息都有这样的一个ack，在返回应答信息的时候，将这个ack一起返给客户端，客户端在接收的时候通过对比ack就知道当前应答信息对应的是哪一个请求了。

比如下面的一个AckFactory的实现类

    public class AckFactoryImpl extends AckFactory {
        @Override
        public String createCallbackAck(OriginReadData originReadData) {
            try {
                //服务端返回的json格式的数据
                String data=originReadData.getBodyString();
                JSONObject jsonObject=new JSONObject(data);
                //获取当前返回消息的ack标识
                return jsonObject.getString("ack");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

当然这个抽象工厂类是根据自己的实际情况去实现，其中回调方法createCallbackAck的参数OriginReadData是服务器返回的数据，只要保证能获取到唯一的ack标识就可以了。

### 四、EasySocket的回调功能演示

经过上面的高级配置，心跳包实现了自动发送和接收，还有可以使用EasySocket的回调功能了，比如发送一个心跳包给服务器，然后服务端返回一个应答信息 ServerHeartBeat

    /**
     * 发送心跳包，应答消息会在下面的onResponse方法中回调
     */
    private void sendHeartBeat(){
        ClientHeartBeat clientHeartBeat = new ClientHeartBeat();
        clientHeartBeat.setMsgId("heart_beat");
        clientHeartBeat.setFrom("client");
        EasySocket.getInstance().upObject(clientHeartBeat)
                .onCallBack(new SimpleCallBack<ServerHeartBeat>(clientHeartBeat) {
                    @Override
                    public void onResponse(ServerHeartBeat serverHeartBeat) {
                        LogUtil.d("心跳包请求反馈：" + serverHeartBeat.toString());
                    }
                });
 
    }
    
执行结果如下：

发送的数据=������C{"from":"client","msgId":"heart_beat","ack":"CCA4W7KXDDNDLYO84SFJ"} 

接收的数据={"from":"server","msgId":"heart_beat","ack":"CCA4W7KXDDNDLYO84SFJ"} 

监听器接收的数据->{"from":"server","msgId":"heart_beat","ack":"CCA4W7KXDDNDLYO84SFJ"} 

心跳包请求反馈：ServerHeartBeat{from='server', msgId='heart_beat', backSign='CCA4W7KXDDNDLYO84SFJ'}

只需要定义好要发送的数据包实例，然后通过EasySocket类upObject发送给服务器，而在onCallBack回调方法中就可以获得此次请求的应答信息，是不是很Easy。

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

以上演示了EasySocket的使用方法，欢迎start，项目地址：https://github.com/jiusetian/EasySocket

### 五、EasySocket的其他配置信息说明（EasySocketOptions）

     
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
         * 获取请求消息唯一标识ack的工厂
         */
        private AckFactory ackFactory;
     
        /**
         * 请求超时时间，单位毫秒
         */
        private long requestTimeout;
        /**
         * 是否开启请求超时检测
         */
        private boolean isOpenRequestTimeout;
        /**
         * 客户端心跳包，默认为null
         */
        private IClientHeart clientHeart;
        /**
         * 是否开启心跳功能，默认关闭
         */
        private boolean isActiveHeart;
        /**
         * 是否启动socket的回调功能，默认关闭
         */
        private boolean isActiveResponseDispatch;

GitHub代码的Demo中还有socket服务端的测试代码，大家可以用本地IP地址对本框架进行测试，欢迎大家点评交流。
