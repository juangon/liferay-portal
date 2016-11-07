package com.liferay.portal.osgi.web.wab.extender.internal.registration;

import com.liferay.portal.osgi.web.wab.extender.WabInitializer;
import com.liferay.portal.osgi.web.wab.extender.WabInitializerRegistry;
import org.osgi.service.component.annotations.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component(immediate = true)
public class WabInitializerRegistryImpl implements WabInitializerRegistry {

	@Override
	public void addWabInitializer(WabInitializer wabInitializer) {
		_wabInitializers.add(wabInitializer);
	}

	@Override
	public List<WabInitializer> getWabInitializers() {
		return Collections.unmodifiableList(_wabInitializers);
	}

	@Override
	public boolean removeWabInitializer(WabInitializer wabInitializer) {
		return _wabInitializers.remove(wabInitializer);
	}

	private final List<WabInitializer> _wabInitializers =
		new CopyOnWriteArrayList<>();
}
