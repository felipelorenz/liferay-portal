/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.upgrade.v1_0_0;

import com.liferay.portal.configuration.persistence.upgrade.ConfigurationUpgradeStepFactory;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Bryan Engler
 */
public class ElasticsearchConfigurationUpgradeProcess extends UpgradeProcess {

	public ElasticsearchConfigurationUpgradeProcess(
		ConfigurationAdmin configurationAdmin,
		ConfigurationUpgradeStepFactory configurationUpgradeStepFactory) {

		_configurationAdmin = configurationAdmin;
		_configurationUpgradeStepFactory = configurationUpgradeStepFactory;
	}

	@Override
	protected void doUpgrade() throws Exception {
		ElasticsearchUpgradeProcessUtil.runUpgradeSteps(
			_configurationAdmin, _configurationUpgradeStepFactory);
	}

	private final ConfigurationAdmin _configurationAdmin;
	private final ConfigurationUpgradeStepFactory
		_configurationUpgradeStepFactory;

}