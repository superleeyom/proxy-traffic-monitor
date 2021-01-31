package com.leeyom.proxy.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ByWave梯子的日常流量监控
 *
 * @author leeyom wang
 * @date 2021/1/31 10:36 上午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ByWaveProxyInfo {

    /**
     * 还有多少天重置
     */
    private String far;

    /**
     * 可使用的流量
     */
    private String trafficFreeNum;

    /**
     * 可使用的流量单位
     */
    private String trafficFreeUnit;

    /**
     * 已使用的流量
     */
    private String trafficUsedNum;

    /**
     * 已使用的流量单位
     */
    private String trafficUsedUnit;

    /**
     * 到期时间
     */
    private String expireDate;

}
