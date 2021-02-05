package com.leeyom.proxy.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.leeyom.proxy.domain.ByWaveProxyInfo;
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
    public static final String USER_AGENT_BY_WAVE = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36";
    public static final String[] LIMIT_ARRAY = {"检测页面", "网站当前访问量较大"};

    public static ByWaveProxyInfo getByWaveProxyInfo(String userName, String password) throws InterruptedException {

        // 设置https协议访问
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");

        // 登录
        HttpRequest loginRequest = HttpUtil.createPost("https://bywave.io/dologin.php");
        loginRequest.header(Header.USER_AGENT, USER_AGENT_BY_WAVE);
        loginRequest.header(Header.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        loginRequest.header(Header.ACCEPT_ENCODING, "gzip, deflate, br");
        loginRequest.header(Header.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9,ko;q=0.8,en;q=0.7,ja;q=0.6,zh-TW;q=0.5");
        loginRequest.header(Header.CACHE_CONTROL, "max-age=0");
        loginRequest.header(Header.CONTENT_LENGTH, "107");
        loginRequest.header(Header.CONTENT_TYPE, "application/x-www-form-urlencoded");
        loginRequest.header(Header.ORIGIN, "https://bywave.io");
        loginRequest.header(Header.REFERER, "https://bywave.io/clientarea.php");

        // 若访问出现限制，可以在这里设置代理ip访问
        // Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("xxx.xxx.xxx.xxx", 80));
        // loginRequest.setProxy(proxy);

        Map<String, Object> form = MapUtil.newHashMap(4);
        form.put("token", IdUtil.simpleUUID());
        form.put("username", userName);
        form.put("password", password);
        // 骂人？？？
        form.put("nmb", "sb");
        loginRequest.form(form);
        HttpResponse loginResponse = loginRequest.execute();
        log.info("ByWaveProxy login response：{}", loginResponse.body());

        if (StrUtil.containsAny(loginResponse.body(), LIMIT_ARRAY)) {
            log.error("ByWaveProxy 访问被拦截，需要更换 ip 后才能访问");
            return null;
        }

        // 休眠5秒，限制访问频率
        Thread.sleep(5000L);

        // 跳转到产品列表
        HttpRequest productRequest = HttpUtil.createGet("https://bywave.io/clientarea.php?action=productdetails&id=68488");
        productRequest.header(Header.USER_AGENT, USER_AGENT_BY_WAVE);
        loginRequest.header(Header.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        loginRequest.header(Header.ACCEPT_ENCODING, "gzip, deflate, br");
        loginRequest.header(Header.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9,ko;q=0.8,en;q=0.7,ja;q=0.6,zh-TW;q=0.5");
        loginRequest.header(Header.REFERER, "https://bywave.io/clientarea.php");

        productRequest.cookie(loginResponse.getCookies());
        String body = productRequest.execute().body();
        Document doc = Jsoup.parse(body);
        log.info("ByWaveProxy productDetails page：{}", body);

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

    public static List<String> getMonoCloudProxyInfo(String email, String password) throws InterruptedException {

        // 设置https协议访问
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");

        // 获取登录需要的cookie和token
        HttpResponse tokenResponse = HttpRequest.get("https://mymonocloud.com/login").header(Header.USER_AGENT, USER_AGENT).execute();
        String loginPage = tokenResponse.body();
        Document loginDocument = Jsoup.parse(loginPage);
        String token = loginDocument.select("input[name=_token]").first().val();
        log.info("monoCloud page token:{}", token);

        // 休眠5秒，限制访问频率
        Thread.sleep(5000L);

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
        log.info("monoCloudProxy login response：{}", loginResponse.body());

        Thread.sleep(5000L);

        // 跳转到产品列表
        HttpRequest productRequest = HttpUtil.createGet("https://mymonocloud.com/home");
        productRequest.header(Header.USER_AGENT, USER_AGENT);
        productRequest.cookie(loginResponse.getCookies());
        String body = productRequest.execute().body();
        log.info("monoCloudProxy home page：{}", body);
        Document document = Jsoup.parse(body);
        Elements elements = document.select("span[data-plugin=counterup]");
        List<String> list = CollUtil.newArrayList();
        for (Element element : elements) {
            list.add(element.text());
        }
        return list;
    }

}
