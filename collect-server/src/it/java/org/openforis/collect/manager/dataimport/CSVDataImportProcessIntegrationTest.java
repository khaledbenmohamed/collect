package org.openforis.collect.manager.dataimport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.dataimport.CSVDataImportProcess.ImportException;
import org.openforis.collect.manager.referencedataimport.ParsingError;
import org.openforis.collect.manager.referencedataimport.ParsingError.ErrorType;
import org.openforis.collect.manager.referencedataimport.ReferenceDataImportStatus;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.RealAttribute;
import org.openforis.idm.model.RealValue;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = {"classpath:test-context.xml"} )
@TransactionConfiguration(defaultRollback=true)
@Transactional
public class CSVDataImportProcessIntegrationTest extends CollectIntegrationTest implements BeanFactoryAware {

	private static final String VALID_TEST_CSV = "data-import-test.csv";
	private static final String VALID_NESTED_ENTITY_TEST_CSV = "data-import-nested-entity-test.csv";
	private static final String INVALID_HEADER_TEST_CSV = "data-import-invalid-header-test.csv";
	private static final String MISSING_REQUIRED_COLUMNS_TEST_CSV ="data-import-missing-required-columns-test.csv";
	private static final String MISSING_RECORD_TEST_CSV ="data-import-missing-record-test.csv";
	private static final String INVALID_VALUES_TEST_CSV = "data-import-invalid-values-test.csv";
	
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private RecordDao recordDao;
	
	private CollectSurvey survey;

	private Unit meterUnit;
	private Unit centimeterUnit;
	private Unit kilometerUnit;
	private BeanFactory beanFactory;
	
	@Before
	public void init() throws IdmlParseException, IOException, SurveyImportException {
		survey = loadSurvey();
		survey.setWork(false);
		surveyManager.importModel(survey);
		meterUnit = survey.getUnit("m");
		centimeterUnit = survey.getUnit("cm");
		kilometerUnit = survey.getUnit("km");
	}
	
	public CSVDataImportProcess importCSVFile(String fileName, int parentEntityDefinitionId) throws Exception {
		File file = getTestFile(fileName);
		CSVDataImportProcess process = beanFactory.getBean(CSVDataImportProcess.class);
		process.setFile(file);
		process.setSurvey(survey);
		process.setParentEntityDefinitionId(parentEntityDefinitionId);
		process.setStep(null);
		process.init();
		try {
			process.call();
		} catch ( ImportException e) {
			
		}
		return process;
	}
	
	@Test
	public void validTest() throws Exception {
		{
			CollectRecord record = createTestRecord(survey, "10_111");
			recordDao.insert(record);
		}
		{
			CollectRecord record = createTestRecord(survey, "10_114");
			recordDao.insert(record);
		}
		EntityDefinition clusterDefn = survey.getSchema().getRootEntityDefinition("cluster");
		
		CSVDataImportProcess process = importCSVFile(VALID_TEST_CSV, clusterDefn.getId());
		ReferenceDataImportStatus<ParsingError> status = process.getStatus();
		assertTrue(status.isComplete());
		assertTrue(status.getSkippedRows().isEmpty());
		assertEquals(3, status.getProcessed());
		
		{
			CollectRecord reloadedRecord = loadRecord("10_111");
			Entity cluster = reloadedRecord.getRootEntity();
			RealAttribute plotDistance = (RealAttribute) cluster.getChild("plot_distance");
			RealValue plotDistanceVal = plotDistance.getValue();
			assertEquals(Double.valueOf(200d), plotDistanceVal.getValue());
			assertEquals(meterUnit, plotDistanceVal.getUnit());
		}
		{
			CollectRecord reloadedRecord = loadRecord("10_114");
			Entity cluster = reloadedRecord.getRootEntity();
			RealAttribute plotDistance = (RealAttribute) cluster.getChild("plot_distance");
			RealValue plotDistanceVal = plotDistance.getValue();
			assertEquals(Double.valueOf(0.3d), plotDistanceVal.getValue());
			assertEquals(kilometerUnit, plotDistanceVal.getUnit());
		}
	}

