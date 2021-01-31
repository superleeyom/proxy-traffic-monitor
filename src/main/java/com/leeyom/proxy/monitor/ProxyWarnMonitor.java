package com.leeyom.proxy.monitor;

import cn.hutool.core.convert.Convert;
import com.leeyom.proxy.domain.ByWaveProxyInfo;
import com.leeyom.proxy.telegram.TelegramBot;
import com.leeyom.proxy.util.ProxyUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 流量监控预警
 *
 * @author leeyom wang
 * @date 2021/1/31 10:55 上午
 */
@Slf4j
public class ProxyWarnMonitor {

    public static void main(String[] args) {
        String byWaveUserName = args[0];
        String byWavePassword = args[1];
        String monoCloudUserName = args[2];
        String monoCloudPassword = args[3];
        String tgChatId = args[4];
        String tgToken = args[5];
        byWaveProxyWarn(byWaveUserName, byWavePassword, new TelegramBot(Convert.toLong(tgChatId), tgToken));
        monoCloudProxyWarn(monoCloudUserName, monoCloudPassword, new TelegramBot(Convert.toLong(tgChatId), tgToken));
    }

    public static void byWaveProxyWarn(String userName, String password, TelegramBot bot) {
        ProxyUtil.checkParam(userName, password, bot);
        try {
            // 获取ByWave流量信息
            ByWaveProxyInfo byWaveProxyInfo = ProxyUtil.getByWaveProxyInfo(userName, password);
            Double freeNum = Convert.toDouble(byWaveProxyInfo.getTrafficFreeNum());
            Double usedNum = Convert.toDouble(byWaveProxyInfo.getTrafficUsedNum());
            double freePercent = freeNum / (freeNum + usedNum);
            if (freePercent < 0.20) {
                String usedStr = usedNum + "/" + (freeNum + usedNum);
                bot.sendMessage("请注意，梯子 ByWave 可使用的流量已经少于20%！！！请节约使用！！！流量使用情况：" + usedStr);
            }
        } catch (Exception e) {
            log.error("MonoCloud Monitor Error:", e);
            bot.sendMessage("MonoCloud Monitor Error:" + e.getMessage());
        }
    }

    public static void monoCloudProxyWarn(String email, String password, TelegramBot bot) {
        ProxyUtil.checkParam(email, password, bot);
        try {
            List<String> list = ProxyUtil.getMonoCloudProxyInfo(email, password);
            Integer usedPercent = Convert.toInt(list.get(0));
            Integer freePercent = 100 - usedPercent;
            if (freePercent < 20) {
                bot.sendMessage("请注意，梯子 MonoCloud 可使用的流量已经少于20%！！！请节约使用！！！");
            }
        } catch (Exception e) {
            log.error("MonoCloud Monitor Error:", e);
            bot.sendMessage("MonoCloud Monitor Error:" + e.getMessage());
        }


    }

}