/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.list.internal.util;

import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectFieldLocalServiceUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.BooleanClause;
import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.MatchQuery;
import com.liferay.portal.kernel.search.NestedQuery;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.TermQuery;
import com.liferay.portal.kernel.search.TermRangeQuery;
import com.liferay.portal.kernel.search.WildcardQuery;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Joshua Cords
 */
public class AssetListFiltersUtil {

	public static BooleanClause[] getFiltersBooleanClauses(
		JSONArray filtersJSONArray, long companyId, Locale locale) {

		if ((filtersJSONArray == null) || (filtersJSONArray.length() == 0)) {
			return new BooleanClause[0];
		}

		BooleanQuery booleanQuery = new BooleanQuery();

		for (int i = 0; i < filtersJSONArray.length(); i++) {
			BooleanClause<Query> booleanClause = _toClause(
				filtersJSONArray.getJSONObject(i), companyId, locale);

			if (booleanClause == null) {
				continue;
			}

			booleanQuery.add(
				booleanClause.getClause(),
				booleanClause.getBooleanClauseOccur());
		}

		if (!booleanQuery.hasClauses()) {
			return new BooleanClause[0];
		}

		return new BooleanClause[] {
			new BooleanClause<>(booleanQuery, BooleanClauseOccur.MUST)
		};
	}

	private static String _emptyToNull(String value) {
		if (Validator.isNull(value)) {
			return null;
		}

		return value;
	}

	private static boolean _isCommonFieldRow(JSONObject filterJSONObject) {
		if ((filterJSONObject.getLong("classNameId") <= 0) &&
			(filterJSONObject.getLong("classTypeId") <= 0)) {

			return true;
		}

		return false;
	}

	private static boolean _isNegatedOperator(String operatorName) {
		if (operatorName.equals("not-contains") ||
			operatorName.equals("not-eq")) {

			return true;
		}

		return false;
	}

	private static String _normalizeDateValue(
		String value, boolean dateTime, boolean endOfBound) {

		if (Validator.isNull(value)) {
			return null;
		}

		String digits = value.replaceAll("[^0-9]", "");

		if (dateTime) {
			String padded = digits + "000000000000";

			String paddedDigits = padded.substring(0, 12);

			return paddedDigits + (endOfBound ? "59" : "00");
		}

		String padded = digits + "00000000";

		String paddedDigits = padded.substring(0, 8);

		return paddedDigits + (endOfBound ? "235959" : "000000");
	}

	private static String _resolveCommonFieldName(
		String propertyName, Locale locale) {

		if (_commonFieldTypes.get(propertyName) == null) {
			return null;
		}

		if (_localizedCommonFieldNames.contains(propertyName)) {
			return Field.getLocalizedName(locale, "localized_" + propertyName);
		}

		return propertyName;
	}

	private static String _resolveCommonFieldType(String propertyName) {
		return _commonFieldTypes.get(propertyName);
	}

	private static ObjectDefinition _resolveObjectDefinition(
		long classNameId, long classTypeId, long companyId) {

		if (classTypeId > 0) {
			ObjectDefinition objectDefinition =
				ObjectDefinitionLocalServiceUtil.fetchObjectDefinition(
					classTypeId);

			if (objectDefinition != null) {
				return objectDefinition;
			}
		}

		if (classNameId <= 0) {
			return null;
		}

		return ObjectDefinitionLocalServiceUtil.
			fetchObjectDefinitionByClassName(
				companyId, PortalUtil.getClassName(classNameId));
	}

	private static ObjectField _resolveObjectField(
		long classNameId, long classTypeId, long companyId, String name) {

		ObjectDefinition objectDefinition = _resolveObjectDefinition(
			classNameId, classTypeId, companyId);

		if (objectDefinition == null) {
			return null;
		}

		return ObjectFieldLocalServiceUtil.fetchObjectField(
			objectDefinition.getObjectDefinitionId(), name);
	}

