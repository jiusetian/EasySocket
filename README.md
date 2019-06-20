# EasySocket

 博客地址：https://blog.csdn.net/liuxingrong666/article/details/91579548

一般来说，socket用于保持TCP长连接进行数据的传输，至于通信协议和数据信息的处理需要自己去实现。而普通的socket框架通常实现的是socket连接、数据传输、断开重连和心跳保活等基本功能，并没有对传输的数据有做进一步的处理！

EasySocket的初衷是希望对传输数据的处理使得socket编程更加简单、方便，传统的socket框架客户端发出一个请求信息，然后服务器返回了一个应答信息，但是我们无法识别这个应答信息是对应哪个请求的，而EasySocket可以将每一个应答信息和请求信息实现对接，从而在socket层面实现了请求回调的功能。

### EasySocket特点：
   
   1、实现了socket层面传输数据的回调功能，使得每一个请求有能够对接其应答信息

   2、消息结构使用（包头+包体）的协议，其中包头存储包体的数据长度，而包体存储是我们要发送的数据实体，这种结构方便数据的读取，解决了TCP通信中数据断包、粘包等问题；
   
   3、智能的心跳包保活机制，自动发送和接收心跳包，实时检测socket连接状态，同时重连机制解决socket的连接问题；
    
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

### 二、EasySocket的初始化配置

一般在项目的Application中对EasySocket进行全局的初始化操作，下面是一个简单的例子

    /**
     * 初始化EasySocket
     */
    private void initEasySocket(){
        //自定义的客户端心跳包数据结构
        ClientHeartBeat clientHeartBeat=new ClientHeartBeat();
        clientHeartBeat.setMsgId("heart_beat");
        clientHeartBeat.setFrom("client");

        //socket配置
        EasySocketOptions options=new EasySocketOptions.Builder()
                .setSignerFactory(new SignerFactoryImpl()) //设置获取回调标识signer的factory
                .setActiveHeart(true) //启动心跳机制，默认也是启动的
                .setClientHeart(clientHeartBeat) //设置客户端的心跳包实例
                .build();

        //初始化EasySocket
        EasySocket.getInstance()
                .mainIP("192.******") //IP地址，可以使用本地IP进行测试
                .mainPort(9999) //端口
                .mainOptions(options) //主连接的配置
                .buildMainConnection(); //创建一个主socket连接
    }

EasySocket的特点之一是实现心跳包的自动管理，所以要想实现心跳包的自动发送和接收，就需要在初始化的时候设置一个给服务器发送的客户端心跳包实例。

还有上面SignerFactoryImpl是本项目的关键，这是一个获取回调标识signer的工厂类，需要使用者自己定义，其中它的抽象类是这样的

public abstract class SignerFactory {
    public abstract String createCallbackSgin(OriginReadData originReadData);
}

能够拿到所谓的回调标识signer是EasySocket实现回调功能的关键，每一个由客户端向服务器发送的信息都会携带一个随机生成的20位的字符串，我们称之为signer，所以服务器接收的每一个信息都能获取到这样的一个signer，在返回应答信息的时候，将这个signer一起返给客户端，客户端在接收的时候通过对比signer就知道当前应答信息对应的是哪一个请求信息了。

比如下面的一个SignerFactory的实现类

public class SignerFactoryImpl extends SignerFactory {

    @Override
    public String createCallbackSgin(OriginReadData originReadData) {
    
        try {
	
            String data=originReadData.getBodyString();
            JSONObject jsonObject=new JSONObject(data);
            return jsonObject.getString("signer");
        } catch (JSONException e) {
	
            e.printStackTrace();
        }
	
        return null;
	
    }
}

当然这个抽象工厂类是根据自己的实际情况去实现，原则上保证能获取到这个唯一signer标识就可以了。

其实上面的两个必须的配置完成了，再对IP和端口进行设置，最后通过buildMainConnection()创建一个socket连接，就可以愉快地使用EasySocket的功能，其他的配置框架都有默认值，当然你也可以自己定义。

### 三、EasySocket的简单调用

经过上面的配置和初始化，我们就可以直接调用EasySocket的接口进行网络通信了，比如发送一个心跳包给服务器

    /**
     * 发送心跳包
     */
    private void sendHeartBeat(){
        ClientHeartBeat clientHeartBeat=new ClientHeartBeat();
        clientHeartBeat.setMsgId("heart_beat");
        clientHeartBeat.setFrom("client");
        DefaultSender defaultSender =new DefaultSender(clientHeartBeat);
        //链式调用方式，发送一个心跳包，同时接收对应的应答信息
        EasySocket.getInstance().upObject(defaultSender)
                .onCallBack(new SimpleCallBack<ServerHeartBeat>(defaultSender) {
                    @Override
                    public void onResponse(ServerHeartBeat serverHeartBeat) {
                        ELog.d("心跳包请求反馈："+serverHeartBeat.toString());
                    }
                });
    }

只需要定义好要发送的心跳包实例，然后通过EasySocket类upObject发送给服务器，而在onCallBack方法回调中就可以获得此次请求的应答信息，是不是很Easy。

此外我们还封装了一个带进度框的请求，非常实用，实用方法如下：

                MySender sender=new MySender();
                sender.setFrom("android");
                sender.setMsgId("my_request");
                DefaultSender defaultSender =new DefaultSender(sender);
                EasySocket.getInstance()
                        .upObject(defaultSender)
                        .onCallBack(new ProgressDialogCallBack<String>(progressDialog,true,true, defaultSender) {
                            @Override
                            public void onResponse(String s) {
                                ELog.d("请求返回的消息="+s);
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

以上演示了EasySocket的简单使用方法，当然还有其他的功能，目前是刚刚推出第一个版本v1.0.0，后期还会继续增加其他的功能和优化项目。

### 四、EasySocket的其他配置信息说明（EasySocketOptions）


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
     * 获取请求消息唯一标识signer的工厂
     */
    private SignerFactory signerFactory;

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
    private IClientHeart clientHeart;
    /**
     * 是否开启心跳功能，默认开启
     */
    private boolean isActiveHeart;

GitHub代码的Demo中还有socket服务端的测试代码，大家可以用本地IP地址对本框架进行测试，欢迎大家点评交流。
  
