package me.izhong.jobs.manage.impl.router;

import com.alipay.sofa.rpc.config.ConsumerConfig;
import me.izhong.jobs.manage.IJobAgentMngFacade;

import java.util.HashMap;

public class SelectService {

    public static void main(String[] args) {
        ConsumerConfig<IJobAgentMngFacade> consumerConfig = new ConsumerConfig<IJobAgentMngFacade>()
                .setInterfaceId(IJobAgentMngFacade.class.getName()) // 指定接口
                .setProtocol("bolt") // 指定协议
                .setDirectUrl("bolt://127.0.0.1:12200"); // 指定直连地址
        // 生成代理类
        //consumerConfig.getProviderInfoListener().
        IJobAgentMngFacade helloService = consumerConfig.refer();
        while (true) {
            System.out.println(helloService.trigger(1L, 2L,
                    new HashMap<String, String>() {{
                    }},
                    new HashMap<String, String>() {{
                    }}));
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
            }
        }
    }

}
