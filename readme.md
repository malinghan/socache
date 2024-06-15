### 整体设计思路
![设计思路](socache设计思路.png)
### 版本迭代计划

**v1.0 处理`redis-cli`输入输出** 
- 搭建netty server，监听6379端口，获取redis-cli输入,内部处理输入输出
