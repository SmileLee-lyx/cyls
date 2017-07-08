package com.scienjus.smartqq.content;

import com.alibaba.fastjson.JSON;

import java.util.Arrays;

/**
 * @author 2333
 */
public class contentUnit {
    public static final int stateContent = 0;
    public static final int stateEmoji = 1;
    int state;
    String content;

    contentUnit(int state, String content) {
        this.state = state;
        this.content = content;
    }

    Object get() {
        switch (state) {
            case stateContent: {
                return content;
            }
            case stateEmoji: {
                return JSON.toJSON(Arrays.asList("face", Integer.parseInt(content)));
            }
            default: {
                return null;
            }
        }
    }
}
