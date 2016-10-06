#前言
最近接触android中js与java交互的东西很多，当然它们之间的交互方式有几种，但是我觉得这几种交互方式都存在一定的不足，这是我决定编写SimpleJavaJsBridge这个库的关键原因。

我会按以下顺序进行本文章：
  
  1.  现有js与java通信方案及不足
  2.  js与java完美通信方案设计
  3.  SimpleJavaJsBridge

现在进入正题
#1. 现有js与java通信方案及不足
先来说明一点js与java通信，指的是js既可以给java发送消息，同时java也可以给js发送消息。那就来屡屡它们之间的通信方案。

**1.1 java给js发送消息**

官方唯一指定方法是通过webview的loadUrl(String)方法进行的，看下的伪代码:

            //例子：调用js的test(param)方法
            webView.loadUrl("javascript:test(1)");

            

调用方法非常的简单，"javascript:"+js方法的名字＋方法的参数值拼接成一个字符串就可以给js发送消息了，犹如是在直接调用js的方法。

**1.2 js给java发送消息**

js给java发送消息实际上只有2种方案，依次来分析下这2种方案。

**1.2.1 官方方法**

先看下伪代码：

            //该类封装了提供给js调用的方法
            public class JSBridge{
                    //提供给js的方法
                    public void invokeByJs(String msg){

                    }
            }

            //把JSBridge对象注入WebView中，同时起一个别名
            webView.addJavascriptInterface(new JSBridge(),"jsBridge");
            
            //js调用java方法
            window.jsBridge.invokeByJs('hello java');
            
            

这种方法其实是把一个对象注入到WebView中，js给java发送消息的方式是

            window.注入WebView的java对象的所对应name值.javaMethod(param...);

这其实也犹如在java代码中调用java的方法，因为java提供给js的方法名，方法参数是啥，js在发送消息时，方法名与参数必须保持一致，这也是这些java代码不能进行混淆的原因。

但是这种方法存在一个严重的漏洞，虽然官方在android4.4的时候给出了相应的解决方案，但是android4.4以下的版本还得解决该漏洞，因此一些巨人们就开始琢磨着解决这个坑，第二种方法由此诞生。

**1.2.2 js传递约定好的字符串给java**

这种方案的主要原理是： 
- 找到一个js可以给java发送消息的入口（这个入口有onJsPrompt,onJsAlert等等)
- js通过入口把消息按既定好的规则拼接成字符串传递给java
- java按照既定好的规则对字符串进行解析
- 根据解析数据，通过反射来调用自己相应方法

这种方法使用起来要比官方方法（第一种方法）麻烦。

**1.3 存在的不足**

上面介绍了js与java的通信方法，那我就来分析下我认为存在的不足。

**1.3.1 java给js发送消息方法和js给java发送消息的官方方法存在的不足**

**1.3.1.1 强依赖**

java给js发送消息的方法，和js给java发送消息的官方方法都存在着**强依赖**的问题，都要高度依赖对方的方法名字，方法参数。强依赖发生于同一模块内，个人觉得不是问题甚至是高内聚的体现。但是java与js可以说是处于两个不同的模块或者叫两个不同世界，只要js提供给java的方法发生变化，java也得改动，同理java提供给js的方法也如此。处于两个不同模块知道对方的细节越少越好，这样耦合性就会降低，耦合性降低的好处就不用说了。

**1.3.1.2 强依赖导致js需要兼容不同的系统**

先看段伪代码：
    
      function location(){
            //是ios系统，采用给ios发送消息的方法
            if(isIOS){
                    给ios发送消息；
            }else if(isAndroid){
                    给android发送消息；
            }
      }
上面的代码展示的是js使用native的定位功能的代码，因为js在给不同的系统发送消息的方式不一样，就会出现if else if 这样的兼容语句。当前js代码只被ios和android使用，假如还会被wp或pc来使用，那if else if岂不是要恶心死。产生该问题的主要原因是：js代码在针对不同的系统自己独有的通信方式进行通信。

