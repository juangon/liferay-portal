package com.liferay.portal.cache.ehcache.internal.configurator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import com.liferay.portal.cache.configuration.PortalCacheManagerConfiguration;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringPool;

import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.FactoryConfiguration;

@Component(immediate = true,
		service = MultiVMEhcachePortalCacheManagerConfigurator.class
	)
public class JGroupsMultiVMEhCachePortalCacheManagerConfigurator extends MultiVMEhcachePortalCacheManagerConfigurator {

	@Activate
	@Override
	protected void activate() {
		super.activate();

		if (!clusterEnabled) {
			return;
		}

		_peerProviderFactoryClass = props.get(
			PropsKeys.EHCACHE_JGROUPS_PEER_PROVIDER_FACTORY_CLASS);
		_peerProviderFactoryPropertiesString = getPortalPropertiesString(
			PropsKeys.EHCACHE_JGROUPS_PEER_PROVIDER_FACTORY_PROPERTIES);
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected void manageConfiguration(
		Configuration configuration,
		PortalCacheManagerConfiguration portalCacheManagerConfiguration) {

		if (!clusterEnabled) {
			return;
		}

		super.manageConfiguration(
			configuration, portalCacheManagerConfiguration);

		FactoryConfiguration peerProviderFactoryConfiguration =
			new FactoryConfiguration();

		peerProviderFactoryConfiguration.setClass(_peerProviderFactoryClass);
		peerProviderFactoryConfiguration.setProperties(
			_peerProviderFactoryPropertiesString);
		peerProviderFactoryConfiguration.setPropertySeparator(StringPool.COMMA);

		configuration.addCacheManagerPeerProviderFactory(
			peerProviderFactoryConfiguration);

	}

	private String _peerProviderFactoryClass;
	private String _peerProviderFactoryPropertiesString;

}
