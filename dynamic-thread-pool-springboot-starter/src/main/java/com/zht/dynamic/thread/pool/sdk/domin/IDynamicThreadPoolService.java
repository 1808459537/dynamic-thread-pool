package com.zht.dynamic.thread.pool.sdk.domin;

import com.zht.dynamic.thread.pool.sdk.domin.model.entity.ThreadPoolConfigEntity;

import java.util.List;

public interface IDynamicThreadPoolService {
    List<ThreadPoolConfigEntity> queryThreadPoolList();

    ThreadPoolConfigEntity queryThreadPoolConfigByName(String name);

    void updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity);
}