	@Test
	public void nestedEntityTest() throws Exception {
		CollectRecord record = createTestRecord(survey, "10_114");
		recordDao.insert(record);
		EntityDefinition clusterDefn = survey.getSchema().getRootEntityDefinition("cluster");
		EntityDefinition plotDefn = (EntityDefinition) clusterDefn.getChildDefinition("plot");
		CSVDataImportProcess process = importCSVFile(VALID_NESTED_ENTITY_TEST_CSV, plotDefn.getId());
		ReferenceDataImportStatus<ParsingError> status = process.getStatus();
		assertTrue(status.isComplete());
		assertTrue(status.getSkippedRows().isEmpty());
		assertEquals(3, status.getProcessed());
		
		CollectRecord reloadedRecord = recordDao.load(survey, record.getId(), Step.ENTRY.getStepNumber());
		Entity reloadedCluster = reloadedRecord.getRootEntity();
		{
			Entity plot = (Entity) reloadedCluster.getChildEntityByKeys("plot", "1", "A");
			CodeAttribute landUse = (CodeAttribute) plot.getChild("land_use");
			assertEquals("2", landUse.getValue().getCode());
		}
		{
			Entity plot = (Entity) reloadedCluster.getChildEntityByKeys("plot", "2", "B");
			CodeAttribute landUse = (CodeAttribute) plot.getChild("land_use");
			assertEquals("3", landUse.getValue().getCode());
		}
	}
	
	//@Test
	public void missingRecordTest() throws Exception {
		{
			CollectRecord record = createTestRecord(survey, "10_111");
			recordDao.insert(record);
		}
		{
			CollectRecord record = createTestRecord(survey, "10_114");
			recordDao.insert(record);
		}
		EntityDefinition clusterDefn = survey.getSchema().getRootEntityDefinition("cluster");
		CSVDataImportProcess process = importCSVFile(MISSING_RECORD_TEST_CSV, clusterDefn.getId());

		ReferenceDataImportStatus<ParsingError> status = process.getStatus();
		assertTrue(status.isError());
		assertEquals(1, status.getSkippedRows().size());
		assertEquals(1, status.getRowsInError().size());
		assertEquals(3, status.getProcessed());

		//verify that the transaction is rolled back properly
		{
			CollectRecord reloadedRecord = loadRecord("10_111");
			Entity cluster = reloadedRecord.getRootEntity();
			RealAttribute plotDistance = (RealAttribute) cluster.getChild("plot_distance");
			RealValue plotDistanceVal = plotDistance.getValue();
			assertEquals(Double.valueOf(100d), plotDistanceVal.getValue());
			assertEquals(meterUnit, plotDistanceVal.getUnit());
		}
		{
			CollectRecord reloadedRecord = loadRecord("10_114");
			Entity cluster = reloadedRecord.getRootEntity();
			RealAttribute plotDistance = (RealAttribute) cluster.getChild("plot_distance");
			RealValue plotDistanceVal = plotDistance.getValue();
			assertEquals(Double.valueOf(100d), plotDistanceVal.getValue());
			assertEquals(meterUnit, plotDistanceVal.getUnit());
		}
	}
	
	@Test
	public void missingRequiredColumnsTest() throws Exception {
		EntityDefinition clusterDefn = survey.getSchema().getRootEntityDefinition("cluster");
		EntityDefinition plotDefn = (EntityDefinition) clusterDefn.getChildDefinition("plot");
		CSVDataImportProcess process = importCSVFile(MISSING_REQUIRED_COLUMNS_TEST_CSV, plotDefn.getId());
		ReferenceDataImportStatus<ParsingError> status = process.getStatus();
		assertFalse(status.isComplete());
		assertTrue(status.isError());
		List<ParsingError> errors = status.getErrors();
		assertEquals(1, errors.size());
		ParsingError headerError = errors.get(0);
		assertEquals(ErrorType.MISSING_REQUIRED_COLUMNS, headerError.getErrorType());
	}
	
	@Test
	public void invalidHeaderTest() throws Exception {
		EntityDefinition clusterDefn = survey.getSchema().getRootEntityDefinition("cluster");
		EntityDefinition plotDefn = (EntityDefinition) clusterDefn.getChildDefinition("plot");
		CSVDataImportProcess process = importCSVFile(INVALID_HEADER_TEST_CSV, plotDefn.getId());
		ReferenceDataImportStatus<ParsingError> status = process.getStatus();
		assertFalse(status.isComplete());
		assertTrue(status.isError());
		List<ParsingError> errors = status.getErrors();
		assertEquals(1, errors.size());
		ParsingError headerError = errors.get(0);
		assertEquals(ErrorType.WRONG_COLUMN_NAME, headerError.getErrorType());
		assertTrue(Arrays.equals(new String[]{"land_usage"}, headerError.getColumns()));
	}
	
