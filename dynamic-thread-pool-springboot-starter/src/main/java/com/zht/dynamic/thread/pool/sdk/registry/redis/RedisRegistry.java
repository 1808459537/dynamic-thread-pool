package com.zht.dynamic.thread.pool.sdk.registry.redis;

import com.zht.dynamic.thread.pool.sdk.domin.model.entity.ThreadPoolConfigEntity;
import com.zht.dynamic.thread.pool.sdk.domin.model.valobj.RegistryEnumVO;
import com.zht.dynamic.thread.pool.sdk.registry.IRegistry;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.*;

public class RedisRegistry implements IRegistry {
    private final RedissonClient redissonClient;

    public RedisRegistry(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void reportThreadPool(List<ThreadPoolConfigEntity> threadPoolEntities) {
        RList<ThreadPoolConfigEntity> list =  redissonClient.getList(RegistryEnumVO.THREAD_POOL_CONFIG_LIST_KEY.getKey());
        if(list.isEmpty())
            list.addAll(threadPoolEntities);
        else {
            for (ThreadPoolConfigEntity th: threadPoolEntities
                 ) {
                for (ThreadPoolConfigEntity ex:list
                     ) {
                    if(th.getThreadPoolName().equals(ex.getThreadPoolName())){
                        list.remove(ex);
                        list.add(th);
                    }
                    else list.add(th);
                }
            }
        }
    }

    @Override
    public void reportThreadPoolConfigParameter(ThreadPoolConfigEntity threadPoolConfigEntity) {
        String cacheKey = RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + threadPoolConfigEntity.getAppName() + "_" + threadPoolConfigEntity.getThreadPoolName();
        RBucket<ThreadPoolConfigEntity> bucket = redissonClient.getBucket(cacheKey);
        bucket.set(threadPoolConfigEntity , Duration.ofDays(3));
    }
}
