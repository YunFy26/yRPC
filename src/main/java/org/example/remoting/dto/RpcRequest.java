package org.example.remoting.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RpcRequest implements Serializable {
    @Serial
    private static final long serialVersionUID  = 1905122041950251207L;
    private String requestId;
    private String interfaceName;
    private String methodName;
    // TODO: kryo 注册 序列化
    private Object[] parameters;
    // TODO: kryo 注册 序列化
    private Class<?>[] paramTypes;
    private String version;
    private String group;

    public String getRpcServiceName() {
        return this.getInterfaceName() + this.getGroup() + this.getVersion();
    }
}