	private static String _resolveSubfield(
		ObjectField objectField, Locale locale) {

		if (objectField.isIndexedAsKeyword()) {
			return "nestedFieldArray.value_keyword";
		}

		String dbType = objectField.getDBType();

		if (ObjectFieldConstants.DB_TYPE_BOOLEAN.equals(dbType)) {
			return "nestedFieldArray.value_boolean";
		}

		if (ObjectFieldConstants.DB_TYPE_DATE.equals(dbType) ||
			ObjectFieldConstants.DB_TYPE_DATE_TIME.equals(dbType)) {

			return "nestedFieldArray.value_date";
		}

		if (ObjectFieldConstants.DB_TYPE_BIG_DECIMAL.equals(dbType) ||
			ObjectFieldConstants.DB_TYPE_DOUBLE.equals(dbType)) {

			return "nestedFieldArray.value_double";
		}

		if (ObjectFieldConstants.DB_TYPE_INTEGER.equals(dbType)) {
			return "nestedFieldArray.value_integer";
		}

		if (ObjectFieldConstants.DB_TYPE_LONG.equals(dbType)) {
			return "nestedFieldArray.value_long";
		}

		if (objectField.isLocalized()) {
			return Field.getLocalizedName(locale, "nestedFieldArray.value");
		}

		String indexedLanguageId = objectField.getIndexedLanguageId();

		if (Validator.isNotNull(indexedLanguageId)) {
			return "nestedFieldArray.value_" + indexedLanguageId;
		}

		return "nestedFieldArray.value_text";
	}

	private static BooleanClause<Query> _toClause(
		JSONObject filterJSONObject, long companyId, Locale locale) {

		if (filterJSONObject == null) {
			return null;
		}

		if (_isCommonFieldRow(filterJSONObject)) {
			return _toCommonFieldClause(
				filterJSONObject, filterJSONObject.getString("propertyName"),
				locale);
		}

		ObjectField objectField = _resolveObjectField(
			filterJSONObject.getLong("classNameId"),
			filterJSONObject.getLong("classTypeId"), companyId,
			filterJSONObject.getString("propertyName"));

		if (objectField == null) {
			return null;
		}

		NestedQuery nestedQuery = _toNestedQuery(
			filterJSONObject, objectField, locale);

		if (nestedQuery == null) {
			return null;
		}

		return new BooleanClause<>(nestedQuery, BooleanClauseOccur.MUST);
	}

	private static BooleanClause<Query> _toCommonFieldClause(
		JSONObject filterJSONObject, String propertyName, Locale locale) {

		if (Validator.isNull(propertyName)) {
			return null;
		}

		String field = _resolveCommonFieldName(propertyName, locale);
		String type = _resolveCommonFieldType(propertyName);

		if ((field == null) || (type == null)) {
			return null;
		}

		String operatorName = GetterUtil.getString(
			filterJSONObject.getString("operatorName"), "contains");

		Query valueQuery = _toCommonFieldValueQuery(
			filterJSONObject, field, operatorName, type);

		if (valueQuery == null) {
			return null;
		}

		return new BooleanClause<>(
			valueQuery,
			_isNegatedOperator(operatorName) ? BooleanClauseOccur.MUST_NOT :
				BooleanClauseOccur.MUST);
	}

	private static Query _toCommonFieldRangeQuery(
		JSONObject filterJSONObject, String field, String operatorName,
		String type) {

		boolean dateType = type.equals("date");

		if (operatorName.equals("between")) {
			JSONArray valueJSONArray = filterJSONObject.getJSONArray("value");

			if ((valueJSONArray == null) || (valueJSONArray.length() < 2)) {
				return null;
			}

			String lowerTerm = _emptyToNull(valueJSONArray.getString(0));
			String upperTerm = _emptyToNull(valueJSONArray.getString(1));

			if (dateType) {
				lowerTerm = _normalizeDateValue(lowerTerm, false, false);
				upperTerm = _normalizeDateValue(upperTerm, false, true);
			}

			return new TermRangeQuery(field, lowerTerm, upperTerm, true, true);
		}

		String value = filterJSONObject.getString("value");

		if (Validator.isNull(value)) {
			return null;
		}

		if (operatorName.equals("gt")) {
			String lowerTerm =
				dateType ? _normalizeDateValue(value, false, true) : value;

			return new TermRangeQuery(field, lowerTerm, null, false, false);
		}

		if (operatorName.equals("ge")) {
			String lowerTerm =
				dateType ? _normalizeDateValue(value, false, false) : value;

			return new TermRangeQuery(field, lowerTerm, null, true, false);
		}

		if (operatorName.equals("lt")) {
			String upperTerm =
				dateType ? _normalizeDateValue(value, false, false) : value;

			return new TermRangeQuery(field, null, upperTerm, false, false);
		}

		if (operatorName.equals("le")) {
			String upperTerm =
				dateType ? _normalizeDateValue(value, false, true) : value;

			return new TermRangeQuery(field, null, upperTerm, false, true);
		}

		return null;
	}

