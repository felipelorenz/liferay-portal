/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.sidecar;

import com.liferay.petra.string.StringBundler;

import java.util.Arrays;
import java.util.List;

/**
 * @author Bryan Engler
 */
public class ElasticsearchDistribution implements Distribution {

	public static final String VERSION = "8.19.18";

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
		"40f72219cdf95b2c9c4d9e3565cceea934d1502c25471b5a9e7568dc97b1db41fa45" +
			"0dad1d91471e40c1e0ed9bad964f0b3c5b10dc3de390871a38c1a260afb1";

	private static final String _ICU_CHECKSUM =
		"1b63f057128f6301b981dc80d5a2a93d1acaa1901420a79b277c1bf845913ed82c3e" +
			"50778481b8c0683ef9fc6023b95f07cc1151cb4f383f62004b162b8b815d";

	private static final String _KUROMOJI_CHECKSUM =
		"41e11e9f220be63ee2ceecfa6f246fd7eb0009dd18ea13e9c869ca5cc6c5ddd50e3e" +
			"5cf2693eafc2981a732eece067ef819c3588966aef4286855d3a49d4f914";

	private static final String _SMARTCN_CHECKSUM =
		"7ad1f5c056561b86008ad86a15f8a741c47b64beeaae6dae8c4f61cf1cfec4c87855" +
			"95eb4e2c927a0c795921ba8f66f30b9893387e10f6cde0d860086fea21fe";

	private static final String _STEMPEL_CHECKSUM =
		"4f362391ada8b28d5e5b4ec07069ddb08cb2e83317f5775e6206d54c61e69cee07a0" +
			"f786fc9f0610f487e348bee0988bd5f2e7fdd8a59460662934f8e787a3b7";

}