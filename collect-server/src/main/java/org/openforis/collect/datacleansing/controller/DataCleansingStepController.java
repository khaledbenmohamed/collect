package org.openforis.collect.datacleansing.controller;

import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.datacleansing.DataCleansingChain;
import org.openforis.collect.datacleansing.DataCleansingChainExecutorJob;
import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.form.DataCleansingStepForm;
import org.openforis.collect.datacleansing.form.validation.DataCleansingStepValidator;
import org.openforis.collect.datacleansing.manager.DataCleansingStepManager;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.web.controller.AbstractSurveyObjectEditFormController;
import org.openforis.commons.web.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

@Controller
@Scope(value=WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/datacleansing/datacleansingsteps")
public class DataCleansingStepController extends AbstractSurveyObjectEditFormController<DataCleansingStep, DataCleansingStepForm, DataCleansingStepManager> {
	
	@Autowired
	private DataCleansingStepManager dataCleansingStepManager;
	@Autowired
	private CollectJobManager collectJobManager;
	@Autowired
	private DataCleansingStepValidator dataCleansingStepValidator;

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(dataCleansingStepValidator);
	}
	
	@Override
	@Autowired
	@Qualifier("dataCleansingStepManager")
	public void setItemManager(DataCleansingStepManager itemManager) {
		super.setItemManager(itemManager);
	}
	
	@Override
	protected DataCleansingStepForm createFormInstance(DataCleansingStep item) {
		return new DataCleansingStepForm(item);
	}
	
	@Override
	protected DataCleansingStep createItemInstance(CollectSurvey survey) {
		return new DataCleansingStep(survey);
	};
	
	@RequestMapping(value="run.json", method = RequestMethod.POST)
	public @ResponseBody
	Response run(@RequestParam int cleansingStepId, @RequestParam Step recordStep) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		DataCleansingStep cleansingStep = dataCleansingStepManager.loadById(survey, cleansingStepId);
		DataCleansingChain chain = new DataCleansingChain(survey);
		chain.addStep(cleansingStep);
		DataCleansingChainExecutorJob job = collectJobManager.createJob(DataCleansingChainExecutorJob.class);
		job.setChain(chain);
		job.setRecordStep(recordStep);
		collectJobManager.startSurveyJob(job);
		Response response = new Response();
		return response;
	}
	
}
