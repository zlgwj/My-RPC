package com.zlgewj.provider.zk;

import cn.hutool.core.net.Ipv4Util;
import cn.hutool.core.net.NetUtil;
import com.zlgewj.config.RpcServiceDefinition;
import com.zlgewj.constants.PropertyConstant;
import com.zlgewj.exception.RpcException;
import com.zlgewj.extension.ExtensionLoader;
import com.zlgewj.provider.ServiceProvider;
import com.zlgewj.registry.ServiceRegistry;
import com.zlgewj.serialize.Serializer;
import com.zlgewj.utils.IPV4Util;
import com.zlgewj.utils.PropertiesUtil;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zlgewj
 * @version 1.0
 * @Date 2023/7/31 13:13
 */
public class ZkServiceProvider implements ServiceProvider {

    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;

    public ZkServiceProvider() {

        this.serviceMap = new ConcurrentHashMap<>();
        this.registeredService = ConcurrentHashMap.newKeySet();
        this.serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(PropertiesUtil.getProperty(PropertyConstant.DISCOVERY_TYPE));
    }

    @Override
    public void addService(RpcServiceDefinition rpcServiceDefinition) {
        if (registeredService.contains(rpcServiceDefinition.getServiceName())) return;
        String serviceName = rpcServiceDefinition.getServiceName();
        serviceMap.put(serviceName, rpcServiceDefinition.getService());
        registeredService.add(serviceName);
    }

    @Override
    public Object getService(String name) throws RpcException {
        if (registeredService.contains(name)) {
            return serviceMap.get(name);
        }else {
            throw new RpcException("服务["+name+"]未找到");
        }
    }

    @Override
    public void publishService(RpcServiceDefinition rpcServiceDefinition) {
        addService(rpcServiceDefinition);
        serviceRegistry.registerService(rpcServiceDefinition.getServiceName(), new InetSocketAddress(IPV4Util.getLocalHostExactAddress().getHostAddress(),PropertiesUtil.getServerPort()));
    }
}