**1.3.1.3 给不存在的接口发送消息没反馈**

java在给js的一个不存在接口发送消息时，java根本不知道该接口不存在，java只会傻傻的等待。同理js在给java的一个不存在接口发送消息时，js是可以通过捕获异常来知道该接口不存在，但是这不是最好的解决方案。
给不存在接口发送消息没反馈会导致js代码充斥着if else if语句，看段伪代码：

       //调用java的定位方法
      function location(){
            //1.1版本以上才会调用定位功能
            if(androidAppVersion > '1.1'){
                    发送消息给java；
            }else{
                    给用户提示，暂不支持定位功能；
            }
      }

这是一段调用java进行定位的js代码，android app在版本1.1的时候才增加了定位的功能，因此对于1.1以下版本是不支持这功能的，因此js代码里面非常有必要根据版本号进行判断。这只是由于版本问题导致if else if的一个小小的缩影。还有一些其他情况导致if else if的产生比如一份js代码被多个业务使用。

 **1.3.2 js给java发送消息的第二种方法存在不足**

上文提到的js给java发送消息的第二种方法，它解决了存在的漏洞，但是这种方法，使用起来要比第一种方法复杂，java会多做以下工作：
- 解析js传递给java的字符串，把调用的接口，参数解析出来
- 把调用的接口，参数映射到相应的方法

不论js传递给java的字符串是json格式还是其他格式，解析这样的字符串肯定是一件无趣的重复的体力劳动。


若想解决以上的问题，我们有必要设计一套完美的通信方案。

#2. js与java完美通信方案设计
**2.1 一套完美的js与java的通信方案应满足以下几点:**

- js与java知道对方的细节越少越好，越少它们的耦合性越低。那到底多少为好呢？我个人觉得互相暴漏给对方一个接口足矣。这样js与native的通信就类似于通过一个管道通信或者说类似于socket通信(降低强依赖)

- js与java之间通信，需要定义好一套通信协议或者叫通信规则，在管道之间传递通信协议。这样它们之间的通信是针对一套定义好的协议进行的，而不是针对每个系统自己独有的通信方式（好处js就不会出现兼容不同的系统的if else if代码）

- 主动发送消息给对方时，对方必须对该消息予以反馈，即使主动发送消息者对反馈消息不感兴趣，（反馈信息可以去掉由于版本兼容等带来的if else if兼容代码）


**2.2 那我们就开始设计js与java之间的通信方案**

**2.2.1 互相暴漏给对方一个接口**

- js为java提供一个唯一的接口，这个接口可以是在java端写死的，也可以是js传递过来的（这样更灵活）。所有发送给js的消息（请求消息和反馈消息）都通过该接口
- java为js提供的一个唯一的接口，因为官方的方法存在漏洞，我们采用在onJsPrompt方法中接收js发送的所有消息，当然大家还可以选择其他方法来接收js的消息，这不是重点。

**2.2.2 js与java之间通信协议的制定**

js与java之间的通信特别类似于网络请求，主动发起消息的行为可以称为request（请求消息），对该消息的反馈可以称为response（响应消息）。

**request**

一个request封装了请求对方的哪个接口，以及该接口所需要的参数。

**response**

一个response封装了状态信息（可以知道处理的结果是成功还是失败）和处理结果。

**如何接收对方发送的response消息?**

大家都应该都会想到，在发送消息的时候传递一个回调接口就行了，但是因为js与java之间是跨语言的，尤其是java是不可能把回调接口传递给js，js虽然可以传递过来但是会有问题，所以这时候有一种解决办法：
- 在给对方发送request消息时，为回调接口生成一个唯一的id值，把id值存入request中发出。
- 同时把回调接口缓存起来。
- 在接收到response时，从response解析这个id值，根据id值查找到回调接口。

因此request和response中还得包含回调id这个值。

**通信协议的格式**

request数据格式：

         {      
              //接口名称
              "interfaceName":"test",
              //回调id值
              "callbackId":"c_111111",
              //传递的参数
              "params":{
                     ....
              }
         }

