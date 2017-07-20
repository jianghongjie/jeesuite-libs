package com.jeesuite.confcenter.springboot;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.jeesuite.common.util.ResourceUtils;
import com.jeesuite.confcenter.ConfigcenterContext;

public class CCPropertySourceLoader implements PropertySourceLoader,PriorityOrdered {

	private ConfigcenterContext ccContext = ConfigcenterContext.getInstance();
	@Override
	public String[] getFileExtensions() {
		return new String[] { "properties"};
	}

	@Override
	public PropertySource<?> load(String name, Resource resource, String profile)
			throws IOException {
		if (profile == null) {
			Properties properties = PropertiesLoaderUtils.loadProperties(resource);
			ResourceUtils.merge(properties);
			
			ccContext.init();
			
			Properties remoteProperties = ccContext.getAllRemoteProperties();
			if(remoteProperties != null){
				Set<Entry<Object, Object>> entrySet = remoteProperties.entrySet();
				for (Entry<Object, Object> entry : entrySet) {
					//本地配置优先
					if(ccContext.isRemoteFirst() == false && properties.containsKey(entry.getKey()))continue;
					properties.put(entry.getKey(), entry.getValue());
				}
			}
			
			if (!properties.isEmpty()) {
				return new PropertiesPropertySource(name, properties);
			}
		}
		return null;
	}
	
	@Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

}
