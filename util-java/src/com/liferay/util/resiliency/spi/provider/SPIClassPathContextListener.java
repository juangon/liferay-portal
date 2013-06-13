/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.util.resiliency.spi.provider;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.process.ClassPathUtil;
import com.liferay.portal.kernel.resiliency.mpi.MPIHelperUtil;
import com.liferay.portal.kernel.resiliency.spi.provider.SPIProvider;
import com.liferay.portal.kernel.util.ReflectionUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;

import java.io.File;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Shuyang Zhou
 */
public class SPIClassPathContextListener implements ServletContextListener {

	public static volatile String SPI_CLASS_PATH = StringPool.BLANK;

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		SPIProvider spiProvider = spiProviderReference.getAndSet(null);

		if (spiProvider != null) {
			MPIHelperUtil.unregisterSPIProvider(spiProvider);
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		ServletContext servletContext = servletContextEvent.getServletContext();

		String contextPath = servletContext.getRealPath(StringPool.BLANK);
		String spiEmbeddedLibDirName = servletContext.getInitParameter(
			"spiEmbeddedLibDir");

		File spiEmbeddedLibDir = new File(contextPath, spiEmbeddedLibDirName);

		if (!spiEmbeddedLibDir.exists() || !spiEmbeddedLibDir.isDirectory()) {
			_log.error(
				"Unable to find SPI embedded lib directory " +
					spiEmbeddedLibDir.getAbsolutePath());

			return;
		}

		List<File> jarFiles = new ArrayList<File>();

		for (File file : spiEmbeddedLibDir.listFiles()) {
			String fileName = file.getName();

			if (fileName.endsWith(".jar")) {
				jarFiles.add(file);
			}
		}

		StringBundler sb = new StringBundler(jarFiles.size() * 2 + 4);

		for (File file : jarFiles) {
			sb.append(file.getAbsolutePath());
			sb.append(File.pathSeparator);
		}

		if (_log.isDebugEnabled()) {
			_log.debug("SPI embedded lib class path " + sb.toString());
		}

		sb.append(contextPath);
		sb.append("/WEB-INF/classes");
		sb.append(File.pathSeparator);

		sb.append(ClassPathUtil.getGlobalClassPath());

		SPI_CLASS_PATH = sb.toString();

		if (_log.isDebugEnabled()) {
			_log.debug("SPI class path " + SPI_CLASS_PATH);
		}

		String spiProviderClassName = servletContext.getInitParameter(
			"spiProviderClassName");

		Thread currentThread = Thread.currentThread();

		ClassLoader contextClassLoader = currentThread.getContextClassLoader();

		try {
			Class<SPIProvider> spiProviderClass =
				(Class<SPIProvider>)loadClassDirectly(
					contextClassLoader, spiProviderClassName);

			SPIProvider spiProvider = spiProviderClass.newInstance();

			boolean result = spiProviderReference.compareAndSet(
				null, spiProvider);

			if (!result) {
				_log.error(
					"Duplicate SPI provider " + spiProvider +
						" is already registered in servlet context " +
							servletContext.getContextPath());
			}
			else {
				MPIHelperUtil.registerSPIProvider(spiProvider);
			}
		}
		catch (Exception e) {
			_log.error(
				"Unable to create SPI provider with name " +
					spiProviderClassName,
				e);
		}
	}

	protected static Class<?> loadClassDirectly(
			ClassLoader classLoader, String className)
		throws Exception {

		synchronized (classLoader) {
			Method findLoadedClassMethod = ReflectionUtil.getDeclaredMethod(
				ClassLoader.class, "findLoadedClass", String.class);

			Class<?> clazz = (Class<?>)findLoadedClassMethod.invoke(
				classLoader, className);

			if (clazz == null) {
				Method findClassMethod = ReflectionUtil.getDeclaredMethod(
					ClassLoader.class, "findClass", String.class);

				clazz = (Class<?>)findClassMethod.invoke(
					classLoader, className);
			}

			Method resolveClassMethod = ReflectionUtil.getDeclaredMethod(
				ClassLoader.class, "resolveClass", Class.class);

			resolveClassMethod.invoke(classLoader, clazz);

			return clazz;
		}
	}

	protected static final AtomicReference<SPIProvider> spiProviderReference =
		new AtomicReference<SPIProvider>();

	private static Log _log = LogFactoryUtil.getLog(
		SPIClassPathContextListener.class);

}