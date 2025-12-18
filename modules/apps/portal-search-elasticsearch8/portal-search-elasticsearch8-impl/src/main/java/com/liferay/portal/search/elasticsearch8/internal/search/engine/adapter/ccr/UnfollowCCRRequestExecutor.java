/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.search.engine.adapter.ccr;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.ccr.ElasticsearchCcrClient;
import co.elastic.clients.elasticsearch.ccr.UnfollowRequest;
import co.elastic.clients.elasticsearch.ccr.UnfollowResponse;

import com.liferay.portal.search.elasticsearch8.internal.connection.ElasticsearchClientResolver;
import com.liferay.portal.search.engine.adapter.ccr.UnfollowCCRRequest;
import com.liferay.portal.search.engine.adapter.ccr.UnfollowCCRResponse;

import java.io.IOException;

/**
 * @author Bryan Engler
 */
public class UnfollowCCRRequestExecutor {

	public UnfollowCCRRequestExecutor(
		ElasticsearchClientResolver elasticsearchClientResolver) {

		_elasticsearchClientResolver = elasticsearchClientResolver;
	}

	public UnfollowCCRResponse execute(UnfollowCCRRequest unfollowCCRRequest) {
		UnfollowRequest unfollowRequest = _createUnfollowRequest(
			unfollowCCRRequest);

		UnfollowResponse acknowledgedResponse = getAcknowledgedResponse(
			unfollowRequest, unfollowCCRRequest);

		return new UnfollowCCRResponse(acknowledgedResponse.acknowledged());
	}

	protected UnfollowResponse getAcknowledgedResponse(
		UnfollowRequest unfollowRequest,
		UnfollowCCRRequest unfollowCCRRequest) {

		ElasticsearchClient elasticsearchClient =
			_elasticsearchClientResolver.getElasticsearchClient(
				unfollowCCRRequest.getConnectionId(),
				unfollowCCRRequest.isPreferLocalCluster());

		ElasticsearchCcrClient elasticsearchCcrClient =
			elasticsearchClient.ccr();

		try {
			return elasticsearchCcrClient.unfollow(unfollowRequest);
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

	private UnfollowRequest _createUnfollowRequest(
		UnfollowCCRRequest unfollowCCRRequest) {

		UnfollowRequest.Builder builder = new UnfollowRequest.Builder();

		builder.index(unfollowCCRRequest.getIndexName());

		return builder.build();
	}

	private final ElasticsearchClientResolver _elasticsearchClientResolver;

}