import okhttp3.*;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class TwitterWeChatBot {

    private static Map<String, Set<Long>> currentFollowingsMap = new HashMap<>();

    public static void main(String[] args) throws IOException, TwitterException {
        // 从属性文件加载配置信息
        // 从 resources 目录中加载属性文件
        InputStream inputStream = TwitterWeChatBot.class.getClassLoader().getResourceAsStream("twitterWeChatBot.properties");
        Properties properties = new Properties();
        properties.load(inputStream);

        // 初始化 Twitter 客户端
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(properties.getProperty("twitter.consumerKey"))
                .setOAuthConsumerSecret(properties.getProperty("twitter.consumerSecret"))
                .setOAuthAccessToken(properties.getProperty("twitter.accessToken"))
                .setOAuthAccessTokenSecret(properties.getProperty("twitter.accessTokenSecret"));
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();

        // 获取推特用户名列表
        List<String> usernames = Arrays.asList(properties.getProperty("twitter.usernames").split(","));

        // 企业微信机器人的 Webhook URL
        String webhookUrl = properties.getProperty("wechat.webhookUrl");

        // 定时任务：每隔一小时检查一次新的关注
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    for (String username : usernames) {
                        Set<Long> currentFollowings = getCurrentFollowings(twitter, username);

                        // 获取之前保存的关注列表
                        Set<Long> previousFollowings = currentFollowingsMap.getOrDefault(username, new HashSet<>());

                        // 比较新的关注列表和之前保存的列表，找出新增关注用户
                        Set<Long> addedFollowings = new HashSet<>(currentFollowings);
                        addedFollowings.removeAll(previousFollowings);

                        // 发送新增关注用户消息到企业微信群聊
                        for (Long userId : addedFollowings) {
                            String userInfo = getUserInfo(twitter, username, userId);
                            sendWeChatMessage(webhookUrl, userInfo);
                        }

                        // 保存当前关注列表
                        currentFollowingsMap.put(username, currentFollowings);
                    }
                } catch (TwitterException | IOException e) {
                    e.printStackTrace();
                }
            }
        };

        // 设置定时任务：每隔一小时执行一次
        Timer timer = new Timer();
        timer.schedule(task, 0, 3600000);
    }

    // 获取当前关注列表
    private static Set<Long> getCurrentFollowings(Twitter twitter, String username) throws TwitterException {
        Set<Long> followings = new HashSet<>();
        long cursor = -1;
        IDs ids;
        do {
            ids = twitter.getFriendsIDs(username, cursor);
            for (long id : ids.getIDs()) {
                followings.add(id);
            }
        } while ((cursor = ids.getNextCursor()) != 0);
        return followings;
    }

    // 获取用户信息
    private static String getUserInfo(Twitter twitter, String username, long userId) throws TwitterException {
        StringBuilder userInfo = new StringBuilder();
        User user = twitter.showUser(userId);
        userInfo.append("用户 ").append(username).append(" 新增关注用户：\n");
        userInfo.append("推特用户名：@").append(user.getScreenName()).append("\n");
        userInfo.append("用户名称：").append(user.getName()).append("\n");
        userInfo.append("推特链接：https://twitter.com/").append(user.getScreenName()).append("\n");
        userInfo.append("粉丝数：").append(user.getFollowersCount()).append("\n");
        userInfo.append("简介信息：").append(user.getDescription()).append("\n\n");
        return userInfo.toString();
    }

    // 发送消息到企业微信
    public static void sendWeChatMessage(String webhookUrl, String message) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // 构建请求体
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String jsonMessage = String.format("{\"msgtype\": \"text\", \"text\": {\"content\": \"%s\"}}", message);
        RequestBody body = RequestBody.create(JSON, jsonMessage);

        // 创建请求
        Request request = new Request.Builder()
                .url(webhookUrl)
                .post(body)
                .build();

        // 发送请求并处理响应
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            System.out.println("Message sent successfully!");
        } else {
            System.err.println("Failed to send message: " + response.body().string());
        }
    }
}
