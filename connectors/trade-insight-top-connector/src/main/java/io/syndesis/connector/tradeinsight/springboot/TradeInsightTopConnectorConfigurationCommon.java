package io.syndesis.connector.tradeinsight.springboot;

import javax.annotation.Generated;

/**
 * Fetches top recommendation from Trade Insight API server
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
public class TradeInsightTopConnectorConfigurationCommon {

    /**
     * Host and port of HTTP service to use (override host in swagger schema)
     */
    private String host;
    /**
     * Delay in milli seconds between scheduling (executing)
     */
    private long schedulerPeriod = 5000L;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public long getSchedulerPeriod() {
        return schedulerPeriod;
    }

    public void setSchedulerPeriod(long schedulerPeriod) {
        this.schedulerPeriod = schedulerPeriod;
    }
}