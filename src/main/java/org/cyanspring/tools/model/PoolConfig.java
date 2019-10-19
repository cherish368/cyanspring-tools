package org.cyanspring.tools.model;

public class PoolConfig {

    private Integer maxIdle;

    private Integer maxTotal;

    private Integer maxWaitMills;

    private boolean blockWhenExhausted;

    private Integer timeBetweenEvictionRunsMillis;

    private Integer minEvictableIdleTimeMillis;

    private boolean testOnBorrow;

    private boolean testWhileIdle;

    public PoolConfig(Integer maxIdle, Integer maxTotal, Integer maxWaitMills, boolean blockWhenExhausted,
                      Integer timeBetweenEvictionRunsMillis, Integer minEvictableIdleTimeMillis,
                      boolean testOnBorrow, boolean testWhileIdle) {
        this.maxIdle = maxIdle;
        this.maxTotal = maxTotal;
        this.maxWaitMills = maxWaitMills;
        this.blockWhenExhausted = blockWhenExhausted;
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
        this.testOnBorrow = testOnBorrow;
        this.testWhileIdle = testWhileIdle;
    }

    public PoolConfig() {
        this.maxIdle = 10;
        this.maxTotal = 100;
        this.maxWaitMills = 1000;
        this.blockWhenExhausted = true;
        this.timeBetweenEvictionRunsMillis = 30000;
        this.minEvictableIdleTimeMillis = 30000;
        this.testOnBorrow = true;
        this.testWhileIdle = true;
    }

}
