package org.openforis.collect.io.data.csv;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;

public class ColumnProviders {

	public static ColumnProvider createAttributeProvider(CSVExportConfiguration config, AttributeDefinition defn) {
		if ( defn instanceof CodeAttributeDefinition ) {
			return new CodeColumnProvider(config, (CodeAttributeDefinition) defn);
		} else if(defn instanceof CoordinateAttributeDefinition){
			return new CoordinateColumnProvider(config, (CoordinateAttributeDefinition) defn);
		} else if(defn instanceof DateAttributeDefinition) {
			return new DateColumnProvider(config, (DateAttributeDefinition) defn);
		} else if(defn instanceof NumberAttributeDefinition){
			return new NumberColumnProvider(config, (NumberAttributeDefinition) defn);
		} else if(defn instanceof RangeAttributeDefinition){
			return new RangeColumnProvider(config, (RangeAttributeDefinition) defn);
		} else if(defn instanceof TaxonAttributeDefinition){
			return new TaxonColumnProvider(config, (TaxonAttributeDefinition) defn);
		} else if(defn instanceof TimeAttributeDefinition){
			return new TimeColumnProvider(config, (TimeAttributeDefinition) defn);
		} else {
			return new SingleFieldAttributeColumnProvider(config, defn);
		}
	}

	public static String generateHeadingPrefix(NodeDefinition nodeDefinition, CSVExportConfiguration config) {
		String result = null;
		switch(config.getHeadingSource()) {
		case ATTRIBUTE_NAME:
			result = nodeDefinition.getName();
			break;
		case INSTANCE_LABEL:
			result = nodeDefinition.getLabel(Type.INSTANCE, config.getLanguageCode());
			break;
		case REPORTING_LABEL:
			result = nodeDefinition.getFailSafeLabel(config.getLanguageCode(), Type.REPORTING, Type.INSTANCE);
			break;
		}
		//default to attribute name
		if (result == null) {
			result = nodeDefinition.getName();
		}
		return result;
	}
}
