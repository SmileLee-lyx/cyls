package org._2333.cyls;

import com.alibaba.fastjson.*;
import com.qidou.dmp.tools.*;
import com.scienjus.smartqq.callback.*;
import com.scienjus.smartqq.client.*;
import com.scienjus.smartqq.model.*;
import org._2333.cyls.cyls.*;
import org.ansj.splitWord.analysis.*;
import org.jsoup.*;
import org.mariuszgromada.math.mxparser.*;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import static java.lang.Thread.*;

/**
 * @author 2333
 */
public class Cyls {

    //刷屏模式，不断回复
    private final boolean floodTheScreen = false;

    private Boolean isPaused = false;

    private Map<Long, Long> qqFromUid = new HashMap<>();
    private List<cylsGroup> groupList = new ArrayList<>();
    private List<cylsFriend> friendList = new ArrayList<>();
    private Map<Long, Integer> groupIndexFromId = new HashMap<>();
    private Map<Long, Integer> friendIndexFromQQ = new HashMap<>();

    private Integer groupIndexNow = null;
    private Integer friendIndexNow = null;
    private String contentNow = null;
    private int repeatCountNow = 0;

    private final String weatherKey = "3511aebb46e04a59b77da9b1c648c398"; //天气查询密钥
    private final String groupOnDevice = ".*"; //所响应的群 正则表达式

    public List<cylsGroup> getGroupList() {
        return groupList;
    }

    private void setup() {
        for (int i = 0; i < friendList.size(); i++) {
            qqFromUid.put(friendList.get(i).getUid(), friendList.get(i).getQQ());
            friendIndexFromQQ.put(friendList.get(i).getQQ(), i);
        }

        //获取好友列表并检查未读到的好友信息
        List<Friend> friends = client.getFriendList();
        for (Friend friend : friends) {
            if (!friendIndexFromQQ.containsKey(client.getQQById(friend.getUserId()))) {
                long uid = friend.getUserId();
                long qq = client.getQQById(uid);
                friendList.add(new cylsFriend(qq, uid, friend.getNickname(), 0, 0, true, client));
                qqFromUid.put(uid, qq);
                friendIndexFromQQ.put(qq, friendList.size() - 1);
            }
        }

        //获取群列表并储存群信息
        List<Group> groups = client.getGroupList();
        for (Group group : groups) {
            groupList.add(new cylsGroup(group.getId(), group.getName(), group.getCode(), client));
            groupIndexFromId.put(group.getId(), groupList.size() - 1);
        }
    }

