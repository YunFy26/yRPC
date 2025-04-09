package org.example.register.zk;

import org.apache.curator.framework.CuratorFramework;
import org.example.enums.LoadBalanceEnum;
import org.example.enums.RpcErrorMessageEnum;
import org.example.exception.RpcException;
import org.example.loadbalance.LoadBalance;
import org.example.register.ServiceDiscover;
import org.example.register.zk.utils.CuratorUtils;
import org.example.remoting.dto.RpcRequest;
import org.example.spi.ExtensionLoader;
import org.example.utils.CollectionUtil;

import java.net.InetSocketAddress;
import java.util.List;

public class ZkServiceDiscover implements ServiceDiscover {

//    private final LoadBalance loadBalance;

    public ZkServiceDiscover() {
//        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(LoadBalanceEnum.LOADBALANCE.getName());
    }
    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if (CollectionUtil.isEmpty(serviceUrlList)) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
        String targetServiceUrl = serviceUrlList.get(0);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);

        return new InetSocketAddress(host, port);
        // load balancing
//        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
//        log.info("Successfully found the service address:[{}]", targetServiceUrl);
//        String[] socketAddressArray = targetServiceUrl.split(":");
//        String host = socketAddressArray[0];
//        int port = Integer.parseInt(socketAddressArray[1]);
//        return new InetSocketAddress(host, port);
    }
}