response数据格式：

        {
                //回调id，同时这也是response的标志
               "responseId":"c_111111",
                //response数据
               "data":{
                   //状态数据
                  "status":"1",
                  "msg":"ok",
                  //response的处理结果数据
                  "values":{
                         ......
                  }
              }
         }

到此通信协议就已经定义好了。

**2.2.3 让繁琐的无趣的重复的苦力活儿不再有**

大家可以看到通信协议request和response都是json格式，从json中解析数据或者把数据封装为json都是重复的苦力活儿。
这也是我一直想着力解决的痛点，解决之道是从retrofit中获得启发的，应用注解来解决以上问题。

关于js与java完美通信的设计思想到此为止，这也是SimpleJavaJsBridge这个库的核心思想，那我们就来看下SimpleJavaJsBridge。

#3. SimpleJavaJsBridge
SimpleJavaJsBridge我为什么要起一个这样的名字，首先它解决了上文中提到的**让繁琐的无趣的重复的苦力活儿不再有**的问题，对于不管是从json中解析数据还是把数据封装成json，使用者都不需要关心，让使用者很省心；并且它使用起来也非常的简单，在稍后的例子中大家会体会到，所以用了simple这个词儿。通过它java可以给js发送消息，并且接收js的响应消息；同时js也可以给java发送消息，同样接收java的响应消息。因此它是java与js之间通信的桥梁，因此它的名字叫为SimpleJavaJsBridge。

**3.1 如何解决繁琐的无趣的重复的苦力活儿？**

解决这个问题思路来自于鼎鼎有名的Retrofit，Retrofit通过注解的方式解决了构建request和解析response的问题，因此注解也可以解决我现在遇到的问题。那我们就来认识下这些注解。
**InvokeJSInterface**用来标注java给js发送消息的方法，它的value值代表js提供的功能的接口名字

**JavaCallback4JS**用来标注java提供给js的回调方法的

**JavaInterface4JS**用来标注java提供给js的接口，它的value值代表功能的接口名字

**Param**用来标注参数或者类的实例属性，它的value值代表参数被存入json中的key值，它的needConvert代表当前的参数是否需要进行转换，因为通过JsonObject类往json中存放的数据是有要求的，JsonObject中只能存放基本数据和JsonObject和JsonArray这些数据类型，对于其他的类型就得进行转换了。因此只要是不能直接通过JsonObject存放的类型该值必须为true

**ParamCallback**用来标注回调类型的参数，比如发送request给js的方法中，需要有一个回调参数，那这个参数必须用它来标注

**ParamResponseStatus**用来标注响应状态类型的参数，比如：statusCode，StatusMsg这些参数，它的value值是json中的key值。


**3.2 SimpleJavaJsBridge使用**

**3.2.1 构建一个SimpleJavaJsBridge实例**
      
        SimpleJavaJsBridge instance = new SimpleJavaJsBridge.Builder()
              .addJavaInterface4JS(javaInterfaces4JS)                                       
              .setWebView(webView)                                   
               .setJSMethodName4Java("_JSBridge._handleMessageFromNative")                                   
              .setProtocol("niu","receive_msg").create();

通过SimpleJavaJsBridge.Builder来构建一个SimpleJavaJsBridge对象，
- addJavaInterface4JS用来添加java提供给js的接口
- setWebView 设置WebView这是必须进行设置的
- setJSMethodName4Java 设置js为java唯一暴漏的方法名字
- setProtocol设置协议字段，这也是必须的，这个字段主要是为了ios而设置的

当然还可以调用其他的一些方法对SimpleJavaJsBridge进行设置

**3.2.2 js给java发送消息**

**js给java的无参接口发送消息**
  
          /** * 给js发送响应消息的接口*/
          public interface   IResponseStatusCallback { 
               void callbackResponse(@ParamResponseStatus("status") int status, @ParamResponseStatus("msg") String msg);
          }

          //java提供给js的"tes4"接口，@ParamCallback标注的是给js发送消息的回调
          @JavaInterface4JS("test4")
          public void test3(@ParamCallback IResponseStatusCallback jsCallback) {  
              进行相应处理...;
              //给js发送响应消息
               jsCallback.callbackResponse(1, "ok");
          }

          //下面是js代码,js给java的"test4"接口发送消息
          _JSNativeBridge._doSendRequest("test4", {}, function(responseData){

          });

