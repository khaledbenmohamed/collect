/**
 * 
 */
package org.openforis.collect.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.SurveyDependencies;
import org.openforis.collect.persistence.xml.CollectIdmlBindingContext;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.metamodel.xml.SurveyUnmarshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
@TransactionConfiguration(defaultRollback = true)
@Transactional
public class SurveyManagerIntegrationTest {
	private static final String SURVEY_NAME = "archenland1";

	protected Survey survey;
	protected EntityDefinition clusterEntityDefinition;
	
	@Autowired
	private SurveyManager surveyManager;

	@Before
	public void before() throws SurveyImportException, IOException, InvalidIdmlException {
		if (survey == null) {
			survey = importModel();
		}
		Schema schema = survey.getSchema();
		clusterEntityDefinition = schema.getRootEntityDefinition("cluster");
	}

	@Test
	public void testEmptyDependencies() {
		SurveyDependencies dependencies = surveyManager.getSurveyDependencies(SURVEY_NAME);
		EntityDefinition timeStudy = (EntityDefinition) clusterEntityDefinition.getChildDefinition("time_study");
		NodeDefinition endTimeDefn = timeStudy.getChildDefinition("end_time");
		Set<String> paths = dependencies.getDependantPaths(endTimeDefn);
		Assert.assertTrue(paths.isEmpty());
	}

	@Test
	public void testNonEmptyStartTime() {
		SurveyDependencies dependencies = surveyManager.getSurveyDependencies(SURVEY_NAME);
		NodeDefinition endTimeDefn = ((EntityDefinition) clusterEntityDefinition.getChildDefinition("time_study")).getChildDefinition("start_time");
		Set<String> paths = dependencies.getDependantPaths(endTimeDefn);
		Assert.assertFalse(paths.isEmpty());
		Assert.assertEquals(1, paths.size());
		String path = paths.iterator().next();
		Assert.assertEquals("parent()/end_time", path);
	}

	@Test
	public void testRequiredDependency() {
		SurveyDependencies dependencies = surveyManager.getSurveyDependencies(SURVEY_NAME);
		NodeDefinition definition = ((EntityDefinition) clusterEntityDefinition.getChildDefinition("plot")).getChildDefinition("share");
		Set<String> paths = dependencies.getDependantPaths(definition);
		Assert.assertFalse(paths.isEmpty());
		Assert.assertEquals(1, paths.size());
		String path = paths.iterator().next();
		Assert.assertEquals("parent()/subplot", path);
	}

	@Test
	public void testRelevantDependency() {
		SurveyDependencies dependencies = surveyManager.getSurveyDependencies(SURVEY_NAME);
		NodeDefinition definition = ((EntityDefinition) clusterEntityDefinition.getChildDefinition("plot")).getChildDefinition("vegetation_type");
		Set<String> paths = dependencies.getDependantPaths(definition);
		Assert.assertFalse(paths.isEmpty());
		Assert.assertEquals(15, paths.size());
	}

	private Survey importModel() throws IOException, SurveyImportException, InvalidIdmlException {
		URL idm = ClassLoader.getSystemResource("test.idm.xml");
		InputStream is = idm.openStream();
		CollectIdmlBindingContext idmlBindingContext = new CollectIdmlBindingContext();
		SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext.createSurveyUnmarshaller();
		Survey survey = surveyUnmarshaller.unmarshal(is);
		surveyManager.importModel(survey);
		return survey;
	}
}
