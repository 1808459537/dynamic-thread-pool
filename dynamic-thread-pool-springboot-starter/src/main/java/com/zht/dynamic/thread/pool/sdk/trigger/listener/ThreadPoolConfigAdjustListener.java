package com.zht.dynamic.thread.pool.sdk.trigger.listener;

import com.zht.dynamic.thread.pool.sdk.domin.IDynamicThreadPoolService;
import com.zht.dynamic.thread.pool.sdk.domin.model.entity.ThreadPoolConfigEntity;
import com.zht.dynamic.thread.pool.sdk.registry.IRegistry;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;

import java.util.List;

public class ThreadPoolConfigAdjustListener implements MessageListener<ThreadPoolConfigEntity> {
    private Logger logger = LoggerFactory.getLogger(ThreadPoolConfigAdjustListener.class);

    private final IDynamicThreadPoolService dynamicThreadPoolService;
    private final IRegistry registry;

    public ThreadPoolConfigAdjustListener(IDynamicThreadPoolService dynamicThreadPoolService, IRegistry registry) {
        this.dynamicThreadPoolService = dynamicThreadPoolService;
        this.registry = registry;
    }


    @Override
    public void onMessage(CharSequence charSequence, ThreadPoolConfigEntity threadPoolConfigEntity) {
        dynamicThreadPoolService.updateThreadPoolConfig(threadPoolConfigEntity);

        List<ThreadPoolConfigEntity> threadPoolConfigEntities = dynamicThreadPoolService.queryThreadPoolList();
        registry.reportThreadPool(threadPoolConfigEntities);

        ThreadPoolConfigEntity ThreadPoolConfigEntity = dynamicThreadPoolService.queryThreadPoolConfigByName(threadPoolConfigEntity.getThreadPoolName());

        registry.reportThreadPoolConfigParameter(ThreadPoolConfigEntity);
    }
}