	private static Query _toCommonFieldValueQuery(
		JSONObject filterJSONObject, String field, String operatorName,
		String type) {

		if (operatorName.equals("between") || operatorName.equals("gt") ||
			operatorName.equals("ge") || operatorName.equals("lt") ||
			operatorName.equals("le")) {

			return _toCommonFieldRangeQuery(
				filterJSONObject, field, operatorName, type);
		}

		String value = filterJSONObject.getString("value");

		if (Validator.isNull(value)) {
			return null;
		}

		if (type.equals("date") &&
			(operatorName.equals("eq") || operatorName.equals("not-eq"))) {

			return new TermRangeQuery(
				field, _normalizeDateValue(value, false, false),
				_normalizeDateValue(value, false, true), true, true);
		}

		if (type.equals("decimal") || type.equals("integer")) {
			return new TermQuery(field, value);
		}

		if (operatorName.equals("contains") ||
			operatorName.equals("not-contains")) {

			return new WildcardQuery(
				field,
				StringPool.STAR + StringUtil.toLowerCase(value) +
					StringPool.STAR);
		}

		return new TermQuery(field, StringUtil.toLowerCase(value));
	}

	private static NestedQuery _toNestedQuery(
		JSONObject filterJSONObject, ObjectField objectField, Locale locale) {

		String propertyName = filterJSONObject.getString("propertyName");
		String value = filterJSONObject.getString("value");

		if (Validator.isNull(propertyName) || Validator.isNull(value)) {
			return null;
		}

		String operatorName = GetterUtil.getString(
			filterJSONObject.getString("operatorName"), "contains");

		String subfield = _resolveSubfield(objectField, locale);

		Query valueQuery = _toValueQuery(
			filterJSONObject, subfield, operatorName, value, objectField);

		if (valueQuery == null) {
			return null;
		}

		BooleanQuery nestedBooleanQuery = new BooleanQuery();

		nestedBooleanQuery.add(
			new TermQuery("nestedFieldArray.fieldName", propertyName),
			BooleanClauseOccur.MUST);
		nestedBooleanQuery.add(
			new TermQuery(
				"nestedFieldArray.valueFieldName",
				subfield.substring(subfield.indexOf(CharPool.PERIOD) + 1)),
			BooleanClauseOccur.MUST);
		nestedBooleanQuery.add(
			valueQuery,
			_isNegatedOperator(operatorName) ? BooleanClauseOccur.MUST_NOT :
				BooleanClauseOccur.MUST);

		return new NestedQuery("nestedFieldArray", nestedBooleanQuery);
	}

	private static Query _toPicklistQuery(
		JSONObject filterJSONObject, String subfield) {

		JSONArray valueJSONArray = filterJSONObject.getJSONArray("value");

		if ((valueJSONArray == null) || (valueJSONArray.length() == 0)) {
			return null;
		}

		BooleanClauseOccur innerOccur = BooleanClauseOccur.SHOULD;

		String quantifier = filterJSONObject.getString("quantifier");

		if (Objects.equals(quantifier, "all")) {
			innerOccur = BooleanClauseOccur.MUST;
		}

		BooleanQuery booleanQuery = new BooleanQuery();

		for (int i = 0; i < valueJSONArray.length(); i++) {
			JSONObject itemJSONObject = valueJSONArray.getJSONObject(i);

			String value = StringUtil.toLowerCase(
				itemJSONObject.getString("value"));

			booleanQuery.add(new TermQuery(subfield, value), innerOccur);
		}

		return booleanQuery;
	}