**js给java的有参接口发送消息**

          /** * 给js发送响应消息的接口*/
          public interface   IResponseStatusCallback { 
               void callbackResponse(@ParamResponseStatus("status") int status, @ParamResponseStatus("msg") String msg);
          }

          /** * 必须有无参构造函数 ，只有被@Param注解的属性才会存入json中*/
          public static class Person {
               @Param("name") 
               String name;

               @Param("age") 
               public int age;  

               public Person() {    } 

               public Person(String name, int age) {  
                  this.name = name;  
                  this.age = age; 
               }
          }

          //java提供给js的“test1”接口，Person是无法直接往JsonObject中存放的，
          //所以needConvert必须为true，会自动把Person中用注解标注的属性放入json中
          @JavaInterface4JS("test1")
          public void test(@Param(needConvert = true) Person personInfo, @ParamCallback IResponseStatusCallback jsCallback) {
                 对收到的数据进行处理....;
                 jsCallback.callback(1, "ok");
          }

          //下面是js代码,js给java的"test1"接口发送消息
          _JSNativeBridge._doSendRequest("test1", {"name":"niu","age":10}, function(responseData){

          });

**3.2.3 java给js发送消息**

        //给js发送消息的方法要定义在一个interface中，这个过程是模仿Retrofit的
        public interface IInvokeJS {

              //复杂类型，只有用@Param标注的属性才会放入json中
              public static class City{
                       @Param("cityName") 
                       public String cityName; 
      
                       @Param("cityProvince") 
                       public String cityProvince;

                       public int cityId;
              }

              //给js的“exam”接口发送数据，参数是需要传递的数据
              @InvokeJSInterface("exam")
              void exam(@Param("test") String testContent, @Param("id") int id,@ParamCallback IJavaCallback2JS iJavaCallback2JS);

              //给js的“exam1”接口发送数据，参数同样也是需要传递的数据
              @InvokeJSInterface("exam1")
              void exam1(@Param(needConvert = true) City city, @ParamCallback IJavaCallback2JS iJavaCallback2JS);
        ｝


       //使用，使用方式和Retrofit一样，先使用SimpleJavaJsBridge的
      //createInvokJSCommand实例方法生成一个IInvokeJS实例
      IInvokeJS invokeJs = simpleJavaJsBridge.createInvokJSCommand(IInvokeJS.class);

      //给js的"exam"发送消息，发送的是基本数据类型
      invokeJs.exam("hello js",20, new IJavaCallback2JS{
                //接收js发送的响应数据的回调方法，该方法的名字可以任意，但必须用@JavaCallback4JS标注
                @JavaCallback4JS
                public void callback(@ParamResponseStatus("msg")String statusMsg,@Param("msg") String msg) {

                }
      });

     City city = new City();
     city.cityName = "长治";
     city.cityId = 11;
     city.cityProvince = "山西";
     //给js的“exam1”发送消息，city是一个复杂对象
     invokeJs.exam1(city, new IJavaCallback2JS{
                @JavaCallback4JS
                public void callback(@ParamResponseStatus("msg")String statusMsg,@Param("msg") String msg) {

                }
      });

#总结
SimpleJavaJsBridge库在js与java的通信中带来以下优点：
- js代码中不再有由于系统或者app版本甚至业务原因产生的if else if的兼容语句
- java不需要再关心数据封装为json或者从json中解析数据的繁琐工作
- 让js与java之间的通信更简单

若你动心了可以下载试用:[SimpleJavaJsBridge](https://github.com/niuxiaowei/SimpleJavaJsBridge.git)

参考：大头鬼的[https://github.com/lzyzsd/JsBridge.git](https://github.com/lzyzsd/JsBridge.git)

