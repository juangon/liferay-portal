/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
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

package com.liferay.portal.osgi.web.servlet.context.helper.internal.order;

import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.osgi.web.servlet.context.helper.definition.WebXMLDefinition;
import com.liferay.portal.osgi.web.servlet.context.helper.order.Order;
import com.liferay.portal.osgi.web.servlet.context.helper.order.Order.Path;
import com.liferay.portal.osgi.web.servlet.context.helper.order.OrderBeforeAndAfterException;
import com.liferay.portal.osgi.web.servlet.context.helper.order.OrderCircularDependencyException;
import com.liferay.portal.osgi.web.servlet.context.helper.order.OrderMaxAttemptsException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Vernon Singleton
 * @author Juan Gonzalez
 */
public class OrderUtil {

	public static List<WebXMLDefinition> getOrderedWebXMLDefinitions(
			List<WebXMLDefinition> webXMLDefinitions,
			List<String> absoluteOrderNames)
		throws OrderBeforeAndAfterException, OrderCircularDependencyException, 
			   OrderMaxAttemptsException {

		if (ListUtil.isEmpty(absoluteOrderNames)) {
			return _getOrderedWebXMLDefinitions(webXMLDefinitions);
		}

		return _getOrderedWebXMLDefinitions(
			webXMLDefinitions, absoluteOrderNames);
	}

	private static Map<String, WebXMLDefinition> _getWebXMLDefinitionsMap(
		List<WebXMLDefinition> webXMLDefinitions) {

		Map<String, WebXMLDefinition> webXMLDefinitionsMap = new HashMap<>();

		for (WebXMLDefinition webXMLDefinition : webXMLDefinitions) {
			String fragmentName = webXMLDefinition.getFragmentName();

			webXMLDefinitionsMap.put(fragmentName, webXMLDefinition);
		}

		return webXMLDefinitionsMap;
	}

	private static List<WebXMLDefinition> _getOrderedWebXMLDefinitions(
			List<WebXMLDefinition> webXMLDefinitions)
		throws OrderBeforeAndAfterException,
			OrderCircularDependencyException, OrderMaxAttemptsException {

		_checkForSpecExceptions(webXMLDefinitions);

		webXMLDefinitions = _preSort(webXMLDefinitions);

		WebXMLDefinition[] webXMLDefinitionsArray = webXMLDefinitions.toArray(
			new WebXMLDefinition[webXMLDefinitions.size()]);

		_innerSort(webXMLDefinitionsArray);

		_postSort(webXMLDefinitionsArray);

		return new ArrayList<>(Arrays.asList(webXMLDefinitionsArray));
	}

	private static String[] _appendAndSort(String[]... groups) {
		Map<String, Integer> map = new HashMap<>();

		if (groups[0] != null) {
			if (Arrays.binarySearch(groups[0], Order.OTHERS) >= 0) {
				map.put(Order.OTHERS, 1);
			}
		}

		for (String[] group : groups) {
			for (String name : group) {
				if (!name.equals(Order.OTHERS)) {
					map.put(name, 1);
				}
			}
		}

		Set<String> set = map.keySet();

		String[] orderedNames = set.toArray(new String[set.size()]);

		Arrays.sort(orderedNames);

		return orderedNames;
	}

	private static void _checkForBothBeforeAndAfter(WebXMLDefinition webXMLDefinition)
		throws OrderBeforeAndAfterException {

		String fragmentName = webXMLDefinition.getFragmentName();
		Order ordering = webXMLDefinition.getOrder();

		EnumMap<Order.Path, String[]> orderingRoutes =
			ordering.getRoutes();

		HashMap<String, Integer> map = new HashMap<>();

		String[] beforeRoutes = orderingRoutes.get(Order.Path.BEFORE);

		for (String name : beforeRoutes) {
			Integer value = map.get(name);

			if (value == null) {
				value = 1;
			}
			else {
				value += 1;
			}

			map.put(name, value);
		}

		String[] afterRoutes = orderingRoutes.get(Order.Path.AFTER);

		for (String name : afterRoutes) {
			Integer value = map.get(name);

			if (value == null) {
				value = 1;
			}
			else {
				value += 1;
			}

			map.put(name, value);
		}

		Set<String> keySet = map.keySet();

		String[] namesToCheck = keySet.toArray(new String[keySet.size()]);

		for (String name : namesToCheck) {
			if (map.get(name) > 1) {
				throw new OrderBeforeAndAfterException(fragmentName, name);
			}
		}
	}

	private static void _checkForSpecExceptions(List<WebXMLDefinition> configs)
		throws OrderBeforeAndAfterException,
			OrderCircularDependencyException {

		for (WebXMLDefinition config : configs) {
			_checkForBothBeforeAndAfter(config);

			for (Order.Path path : Order.Path.values()) {
				_mapRoutes(config, path, configs);
			}
		}
	}

