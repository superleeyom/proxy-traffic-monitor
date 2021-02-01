package com.leeyom.proxy.util;

import cn.hutool.core.util.StrUtil;
import com.leeyom.proxy.common.exception.BizException;
import com.leeyom.proxy.telegram.TelegramBot;
import lombok.extern.slf4j.Slf4j;

/**
 * 参数校验
 *
 * @author leeyom wang
 * @date 2021/2/1 11:31 上午
 */
@Slf4j
public class Validator {

    public static void checkParam(String userName, String password, TelegramBot bot) {
        if (StrUtil.isBlank(userName) || StrUtil.isBlank(password)) {
            log.warn("用户名和姓名不能为空");
        }
        if (bot.getChatId() == 0 || StrUtil.isBlank(bot.getToken())) {
            throw new BizException("telegram的chatId和token不能为空");
        }
    }

}