	private static Query _toRangeQuery(
		JSONObject filterJSONObject, String subfield, String operatorName,
		ObjectField objectField) {

		boolean dateSubfield = subfield.endsWith(".value_date");

		boolean dateTime = false;

		if (dateSubfield &&
			ObjectFieldConstants.DB_TYPE_DATE_TIME.equals(
				objectField.getDBType())) {

			dateTime = true;
		}

		if (operatorName.equals("between")) {
			JSONArray valueJSONArray = filterJSONObject.getJSONArray("value");

			if ((valueJSONArray == null) || (valueJSONArray.length() < 2)) {
				return null;
			}

			String lowerTerm = _emptyToNull(valueJSONArray.getString(0));
			String upperTerm = _emptyToNull(valueJSONArray.getString(1));

			if (dateSubfield) {
				lowerTerm = _normalizeDateValue(lowerTerm, dateTime, false);
				upperTerm = _normalizeDateValue(upperTerm, dateTime, true);
			}

			return new TermRangeQuery(
				subfield, lowerTerm, upperTerm, true, true);
		}

		String value = filterJSONObject.getString("value");

		if (Validator.isNull(value)) {
			return null;
		}

		if (operatorName.equals("gt")) {
			String lowerTerm =
				dateSubfield ? _normalizeDateValue(value, dateTime, true) :
					value;

			return new TermRangeQuery(subfield, lowerTerm, null, false, false);
		}

		if (operatorName.equals("ge")) {
			String lowerTerm =
				dateSubfield ? _normalizeDateValue(value, dateTime, false) :
					value;

			return new TermRangeQuery(subfield, lowerTerm, null, true, false);
		}

		if (operatorName.equals("lt")) {
			String upperTerm =
				dateSubfield ? _normalizeDateValue(value, dateTime, false) :
					value;

			return new TermRangeQuery(subfield, null, upperTerm, false, false);
		}

		if (operatorName.equals("le")) {
			String upperTerm =
				dateSubfield ? _normalizeDateValue(value, dateTime, true) :
					value;

			return new TermRangeQuery(subfield, null, upperTerm, false, true);
		}

		return null;
	}

	private static Query _toValueQuery(
		JSONObject filterJSONObject, String subfield, String operatorName,
		String value, ObjectField objectField) {

		if (operatorName.equals("contains") ||
			operatorName.equals("not-contains")) {

			if (objectField.getListTypeDefinitionId() != 0) {
				return _toPicklistQuery(filterJSONObject, subfield);
			}

			if (subfield.endsWith(".value_keyword")) {
				return new WildcardQuery(
					subfield,
					StringPool.STAR + StringUtil.toLowerCase(value) +
						StringPool.STAR);
			}
		}

		if (operatorName.equals("between") || operatorName.equals("gt") ||
			operatorName.equals("ge") || operatorName.equals("lt") ||
			operatorName.equals("le")) {

			return _toRangeQuery(
				filterJSONObject, subfield, operatorName, objectField);
		}

		if (subfield.endsWith(".value_date") &&
			(operatorName.equals("eq") || operatorName.equals("not-eq"))) {

			boolean dateTime = ObjectFieldConstants.DB_TYPE_DATE_TIME.equals(
				objectField.getDBType());

			return new TermRangeQuery(
				subfield, _normalizeDateValue(value, dateTime, false),
				_normalizeDateValue(value, dateTime, true), true, true);
		}

		if (subfield.endsWith(".value_keyword")) {
			return new TermQuery(subfield, StringUtil.toLowerCase(value));
		}

		if (subfield.endsWith(".value_boolean") ||
			subfield.endsWith(".value_double") ||
			subfield.endsWith(".value_integer") ||
			subfield.endsWith(".value_long") || operatorName.equals("eq") ||
			operatorName.equals("not-eq")) {

			return new TermQuery(subfield, value);
		}

		return new MatchQuery(subfield, value);
	}

	// Common Fields are indexed at the document root by
	// AssetEntryDocumentContributor, not under nestedFieldArray. Keep this
	// registry in sync with the group emitted by
	// AssetListTypePropertiesUtil#_getCommonFieldsItemsJSONArray.

	private static final Map<String, String> _commonFieldTypes =
		HashMapBuilder.put(
			Field.CREATE_DATE, "date"
		).put(
			Field.DESCRIPTION, "text"
		).put(
			Field.DISPLAY_DATE, "date"
		).put(
			Field.EXPIRATION_DATE, "date"
		).put(
			Field.MODIFIED_DATE, "date"
		).put(
			Field.PRIORITY, "decimal"
		).put(
			Field.PUBLISH_DATE, "date"
		).put(
			Field.REVIEW_DATE, "date"
		).put(
			Field.STATUS, "integer"
		).put(
			Field.TITLE, "text"
		).put(
			Field.USER_NAME, "text"
		).put(
			"externalReferenceCode", "text"
		).put(
			"viewCount", "integer"
		).build();

	private static final Set<String> _localizedCommonFieldNames =
		SetUtil.fromArray(Field.DESCRIPTION, Field.TITLE);

}