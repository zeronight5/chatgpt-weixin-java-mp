# 简介

接入了ChatGPT的微信订阅号后台，通过 [ChatGPT](https://platform.openai.com/docs/api-reference/chat) 接口生成对话内容，使用 [WxJava](https://github.com/Wechat-Group/WxJava) 实现微信订阅号消息的接收和回复，使用[Azure认知服务](https://learn.microsoft.com/zh-cn/azure/cognitive-services/speech-service/)进行文字转语音。已实现的特性如下：

- **文本对话** 关键词触发ChatGPT功能，使用ChatGPT生成回复内容，完成自动回复。
- **语音对话** 支持用户语音输入，并用ChatGPT生成回复后用Azure语音合成功能回复语音。
- **上下文记忆** 支持多轮对话记忆，且为每个用户维护独立的上下会话。

# 更新日志

>**2023.03.04：** 完成初步功能

## 配置

配置文件[application.yaml](src/main/resources/application.yaml)中，需要修改对应的服务的配置信息。
