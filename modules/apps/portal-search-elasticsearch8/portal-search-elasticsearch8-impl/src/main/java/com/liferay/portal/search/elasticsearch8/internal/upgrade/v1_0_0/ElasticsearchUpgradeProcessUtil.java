/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.upgrade.v1_0_0;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.persistence.upgrade.ConfigurationUpgradeStepFactory;
import com.liferay.portal.file.install.constants.FileInstallConstants;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Release;
import com.liferay.portal.kernel.service.ReleaseLocalServiceUtil;
import com.liferay.portal.kernel.upgrade.UpgradeStep;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.elasticsearch8.configuration.ElasticsearchConfiguration;
import com.liferay.portal.search.elasticsearch8.configuration.ElasticsearchConnectionConfiguration;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Joshua Cords
 */
public class ElasticsearchUpgradeProcessUtil {

	public static void doUpgrade(
			ConfigurationAdmin configurationAdmin,
			ConfigurationUpgradeStepFactory configurationUpgradeStepFactory)
		throws Exception {

		runUpgradeSteps(configurationAdmin, configurationUpgradeStepFactory);
	}

	public static void runUpgradeSteps(
			ConfigurationAdmin configurationAdmin,
			ConfigurationUpgradeStepFactory configurationUpgradeStepFactory)
		throws Exception {

		_upgradeElasticsearchConfiguration(
			configurationAdmin, configurationUpgradeStepFactory);
		_upgradeElasticsearchConnectionConfigurations(
			configurationAdmin, configurationUpgradeStepFactory);
	}

	public static void upgrade(
		BundleContext bundleContext, ConfigurationAdmin configurationAdmin) {

		if (!_upgraded.compareAndSet(false, true)) {
			return;
		}

		Bundle bundle = bundleContext.getBundle();

		Release release = ReleaseLocalServiceUtil.fetchRelease(
			bundle.getSymbolicName());

		if ((release == null) ||
			!Objects.equals(release.getSchemaVersion(), "0.0.1")) {

			return;
		}

		try {
			Configuration[] elasticsearch8configurations =
				configurationAdmin.listConfigurations(
					String.format(
						"(service.pid=%s)",
						ElasticsearchConfiguration.class.getName()));

			if (ArrayUtil.isNotEmpty(elasticsearch8configurations)) {
				return;
			}

			Configuration[] elasticsearch7configurations =
				configurationAdmin.listConfigurations(
					String.format(
						"(service.pid=%s)",
						_CLASS_NAME_ELASTICSEARCH7_CONFIGURATION));

			if (ArrayUtil.isEmpty(elasticsearch7configurations)) {
				return;
			}

			ServiceReference<ConfigurationUpgradeStepFactory> serviceReference =
				bundleContext.getServiceReference(
					ConfigurationUpgradeStepFactory.class);

			if (serviceReference == null) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Skipping Elasticsearch configuration upgrade " +
							"because ConfigurationUpgradeStepFactory is not " +
								"yet available");
				}

