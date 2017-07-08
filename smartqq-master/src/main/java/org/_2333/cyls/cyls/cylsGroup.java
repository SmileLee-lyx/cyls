package org._2333.cyls.cyls;

import com.scienjus.smartqq.client.SmartQQClient;
import com.scienjus.smartqq.model.GroupInfo;

import java.util.List;

/**
 * @author 2333
 */
public class cylsGroup {
    private long id;
    private String name;
    private long code;

    private GroupInfo groupInfo = null;
    private SmartQQClient client;

    private List<String> QueryResult=null;
    private Integer QueryIndex=null;

    private List<Long> songIds = null;

    private String contentNow="";
    private int repeatCount=0;

    public cylsGroup(long id, String name, long code, SmartQQClient client) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.client = client;
    }

    public GroupInfo getGroupInfo() {
        if (groupInfo == null) groupInfo = client.getGroupInfo(code);
        return groupInfo;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getQueryResult() {
        return QueryResult;
    }

    public void setQueryResult(List<String> queryResult) {
        QueryResult = queryResult;
    }

    public Integer getQueryIndex() {
        return QueryIndex;
    }

    public void setQueryIndex(Integer queryIndex) {
        QueryIndex = queryIndex;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public List<Long> getSongIds() {
        return songIds;
    }

    public void setSongIds(List<Long> songIds) {
        this.songIds = songIds;
    }

    public String getContentNow() {
        return contentNow;
    }

    public void setContentNow(String contentNow) {
        this.contentNow = contentNow;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public int getRepeatCount(String content){
        if(!content.equals(contentNow)){
            contentNow=content;
            return repeatCount=1;
        }
        return ++repeatCount;
    }
}
