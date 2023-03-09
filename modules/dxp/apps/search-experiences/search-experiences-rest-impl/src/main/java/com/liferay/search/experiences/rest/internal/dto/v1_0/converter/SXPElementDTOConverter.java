/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.search.experiences.rest.internal.dto.v1_0.converter;

import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import com.liferay.portal.vulcan.util.LocalizedMapUtil;
import com.liferay.search.experiences.rest.dto.v1_0.ElementDefinition;
import com.liferay.search.experiences.rest.dto.v1_0.Field;
import com.liferay.search.experiences.rest.dto.v1_0.FieldSet;
import com.liferay.search.experiences.rest.dto.v1_0.SXPElement;
import com.liferay.search.experiences.rest.dto.v1_0.UiConfiguration;
import com.liferay.search.experiences.rest.dto.v1_0.util.ElementDefinitionUtil;
import com.liferay.search.experiences.service.SXPElementLocalService;

import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryan Engler
 */
@Component(
	enabled = false,
	property = "dto.class.name=com.liferay.search.experiences.model.SXPElement",
	service = {DTOConverter.class, SXPElementDTOConverter.class}
)
public class SXPElementDTOConverter
	implements DTOConverter
		<com.liferay.search.experiences.model.SXPElement, SXPElement> {

	@Override
	public String getContentType() {
		return SXPElement.class.getSimpleName();
	}

	@Override
	public SXPElement toDTO(DTOConverterContext dtoConverterContext)
		throws Exception {

		com.liferay.search.experiences.model.SXPElement sxpElement =
			_sxpElementLocalService.getSXPElement(
				(Long)dtoConverterContext.getId());

		return toDTO(dtoConverterContext, sxpElement);
	}

	@Override
	public SXPElement toDTO(
			DTOConverterContext dtoConverterContext,
			com.liferay.search.experiences.model.SXPElement sxpElement)
		throws Exception {

		return new SXPElement() {
			{
				createDate = sxpElement.getCreateDate();
				description = _language.get(
					dtoConverterContext.getLocale(),
					sxpElement.getDescription(dtoConverterContext.getLocale()));
				description_i18n = LocalizedMapUtil.getI18nMap(
					true, sxpElement.getDescriptionMap());
				elementDefinition = _toElementDefinition(
					dtoConverterContext.getLocale(),
					sxpElement.getElementDefinitionJSON());
				externalReferenceCode = sxpElement.getExternalReferenceCode();
				id = sxpElement.getSXPElementId();
				modifiedDate = sxpElement.getModifiedDate();
				readOnly = sxpElement.getReadOnly();
				schemaVersion = sxpElement.getSchemaVersion();
				title = _language.get(
					dtoConverterContext.getLocale(),
					sxpElement.getTitle(dtoConverterContext.getLocale()));
				title_i18n = LocalizedMapUtil.getI18nMap(
					true, sxpElement.getTitleMap());
				type = sxpElement.getType();
				userName = sxpElement.getUserName();
				version = sxpElement.getVersion();
			}
		};
	}

	private ElementDefinition _toElementDefinition(Locale locale, String json) {
		try {
			ElementDefinition elementDefinition =
				ElementDefinitionUtil.toElementDefinition(json);

			try {
				UiConfiguration uiConfiguration =
					elementDefinition.getUiConfiguration();

				FieldSet[] fieldSets = uiConfiguration.getFieldSets();

				for (FieldSet fieldSet : fieldSets) {
					Field[] fields = fieldSet.getFields();

					for (Field field : fields) {
						Map<String, String> helpText_i18n =
							field.getHelpText_i18n();
						Map<String, String> label_i18n = field.getLabel_i18n();

						if (helpText_i18n != null) {
							field.setHelpText(
								_language.get(
									locale, helpText_i18n.get("en_US")));
						}

						if (label_i18n != null) {
							field.setLabel(
								_language.get(locale, label_i18n.get("en_US")));
						}
					}
				}
			}
			catch (Exception exception) {
				if (_log.isWarnEnabled()) {
					_log.warn(exception);
				}
			}

			return elementDefinition;
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(exception);
			}

			return null;
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SXPElementDTOConverter.class);

	@Reference
	private Language _language;

	@Reference
	private SXPElementLocalService _sxpElementLocalService;

}