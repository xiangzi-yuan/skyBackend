package com.sky.service;

/**
 * 缓存版本号管理（基于 Redis）。
 *
 * <p>用于列表缓存的“版本号 key”策略：{bizKey}:v{ver}，写操作仅 bump 版本号，不枚举删除历史 key。</p>
 */
public interface VersionService {

    /**
     * 获取版本号；不存在则初始化为 1。
     *
     * <p>Redis 异常时降级返回 1，不影响主业务。</p>
     */
    long getOrInit(String verKey);

    /**
     * 版本号 +1；异常只记录日志，不抛出。
     */
    void bump(String verKey);

    /**
     * 事务提交后 bump；若无事务同步则直接 bump 兜底。
     */
    void bumpAfterCommit(String verKey);
}

