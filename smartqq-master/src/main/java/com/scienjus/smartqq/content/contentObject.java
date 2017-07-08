package com.scienjus.smartqq.content;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;

import javax.print.DocFlavor;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 2333
 */
public class contentObject {
    private List<contentUnit> units;

    private int getFirstSigleSquareBracket(String str) {
        int begin = 0, i;
        while ((i = str.indexOf('[', begin)) != -1) {
            if (i == 0 || str.charAt(i - 1) != '\\') return i;
            begin = i + 1;
        }
        return -1;
    }

    public contentObject(String content) {
        units = new ArrayList<>();
        while (content != null) {
            int i;
            contentUnit unit;
            if ((i = getFirstSigleSquareBracket(content)) != -1) {
                if (i == 0) {
                    i = content.indexOf(']') + 1;
                    try {
                        unit = new contentUnit(contentUnit.stateEmoji,
                                JSON.parseArray(content.substring(0, i)).getInteger(1).toString());
                    } catch (ClassCastException e) {
                        unit = new contentUnit(contentUnit.stateContent,
                                content.substring(0, i).replace("\\\\", "\\").replace("\\[", "[").replace("\\]", "]"));
                    }
                    content = content.substring(i);
                } else {
                    unit = new contentUnit(contentUnit.stateContent,
                            content.substring(0, i).replace("\\\\", "\\").replace("\\[", "[").replace("\\]", "]"));
                    content = content.substring(i);
                }
            } else {
                unit = new contentUnit(contentUnit.stateContent,
                        content.replace("\\\\", "\\").replace("\\[", "[").replace("\\]", "]"));
                content = null;
            }
            units.add(unit);
        }
    }

    public contentObject(JSONArray json) {
        units = new ArrayList<>();
        for (int i = 0; i < json.size(); ++i) {
            contentUnit unit;
            try {
                JSONArray emoji = json.getJSONArray(i);
                unit = new contentUnit(contentUnit.stateEmoji, emoji.getInteger(1).toString());
            } catch (ClassCastException | JSONException e) {
                unit = new contentUnit(contentUnit.stateContent, json.getString(i));
            }
            units.add(unit);
        }
    }

    public String getContent() {
        String str = "";
        for (contentUnit unit : units) {
            switch (unit.state) {
                case contentUnit.stateContent: {
                    str = str + unit.content.replace("\\", "\\\\").replace("[", "\\[").replace("]", "\\]");
                    break;
                }
                case contentUnit.stateEmoji: {
                    str = str + "[\"face\"," + unit.content + "]";
                    break;
                }
                default: {
                    //do nothing
                }
            }
        }
        return str;
    }

    public JSONArray getJson() {
        JSONArray json = new JSONArray(0);
        for (contentUnit unit : units) {
            json.add(unit.get());
        }
        return json;
    }

}
