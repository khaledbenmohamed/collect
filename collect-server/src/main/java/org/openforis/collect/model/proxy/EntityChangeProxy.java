package org.openforis.collect.model.proxy;

import java.util.Map;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.NodeChange.EntityChange;
import org.openforis.collect.spring.MessageContextHolder;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;

/**
 * 
 * @author S. Ricci
 *
 */
public class EntityChangeProxy extends NodeChangeProxy<EntityChange> {

	public EntityChangeProxy(
			MessageContextHolder messageContextHolder,
			EntityChange change) {
		super(messageContextHolder, change);
	}
	
	@ExternalizedProperty
	public Map<String, Boolean> getRelevant() {
		return change.getChildrenRelevance();
	}

	@ExternalizedProperty
	public Map<String, Boolean> getRequired() {
		return change.getChildrenRequireness();
	}

	@ExternalizedProperty
	public Map<String, ValidationResultFlag> getMinCountValidation() {
		return change.getChildrenMinCountValidation();
	}

	@ExternalizedProperty
	public Map<String, ValidationResultFlag> getMaxCountValidation() {
		return change.getChildrenMaxCountValidation();
	}

}