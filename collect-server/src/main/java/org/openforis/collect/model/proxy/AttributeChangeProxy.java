package org.openforis.collect.model.proxy;

import java.util.Map;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.NodeChange.AttributeChange;
import org.openforis.collect.spring.MessageContextHolder;
import org.openforis.idm.model.Attribute;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeChangeProxy extends NodeChangeProxy<AttributeChange> {

	public AttributeChangeProxy(
			MessageContextHolder messageContextHolder,
			AttributeChange change) {
		super(messageContextHolder, change);
	}
	
	@ExternalizedProperty
	public ValidationResultsProxy getValidationResults() {
		if ( change.getValidationResults() == null ) {
			return null;
		} else {
			return new ValidationResultsProxy(messageContextHolder, (Attribute<?, ?>) change.getNode(), change.getValidationResults());
		}
	}

	@ExternalizedProperty
	public Map<Integer, Object> getUpdatedFieldValues() {
		return change.getUpdatedFieldValues();
	}
	
}