花了三天时间做了一个`简易聊天室`，主要是为了完成某项作业，其次再次熟悉下相关的API，其实主要用到的只是就是`swing`包下的控件集类、`Socket`相关编程基础、简单的`IO`流，多线程相关知识。

运行环境：</br>
jdk 1.8
eclipse 

**目录：**


<a href="#first_function">1.功能<a/></br>
<a href="#zero_think">2.实现思路</a></br>
<a href="#second_process_pic">3.流程图<a/></br>
<a href="#third_msg_inter">4.数据交互<a/></br>
<a href="#four_run_snapshot">5.运行截图<a/></br>
<a href="#code_download">6.代码下载<a/></br>
<a href="#summary">7.BUG<a/></br>
	
<a id="first_function">1.功能<a/>

- 服务端
    1.	实时监测客户端连接
	2.	刷新在线用户
	3.	用户下线通知
	4.	通知客户端在线用户列表
	5.	数据转发，包括单聊数据和群聊数据

- 客户端
    1.	实时刷新在线用户
	2.	发送单聊数据和群聊数据

<a id="zero_think">2.实现思路</a>

首先服务端在某一端口跑起来，无限的接收客户端的连接，客户端连接客户端时将自己的用户名和IP（服务器获取到转发给其他客户端以实现在线用户的实时刷新），此时服务端为将每一个连上的客户端放到集合中，并为每一个客户端单开一个线程以实现和此客户端的数据交互（服务端接收发送的数据并转发），客户端也要开启线程随时接受服务器转发的消息。这样就实现可客户端和服务器的交互。


<a id="second_process_pic">3.流程图<a/>


- 1.服务端

<img src="http://img.blog.csdn.net/20160530201628298" width="1080px"/>

- 2.客户端

<img src="http://img.blog.csdn.net/20160530201813487" width="1080px"/>

<a id="third_msg_inter">4.数据交互<a/></br>
<img src="http://img.blog.csdn.net/20160530202045737" width="700px"/>

<a id="four_run_snapshot">5.运行截图<a/>

- 1.客户端截图

![这里写图片描述](http://img.blog.csdn.net/20160530202252957)
![这里写图片描述](http://img.blog.csdn.net/20160530202318707)
![这里写图片描述](http://img.blog.csdn.net/20160530202332693)
![这里写图片描述](http://img.blog.csdn.net/20160530202422976)

- 2.服务端截图

![这里写图片描述](http://img.blog.csdn.net/20160530202350913)
![这里写图片描述](http://img.blog.csdn.net/20160530202414130)

<a id="code_download">6.代码下载<a/>
https://github.com/tongsdroid/LAN_Online_Chat，
代码写得有点乱，怕没人会有看下去的勇气，所以不敢贴出来，不过还是比较简单的，整个项目服务端和客户端是分开的两个程序，注释也比较详细，加上小学英文应该差不多能理解了

欢迎下载交流，我的邮箱:tongsdroid@gmail.com

<a id="summary">7.BUG<a/>

由于知识的深度不够，所以bug其实还是很严重，虽然跑起来没差

1.代码写得比较乱，可能还是写的太少了，不过后期还会重构的。</br>
2.读取流和输出流用完后不太会关闭，导致运行时异常</br>
3.客户端断开连接，虽然知道要关闭IO流，关闭服务端对应的线程，关闭socket连接，但是实际操作起来总是处在一种拆东墙补西墙的处境，做好这个那边报异常，做好那个这边又报异常</br>
4.服务端断开连接，所有的socket要断开，IO流也要关闭</br>



