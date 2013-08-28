package org.openforis.collect.manager.dataimport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.manager.referencedataimport.Line;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FieldDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataLine extends Line {

	private Map<AttributeDefinition, String> recordKeysByDefn;
	private Map<AttributeDefinition, String> ancestorKeysByDefn;
	private Map<FieldDefinition<?>, String> fieldValues;
	private Map<FieldDefinition<?>, String> columnNameByField;
	
	public DataLine() {
		recordKeysByDefn = new HashMap<AttributeDefinition, String>();
		ancestorKeysByDefn = new HashMap<AttributeDefinition, String>();
		fieldValues = new HashMap<FieldDefinition<?>, String>();
		columnNameByField = new HashMap<FieldDefinition<?>, String>();
	}
	
	public void setAncestorKey(AttributeDefinition keyDefn, String value) {
		if ( keyDefn.getParentEntityDefinition() == keyDefn.getRootEntity() ) {
			recordKeysByDefn.put(keyDefn, value);
		}
		ancestorKeysByDefn.put(keyDefn, value);
	}

	public void setFieldValue(FieldDefinition<?> fieldDefinition, String value) {
		fieldValues.put(fieldDefinition, value);
	}
	
	public void setColumnNameByField(FieldDefinition<?> fieldDefinition, String colName) {
		columnNameByField.put(fieldDefinition, colName);
	}
	
	public String[] getRecordKeyValues(EntityDefinition rootEntityDefn) {
		List<AttributeDefinition> rootKeyAttrDefns = rootEntityDefn.getKeyAttributeDefinitions();
		String[] recordKeys = new String[rootKeyAttrDefns.size()];
		for (int i = 0; i < rootKeyAttrDefns.size(); i++) {
			AttributeDefinition keyDefn = rootKeyAttrDefns.get(i);
			String key = recordKeysByDefn.get(keyDefn);
			recordKeys[i] = key;
		}
		return recordKeys;
	}
	
	public Map<AttributeDefinition, String> getRecordKeys() {
		return recordKeysByDefn;
	}
	
	public Map<AttributeDefinition, String> getAncestorKeys() {
		return ancestorKeysByDefn;
	} 
	
	public Map<FieldDefinition<?>, String> getFieldValues() {
		return fieldValues;
	}
	
	public String getColumnName(FieldDefinition<?> fieldDefn) {
		return columnNameByField.get(fieldDefn);
	}

	public Map<FieldDefinition<?>, String> getColumnNamesByField() {
		return columnNameByField;
	}
	
}
