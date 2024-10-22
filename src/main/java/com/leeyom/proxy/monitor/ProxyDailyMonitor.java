package com.leeyom.proxy.monitor;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.leeyom.proxy.domain.ByWaveProxyInfo;
import com.leeyom.proxy.telegram.TelegramBot;
import com.leeyom.proxy.util.ProxyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 监控实现
 *
 * @author leeyom wang
 * @date 2021/1/30 3:29 下午
 */
@Slf4j
@Service
public class ProxyDailyMonitor {

    public static void main(String[] args) {
        String byWaveUserName = args[0];
        String byWavePassword = args[1];
        String monoCloudUserName = args[2];
        String monoCloudPassword = args[3];
        String tgChatId = args[4];
        String tgToken = args[5];

        if (StrUtil.isAllNotBlank(byWaveUserName, byWavePassword, tgChatId, tgToken)) {
            monitorByWaveDaily(byWaveUserName, byWavePassword, new TelegramBot(Convert.toLong(tgChatId), tgToken));
        } else {
            log.warn("ByWave 监控失败，请检查参数是否遗漏");
        }
        if (StrUtil.isAllNotBlank(monoCloudUserName, monoCloudPassword, tgChatId, tgToken)) {
            monitorMonoCloudDaily(monoCloudUserName, monoCloudPassword, new TelegramBot(Convert.toLong(tgChatId), tgToken));
        } else {
            log.warn("MonoCloud 监控失败，请检查参数是否遗漏");
        }
    }

    public static void monitorByWaveDaily(String userName, String password, TelegramBot bot) {
        try {
            // 获取ByWave流量信息
            ByWaveProxyInfo byWaveProxyInfo = ProxyUtil.getByWaveProxyInfo(userName, password);
            String msg;
            if (ObjectUtil.isNull(byWaveProxyInfo)) {
                msg = "ByWaveProxy 访问被限制，请更换 ip 后重试";
            } else {
                msg = "梯子 ByWave 流量 " + DateUtil.today() + " 使用情况：" + "\n" +
                        "- 重置流量：" + ReUtil.getFirstNumber(byWaveProxyInfo.getFar()) + "days" + "\n" +
                        "- 可使用的流量：" + byWaveProxyInfo.getTrafficFreeNum() + byWaveProxyInfo.getTrafficFreeUnit() + "\n" +
                        "- 已使用的流量：" + byWaveProxyInfo.getTrafficUsedNum() + byWaveProxyInfo.getTrafficUsedUnit() + "\n" +
                        "- 服务到期：" + byWaveProxyInfo.getExpireDate() + "\n";
            }
            bot.sendMessage(msg);
        } catch (Exception e) {
            log.error("ByWave Daily Monitor Error:", e);
            bot.sendMessage("ByWave Daily Monitor Error:" + ExceptionUtil.getMessage(e));
        }
    }

    public static void monitorMonoCloudDaily(String email, String password, TelegramBot bot) {
        try {
            // 获取MonoCloud流量信息
            List<String> list = ProxyUtil.getMonoCloudProxyInfo(email, password);
            String msg = "梯子 MonoCloud 流量 " + DateUtil.today() + " 使用情况：" + "\n" +
                    "- 已使用：" + list.get(0) + "%" + "\n" +
                    "- 剩余流量：" + list.get(1) + "GB" + "\n" +
                    "- 重置流量：" + list.get(2) + "days" + "\n" +
                    "- 服务到期：" + list.get(3) + "days" + "\n";
            // 推送到机器人
            bot.sendMessage(msg);
        } catch (Exception e) {
            log.error("MonoCloud Daily Monitor Error:", e);
            bot.sendMessage("MonoCloud Daily Monitor Error:" + ExceptionUtil.getMessage(e));
        }
    }

}
