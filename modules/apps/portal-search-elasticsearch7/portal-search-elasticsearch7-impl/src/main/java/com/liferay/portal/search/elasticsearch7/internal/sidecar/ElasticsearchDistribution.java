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

	public static final String VERSION = "7.17.17";

	@Override
	public Distributable getElasticsearchDistributable() {
		return new DistributableImpl(
			StringBundler.concat(
				"https://artifacts.elastic.co/downloads/elasticsearch",
				"/elasticsearch-", VERSION, "-no-jdk-linux-x86_64.tar.gz"),
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
		"117dbbf3e095302df5bb133a3179e997f811f8904cb4e20708d890bfec601c6bf118" +
			"b51fc0b51c50413e4a036f7f6bacae2780638e62c44d0b55cbf854589bb9";

	private static final String _ICU_CHECKSUM =
		"8b028b62d54d535c9810f509c82982d3ef7913c947ee7072c52cd4f23405413ee6ee" +
			"9d056f05207b3e32804d78cafbecd4b17ad804d513e2e11be9a70921a763";

	private static final String _KUROMOJI_CHECKSUM =
		"ce66ae50edef982082d6e65da71fb34c9f0192bb43741e61d32ab1d6a74c0df804f9" +
			"1f9da2cbd846e8d5468d54f9064bbaec90b08d14d4cc28c4948bfa11c952";

	private static final String _SMARTCN_CHECKSUM =
		"7af7693d1e02c616846f6a56b8ae72e9d8b74e217bda6fb1258cffe27fb426172d73" +
			"832737cbcc9bacc89e861c69dd57a801ef58a4e0481356b07380c2c2f941";

	private static final String _STEMPEL_CHECKSUM =
		"f96e7e4c2bbc86deff0635fe510d38381a97fff3e8d802249b6ea40ef46655fbf278" +
			"34b9bd9e8ebf9fb8e38235999085372c2f76e6e3fb56bda027093a1113ea";

}