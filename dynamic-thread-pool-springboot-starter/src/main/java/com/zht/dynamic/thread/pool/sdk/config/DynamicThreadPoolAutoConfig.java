package com.zht.dynamic.thread.pool.sdk.config;

import com.alibaba.fastjson.JSON;
import com.zht.dynamic.thread.pool.sdk.domin.DynamicThreadPoolService;
import com.zht.dynamic.thread.pool.sdk.domin.IDynamicThreadPoolService;
import com.zht.dynamic.thread.pool.sdk.domin.model.entity.ThreadPoolConfigEntity;
import com.zht.dynamic.thread.pool.sdk.domin.model.valobj.RegistryEnumVO;
import com.zht.dynamic.thread.pool.sdk.registry.IRegistry;
import com.zht.dynamic.thread.pool.sdk.registry.redis.RedisRegistry;
import com.zht.dynamic.thread.pool.sdk.trigger.job.ThreadPoolDataReportJob;
import com.zht.dynamic.thread.pool.sdk.trigger.listener.ThreadPoolConfigAdjustListener;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;


@Configuration
@EnableScheduling
@EnableConfigurationProperties(DynamicThreadPoolAutoProperties.class)
public class DynamicThreadPoolAutoConfig {
    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolAutoConfig.class);

    String applicationName;
    @Bean("redissonClient")
    public RedissonClient redissonClient(DynamicThreadPoolAutoProperties properties) {
        Config config = new Config();
        // 根据需要可以设定编解码器；https://github.com/redisson/redisson/wiki/4.-%E6%95%B0%E6%8D%AE%E5%BA%8F%E5%88%97%E5%8C%96
        config.setCodec(JsonJacksonCodec.INSTANCE);

        config.useSingleServer()
                .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                .setPassword(properties.getPassword())
                .setConnectionPoolSize(properties.getPoolSize())
                .setConnectionMinimumIdleSize(properties.getMinIdleSize())
                .setIdleConnectionTimeout(properties.getIdleTimeout())
                .setConnectTimeout(properties.getConnectTimeout())
                .setRetryAttempts(properties.getRetryAttempts())
                .setRetryInterval(properties.getRetryInterval())
                .setPingConnectionInterval(properties.getPingInterval())
                .setKeepAlive(properties.isKeepAlive())
        ;

        RedissonClient redissonClient = Redisson.create(config);

        logger.info("动态线程池，注册器（redis）链接初始化完成。{} {} {}", properties.getHost(), properties.getPoolSize(), !redissonClient.isShutdown());

        return redissonClient;
    }

    @Bean
    public IRegistry redisRegistry(RedissonClient redissonClient){
        return new RedisRegistry(redissonClient);
    }

    @Bean("dynamicThreadPollService")
    public DynamicThreadPoolService dynamicThreadPollService(ApplicationContext applicationContext , Map<String ,ThreadPoolExecutor> threadPoolExecutorMap){
       applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");

       return new DynamicThreadPoolService(applicationName , threadPoolExecutorMap);
    }

    @Bean()
    public ThreadPoolDataReportJob threadPoolDataReportJob(IDynamicThreadPoolService DynamicThreadPoolService , IRegistry registry){
        return new ThreadPoolDataReportJob(DynamicThreadPoolService , registry);
    }

    @Bean
    public ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener(IDynamicThreadPoolService dynamicThreadPoolService, IRegistry registry){
        return new ThreadPoolConfigAdjustListener(dynamicThreadPoolService ,registry);
    }
    @Bean("dynamicThreadPoolRedisTopic")
    public RTopic threadPoolConfigAdjustListener(RedissonClient redissonClient , ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener){
        RTopic topic = redissonClient.getTopic(RegistryEnumVO.DYNAMIC_THREAD_POOL_REDIS_TOPIC.getKey()+ "_" +applicationName);
        topic.addListener(ThreadPoolConfigEntity.class , threadPoolConfigAdjustListener);
        return topic;
    }
}

