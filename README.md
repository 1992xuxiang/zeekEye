# zeekEye
##-- Programmable spidering of web sites with Java

@(网络爬虫)[新浪微博|数据分析|帮助]

**zeekEye**是一款新浪微博爬虫，采用`Java`语言开发，基于`hetrix`爬虫架构,使用`HTTPClient4.0`和`Apache4.0`网络包.

特点概述：

- **数据存储**：采用`SQL Server`数据库存储数据，支持多线程并发操作.

- **功能实现**：模拟微博登录、爬取微博用户信息、用户评论、提取数据、建立数据表、数据成份分析、互粉推荐。待更新... 

欢迎Fork ! 

-------------------

## 安装

``` python
  git clone git@github.com:crazyacking/Spider--Java.git
  cd Spider--Java
```
默认编辑器是IntelliJ IDEA 14.1.4，Eclipse也能完美运行.
## API(如何使用)
### Creating a Spider
``` python
  var spider = require('spider');
  var s = spider();
```

#### weibo-Spider(选项)

"选项"包含以下字段：
* `maxSockets` - 线程池中最大并行线程数. 默认为 `4`.
* `userAgent` - 发送到远程服务器的用户代理请求. 默认为 `Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; en-US) AppleWebKit/534.7 (KHTML, like Gecko) Chrome/7.0.517.41 Safari/534.7` (firefox userAgent String).
* `cache` -  缓存对象。默认为非缓存，具体看最新版本代码缓存对象的实现细节.
* `pool` - 一个包含该请求代理的哈希线程池。如果省略，将使用全局设置的maxsockets.

### 添加路由处理程序

#### spider.route(主机，模式)
其中参数如下 :

* `hosts` - A string -- or an array of string -- representing the `host` part of the targeted URL(s).
* `pattern` - The pattern against which spider tries to match the remaining (`pathname` + `search` + `hash`) of the URL(s).
* `cb` - A function of the form `function(window, $)` where
  * `this` - Will be a variable referencing the `Routes.match` return object/value with some other goodies added from spider. For more info see http://www.cnblogs.com/crazyacking/category/686354.html
  * `window` - Will be a variable referencing the document's window.
  * `$` - Will be the variable referencing the jQuery Object.

### 爬虫抓取url队列.

`spider.get(url)`其中'url'是要抓取的网络url.

### 拓展 / 更新缓存

目前更新缓存暂提供以下方法:

* `get(url, cb)` - Returns `url`'s `body` field via the `cb` callback/continuation if it exists. Returns `null` otherwise.
  * `cb` - Must be of the form `function(retval) {...}'
* `getHeaders(url, cb)` - Returns `url`'s `headers` field via the `cb` callback/continuation if it exists. Returns `null` otherwise.
  * `cb` - Must be of the form `function(retval) {...}`
* `set(url, headers, body)` - Sets/Saves `url`'s `headers` and `body` in the cache.

### 设置冗余/日志级别
`spider.log(level)` - Where `level` is a string that can be any of `"debug"`, `"info"`, `"error"`

###Source Code
The source code of Spider Queen is made available for study purposes only. Neither it, its source code, nor its byte code may be modified and recompiled for public use by anyone except us.

We do accept and encourage private modifications with the intent for said modifications to be added to the official public version.


## 反馈与建议
- 微博：[@crazyacking](http://weibo.com/u/3736544454)，[@GGock](http://weibo.com/ggock "crazyacking")
- 邮箱：<crazyacking@gmail.com>

---------
感谢阅读这份帮助文档。如果你有好的建议，欢迎反馈。
