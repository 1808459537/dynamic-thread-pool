package com.zht.dynamic.thread.pool.sdk.registry;

import com.zht.dynamic.thread.pool.sdk.domin.model.entity.ThreadPoolConfigEntity;

import java.util.List;

public interface IRegistry {
    void reportThreadPool(List<ThreadPoolConfigEntity> threadPoolEntities);

    void reportThreadPoolConfigParameter(ThreadPoolConfigEntity threadPoolConfigEntity);
}
