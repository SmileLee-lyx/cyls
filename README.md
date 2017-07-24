# Cyls
### 简介
一个基于[smartqq API(ScienJus)][smartqq]的机器人。

### 语言
- 本程序由kotlin编写。<br>
- 为了与程序的主体部分兼容，将 [com.scienjus.smartqq] 的所有内容转为了kotlin。

### 功能
- 云裂为QQ自动回复机器人，不具备学习能力。
- 由于不存在获取好友或群成员的QQ号的渠道，
云裂目前通过添加好友并设置备注名的方法来区分不同人。
这决定了云裂目前暂不回复非好友的其他群成员。
- 云裂目前实现的功能包括在少量关键字触发下自动回复，
通过一些预设的命令来进行控制。

### 使用注意事项
在项目根目录下新建文件夹cylsData，并新建两个文件：

```properties
cylsInfo.properties
index=0
```
这个文件用于记录生成的chattingLog的编号。
将在 cylsData/ 下生成 chattingLog_\[index].txt ，
并在文件中记录收到的消息和做出的回复。每次启动云裂后，
index自动增加1。
```properties
# owner.properties
owner=[owner's mark name]
```

[smartqq]: https://github.com/ScienJus/smartqq
[com.scienjus.smartqq]: https://github.com/ThomasVadeSmileLee/cyls/tree/master/src/main/kotlin/com/scienjus/smartqq
