/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch7.internal.sidecar;

import com.liferay.petra.string.StringBundler;

import java.util.Arrays;
import java.util.List;

/**
 * @author Bryan Engler
 */
public class ElasticsearchDistribution implements Distribution {

	public static final String VERSION = "8.17.3";

	@Override
	public Distributable getElasticsearchDistributable() {
		return new DistributableImpl(
			StringBundler.concat(
				"https://artifacts.elastic.co/downloads/elasticsearch",
				"/elasticsearch-", VERSION, "-linux-x86_64.tar.gz"),
			_ELASTICSEARCH_CHECKSUM);
	}

	@Override
	public List<Distributable> getPluginDistributables() {
		return Arrays.asList(
			new DistributableImpl(
				_getDownloadURLString("analysis-icu"), _ICU_CHECKSUM),
			new DistributableImpl(
				_getDownloadURLString("analysis-kuromoji"), _KUROMOJI_CHECKSUM),
			new DistributableImpl(
				_getDownloadURLString("analysis-smartcn"), _SMARTCN_CHECKSUM),
			new DistributableImpl(
				_getDownloadURLString("analysis-stempel"), _STEMPEL_CHECKSUM));
	}

	private String _getDownloadURLString(String plugin) {
		return StringBundler.concat(
			"https://artifacts.elastic.co/downloads/elasticsearch-plugins/",
			plugin, "/", plugin, "-", VERSION, ".zip");
	}

	private static final String _ELASTICSEARCH_CHECKSUM =
		"6aef4fc84ebbfc98e6662418c734ea89cae8e53a8d6c1fbd5352807bc427e040e62b" +
			"e568b502abdceb7a2f57534eae60e31712d0583d3752fd39b7b8a3632d3b"; //this is still from 7.17.26 need to change to the 8.17.3 no-jdk that we will create

	private static final String _ICU_CHECKSUM =
		"3e3b8e7318a7a04fcddf38bf837e6fe4e7afd6542d92b9db4d2f8b11cf3b8fd85925" +
			"75c1c2fc320738139346add45350f2fbf6f61ab4236cb06590576e1bef41";

	private static final String _KUROMOJI_CHECKSUM =
		"f7e9942e4983498f8fefe4650d0ca016597aa4036cb816213e569a8ee02964100ae0" +
			"457c3aa5ff5bccc7af2e31560e1bc9b733c825430f589472507316ec10f0";

	private static final String _SMARTCN_CHECKSUM =
		"714b3a2f13e093a3cd0c0a7189dd945ad18c2253536ca4542878dbb2a38475c5b53e" +
			"b19e930055c5e0f5dd4246117e09420a03f899f2c64615a426e276dd45e9";

	private static final String _STEMPEL_CHECKSUM =
		"591f8dbffd1611aab491bf1f1c65a70d0ee3c62df2ab53b6d6bdc1323b4b28e161a0" +
			"9d24a38fd5a1051f38a685f036d50f65198a585dea9e4ec2282387ec65a9";

}