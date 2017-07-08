package org._2333.cyls.cyls;

import com.scienjus.smartqq.client.SmartQQClient;

/**
 * @author 2333
 */
public class cylsFriend implements Comparable<cylsFriend> {
    private long qq;
    private long uid;
    private String name;
    /**
     * 0:normal
     * 1:admin
     * 2:owner
     */
    private int adminLevel;
    /**
     * 0:normal
     * 1:ignored
     * 2:banned
     */
    private int ignoreLevel;
    private boolean friend;

    private SmartQQClient client;

    public cylsFriend(long qq, long uid, String name, int adminLevel, int ignoreLevel, boolean friend, SmartQQClient client) {
        this.qq = qq;
        this.uid = uid;
        this.name = name;
        this.adminLevel = adminLevel;
        this.ignoreLevel = ignoreLevel;
        this.friend = friend;
        this.client = client;
    }

    public boolean isOwner() {
        return adminLevel >= 2;
    }

    public boolean isAdmin() {
        return adminLevel >= 1;
    }

    public boolean isBanned() {
        return ignoreLevel >= 2;
    }

    public boolean isIgnored() {
        return ignoreLevel >= 1;
    }

    public int authorize() {
        int adminLevel_ = adminLevel;
        adminLevel = Math.max(adminLevel, 1);
        return adminLevel_;
    }

    public int unauthorize() {
        int adminLevel_ = adminLevel;
        if (adminLevel < 2) adminLevel = 0;
        return adminLevel_;
    }

    public int ignore() {
        int ignoreLevel_ = ignoreLevel;
        ignoreLevel = Math.max(ignoreLevel, 1);
        return ignoreLevel_;
    }

    public int recognize() {
        int ignoreLevel_ = ignoreLevel;
        if (ignoreLevel < 2) ignoreLevel = 0;
        return ignoreLevel_;
    }

    public int ban() {
        int ignoreLevel_ = ignoreLevel;
        ignoreLevel = 2;
        return ignoreLevel_;
    }

    public int unban() {
        int ignoreLevel_ = ignoreLevel;
        ignoreLevel = 0;
        return ignoreLevel_;
    }

    public long getQQ() {
        return qq;
    }

    public void setQQ(long qq) {
        this.qq = qq;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public int getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(int adminLevel) {
        this.adminLevel = adminLevel;
    }

    public int getIgnoreLevel() {
        return ignoreLevel;
    }

    public void setIgnoreLevel(int ignoreLevel) {
        this.ignoreLevel = ignoreLevel;
    }

    public boolean isFriend() {
        return friend;
    }

    public void setFriend(boolean friend) {
        this.friend = friend;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int compareTo(cylsFriend friend) {
        return getQQ() > friend.getQQ() ? 1 : -1;
    }
}
