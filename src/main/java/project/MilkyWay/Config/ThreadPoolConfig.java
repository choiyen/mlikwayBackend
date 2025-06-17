package project.MilkyWay.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadPoolConfig
{
    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor executor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);     // vCPU 개수와 동일하게
        executor.setMaxPoolSize(4);      // 코어 2개보다 약간 여유있게
        executor.setQueueCapacity(20);   // 요청 버퍼링용
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }
}
