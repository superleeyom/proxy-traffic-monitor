package com.leeyom.proxy.telegram;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.HttpUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 电报机器人
 *
 * @author leeyom wang
 * @date 2021/1/30 5:57 下午
 */
@Slf4j
@Data
public class TelegramBot {

    /**
     * 聊天id
     */
    private long chatId;

    /**
     * bot token
     */
    private String token;

    public TelegramBot(long chatId, String token) {
        this.chatId = chatId;
        this.token = token;
    }

    /**
     * 机器人推送消息
     *
     * @param msg 消息
     */
    public void sendMessage(String msg) {
        String url = "https://api.telegram.org/bot" + token + "/sendMessage?chat_id=" + chatId + "&text=" + msg;
        String response = HttpUtil.get(url, CharsetUtil.CHARSET_UTF_8);
        log.info(response);
    }

}
