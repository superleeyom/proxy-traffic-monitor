package com.leeyom.proxy.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.leeyom.proxy.common.exception.BizException;
import com.leeyom.proxy.domain.ByWaveProxyInfo;
import com.leeyom.proxy.telegram.TelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Map;

/**
 * 监控器工具类
 *
 * @author leeyom wang
 * @date 2021/1/31 10:34 上午
 */
@Slf4j
public class ProxyUtil {

    public static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_0_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36";

    public static ByWaveProxyInfo getByWaveProxyInfo(String userName, String password) {
        // 登录
        HttpRequest loginRequest = HttpUtil.createPost("https://bywave.io/dologin.php");
        loginRequest.header(Header.USER_AGENT, USER_AGENT);
        Map<String, Object> form = MapUtil.newHashMap(4);
        form.put("token", IdUtil.simpleUUID());
        form.put("username", userName);
        form.put("password", password);
        // 骂人？？？
        form.put("nmb", "sb");
        loginRequest.form(form);
        HttpResponse loginResponse = loginRequest.execute();

        // 跳转到产品列表
        HttpRequest productRequest = HttpUtil.createGet("https://bywave.io/clientarea.php?action=productdetails&id=68488");
        productRequest.header(Header.USER_AGENT, USER_AGENT);
        productRequest.cookie(loginResponse.getCookies());
        String body = productRequest.execute().body();
        Document doc = Jsoup.parse(body);

        // 还有多少天重置
        String far = doc.select(".countdown-time div").first().text();
        // 可使用的流量
        String trafficFreeNum = doc.select(".traffic-texts .traffic-free .traffic-text .traffic-num").text();
        // 可使用的流量单位
        String trafficFreeUnit = doc.select(".traffic-texts .traffic-free .traffic-text .traffic-unit").text();
        // 已使用的流量
        String trafficUsedNum = doc.select(".traffic-texts .traffic-used .traffic-text .traffic-num").text();
        // 已使用的流量单位
        String trafficUsedUnit = doc.select(".traffic-texts .traffic-used .traffic-text .traffic-unit").text();
        // 到期时间
        String expireDate = doc.select(".products-renew .products-duo-text .products-duotime").text();

        return new ByWaveProxyInfo(far, trafficFreeNum, trafficFreeUnit, trafficUsedNum, trafficUsedUnit, expireDate);

    }

    public static List<String> getMonoCloudProxyInfo(String email, String password) {

        // 获取登录需要的cookie和token
        HttpResponse tokenResponse = HttpRequest.get("https://mymonocloud.com/login").header(Header.USER_AGENT, USER_AGENT).execute();
        String loginPage = tokenResponse.body();
        Document loginDocument = Jsoup.parse(loginPage);
        String token = loginDocument.select("input[name=_token]").first().val();

        // 登录
        HttpRequest loginRequest = HttpUtil.createPost("https://mymonocloud.com/login");
        loginRequest.header(Header.USER_AGENT, USER_AGENT);
        loginRequest.header(Header.CONTENT_TYPE, "application/x-www-form-urlencoded");
        Map<String, Object> form = MapUtil.newHashMap();
        form.put("_token", token);
        form.put("email", email);
        form.put("password", password);
        loginRequest.form(form);
        loginRequest.cookie(tokenResponse.getCookies());
        HttpResponse loginResponse = loginRequest.execute();

        // 跳转到产品列表
        HttpRequest productRequest = HttpUtil.createGet("https://mymonocloud.com/home");
        productRequest.header(Header.USER_AGENT, USER_AGENT);
        productRequest.cookie(loginResponse.getCookies());
        String body = productRequest.execute().body();
        Document document = Jsoup.parse(body);
        Elements elements = document.select("span[data-plugin=counterup]");
        List<String> list = CollUtil.newArrayList();
        for (Element element : elements) {
            list.add(element.text());
        }
        return list;
    }

    public static void checkParam(String userName, String password, TelegramBot bot) {
        if (StrUtil.isBlank(userName) || StrUtil.isBlank(password)) {
            log.warn("用户名和姓名不能为空");
        }
        if (bot.getChatId() == 0 || StrUtil.isBlank(bot.getToken())) {
            throw new BizException("telegram的chatId和token不能为空");
        }
    }

}