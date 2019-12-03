# sofa-dashboard

任务调度程序

分管理，控制，调度三个部分，任务脚本支持groovy

- 管理器 sofa-admin-bootstrap

主要内容包括: 用户管理，角色管理

- 控制器 sofa-jobs-bootstrap

主要内容包括: 任务组管理，任务管理，任务调度，状态检测


- 执行器 sofa-jobs-agent-bootstrap

主要内容包括: 任务执行

# 主要技术

- alipay-sofa
- spring boot
- mongo
- zookeeper

# 主要亮点

- 分布式调度，分管理，调度，执行
- 执行器会使用单独的进行执行，放弃相互影响
- 支持脚本groovy在线编辑执行
- 任务异常检测，自动终止
- 逐步增加工具类，ftp，email


# 参考其他开源项目

- ruoyi 非常棒的管理平台
- xxl-job 任务调度
