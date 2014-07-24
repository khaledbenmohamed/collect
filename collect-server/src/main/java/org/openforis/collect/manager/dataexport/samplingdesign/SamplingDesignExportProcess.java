package org.openforis.collect.manager.dataexport.samplingdesign;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignFileColumn;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.SamplingDesignSummaries;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.idm.metamodel.SamplingPoints;
import org.openforis.idm.metamodel.SamplingPoints.Attribute;

/**
 * 
 * @author S. Ricci
 *
 */
public class SamplingDesignExportProcess {
	
	private final Log log = LogFactory.getLog(SamplingDesignExportProcess.class);
	
	private SamplingDesignManager samplingDesignManager;
	
	public SamplingDesignExportProcess(SamplingDesignManager samplingDesignManager) {
		super();
		this.samplingDesignManager = samplingDesignManager;
	}

	public void exportToCSV(OutputStream out, CollectSurvey survey) {
		CsvWriter writer = null;
		try {
			writer = new CsvWriter(out);
			SamplingDesignSummaries summaries = survey.isWork() ? 
				samplingDesignManager.loadBySurveyWork(survey.getId()): 
				samplingDesignManager.loadBySurvey(survey.getId());
				
			ArrayList<String> colNames = new ArrayList<String>();
			colNames.addAll(Arrays.asList(SamplingDesignFileColumn.LEVEL_COLUMN_NAMES));
			colNames.add(SamplingDesignFileColumn.X.getColumnName());
			colNames.add(SamplingDesignFileColumn.Y.getColumnName());
			colNames.add(SamplingDesignFileColumn.SRS_ID.getColumnName());

			//info columns
			SamplingPoints samplingPoints = survey.getSamplingPoints();
			if ( samplingPoints != null ) {
				List<Attribute> infoAttributes = samplingPoints.getAttributes(false);
				for (Attribute attribute : infoAttributes) {
					colNames.add(attribute.getName());
				}
			}
			writer.writeHeaders(colNames.toArray(new String[0]));
			
			List<SamplingDesignItem> items = summaries.getRecords();
			for (SamplingDesignItem item : items) {
				writeSummary(writer, survey, item);
			}
		} catch (Exception e) {
			log.error(e);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	protected void writeSummary(CsvWriter writer, CollectSurvey survey, SamplingDesignItem item) {
		List<String> lineValues = new ArrayList<String>();
		
		//write level columns
		List<String> levelCodes = item.getLevelCodes();
		SamplingDesignFileColumn[] levelColumns = SamplingDesignFileColumn.LEVEL_COLUMNS;
		for (int level = 1; level <= levelColumns.length; level++) {
			String levelCode = level <= levelCodes.size() ? item.getLevelCode(level): "";
			lineValues.add(levelCode);
		}
		lineValues.add(item.getX().toString());
		lineValues.add(item.getY().toString());
		lineValues.add(item.getSrsId());
		
		//write info columns
		SamplingPoints samplingPoints = survey.getSamplingPoints();
		if ( samplingPoints != null ) {
			List<Attribute> infoAttributes = samplingPoints.getAttributes(false);
			for (int i = 0; i < infoAttributes.size(); i++) {
				lineValues.add(item.getInfoAttribute(i));
			}
		}
		writer.writeNext(lineValues.toArray(new String[0]));
	}

}
