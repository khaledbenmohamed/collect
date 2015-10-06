package org.openforis.collect.datacleansing.form.validation;

import java.util.List;

import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.DataErrorQueryGroup;
import org.openforis.collect.datacleansing.form.DataErrorQueryGroupForm;
import org.openforis.collect.datacleansing.manager.DataErrorQueryGroupManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class DataErrorQueryGroupValidator extends SimpleValidator<DataErrorQueryGroupForm> {

	private static final String TITLE_FIELD = "title";
	private static final String QUERY_IDS_FIELD = "queryIds";
	
	@Autowired
	private DataErrorQueryGroupManager dataErrorQueryGroupManager;
	
	@Override
	public void validateForm(DataErrorQueryGroupForm target, Errors errors) {
		if (validateRequiredField(errors, TITLE_FIELD)) {
			validateTitleUniqueness(target, errors);
		}
		if (validateRequiredField(errors, QUERY_IDS_FIELD)) {
			validateQueryUniqueness(target, errors);
		}
	}

	private boolean validateQueryUniqueness(DataErrorQueryGroupForm target, Errors errors) {
		CollectSurvey survey = getActiveSurvey();
		List<DataErrorQueryGroup> items = dataErrorQueryGroupManager.loadBySurvey(survey);
		for (DataErrorQueryGroup item : items) {
			if (! item.getId().equals(target.getId())) {
				List<DataErrorQuery> queries = item.getQueries();
				List<Integer> queryIds = CollectionUtils.project(queries, "id");
				if (queryIds.equals(target.getQueryIds())) {
					rejectDuplicateValue(errors, QUERY_IDS_FIELD);
					return false;
				}
			}
		}
		return true;
	}

	private boolean validateTitleUniqueness(DataErrorQueryGroupForm target, Errors errors) {
		String title = target.getTitle();
		CollectSurvey survey = getActiveSurvey();
		List<DataErrorQueryGroup> items = dataErrorQueryGroupManager.loadBySurvey(survey);
		for (DataErrorQueryGroup item : items) {
			if (! item.getId().equals(target.getId()) && item.getTitle().equalsIgnoreCase(title)) {
				rejectDuplicateValue(errors, TITLE_FIELD);
				return false;
			}
		}
		return true;
	}


}
