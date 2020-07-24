##  此demo主要用来测试正式环境使用

[TOC]

###  1. 运行指南：

Android Studio：import  project（gradle ，and ant ）,下载依赖包，build and run 

APP：左侧点击扫描按钮，扫描对应配置的二维码，配置二维码地址：[http://doc.oa.zego.im/tool/QrcodeConfig](http://doc.oa.zego.im/tool/QrcodeConfig)，标题一栏可输入 隔离域名。

###  2. 注意事项：

如果没有配置CDN，或者单流不转退CDN的，需要勾上复选框，观众优先从UDP拉流。

###  解释：
此demo并没有对各类资源进行回收销毁的逻辑，只用来做基础推拉流的正式环境测试验证，无需过度关注细节逻辑，基础功能是可支撑的。