				return;
			}

			try {
				runUpgradeSteps(
					configurationAdmin,
					bundleContext.getService(serviceReference));
			}
			finally {
				bundleContext.ungetService(serviceReference);
			}

			ReleaseLocalServiceUtil.updateRelease(
				bundle.getSymbolicName(), "1.0.0", "0.0.1");
		}
		catch (Exception exception) {
			_log.error(
				"Unable to upgrade Elasticsearch configuration", exception);
		}
	}

	private static void _updateProperties(
		Dictionary<String, Object> properties) {

		properties.remove("discoveryZenPingUnicastHostsPort");

		Object embeddedHttpPort = properties.remove("embeddedHttpPort");
		String sidecarHttpPort = GetterUtil.getString(
			properties.get("sidecarHttpPort"));

		if ((embeddedHttpPort != null) &&
			sidecarHttpPort.equals(StringPool.BLANK)) {

			properties.put("sidecarHttpPort", String.valueOf(embeddedHttpPort));
		}

		String operationMode = GetterUtil.getString(
			properties.remove("operationMode"));

		if (StringUtil.equals(operationMode, "REMOTE")) {
			if (_log.isWarnEnabled()) {
				_log.warn("The operationMode property is no longer supported");
			}

			properties.put("productionModeEnabled", Boolean.TRUE);
		}

		Object trackTotalHits = properties.remove("trackTotalHits");

		if ((trackTotalHits != null) &&
			!GetterUtil.getBoolean(trackTotalHits)) {

			int indexMaxResultWindow = GetterUtil.getInteger(
				properties.get("indexMaxResultWindow"));

			if (indexMaxResultWindow > 0) {
				properties.put("trackTotalHitsLimit", indexMaxResultWindow);
			}
		}

		properties.remove("restClientLoggerLevel");
	}

	private static void _upgradeElasticsearchConfiguration(
			ConfigurationAdmin configurationAdmin,
			ConfigurationUpgradeStepFactory configurationUpgradeStepFactory)
		throws Exception {

		Configuration elasticsearch7configuration =
			configurationAdmin.getConfiguration(
				_CLASS_NAME_ELASTICSEARCH7_CONFIGURATION, StringPool.QUESTION);

		Dictionary<String, Object> elasticsearch7properties =
			elasticsearch7configuration.getProperties();

		if (elasticsearch7properties == null) {
			return;
		}

		if (_log.isWarnEnabled()) {
			_log.warn(
				"Elasticsearch 7 configuration detected. Attempting to " +
					"migrate properties to Elasticsearch 8. Manual updates " +
						"to the configuration may be required.");
		}

		_updateProperties(elasticsearch7properties);

		UpgradeStep upgradeStep =
			configurationUpgradeStepFactory.createUpgradeStep(
				_CLASS_NAME_ELASTICSEARCH7_CONFIGURATION,
				ElasticsearchConfiguration.class.getName());

		upgradeStep.upgrade();

		Configuration elasticsearch8configuration =
			configurationAdmin.getConfiguration(
				ElasticsearchConfiguration.class.getName(),
				StringPool.QUESTION);

		Dictionary<String, Object> elasticsearch8properties =
			elasticsearch8configuration.getProperties();

		_updateProperties(elasticsearch8properties);

		Enumeration<String> enumeration = elasticsearch7properties.keys();

		while (enumeration.hasMoreElements()) {
			String key = enumeration.nextElement();

			if (key.equals(FileInstallConstants.FELIX_FILE_INSTALL_FILENAME) ||
				key.startsWith("service.")) {

				continue;
			}

			elasticsearch8properties.put(
				key, elasticsearch7properties.get(key));
		}

		elasticsearch8configuration.update(elasticsearch8properties);
	}

	private static void _upgradeElasticsearchConnectionConfigurations(
			ConfigurationAdmin configurationAdmin,
			ConfigurationUpgradeStepFactory configurationUpgradeStepFactory)
		throws Exception {

		UpgradeStep upgradeStep =
			configurationUpgradeStepFactory.createUpgradeStep(
				_CLASS_NAME_ELASTICSEARCH7_CONNECTION_CONFIGURATION,
				ElasticsearchConnectionConfiguration.class.getName());

		upgradeStep.upgrade();

		String filterString = String.format(
			"(&(service.factoryPid=%s)(active=%s))",
			ElasticsearchConnectionConfiguration.class.getName(), true);

		Configuration[] configurations = configurationAdmin.listConfigurations(
			filterString);

		if (ArrayUtil.isEmpty(configurations)) {
			return;
		}

		for (Configuration configuration : configurations) {
			Dictionary<String, Object> elasticsearch8properties =
				configuration.getProperties();

			configuration.update(elasticsearch8properties);
		}
	}

	private static final String _CLASS_NAME_ELASTICSEARCH7_CONFIGURATION =
		"com.liferay.portal.search.elasticsearch7.configuration." +
			"ElasticsearchConfiguration";

	private static final String
		_CLASS_NAME_ELASTICSEARCH7_CONNECTION_CONFIGURATION =
			"com.liferay.portal.search.elasticsearch7.configuration." +
				"ElasticsearchConnectionConfiguration";

	private static final Log _log = LogFactoryUtil.getLog(
		ElasticsearchUpgradeProcessUtil.class);

	private static final AtomicBoolean _upgraded = new AtomicBoolean();

}