	private static Map<String, Integer> _descendingByValue(
		Map<String, Integer> map) {

		List<Map.Entry<String, Integer>> list = new LinkedList<>(
			map.entrySet());

		Collections.sort(list, _COMPARATOR);

		Map<String, Integer> result = new LinkedHashMap<>();

		for (Map.Entry<String, Integer> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}

	private static LinkedList<String> _extractNamesList(
		WebXMLDefinition[] configs) {

		LinkedList<String> names = new LinkedList<>();

		for (WebXMLDefinition config : configs) {
			names.add(config.getFragmentName());
		}

		return names;
	}

	private static List<WebXMLDefinition> _getOrderedWebXMLDefinitions(
		List<WebXMLDefinition> configs, List<String> absoluteOrder) {

		List<WebXMLDefinition> orderedList = new ArrayList<>();

		List<WebXMLDefinition> configList = new CopyOnWriteArrayList<>();

		configList.addAll(configs);

		for (String name : absoluteOrder) {
			if (Order.OTHERS.equals(name)) {
				continue;
			}

			boolean found = false;

			for (WebXMLDefinition config : configList) {
				String fragmentName = config.getFragmentName();

				if (!found && name.equals(fragmentName)) {
					found = true;

					orderedList.add(config);

					configList.remove(config);
				}
				else if (found && name.equals(fragmentName)) {
					break;
				}
			}
		}

		int othersIndex = absoluteOrder.indexOf(Order.OTHERS);

		if (othersIndex != -1) {
			for (WebXMLDefinition config : configList) {
				orderedList.add(othersIndex, config);
			}
		}

		return orderedList;
	}

	private static int _innerSort(WebXMLDefinition[] configs)
		throws OrderMaxAttemptsException {

		int attempts = 0;
		boolean attempting = true;

		while (attempting) {
			if (attempts > _MAX_ATTEMPTS) {
				throw new OrderMaxAttemptsException(_MAX_ATTEMPTS);
			}

			attempting = false;

			int last = configs.length - 1;

			for (int i = 0; i < configs.length; i++) {
				int first = i;
				int second = first + 1;

				if (first == last) {
					second = first;
					first = 0;
				}

				if (_isDisordered(configs[first], configs[second])) {
					WebXMLDefinition temp = configs[first];

					configs[first] = configs[second];
					configs[second] = temp;

					attempting = true;
				}
			}

			attempts++;
		}

		return attempts;
	}

	private static boolean _isDisordered(
		WebXMLDefinition config1, WebXMLDefinition config2) {

		String config1Name = config1.getFragmentName();
		String config2Name = config2.getFragmentName();

		Order config1Ordering = config1.getOrder();
		Order config2Ordering = config2.getOrder();

		if (config1Ordering.isOrdered() && !config2Ordering.isOrdered()) {
			EnumMap<Path, String[]> routes = config1Ordering.getRoutes();

			if (!ArrayUtil.isEmpty(routes.get(Order.Path.AFTER)) &&
				!config1Ordering.isBeforeOthers()) {

				return true;
			}
		}

		// they are not in the specified order

		if (config2Ordering.isBefore(config1Name) ||
			config1Ordering.isAfter(config2Name)) {

			return true;
		}

		// config1 should be after others, but it is not

		if (config1Ordering.isAfterOthers() &&
			!config1Ordering.isBefore(config2Name) &&
			!(config1Ordering.isAfterOthers() &&
			config2Ordering.isAfterOthers())) {

			return true;
		}

		// config2 should be before others, but it is not

		if (config2Ordering.isBeforeOthers() &&
			!config2Ordering.isAfter(config1Name) &&
			!(config1Ordering.isBeforeOthers() &&
			config2Ordering.isBeforeOthers())) {

			return true;
		}

		return false;
	}

	private static void _mapRoutes(
			WebXMLDefinition config, Order.Path path,
			List<WebXMLDefinition> webXMLs)
		throws OrderCircularDependencyException {

		String configName = config.getFragmentName();
		Order configOrdering = config.getOrder();

		EnumMap<Order.Path, String[]> configOrderingRoutes =
			configOrdering.getRoutes();
		String[] routePathNames = configOrderingRoutes.get(path);

		for (String routePathName : routePathNames) {
			if (routePathName.equals(Order.OTHERS)) {
				continue;
			}

			for (WebXMLDefinition otherConfig : webXMLs) {
				String otherConfigName = otherConfig.getFragmentName();

				if (!routePathName.equals(otherConfigName)) {
					continue;
				}

				Order otherConfigOrdering = otherConfig.getOrder();

				EnumMap<Order.Path, String[]> otherConfigOrderingRoutes =
					otherConfigOrdering.getRoutes();

				String[] otherRoutePathNames = otherConfigOrderingRoutes.get(
					path);

				if (Arrays.binarySearch(otherRoutePathNames, configName) >= 0) {
					throw new OrderCircularDependencyException(path, webXMLs);
				}

				// If I am before them, they should be informed
				// that they are after me. Similarly, if I am after
				// them, then they should be informed that they are
				// before me.

				Order.Path oppositePath;

				if (path == Order.Path.BEFORE) {
					oppositePath = Order.Path.AFTER;
				}
				else {
					oppositePath = Order.Path.BEFORE;
				}

				String[] oppositePathNames = otherConfigOrderingRoutes.get(
					oppositePath);

				if (Arrays.binarySearch(oppositePathNames, configName) < 0) {
					EnumMap<Order.Path, String[]> routes = new EnumMap<>(
						Order.Path.class);

					routes.put(path, otherRoutePathNames);
					routes.put(
						oppositePath,
						_appendAndSort(
							otherConfigOrderingRoutes.get(oppositePath),
							new String[] {configName}));

					otherConfigOrdering.setRoutes(routes);
				}

				// If I am before them and they are before others,
				// then I should be informed that I am before
				// others too. Similarly, if I am after them and
				// they are after others, then I should be informed
				// that I am after others too.

				if (ArrayUtil.isNotEmpty(otherRoutePathNames)) {
					EnumMap<Order.Path, String[]> routes = new EnumMap<>(
						Order.Path.class);

					routes.put(
						path,
						_appendAndSort(routePathNames, otherRoutePathNames));
					routes.put(
						oppositePath, configOrderingRoutes.get(oppositePath));

					configOrdering.setRoutes(routes);
				}
			}
		}
	}

	private static void _postSort(WebXMLDefinition[] configs) {
		int i = 0;

		while (i < configs.length) {
			LinkedList<String> names = _extractNamesList(configs);

			boolean done = true;

			for (int j = 0; j < configs.length; j++) {
				int k = 0;

				for (String configName : names) {
					String fragmentName = configs[j].getFragmentName();

					if (fragmentName.equals(configName)) {
						break;
					}

					Order ordering = configs[j].getOrder();

					if (ordering.isBefore(configName)) {

						// We have a document that is out of order,
						// and his index is k, he belongs at index j, and all
						// the documents in between need to be shifted left.

						WebXMLDefinition temp = null;

						for (int m = 0; m < configs.length; m++) {

							// This is one that is out of order and needs
							// to be moved.

							if (m == k) {
								temp = configs[m];
							}

							// This is one in between that needs to be shifted
							// left.

							if ((temp != null) && (m != j)) {
								configs[m] = configs[m + 1];
							}

							// This is where the one that is out of order needs

							//to be moved to.

							if (m == j) {
								configs[m] = temp;

								done = false;

								break;
							}
						}

						if (!done) {
							break;
						}
					}

					k = k + 1;
				}
			}

			if (done) {
				break;
			}
		}
	}

	private static List<WebXMLDefinition> _preSort(
		List<WebXMLDefinition> configs) {

		List<WebXMLDefinition> newConfigList = new ArrayList<>();
		List<WebXMLDefinition> anonAndUnordered = new LinkedList<>();
		Map<String, Integer> namedMap = new LinkedHashMap<>();

		for (WebXMLDefinition config : configs) {
			Order configOrdering = config.getOrder();

			EnumMap<Order.Path, String[]> configOrderingRoutes =
				configOrdering.getRoutes();
			String[] beforePathNames = configOrderingRoutes.get(
				Order.Path.BEFORE);
			String[] afterPathNames = configOrderingRoutes.get(
				Order.Path.AFTER);

			String configName = config.getFragmentName();

			if (Validator.isNull(configName) && !configOrdering.isOrdered()) {
				anonAndUnordered.add(config);
			}
			else {
				int totalPathNames =
					beforePathNames.length + afterPathNames.length;
				namedMap.put(configName, totalPathNames);
			}
		}

		namedMap = _descendingByValue(namedMap);

		Map<String, WebXMLDefinition> configMap = _getWebXMLDefinitionsMap(
			configs);

		// add named configs to the list in the correct preSorted order

		for (Map.Entry<String, Integer> entry : namedMap.entrySet()) {
			String key = entry.getKey();
			newConfigList.add(configMap.get(key));
		}

		// add configs that are both anonymous and unordered, to the list in
		// their original, incoming order

		for (WebXMLDefinition config : anonAndUnordered) {
			newConfigList.add(config);
		}

		return newConfigList;
	}

	private static final MapEntryComparator _COMPARATOR =
		new MapEntryComparator();

	private static final int _MAX_ATTEMPTS =
		(Integer.MAX_VALUE / (Byte.MAX_VALUE * Byte.MAX_VALUE *
			Byte.MAX_VALUE));

	private static class MapEntryComparator
		implements Comparator<Map.Entry<String, Integer>> {

		@Override
		public int compare(
			Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {

			Integer valueA = a.getValue();
			Integer valueB = b.getValue();

			return valueB.compareTo(valueA);
		}

	}

}