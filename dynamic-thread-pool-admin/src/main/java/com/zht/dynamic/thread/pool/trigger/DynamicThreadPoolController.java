package com.zht.dynamic.thread.pool.trigger;

import com.zht.dynamic.thread.pool.sdk.domin.model.entity.ThreadPoolConfigEntity;
import com.zht.dynamic.thread.pool.types.Response;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/dynamic/thread/pool/")
public class DynamicThreadPoolController {

    @Resource
    public RedissonClient redissonClient;

    // 查询API
    @RequestMapping(value = "query_thread_pool_list", method = RequestMethod.GET)
    public Response<List<ThreadPoolConfigEntity>> queryList(){
        try{
            RList<ThreadPoolConfigEntity> cacheList = redissonClient.getList("THREAD_POOL_CONFIG_LIST_KEY");
            return Response.<List<ThreadPoolConfigEntity>>builder()
                    .code(Response.Code.SUCCESS.getCode())
                    .info(Response.Code.SUCCESS.getInfo())
                    .data(cacheList.readAll())
                    .build();
        }catch (Exception e){
            return Response.<List<ThreadPoolConfigEntity>>builder()
                    .code(Response.Code.UN_ERROR.getCode())
                    .info(Response.Code.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "query_thread_pool_config" , method = RequestMethod.GET)
    public Response<ThreadPoolConfigEntity> query(@RequestParam String appName , @RequestParam String threadPoolName){
        try{
            String key = "THREAD_POOL_CONFIG_PARAMETER_LIST_KEY" + "_" + appName + "_" + threadPoolName;
            ThreadPoolConfigEntity threadPoolConfig = redissonClient.<ThreadPoolConfigEntity>getBucket(key).get();
            return Response.<ThreadPoolConfigEntity>builder()
                    .code(Response.Code.SUCCESS.getCode())
                    .info(Response.Code.SUCCESS.getInfo())
                    .data(threadPoolConfig)
                    .build();
        }catch (Exception e){
            return Response.<ThreadPoolConfigEntity>builder()
                    .code(Response.Code.UN_ERROR.getCode())
                    .info(Response.Code.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "update_thread_pool_config" , method = RequestMethod.POST)
    public Response<Boolean> updateThreadPoolConfig(@RequestBody ThreadPoolConfigEntity threadPoolConfigEntity){

        RTopic rTopic = redissonClient.getTopic("DYNAMIC_THREAD_POOL_REDIS_TOPIC" + "_" + threadPoolConfigEntity.getAppName());
        rTopic.publish(threadPoolConfigEntity);
        return Response.<Boolean>builder()
                .code(Response.Code.SUCCESS.getCode())
                .info(Response.Code.SUCCESS.getInfo())
                .data(true)
                .build();
    }
}
