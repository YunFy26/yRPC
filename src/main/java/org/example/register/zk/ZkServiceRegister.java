package org.example.register.zk;

import org.apache.curator.framework.CuratorFramework;
import org.example.register.ServiceRegister;
import org.example.register.zk.utils.CuratorUtils;

import java.net.InetSocketAddress;


/**
 * TODO: Zookeeper
 */
public class ZkServiceRegister implements ServiceRegister {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient, servicePath);
    }
}