	@Test
	public void invalidValuesTest() throws Exception {
		{
			CollectRecord record = createTestRecord(survey, "10_111");
			recordDao.insert(record);
		}
		{
			CollectRecord record = createTestRecord(survey, "10_114");
			recordDao.insert(record);
		}
		EntityDefinition clusterDefn = survey.getSchema().getRootEntityDefinition("cluster");
		CSVDataImportProcess process = importCSVFile(INVALID_VALUES_TEST_CSV, clusterDefn.getId());
		ReferenceDataImportStatus<ParsingError> status = process.getStatus();
		assertFalse(status.isComplete());
		assertTrue(status.isError());
		List<ParsingError> errors = status.getErrors();
		assertEquals(4, errors.size());
		{
			ParsingError error = errors.get(0);
			assertEquals(2, error.getRow());
			assertEquals(ErrorType.INVALID_VALUE, error.getErrorType());
			assertTrue(Arrays.equals(new String[]{"vehicle_location_srs"}, error.getColumns()));
		}
		{
			ParsingError error = errors.get(1);
			assertEquals(4, error.getRow());
			assertEquals(ErrorType.INVALID_VALUE, error.getErrorType());
			assertTrue(Arrays.equals(new String[]{"vehicle_location_x"}, error.getColumns()));
		}
		{
			ParsingError error = errors.get(2);
			assertEquals(4, error.getRow());
			assertEquals(ErrorType.INVALID_VALUE, error.getErrorType());
			assertTrue(Arrays.equals(new String[]{"plot_distance"}, error.getColumns()));
		}
		{
			ParsingError error = errors.get(3);
			assertEquals(5, error.getRow());
			assertEquals(ErrorType.INVALID_VALUE, error.getErrorType());
			assertTrue(Arrays.equals(new String[]{"vehicle_location_y"}, error.getColumns()));
		}
	}
	
	private CollectRecord loadRecord(String key) {
		List<CollectRecord> summaries = recordDao.loadSummaries(survey, "cluster", key);
		CollectRecord summary = summaries.get(0);
		CollectRecord reloadedRecord = recordDao.load(survey, summary.getId(), summary.getStep().getStepNumber());
		return reloadedRecord;
	}
	
	protected boolean containsError(List<ParsingError> errors, long row,
			String column) {
		for (ParsingError error : errors) {
			if ( error.getRow() == row && Arrays.asList(error.getColumns()).contains(column) ) {
				return true;
			}
		}
		return false;
	}

	private CollectRecord createTestRecord(CollectSurvey survey, String id) {
		CollectRecord record = new CollectRecord(survey, "2.0");
		Entity cluster = record.createRootEntity("cluster");
		record.setCreationDate(new GregorianCalendar(2011, 11, 31, 23, 59).getTime());
		record.setStep(Step.ENTRY);
		EntityBuilder.addValue(cluster, "id", new Code(id));
		EntityBuilder.addValue(cluster, "plot_distance", 100d, meterUnit);
		{
			Entity plot = EntityBuilder.addEntity(cluster, "plot");
			EntityBuilder.addValue(plot, "no", new Code("1"));
			EntityBuilder.addValue(plot, "subplot", "A");
			EntityBuilder.addValue(plot, "land_use", new Code("1"));
		}
		{
			Entity plot = EntityBuilder.addEntity(cluster, "plot");
			EntityBuilder.addValue(plot, "no", new Code("2"));
			EntityBuilder.addValue(plot, "subplot", "A");
			EntityBuilder.addValue(plot, "land_use", new Code("2"));
		}
		record.updateRootEntityKeyValues();
		record.updateEntityCounts();
		return record;
	}

	protected File getTestFile(String fileName) throws URISyntaxException {
		URL fileUrl = ClassLoader.getSystemResource(fileName);
		File file = new File(fileUrl.toURI());
		return file;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
}
