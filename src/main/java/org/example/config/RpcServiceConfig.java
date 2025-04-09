package org.example.config;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceConfig {

    @Builder.Default
    private String version = "";

    @Builder.Default
    private String group = "";

    private Object service;

    /**
     * 生成服务的唯一标识符
     * @return 唯一标识符：接口名 + 分组 + 版本号
     */
    public String getRpcServiceName() {
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }

    /**
     * 获取服务的接口全限定名
     * @return 服务的接口全限定名
     */
    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
