package org.example.provider.impl;

import org.example.config.RpcServiceConfig;
import org.example.enums.RpcErrorMessageEnum;
import org.example.enums.ServiceRegisterEnum;
import org.example.exception.RpcException;
import org.example.provider.ServiceProvider;
import org.example.register.ServiceRegister;
import org.example.remoting.transport.server.RpcServer;
import org.example.spi.ExtensionLoader;

import javax.imageio.spi.ServiceRegistry;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ZkServiceProvider implements ServiceProvider {

    private final Map<String, Object> serviceMap;

    private final Set<String> registeredService;

    private final ServiceRegister serviceRegister;

    public ZkServiceProvider() {
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegister = ExtensionLoader.getExtensionLoader(ServiceRegister.class).getExtension(ServiceRegisterEnum.ZK.getName());
    }

    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        // TODO:ZkServiceProvider 中的 addService
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if (registeredService.contains(rpcServiceName)) {
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, rpcServiceConfig.getService());
//        log.info("Add service: {} and interfaces:{}", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());
    }

    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if (service == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        // TODO:ZkServiceProvider 中的 publishService
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            serviceRegister.registerService(rpcServiceConfig.getRpcServiceName(), new InetSocketAddress(host, RpcServer.PORT));
            addService(rpcServiceConfig);
        } catch (UnknownHostException e) {
            System.out.println(1);
//            log.error("occur exception when getHostAddress", e);
        }
    }
}
