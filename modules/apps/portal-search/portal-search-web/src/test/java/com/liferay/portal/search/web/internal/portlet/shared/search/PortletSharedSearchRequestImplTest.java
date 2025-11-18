/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.web.internal.portlet.shared.search;

import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.processor.PortletRegistry;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.nodes.Document;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Joshua Cords
 */
public class PortletSharedSearchRequestImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetSegmentExperiencePortletIdsWithPortletFragments() {
		PortletSharedSearchRequestImpl portletSharedSearchRequestImpl =
			new PortletSharedSearchRequestImpl();

		ReflectionTestUtil.setFieldValue(
			portletSharedSearchRequestImpl, "_fragmentEntryLinkLocalService",
			_fragmentEntryLinkLocalService);
		TestPortletRegistry testPortletRegistry = new TestPortletRegistry();

		ReflectionTestUtil.setFieldValue(
			portletSharedSearchRequestImpl, "_portletRegistry",
			testPortletRegistry);

		FragmentEntryLink fragmentEntryLink1 = _createFragmentEntryLink(
			"@liferay_portlet['portletName'='com_liferay_sample_web']");
		FragmentEntryLink fragmentEntryLink2 = _createFragmentEntryLink(
			"<lfr-widget-sample id=\"myInstance\"></lfr-widget-sample>");

		List<FragmentEntryLink> fragmentEntryLinks = List.of(
			fragmentEntryLink1, fragmentEntryLink2);

		Layout layout = Mockito.mock(Layout.class);

		Mockito.when(
			layout.getGroupId()
		).thenReturn(
			_GROUP_ID
		);
		Mockito.when(
			layout.getPlid()
		).thenReturn(
			_PLID
		);

		Mockito.when(
			_fragmentEntryLinkLocalService.
				getFragmentEntryLinksBySegmentsExperienceId(
					_GROUP_ID, _SEGMENTS_EXPERIENCE_ID, _PLID)
		).thenReturn(
			fragmentEntryLinks
		);

		testPortletRegistry.putPortletIds(
			fragmentEntryLink1, List.of(_LIFERAY_PORTLET_RUNTIME_PORTLET_ID));
		testPortletRegistry.putPortletIds(
			fragmentEntryLink2, List.of(_WIDGET_PORTLET_ID));

		Set<String> segmentExperiencePortletIds = ReflectionTestUtil.invoke(
			portletSharedSearchRequestImpl, "_getSegmentExperiencePortletIds",
			new Class<?>[] {Layout.class, long.class}, layout,
			_SEGMENTS_EXPERIENCE_ID);

		Assert.assertEquals(
			new HashSet<>(
				List.of(
					_LIFERAY_PORTLET_RUNTIME_PORTLET_ID, _WIDGET_PORTLET_ID)),
			segmentExperiencePortletIds);
	}

	private FragmentEntryLink _createFragmentEntryLink(String html) {
		FragmentEntryLink fragmentEntryLink = Mockito.mock(
			FragmentEntryLink.class);

		Mockito.when(
			fragmentEntryLink.getHtml()
		).thenReturn(
			html
		);
		Mockito.when(
			fragmentEntryLink.isTypePortlet()
		).thenReturn(
			false
		);

		return fragmentEntryLink;
	}

	private static final long _GROUP_ID = 1L;

	private static final String _LIFERAY_PORTLET_RUNTIME_PORTLET_ID =
		"com_liferay_sample_web_runtime";

	private static final long _PLID = 2L;

	private static final long _SEGMENTS_EXPERIENCE_ID = 3L;

	private static final String _WIDGET_PORTLET_ID = "widget_portlet_id";

	private static final FragmentEntryLinkLocalService
		_fragmentEntryLinkLocalService = Mockito.mock(
			FragmentEntryLinkLocalService.class);

	private static final class TestPortletRegistry implements PortletRegistry {

		@Override
		public List<String> getFragmentEntryLinkPortletIds(
			Document document, FragmentEntryLink fragmentEntryLink) {

			return _portletIds.get(fragmentEntryLink);
		}

		@Override
		public List<String> getFragmentEntryLinkPortletIds(
			FragmentEntryLink fragmentEntryLink) {

			return _portletIds.get(fragmentEntryLink);
		}

		@Override
		public List<String> getPortletAliases() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getPortletName(String alias) {
			throw new UnsupportedOperationException();
		}

		public void putPortletIds(
			FragmentEntryLink fragmentEntryLink, List<String> portletIds) {

			_portletIds.put(fragmentEntryLink, portletIds);
		}

		@Override
		public void registerAlias(String alias, String portletName) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void unregisterAlias(String alias) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writePortletPaths(
			FragmentEntryLink fragmentEntryLink,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) {

			throw new UnsupportedOperationException();
		}

		private final Map<FragmentEntryLink, List<String>> _portletIds =
			new HashMap<>();

	}

}