    /**
     * 读取信息
     */
    private void save() {
        try {
            JSONObject obj = new JSONObject();
            JSONArray friends = new JSONArray();
            for (cylsFriend cylsfriend : friendList) {
                JSONObject friend = new JSONObject();
                friend.put("qq", cylsfriend.getQQ());
                friend.put("name", cylsfriend.getName());
                friend.put("adminLevel", cylsfriend.getAdminLevel());
                friend.put("ignoreLevel", cylsfriend.getIgnoreLevel());
                friend.put("friend", cylsfriend.isFriend());
                friends.add(JSON.parseObject(JSON.toJSONString(friend)));
            }
            obj.put("friends", friends);

            FileOutputStream out = new FileOutputStream(new File("SavedFile.txt"));//文件地址
            out.write(JsonFormatTool.formatJson(obj.toString()).getBytes());
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    /**
     * 保存信息
     */
    private void load() {
        try {
            LineNumberReader in = new LineNumberReader(new InputStreamReader(new FileInputStream("SavedFile.txt")));//文件地址
            String json = "";
            String line;
            while ((line = in.readLine()) != null) {
                json = json + line;
            }
            JSONObject obj = JSON.parseObject(json);
            JSONArray friends = obj.getJSONArray("friends");
            for (int i = 0; i < friends.size(); i++) {
                JSONObject friend = friends.getJSONObject(i);
                long qq = friend.getLongValue("qq");
                String name = friend.getString("name");
                int adminLevel = friend.getIntValue("adminLevel");
                int ignoreLevel = friend.getIntValue("ignorelevel");
                boolean isfriend = friend.getBooleanValue("friend");
                friendList.add(new cylsFriend(qq, 0, name, adminLevel, ignoreLevel, isfriend, client));
            }
            Collections.sort(friendList);
            for (int i = 0; i < friendIndexFromQQ.size(); i++) {
                friendIndexFromQQ.put(friendList.get(i).getQQ(), i);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void update() {

    }

    /**
     * @param x 范围：0~(x-1)
     * @return 一个随机整数
     */
    private int randomInt(int x) {
        return (int) (Math.random() * x);
    }

    /**
     */
    private void DoNothing() {
        //什么都不干
    }

    private String getSystemTime() {
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return time.format(new Date());
    }

    private long groupIdFromIndex(int index) {
        return groupList.get(index).getId();
    }

    /**
     * @param index 被查询的成员的index
     * @return 该成员是否为主人
     */
    private boolean isOwner(int index) {
        return friendList.get(index).isOwner();
    }

    /**
     * @param index 被查询的成员的index
     * @return 该成员是否为管理
     */
    private boolean isAdmin(int index) {
        return friendList.get(index).isAdmin();
    }

    /**
     * @param index 被查询的成员的index
     * @return 该成员是否被忽略
     */
    private boolean isIgnored(int index) {
        return friendList.get(index).isIgnored();
    }

    /**
     * @param index 被查询的成员的index
     * @return 该成员是否被忽略
     */
    private boolean isBanned(int index) {
        return friendList.get(index).isBanned();
    }

    /**
     * @param index 被设为管理的成员inxed
     */
    private void authorize(int index) {
        friendList.get(index).authorize();
    }

    /**
     * @param index 被取消管理的成员index
     */
    private void unAuthorize(int index) {
        friendList.get(index).unauthorize();
    }

    /**
     * @param index 被屏蔽的成员index
     */
    private void ignore(int index) {
        friendList.get(index).ignore();
    }

    /**
     * @param index 被取消屏蔽的成员QQ
     */
    private void recognize(int index) {
        friendList.get(index).recognize();
    }

    /**
     * @param index 被完全屏蔽的成员index
     */
    private void ban(int index) {
        friendList.get(index).ban();
    }

    /**
     * @param index 被取消完全屏蔽的成员index
     */
    private void unBan(int index) {
        friendList.get(index).unban();
    }

    /**
     * //@param index 回复的群index
     * //@param chance 回复的概率
     * //@param content 发送的内容
     * //@param contents 发送的内容数组
     */
    private void reply(int index, String content) {
        System.out.println("[" + getSystemTime() + "]>" + content);
        client.sendMessageToGroup(groupIdFromIndex(index), content);
    }

    private void replyByChance(int index, double chance, String content) {
        if (Math.random() < chance) {
            reply(index, content);
        }
    }

    private void replyRandomly(int index, String... contents) {
        reply(index, contents[randomInt(contents.length)]);
    }

    private void replyRandomly(int index, List<String> contents) {
        reply(index, contents.get(randomInt(contents.size())));
    }

    private void replyRandomlyByChance(int index, double chance, String... contents) {
        if (Math.random() < chance) {
            replyRandomly(index, contents);
        }
    }

    private void replyRandomlyByChance(int index, double chance, List<String> contents) {
        if (Math.random() < chance) {
            replyRandomly(index, contents);
        }
    }

    private long QQFromUid(long uid) {
        if (!qqFromUid.containsKey(uid)) {
            long qq = client.getQQById(uid);
            qqFromUid.put(uid, qq);
            int index = friendIndexFromQQ.get(qq);
            friendList.get(index).setUid(uid);
        }
        return qqFromUid.get(uid);
    }

    /**
     * @param cityName 查询的城市名
     * @param d        0=今天 1=明天 2=后天
     */
    private void getWeather(String cityName, int d) throws InterruptedException {
        cityName = cityName.replaceAll(" |　|\t|\n", "");
        if (cityName.equals("")) {
            reply(groupIndexNow, "请输入城市名称进行查询哦|•ω•`)");
        } else {
            String[] days = {"今天", "明天", "后天"};
            String msg = "云裂天气查询服务|•ω•`)\n";
            msg = msg + "下面查询" + cityName + days[d] + "的天气——";
            reply(groupIndexNow, msg);
            String web = "https://free-api.heweather.com/v5/forecast?city=" + cityName + "&key=" + weatherKey;
            String result = WebUtil.request(web, null, "GET");
            if (result == null) {
                reply(groupIndexNow, "啊呀，真抱歉，查询失败的说，请确认这个地名是国内的城市名……|•ω•`)");
            } else {
                JSONObject weather = JSON.parseObject(result);
                JSONArray something = weather.getJSONArray("HeWeather5");
                JSONObject anotherThing = something.getJSONObject(0);
                JSONObject basic = anotherThing.getJSONObject("basic");
                if (basic == null) {
                    reply(groupIndexNow, "啊呀，真抱歉，查询失败的说，请确认这个地名是国内的城市名……|•ω•`)");
                } else {
                    sleep(1000);
                    JSONArray forecast = anotherThing.getJSONArray("daily_forecast");
                    JSONObject day = forecast.getJSONObject(d);
                    JSONObject cond = day.getJSONObject("cond");
                    if (cond.getString("txt_d").equals(cond.getString("txt_n"))) {
                        msg = "全天" + cond.getString("txt_d") + ",";
                    } else {
                        msg = "白天" + cond.getString("txt_d") + "，夜晚" + cond.getString("txt_n") + "，";
                    }
                    JSONObject tmp = day.getJSONObject("tmp");
                    msg = msg + "最高温与最低温为" + tmp.getString("max") + "℃和" + tmp.getString("min") + "℃，\n";
                    JSONObject wind = day.getJSONObject("wind");
                    msg = msg + wind.getString("dir") + wind.getString("sc") + "级|•ω•`)";
                    reply(groupIndexNow, msg);
                }
            }
        }
    }

    private void checkGroupUser(long uid, int index) {
        if (!qqFromUid.containsKey(uid)) {
            long qq = client.getQQById(uid);
            qqFromUid.put(uid, qq);
            if (!friendIndexFromQQ.containsKey(qq)) {
                List<GroupUser> groupUsers = groupList.get(index).getGroupInfo().getUsers();
                for (GroupUser groupUser : groupUsers) {
                    if (groupUser.getUin() == uid) {
                        String name = groupUser.getNick();
                        friendList.add(new cylsFriend(qq, uid, name, 0, 0, false, client));
                        friendIndexFromQQ.put(qq, friendList.size() - 1);
                        break;
                    }
                }
            }
        }
    }

    private void checkGroup(int index) {
        List<GroupUser> groupUsers = groupList.get(index).getGroupInfo().getUsers();
        for (GroupUser groupUser : groupUsers) {
            checkGroupUser(groupUser.getUin(), index);
        }
    }

    private void checkGroupUserQQ(long qq, int index) {
        if (!friendIndexFromQQ.containsKey(qq)) {
            checkGroup(index);
        }
    }

    private String getFriendName(Integer index) {
        try {
            return friendList.get(index).getName();
        } catch (Exception e) {
            return "系统消息";
        }
    }

    private String getGroupName(int index) {
        return groupList.get(index).getName();
    }

    /**
     * 指令树的节点
     * 作用：当输入指令时，从root节点开始查找
     * 执行找到的节点的run函数
     * 以响应指令
     */
    private TreeNode root = new TreeNode("root", null) {
        @Override
        public void run(String str) {
        }
    };
    private TreeNode cyls = new TreeNode("cyls", root) {
        @Override
        public void run(String str) {
            reply(groupIndexNow, "输入cyls.help查看帮助 |•ω•`)");
        }
    };
    private TreeNode cylsSudo = new TreeNode("sudo", cyls) {
        @Override
        public void run(String str) {
            reply(groupIndexNow, "输入cyls.help.sudo查看你的权利哦 |•ω•`)");
        }
    };
    private TreeNode cylsSudoBan = new TreeNode("ban", cylsSudo) {
        @Override
        public void run(String str) {
            long qq = Long.parseLong(str);
            checkGroupUserQQ(qq, groupIndexNow);
            int index = friendIndexFromQQ.get(qq);
            if (isOwner(friendIndexNow) && !isOwner(index)) {
                ban(index);
                reply(groupIndexNow, getFriendName(index) + "已被屏蔽 |•ω•`)");
                save();
            } else {
                reply(groupIndexNow, "你的权限不足哦");
                reply(groupIndexNow, "不如输入cyls.sudo.test查看你的权限吧|•ω•`)");
            }
        }
    };
    private TreeNode cylsSudoUnban = new TreeNode("unban", cylsSudo) {
        @Override
        public void run(String str) {
            long qq = Long.parseLong(str);
            checkGroupUserQQ(qq, groupIndexNow);
            int index = friendIndexFromQQ.get(qq);
            if (isOwner(friendIndexNow)) {
                unBan(index);
                reply(groupIndexNow, "" + getFriendName(index) + "已被解除彻底屏蔽|•ω•`)");
                save();
            } else {
                reply(groupIndexNow, "你的权限不足哦");
                reply(groupIndexNow, "不如输入cyls.sudo.test查看你的权限吧|•ω•`)");
            }
        }
    };
    private TreeNode cylsSudoIgnore = new TreeNode("ignore", cylsSudo) {
        @Override
        public void run(String str) {
            long qq = Long.parseLong(str);
            checkGroupUserQQ(qq, groupIndexNow);
            int index = friendIndexFromQQ.get(qq);
            if (isOwner(friendIndexNow)) {
                ignore(index);
                reply(groupIndexNow, "" + getFriendName(index) + "已被屏蔽，然而这么做是不是不太好…… |•ω•`)");
                save();
            } else if (isAdmin(friendIndexNow) && !isAdmin(index)) {
                ignore(index);
                reply(groupIndexNow, "" + getFriendName(index) + "已被屏蔽，然而这么做是不是不太好…… |•ω•`)");
                save();
            } else {
                reply(groupIndexNow, "你的权限不足哦");
                reply(groupIndexNow, "不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)");
            }
        }
    };
    private TreeNode cylsSudoRecognize = new TreeNode("recognize", cylsSudo) {
        @Override
        public void run(String str) {
            long qq = Long.parseLong(str);
            checkGroupUserQQ(qq, groupIndexNow);
            int index = friendIndexFromQQ.get(qq);
            if (isOwner(friendIndexNow)) {
                recognize(index);
                reply(groupIndexNow, "" + getFriendName(index) + "已被解除屏蔽，当初为什么要屏蔽他呢…… |•ω•`)");
                save();
            } else if (isAdmin(friendIndexNow) && !isAdmin(index)) {
                recognize(index);
                reply(groupIndexNow, "" + getFriendName(index) + "已被解除屏蔽，当初为什么要屏蔽他呢…… |•ω•`)");
                save();
            } else {
                reply(groupIndexNow, "你的权限不足哦");
                reply(groupIndexNow, "不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)");
            }
        }
    };
    private TreeNode cyslSudoAuthorize = new TreeNode("authorize", cylsSudo) {
        @Override
        public void run(String str) {
            long qq = Long.parseLong(str);
            checkGroupUserQQ(qq, groupIndexNow);
            int index = friendIndexFromQQ.get(qq);
            if (isOwner(friendIndexNow)) {
                if (!isAdmin(index)) {
                    authorize(index);
                    reply(groupIndexNow, "" + getFriendName(index) + "已被设置为管理员啦 |•ω•`)");
                    save();
                } else {
                    reply(groupIndexNow, "" + getFriendName(index) + "已经是管理员了，" +
                            "再设置一次有什么好处么…… |•ω•`)");
                }
            } else {
                reply(groupIndexNow, "你的权限不足哦");
                reply(groupIndexNow, "不如输入cyls.sudo.test查看你的权限吧|･ω･｀)");
            }
        }
    };
    private TreeNode cylsSudoUnauthorize = new TreeNode("unauthorize", cylsSudo) {
        @Override
        public void run(String str) {
            long qq = Long.parseLong(str);
            checkGroupUserQQ(qq, groupIndexNow);
            int index = friendIndexFromQQ.get(qq);
            if (isOwner(friendIndexNow)) {
                if (isOwner(index)) {
                    reply(groupIndexNow, "" + getFriendName(index) + "是云裂的主人哦，" +
                            "不能被取消管理员身份…… |•ω•`)");
                } else if (isAdmin(index)) {
                    unAuthorize(index);
                    reply(groupIndexNow, "" + getFriendName(index) + "已被取消管理员身份……" +
                            "不过，真的要这样么 |•ω•`)");
                    save();
                } else {
                    reply(groupIndexNow, "" + getFriendName(index) + "并不是管理员啊，" +
                            "主人你是怎么想到这么做的啊…… |•ω•`)");
                }
            } else {
                reply(groupIndexNow, "你的权限不足哦");
                reply(groupIndexNow, "不如输入cyls.sudo.test查看你的权限吧 |•ω•`)");
            }
        }
    };
    private TreeNode cylsSudoPause = new TreeNode("pause", cylsSudo) {
        @Override
        public void run(String str) {
            if (isAdmin(friendIndexNow)) {
                if (!isPaused) {
                    System.out.println(2333);
                    reply(groupIndexNow, "通讯已中断（逃 |•ω•`)");
                    isPaused = true;
                } else {
                    reply(groupIndexNow, "已处于中断状态了啊……不能再中断一次了 |•ω•`)");
                }
            } else {
                reply(groupIndexNow, "你的权限不足哦");
                reply(groupIndexNow, "不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)");
            }
        }
    };
    private TreeNode cylsSudoResume = new TreeNode("resume", cylsSudo) {
        @Override
        public void run(String str) {
            if (isAdmin(friendIndexNow)) {
                if (isPaused) {
                    reply(groupIndexNow, "通讯恢复啦 |•ω•`)");
                    isPaused = false;
                } else {
                    reply(groupIndexNow, "通讯并没有中断啊，为什么要恢复呢 |•ω•`)");
                }
            } else {
                reply(groupIndexNow, "你的权限不足哦");
                reply(groupIndexNow, "不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)");
            }
        }
    };
    private TreeNode cylsSudoTest = new TreeNode("test", cylsSudo) {
        @Override
        public void run(String str) {
            if (isOwner(friendIndexNow)) {
                reply(groupIndexNow, "你是云裂的主人哦|•ω•`)");
                reply(groupIndexNow, "输入cyls.help.sudo查看……说好的主人呢，为什么连自己的权利都不知道(╯‵□′)╯︵┴─┴");
            } else if (isAdmin(friendIndexNow)) {
                reply(groupIndexNow, "你是云裂的管理员呢|•ω•`)");
                reply(groupIndexNow, "输入cyls.help.sudo来查看你的权利哦|•ω•`)");
            } else {
                reply(groupIndexNow, "你暂时只是个普通成员呢……|•ω•`)");
                reply(groupIndexNow, "输入cyls.help.sudo来查看你的权利哦|•ω•`)");
            }
        }
    };
    private TreeNode cylsSudoCheck = new TreeNode("check", cylsSudo) {
        @Override
        public void run(String str) {
            reply(groupIndexNow, "自检完毕\n一切正常哦|･ω･｀)");
        }
    };
    private TreeNode cylsSudoSay = new TreeNode("say", cylsSudo) {
        @Override
        public void run(String str) {
            if (isAdmin(friendIndexNow)) {
                if (str.matches(".*(hubble|哈勃).*")) {
                    reply(groupIndexNow, "云裂拒绝说和哈勃有关的内容哦|•ω•`)");
                } else {
                    reply(groupIndexNow, str);
                }
            } else {
                reply(groupIndexNow, "你的权限不足哦");
                reply(groupIndexNow, "不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)");
            }
        }
    };
    private TreeNode cylsSudoRename = new TreeNode("rename", cylsSudo) {
        @Override
        public void run(String str) {
            reply(groupIndexNow, "你已将自己的备注名改为" + str + "|•ω•`)");
            friendList.get(friendIndexNow).setName(str);
            save();
        }
    };
    private TreeNode cylsSudoRenameOther = new TreeNode("other", cylsSudoRename) {
        @Override
        public void run(String str) {
            if (isOwner(friendIndexNow)) {
                int i = str.indexOf(" ");
                long qq = Long.parseLong(str.substring(0, i));
                checkGroupUserQQ(qq, groupIndexNow);
                String name = str.substring(i + 1);
                reply(groupIndexNow, "你已将" + qq + "的备注名改为" + name + "|•ω•`)");
                friendList.get(friendIndexFromQQ.get(qq)).setName(name);
                save();
            } else {
                reply(groupIndexNow, "你的权限不足哦");
                reply(groupIndexNow, "不如输入cyls.sudo.test查看你的权限吧 |•ω•`)");
            }
        }
    };
    private TreeNode cylsSudoUpdate = new TreeNode("update", cylsSudo) {
        @Override
        public void run(String str) {
            if (isAdmin(friendIndexNow)) {
                update();
                reply(groupIndexNow, "数据更新完毕 |•ω•`)");
            } else {
                reply(groupIndexNow, "你的权限不足哦");
                reply(groupIndexNow, "不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)");
            }
        }
    };
    private TreeNode cylsSudoSave = new TreeNode("save", cylsSudo) {
        @Override
        public void run(String str) {
            if (isOwner(friendIndexNow)) {
                save();
                reply(groupIndexNow, "已保存完毕|•ω•`)");
            } else {
                reply(groupIndexNow, "你的权限不足哦");
                reply(groupIndexNow, "不如输入cyls.sudo.test查看你的权限吧 |•ω•`)");
            }
        }
    };
    private TreeNode cylsSudoLoad = new TreeNode("load", cylsSudo) {
        @Override
        public void run(String str) {
            if (isOwner(friendIndexNow)) {
                load();
                update();
                reply(groupIndexNow, "已读取完毕|•ω•`)");
            } else {
                reply(groupIndexNow, "你的权限不足哦");
                reply(groupIndexNow, "不如输入cyls.sudo.test查看你的权限吧 |•ω•`)");
            }
        }
    };
    private TreeNode cylsSudoQuit = new TreeNode("quit", cylsSudo) {
        @Override
        public void run(String str) {
            if (isOwner(friendIndexNow)) {
                reply(groupIndexNow, "尝试关闭通讯中…… |•ω•`)");
                reply(groupIndexNow, "通讯已关闭，大家再……");
                save();
                System.exit(0);
            } else {
                reply(groupIndexNow, "你的权限不足哦");
                reply(groupIndexNow, "不如输入cyls.sudo.test查看你的权限吧 |•ω•`)");
            }
        }
    };
    private TreeNode cylsWeather = new TreeNode("weather", cyls) {
        @Override
        public void run(String str) throws InterruptedException {
            reply(groupIndexNow, "输入cyls.help.weather查看天气功能的帮助哦 |•ω•`)");
        }
    };
    private TreeNode cylsWeatherToday = new TreeNode("today", cylsWeather) {
        @Override
        public void run(String str) throws InterruptedException {
            getWeather(str, 0);
        }
    };
    private TreeNode cylsWeatherTomorrow = new TreeNode("tomorrow", cylsWeather) {
        @Override
        public void run(String str) throws InterruptedException {
            getWeather(str, 1);
        }
    };
    private TreeNode cylsWeatherDay0 = new TreeNode("day0", cylsWeather) {
        @Override
        public void run(String str) throws InterruptedException {
            getWeather(str, 0);
        }
    };
    private TreeNode cylsWeatherDay1 = new TreeNode("day1", cylsWeather) {
        @Override
        public void run(String str) throws InterruptedException {
            getWeather(str, 1);
        }
    };
    private TreeNode cylsWeatherDay2 = new TreeNode("day2", cylsWeather) {
        @Override
        public void run(String str) throws InterruptedException {
            getWeather(str, 2);
        }
    };
    private TreeNode cylsSong = new TreeNode("song", cyls) {
        @Override
        public void run(String str) throws Exception {
            reply(groupIndexNow, "输入cyls.help.song查看天气功能的帮助哦 |•ω•`)");
        }
    };
    private TreeNode cylsSongQuery = new TreeNode("query", cylsSong) {
        @Override
        public void run(String str) throws Exception {
            str = str.replace(' ', '+');
            String website = "http://music.163.com/api/search/get?s=" + str + "&type=1";

            Map<String, String> param = new HashMap<>();
            param.put("referer", "http://music.163.com");
            String s = WebUtil.request(website, param, "POST");
            JSONObject obj = JSON.parseObject(s);

            if (obj.getIntValue("code") == 200) {
                JSONArray songs = obj.getJSONObject("result").getJSONArray("songs");
                int total = songs.size();
                total = Math.min(total, 10);
                String msg = "共找到" + total + "首歌曲|•ω•`)\n";

                List<Long> songIds = new ArrayList<>();

                for (int i = 0; i < total; i++) {
                    JSONObject song = songs.getJSONObject(i);
                    String name = song.getString("name");
                    String artist = song.getJSONArray("artists").getJSONObject(0).getString("name");
                    msg = msg + "歌曲名：" + name + " 歌手名：" + artist + "\n";
                    songIds.add(song.getLong("id"));
                }

                groupList.get(groupIndexNow).setSongIds(songIds);

                msg = msg + "输入cyls.song.play t来播放第t首歌哦|•ω•`)";
                reply(groupIndexNow, msg);
            } else {
                reply(groupIndexNow, "未找到这首歌曲|•ω•`)");
            }
        }
    };
    private TreeNode cylsSongPlay = new TreeNode("play", cylsSong) {
        @Override
        public void run(String str) throws Exception {
            int index = Integer.parseInt(str) - 1;
            List<Long> songIds = groupList.get(groupIndexNow).getSongIds();
            if (songIds == null) {
                reply(groupIndexNow, "你还未查询过任何歌曲哦|•ω•`)");
            } else {
                if (index < 0 || index >= songIds.size()) {
                    reply(groupIndexNow, "你输入的歌曲序号不存在哦|•ω•`)");
                } else {
                    String website = "http://music.163.com/api/song/lyric/?id=" + songIds.get(index) + "&lv=1&kv=1&tv=1";
                    JSONObject obj = JSON.parseObject(Jsoup.connect(website).get().text());
                    String lyric = obj.getJSONObject("lrc").getString("lyric");
                    String[] words = lyric.replaceAll("\\[[^\\[\\]]*]", "").split("\n");
                    String msg = "";
                    int i = 0, count = 0;
                    while (count < 6 && i < words.length) {
                        if (!words[i].equals("")) {
                            msg = msg + words[i] + "\n";
                            ++count;
                        }
                        ++i;
                    }
                    msg = msg + "点击链接收听|•ω•`)：http://music.163.com/#/song?id=" + songIds.get(index);
                    reply(groupIndexNow, msg);
                }
            }
        }
    };
    private TreeNode cylsUtil = new TreeNode("util", cyls) {
        @Override
        public void run(String str) {
            reply(groupIndexNow, "输入cyls.help.util查看有关工具功能的帮助哦 |•ω•`)");
        }
    };
    private TreeNode cylsUtilMath = new TreeNode("math", cylsUtil) {
        @Override
        public void run(String str) {
            Expression e = new Expression(str);
            reply(groupIndexNow, "结果是： |•ω•`)\n" + (new Double(e.calculate())).toString());
        }
    };
    private TreeNode cylsUtilDice = new TreeNode("dice", cylsUtil) {
        @Override
        public void run(String str) {
            reply(groupIndexNow, "结果是： |•ω•`)\n" + (randomInt(6) + 1));
        }
    };
    private TreeNode cylsUtilQuery = new TreeNode("query", cylsUtil) {
        @Override
        public void run(String str) throws InterruptedException {
            checkGroup(groupIndexNow);
            if (!str.equals("")) {
                String msg = "";
                int count = 0;
                cylsGroup group = groupList.get(groupIndexNow);
                List<GroupUser> groupUsers = group.getGroupInfo().getUsers();
                group.setQueryResult(new ArrayList<String>());
                for (GroupUser groupUser : groupUsers) {
                    if (groupUser.getCard() != null && groupUser.getCard().contains(str)) {
                        msg = msg + groupUser.getCard() + " " + QQFromUid(groupUser.getUin());
                        ++count;
                        if (count % 10 == 0) {
                            msg = "下面是第" + (count - 9) + "至第" + count + "条结果|•ω•`)\n" + msg;
                            group.getQueryResult().add(msg);
                            msg = "";
                        } else {
                            msg = msg + "\n";
                        }
                    } else if (groupUser.getNick().contains(str)) {
                        msg = msg + groupUser.getNick() + " " + QQFromUid(groupUser.getUin());
                        ++count;
                        if (count % 10 == 0) {
                            msg = "下面是第" + (count - 9) + "至第" + count + "条结果|•ω•`)\n" + msg;
                            group.getQueryResult().add(msg);
                            msg = "";
                        } else {
                            msg = msg + "\n";
                        }
                    } else if (Long.valueOf(QQFromUid(groupUser.getUin())).toString().contains(str)) {
                        msg = msg + friendList.get(friendIndexFromQQ.get(qqFromUid.get(groupUser.getUin()))).getName()
                                + " " + QQFromUid(groupUser.getUin());
                        ++count;
                        if (count % 10 == 0) {
                            msg = "下面是第" + (count - 9) + "至第" + count + "条结果|•ω•`)\n" + msg;
                            group.getQueryResult().add(msg);
                            msg = "";
                        } else {
                            msg = msg + "\n";
                        }
                    }
                }
                if (count == 0) {
                    msg = "未查到这个人哦|•ω•`)";
                    group.setQueryIndex(null);
                    reply(groupIndexNow, msg);
                } else {
                    if (count % 10 != 0) {
                        if (count % 10 == 1) {
                            msg = "下面是第" + count + "条结果|•ω•`)\n" + msg;
                        } else {
                            msg = "下面是第" + (count - count % 10 + 1) + "至第" + count + "条结果|•ω•`)\n" + msg;
                        }
                        msg = msg.substring(0, msg.length() - 1);
                        group.getQueryResult().add(msg);
                    }
                    reply(groupIndexNow, "查询完毕|•ω•`)\n" +
                            "共有" + count + "人表示躺枪|•ω•`)");
                    sleep(1000);
                    reply(groupIndexNow, group.getQueryResult().get(0));
                    if (count > 10) {
                        reply(groupIndexNow, "查询结果未完全显示|•ω•`)" + "\n" +
                                "你可用过下面的命令来查看其余结果" + "\n" +
                                "查看下面10条结果：" + "\n" +
                                "cyls.util.query.next" + "\n" +
                                "查看上面10条结果：" + "\n" +
                                "cyls.util.query.previous " + "\n" +
                                "查看第t组结果：" + "\n" +
                                "cyls.util.query.index t ");
                    }
                    group.setQueryIndex(0);
                }
            }
        }
    };
    private TreeNode cylsUtilQueryName = new TreeNode("name", cylsUtilQuery) {
        @Override
        public void run(String str) throws InterruptedException {
            checkGroup(groupIndexNow);
            if (!str.equals("")) {
                String msg = "";
                int count = 0;
                cylsGroup group = groupList.get(groupIndexNow);
                List<GroupUser> groupUsers = group.getGroupInfo().getUsers();
                group.setQueryResult(new ArrayList<String>());
                for (GroupUser groupUser : groupUsers) {
                    if (groupUser.getCard() != null && groupUser.getCard().contains(str)) {
                        msg = msg + groupUser.getCard() + " " + QQFromUid(groupUser.getUin());
                        ++count;
                        if (count % 10 == 0) {
                            msg = "下面是第" + (count - 9) + "至第" + count + "条结果|•ω•`)\n" + msg;
                            group.getQueryResult().add(msg);
                            msg = "";
                        } else {
                            msg = msg + "\n";
                        }
                    } else if (groupUser.getNick().contains(str)) {
                        msg = msg + groupUser.getNick() + " " + QQFromUid(groupUser.getUin());
                        ++count;
                        if (count % 10 == 0) {
                            msg = "下面是第" + (count - 9) + "至第" + count + "条结果|•ω•`)\n" + msg;
                            group.getQueryResult().add(msg);
                            msg = "";
                        } else {
                            msg = msg + "\n";
                        }
                    }
                }
                if (count == 0) {
                    msg = "未查到这个人哦|•ω•`)";
                    group.setQueryIndex(null);
                    reply(groupIndexNow, msg);
                } else {
                    if (count % 10 != 0) {
                        if (count % 10 == 1) {
                            msg = "下面是第" + count + "条结果|•ω•`)\n" + msg;
                        } else {
                            msg = "下面是第" + (count - count % 10 + 1) + "至第" + count + "条结果|•ω•`)\n" + msg;
                        }
                        msg = msg.substring(0, msg.length() - 1);
                        group.getQueryResult().add(msg);
                    }
                    reply(groupIndexNow, "查询完毕|•ω•`)\n" +
                            "共有" + count + "人表示躺枪|•ω•`)");
                    sleep(1000);
                    reply(groupIndexNow, group.getQueryResult().get(0));
                    if (count > 10) {
                        reply(groupIndexNow, "查询结果未完全显示|•ω•`)" + "\n" +
                                "你可用过下面的命令来查看其余结果" + "\n" +
                                "查看下面10条结果：" + "\n" +
                                "cyls.util.query.next" + "\n" +
                                "查看上面10条结果：" + "\n" +
                                "cyls.util.query.previous " + "\n" +
                                "查看第t组结果：" + "\n" +
                                "cyls.util.query.index t ");
                    }
                    group.setQueryIndex(0);
                }
            }
        }
    };
    private TreeNode cylsUtilQueryQQ = new TreeNode("qq", cylsUtilQuery) {
        @Override
        public void run(String str) throws InterruptedException {
            checkGroup(groupIndexNow);
            if (!str.equals("")) {
                String msg = "";
                int count = 0;
                cylsGroup group = groupList.get(groupIndexNow);
                List<GroupUser> groupUsers = group.getGroupInfo().getUsers();
                group.setQueryResult(new ArrayList<String>());
                for (GroupUser groupUser : groupUsers) {
                    if (Long.valueOf(QQFromUid(groupUser.getUin())).toString().contains(str)) {
                        msg = msg + groupUser.getNick()
                                + " " + QQFromUid(groupUser.getUin());
                        ++count;
                        if (count % 10 == 0) {
                            msg = "下面是第" + (count - 9) + "至第" + count + "条结果|•ω•`)\n" + msg;
                            group.getQueryResult().add(msg);
                            msg = "";
                        } else {
                            msg = msg + "\n";
                        }
                    }
                }
                if (count == 0) {
                    msg = "未查到这个人哦|•ω•`)";
                    group.setQueryIndex(null);
                    reply(groupIndexNow, msg);
                } else {
                    if (count % 10 != 0) {
                        if (count % 10 == 1) {
                            msg = "下面是第" + count + "条结果|•ω•`)\n" + msg;
                        } else {
                            msg = "下面是第" + (count - count % 10 + 1) + "至第" + count + "条结果|•ω•`)\n" + msg;
                        }
                        msg = msg.substring(0, msg.length() - 1);
                        group.getQueryResult().add(msg);
                    }
                    reply(groupIndexNow, "查询完毕|•ω•`)\n" +
                            "共有" + count + "人表示躺枪|•ω•`)");
                    sleep(1000);
                    reply(groupIndexNow, group.getQueryResult().get(0));
                    if (count > 10) {
                        reply(groupIndexNow, "查询结果未完全显示|•ω•`)" + "\n" +
                                "你可用过下面的命令来查看其余结果" + "\n" +
                                "查看下面10条结果：" + "\n" +
                                "cyls.util.query.next" + "\n" +
                                "查看上面10条结果：" + "\n" +
                                "cyls.util.query.previous " + "\n" +
                                "查看第t组结果：" + "\n" +
                                "cyls.util.query.index t ");
                    }
                    group.setQueryIndex(0);
                }
            }
        }
    };
    private TreeNode cylsUtilQueryNext = new TreeNode("next", cylsUtilQuery) {
        @Override
        public void run(String str) {
            cylsGroup group = groupList.get(groupIndexNow);
            if (group.getQueryResult() == null) {
                reply(groupIndexNow, "没有结果可以显示哦|•ω•`)");
            } else {
                List<String> QueryResult = group.getQueryResult();
                if (group.getQueryIndex() + 1 < QueryResult.size()) {
                    group.setQueryIndex(group.getQueryIndex() + 1);
                    reply(groupIndexNow, QueryResult.get(group.getQueryIndex()));
                } else {
                    reply(groupIndexNow, "已经是最后几条记录了|•ω•`)");
                }
            }
        }
    };
    private TreeNode cylsUtilQueryPrevious = new TreeNode("previous", cylsUtilQuery) {
        @Override
        public void run(String str) {
            cylsGroup group = groupList.get(groupIndexNow);
            if (group.getQueryResult() == null) {
                reply(groupIndexNow, "没有结果可以显示哦|•ω•`)");
            } else {
                List<String> QueryResult = group.getQueryResult();
                if (group.getQueryIndex() - 1 >= 0) {
                    group.setQueryIndex(group.getQueryIndex() - 1);
                    reply(groupIndexNow, QueryResult.get(group.getQueryIndex()));
                } else {
                    reply(groupIndexNow, "已经是最前几条记录了|•ω•`)");
                }
            }
        }
    };
    private TreeNode cylsUtilQueryIndex = new TreeNode("index", cylsUtilQuery) {
        @Override
        public void run(String str) {
            int t = Integer.parseInt(str);
            cylsGroup group = groupList.get(groupIndexNow);
            if (group.getQueryResult() == null) {
                reply(groupIndexNow, "没有结果可以显示哦|•ω•`)");
            } else {
                List<String> QueryResult = group.getQueryResult();
                if (t >= 0 && t < QueryResult.size()) {
                    group.setQueryIndex(t);
                    reply(groupIndexNow, QueryResult.get(t));
                } else {
                    reply(groupIndexNow, "消息序号不合法|•ω•`)");
                }
            }
        }
    };
    private TreeNode cylsUtilBaidu = new TreeNode("baidu", cylsUtil) {
        @Override
        public void run(String str) throws Exception {
            if (str.equals("")) {
                reply(groupIndexNow, "请输入查询内容|•ω•`)");
            } else {
                try {
                    String website = "https://www.baidu.com/s?wd=" + str;
                    String url = Jsoup.connect(website).get().select("div.c-row div.c-span6 a[href].op-bk-polysemy-album").attr("href");
                    System.out.println(233);
                    String result = Jsoup.connect(website).get().select("div.c-row div.c-span18 p").first().text();
                    reply(groupIndexNow, "百度链接|•ω•`)：" + "https://www.baidu.com/s?wd=" + URLEncoder.encode(str, "UTF-8") + "\n" +
                            "搜索结果|•ω•`)：" + result + "\n" +
                            "链接|•ω•`)：" + url);
                } catch (NullPointerException e) {
                    reply(groupIndexNow, "百度链接：https://www.baidu.com/s?wd=" + URLEncoder.encode(str, "UTF-8") + "\n什么都没有查到哦|•ω•`)");
                }
            }
        }
    };
    private TreeNode cylsHelp = new TreeNode("help", cyls) {
        @Override
        public void run(String str) {
            reply(groupIndexNow, "欢迎来到帮助系统哦 |•ω•`)" + "\n" +
                    "通过cyls.help输出本帮助信息，" + "\n" +
                    "输入cyls.help.sudo来查看你当前的权利，" + "\n" +
                    "使用cyls.help.util来查看工具功能的帮助信息，" + "\n" +
                    "而用cyls.help.weather可以查看关于天气功能的帮助。" + "\n" +
                    "祝你玩得愉快! |•ω•`)");
        }
    };
    private TreeNode cylsHelpSudo = new TreeNode("sudo", cylsHelp) {
        @Override
        public void run(String str) {
            if (isOwner(friendIndexNow)) {
                reply(groupIndexNow, "你可是云裂的主人呢，连这都不知道 |•ω•`)" + "\n" +
                        "你可以让云裂彻底屏蔽与解除彻底屏蔽任何一名成员" + "\n" +
                        "cyls.sudo.ban/unban QQ号" + "\n" +
                        "可以让云裂屏蔽与解除屏蔽任何一名成员" + "\n" +
                        "cyls.sudo.ignore/recognize QQ号" + "\n" +
                        "可以将其他成员设置为云裂的管理员或取消管理员身份" + "\n" +
                        "cyls.sudo.authorize/unauthorize QQ号" + "\n" +
                        "可以进行通讯的中断与恢复" + "\n" +
                        "cyls.sudo.pause/resume" + "\n" +
                        "可以测试自己的权限" + "\n" +
                        "cyls.sudo.counter" + "\n" +
                        "可以让云裂自检" + "\n" +
                        "cyls.sudo.check" + "\n" +
                        "可以让云裂说特定的内容" + "\n" +
                        "cyls.sudo.say 要说的话" + "\n" +
                        "可以让云裂更新数据" + "\n" +
                        "cyls.sudo.update" + "\n" +
                        "还可以终止连接" + "\n" +
                        "cyls.sudo.quit" + "\n" +
                        "看你的权利这么多，你还全忘了 |•ω•`)");
            } else if (isAdmin(friendIndexNow)) {
                reply(groupIndexNow, "你是云裂的管理员，连这都不知道，一看就是新上任的|•ω•`)" + "\n" +
                        "可以让云裂屏蔽与解除屏蔽任何一名成员" + "\n" +
                        "cyls.sudo.ignore/recognize QQ号" + "\n" +
                        "可以进行通讯的中断与恢复" + "\n" +
                        "cyls.sudo.pause/resume" + "\n" +
                        "可以测试自己的权限" + "\n" +
                        "cyls.sudo.counter" + "\n" +
                        "可以让云裂自检" + "\n" +
                        "cyls.sudo.check" + "\n" +
                        "可以让云裂说特定的内容" + "\n" +
                        "cyls.sudo.say 要说的话" + "\n" +
                        "也可以让云裂更新数据" + "\n" +
                        "cyls.sudo.update");
            } else {
                reply(groupIndexNow, "你是普通成员，权力有限呢|•ω•`)" + "\n" +
                        "可以测试自己的权限" + "\n" +
                        "cyls.sudo.counter" + "\n" +
                        "可以让云裂自检" + "\n" +
                        "cyls.sudo.check" + "\n" +
                        "不如向主人申请权限吧|•ω•`)");
            }
        }
    };
    private TreeNode cylsHelpUtil = new TreeNode("util", cylsHelp) {
        @Override
        public void run(String str) {
            reply(groupIndexNow, "云裂拥有许多工具功能呢 |•ω•`)" + "\n" +
                    "例如：要计算某个表达式的值，你可以输入：" + "\n" +
                    "cyls.util.math 表达式" + "\n" +
                    "你可以输入下面的命令来掷骰子：" + "\n" +
                    "cyls.util.dice" + "\n" +
                    "你也可以查询群成员的名片，昵称或qq号：" + "\n" +
                    "cyls.util.query 查询的内容" + "\n" +
                    "你还可以随时百度：" + "\n" +
                    "cyls.util.baidu 查询的内容" + "\n" +
                    "要获得关于查询功能的进一步帮助，你可以输入：" + "\n" +
                    "cyls.help.util.query" + "\n" +
                    "不如试一试? |•ω•`)");
        }
    };
    private TreeNode cylsHelpUtilQuery = new TreeNode("query", cylsHelpUtil) {
        @Override
        public void run(String str) {
            reply(groupIndexNow, "云裂的搜索功能是非常强大的 |•ω•`)" + "\n" +
                    "如果同时查询群成员的名片，昵称或qq号，你只需输入：" + "\n" +
                    "cyls.util.query 查询的内容" + "\n" +
                    "要搜索群成员的名片和昵称，你可以输入：" + "\n" +
                    "cyls.util.query.name 查询的内容" + "\n" +
                    "要搜索群成员的qq号，你可以输入：" + "\n" +
                    "cyls.util.query.qq 查询的内容" + "\n" +
                    "搜索完毕后，若结果过长，不会一次显示完毕。你有几种选择：" + "\n" +
                    "查看下面10条结果：" + "\n" +
                    "cyls.util.query.next" + "\n" +
                    "查看上面10条结果：" + "\n" +
                    "cyls.util.query.previous " + "\n" +
                    "查看第t组结果：" + "\n" +
                    "cyls.util.query.index t " + "\n" +
                    "不如试一试? |•ω•`)");
        }
    };
    private TreeNode cylsHelpWeather = new TreeNode("weather", cylsHelp) {
        @Override
        public void run(String str) {
            reply(groupIndexNow, "云裂可是拥有天气查询功能的哦 |•ω•`)" + "\n" +
                    "要查询今天的天气，你可以输入如下命令：" + "\n" +
                    "cyls.weather.today 城市名" + "\n" +
                    "要查询明天的天气预报，你可以输入：" + "\n" +
                    "cyls.weather.tomorrow 城市名" + "\n" +
                    "你也可以这样来查询今天，明天，后天的天气预报：" + "\n" +
                    "cyls.weather.day[0,1,2] 城市名" + "\n" +
                    "不如试一试? |•ω•`)");
        }
    };

    private TreeNode rootRegexCyls = new TreeNode("", null) {
        @Override
        public void run(String str) throws Exception {
            if (friendList.get(friendIndexNow).getName().contains("哈勃")) {
                if (str.contains("因为你可爱")) {
                    reply(groupIndexNow, "哈勃你是有多无聊 |•ω•`)");
                } else if (str.contains("\uD83C\uDFB5")) {
                } else if (str.contains("与哈勃私聊")) {
                    reply(groupIndexNow, "整天闲了没事为什么要和你私聊呢|•ω•`)");
                } else if (str.contains("脏话")) {
                    reply(groupIndexNow, "人家说脏话关你什么事|•ω•`)");
                } else if (str.contains("不比我少")) {
                    reply(groupIndexNow, "我水是不小心，你水就是你的不对了");
                } else {
                    replyByChance(groupIndexNow, 0.3, "看来哈勃又在水了 |•ω•`)");
                }
            } else {
                if (str.matches("hubble\\..*")) {
                    replyByChance(groupIndexNow, 0.3, "哈勃这种无聊的东西叫他干嘛 |•ω•`)");
                } else if (str.matches(".*(哈勃|hubble).*")) {
                    replyByChance(groupIndexNow, 0.1, "你们是有多无聊整天喊哈勃 |•ω•`)");
                } else if (repeatCountNow % 5 == 0) {
                    reply(groupIndexNow, "你们这样刷队形是不对的 |•ω•`)");
                } else {
                    replyByChance(groupIndexNow, 0.01, "你为什么一直要说\"" + str + "\"呢 |•ω•`)");
                }
            }
        }
    };
    private TreeNode disableRegexCyls = new TreeNode(".*被管理员禁言.*", rootRegexCyls) {
        @Override
        public void run(String str) throws Exception {
            reply(groupIndexNow, "为什么要互相伤害呢|•ω•`)");
        }
    };
    private TreeNode enableRegexCyls = new TreeNode(".*被管理员解除禁言.*", rootRegexCyls) {
        @Override
        public void run(String str) throws Exception {
        }
    };
    private TreeNode cylsRegexCyls = new TreeNode(".*云裂.*", rootRegexCyls) {
        @Override
        public void run(String str) throws Exception {
            if (friendList.get(friendIndexNow).getName().contains("哈勃")) {
                replyByChance(groupIndexNow, 0.3, "哈勃不要再叫我了 (╯‵□′)╯︵┴─┴");
            } else {
                reply(groupIndexNow, "叫我做什么 |•ω•`)");
            }
        }
    };
    private TreeNode cylsMathRegexCyls = new TreeNode(".*算.*|.*等于.*", cylsRegexCyls) {
        @Override
        public void run(String str) throws Exception {
            str = str.replaceAll("！", "!");
            str = str.replaceAll("（", "(");
            str = str.replaceAll("）", ")");
            str = str.replaceAll("∧", "^");
            str = str.replaceAll("×", "*");
            str = str.replaceAll("÷", "/");
            str = str.replaceAll(" ", "");
            int maxLength = 0, maxIndex = -1;
            boolean flag = false;
            final String expressionRegex = "[0-9a-z+\\-*/=!<>\\^|&(),.]";
            final String EmptyString = "";
            for (int i = 0; i < str.length(); ++i) {
                if ((EmptyString + str.charAt(i)).matches(expressionRegex)) {
                    if (!flag) {
                        flag = true;
                        int j;
                        for (j = i; j < str.length() && (EmptyString + str.charAt(j)).matches(expressionRegex); ++j) {
                            ;
                        }
                        if (j - i > maxLength) {
                            maxLength = j - i;
                            maxIndex = i;
                        }
                    }
                } else {
                    flag = false;
                }
            }
            if (maxIndex != -1) {
                Expression e = new Expression(str.substring(maxIndex, maxIndex + maxLength));
                reply(groupIndexNow, "结果是： |•ω•`)\n" + (new Double(e.calculate())).toString());
            }
        }
    };
    private TreeNode cylsCheckRegexCyls = new TreeNode(".*自检.*", cylsRegexCyls) {
        @Override
        public void run(String str) throws Exception {
            reply(groupIndexNow, "自检完毕\n一切正常哦 |•ω•`)");
        }
    };
    private TreeNode cylsConfessionRegexCyls = new TreeNode(".*表白云裂.*", cylsRegexCyls) {
        @Override
        public void run(String str) throws Exception {
            if (friendList.get(friendIndexNow).getName().contains("哈勃")) {
                reply(groupIndexNow, "丑拒|•ω•`)");
            } else {
                reply(groupIndexNow, "表白" + getFriendName(friendIndexNow) + "|•ω•`)");
            }
        }
    };
    private TreeNode confessionRegexCyls = new TreeNode(".*表白.*", rootRegexCyls) {
        @Override
        public void run(String str) throws Exception {
            if (str.matches(".*(哈勃|hubble).*")) {
                reply(groupIndexNow, "哈勃这种愚蠢的东西，表白做什么 |•ω•`)");
            } else {
                reply(groupIndexNow, "表白+1 |•ω•`)");
            }
        }
    };
    private TreeNode redpackRegexCyls = new TreeNode("\\[QQ红包\\].*", rootRegexCyls) {
        @Override
        public void run(String str) throws Exception {
            reply(groupIndexNow, "整天发红包做什么…… |•ω•`)");
        }
    };

    //Smart QQ API
    private SmartQQClient client;

    private MessageCallback callback = new MessageCallback() {
        @Override
        public void onMessage(Message message) {
        }

        @Override
        public void onGroupMessage(GroupMessage message) {
            try {
                sleep(233);
                groupIndexNow = groupIndexFromId.get(message.getGroupId());
                checkGroupUser(message.getUserId(), groupIndexNow);
                friendIndexNow = friendIndexFromQQ.get(QQFromUid(message.getUserId()));
                contentNow = message.getContent();
                String groupName = getGroupName(groupIndexNow);//获取群名

                repeatCountNow = groupList.get(groupIndexNow).getRepeatCount(contentNow);

                if (groupName.matches(groupOnDevice)) {
                    String output = "[" + getSystemTime() + "](" + groupName + ")" +
                            getFriendName(friendIndexNow) +
                            ":" + contentNow;
                    System.out.println(output); //输出消息内容
                    if (friendIndexNow != null && !isBanned(friendIndexNow)) {
                        if (contentNow.matches("cyls\\..*")) { //当前消息为指令
                            try {
                                root.runPath(contentNow); //执行指令相应的函数
                            } catch (TreeNode.PathDoesNotExistException e) {
                                reply(groupIndexNow, "请确保命令存在哦|•ω•`)");
                            }
                        } else if (!isPaused && !isIgnored(friendIndexNow)) { //当前消息不是指令
                            rootRegexCyls.runRegexPath(contentNow); //匹配预设的指令并执行函数
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDiscussMessage(DiscussMessage message) {
            //System.out.println(message.getContent());
        }
    };

    Cyls() {
        client = new SmartQQClient(callback);
    }

    void start() {
        String ToAnalysisSetUp = ToAnalysis.parse("233").toString(); //初始化分词库，无实际作用
        load();//程序开始时读取好友和群友信息
        setup();//载入群等信息
        System.out.println("[" + getSystemTime() + "]云裂准备就绪！");
    }
}