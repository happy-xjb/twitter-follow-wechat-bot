# Twitter WeChat Bot

## 介绍

这个项目是一个用于监控 Twitter 用户关注列表，并将新增关注的用户信息发送到企业微信群的机器人。

## 功能特性

- 监控指定的 Twitter 用户的关注列表
- 定时检查新增关注的用户，并发送通知到企业微信群
- 提供详细的新增关注用户信息，包括用户名、名称、推特链接、粉丝数和简介

## 如何使用

### 1. 获取 Twitter API 凭证

在使用前，需要注册 Twitter 开发者账号，并创建一个新的应用程序来获取 API 凭证。

### 2. 配置应用程序

将您的 Twitter API 凭证和企业微信机器人的 Webhook URL 配置到 `twitterWeChatBot.properties` 文件中。确保文件位于项目的 `resources` 目录下。

示例 `twitterWeChatBot.properties` 文件：

Twitter API 认证信息
twitter.consumerKey=your_consumer_key
twitter.consumerSecret=your_consumer_secret
twitter.accessToken=your_access_token
twitter.accessTokenSecret=your_access_token_secret

推特用户名列表，多个用户名用逗号分隔
twitter.usernames=user1,user2,user3

企业微信机器人的 Webhook URL
wechat.webhookUrl=https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=your_webhook_key


### 3. 运行程序

在项目根目录下运行主类 `TwitterWeChatBot`，程序将定时检查 Twitter 用户的关注列表，并在有新的关注时发送通知到企业微信群。
