/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import liquibase.util.StringUtils;

import org.openforis.collect.designer.component.SchemaTreeModel;
import org.openforis.collect.designer.form.CodeAttributeDefinitionFormObject;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.MessageUtil.ConfirmParams;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.SurveyObject;
import org.zkoss.bind.Binder;
import org.zkoss.bind.Form;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Window;

/**
 * @author S. Ricci
 *
 */
public class CodeAttributeVM extends AttributeVM<CodeAttributeDefinition> {

	private static final String FORM_ID = "fx";

	private Window parentSelectorPopUp;

	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentEntity") EntityDefinition parentEntity, 
			@ExecutionArgParam("item") CodeAttributeDefinition attributeDefn, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		super.init(parentEntity, attributeDefn, newItem);
	}

	@Command
	public void onListChanged(@ContextParam(ContextType.BINDER) final Binder binder,
			@BindingParam("list") final CodeList list) {
		if ( editedItem.hasDependentCodeAttributeDefinitions() ) {
			ConfirmParams confirmParams = new ConfirmParams(new MessageUtil.CompleteConfirmHandler() {
				
				@Override
				public void onOk() {
					CodeAttributeDefinitionFormObject fo = (CodeAttributeDefinitionFormObject) getFormObject();
					fo.setParentCodeAttributeDefinition(null);
					fo.setList(list);
					notifyChange("formObject");
					dispatchApplyChangesCommand(binder);
				}
				@Override
				public void onCancel() {
					Form form = getForm(binder);
					CodeList oldList = editedItem.getList();
					setValueOnFormField(form, "list", oldList);
				}
			}, "survey.schema.attribute.code.confirm_change_list_on_referenced_node.message");
			confirmParams.setOkLabelKey("survey.schema.attribute.code.confirm_change_list_on_referenced_node.ok");
			confirmParams.setTitleKey("survey.schema.attribute.code.confirm_change_list_on_referenced_node.title");
			List<String> dependentAttributePaths = new ArrayList<String>();
			for (CodeAttributeDefinition codeAttributeDefinition : editedItem.getDependentCodeAttributeDefinitions()) {
				dependentAttributePaths.add(codeAttributeDefinition.getPath());
			}
			confirmParams.setMessageArgs(new String[]{StringUtils.join(dependentAttributePaths, ", ")});
			MessageUtil.showConfirm(confirmParams);
		}
	}
	
	@GlobalCommand
	public void codeListsPopUpClosed(@ContextParam(ContextType.BINDER) Binder binder, 
			@BindingParam(CodeListsVM.EDITING_ATTRIBUTE_PARAM) Boolean editingAttribute, 
			@BindingParam(CodeListsVM.SELECTED_CODE_LIST_PARAM) CodeList selectedCodeList) {
		if ( editingAttribute ) {
			if ( selectedCodeList != null ) {
				onListChanged(binder, selectedCodeList);
			}
			validateForm(binder);
		}
	}

	protected Form getForm(Binder binder) {
		Component view = binder.getView();
		return (Form) view.getAttribute(FORM_ID);
	}

	@Command
	public void openParentAttributeSelector() {
		String title = Labels.getLabel("survey.schema.attribute.code.select_parent_for_node", new String[]{editedItem.getName()});

		final Collection<CodeAttributeDefinition> assignableParentAttributes = editedItem.getAssignableParentCodeAttributeDefinitions();
		if ( assignableParentAttributes.isEmpty() ) {
			MessageUtil.showWarning("survey.schema.attribute.code.no_assignable_parent_available");
		} else {
			CodeAttributeDefinition parentCodeAttributeDefinition = ((CodeAttributeDefinitionFormObject) formObject).getParentCodeAttributeDefinition();
			SchemaTreeModel.Predicate<SurveyObject> includePredicate = new SchemaTreeModel.Predicate<SurveyObject>() {
				@Override
				public boolean evaluate(SurveyObject item) {
					return item instanceof UITab || item instanceof EntityDefinition ||
							item instanceof CodeAttributeDefinition && assignableParentAttributes.contains(item);
				}
			};
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("rootEntity", editedItem.getRootEntity());
			args.put("version", null);
			args.put("title", title);
			args.put("includePredicate", includePredicate);
			args.put("selection", parentCodeAttributeDefinition);
			parentSelectorPopUp = openPopUp(Resources.Component.SCHEMA_TREE_POPUP.getLocation(), true, args);
		}
	}

	@GlobalCommand
	public void closeParentAttributeSelector() {
		if ( parentSelectorPopUp != null ) {
			closePopUp(parentSelectorPopUp);
			parentSelectorPopUp = null;
		}
	}
	
	@GlobalCommand
	public void schemaTreeNodeSelected(@BindingParam("node") SurveyObject surveyObject) {
		CodeAttributeDefinition parentAttrDefn = (CodeAttributeDefinition) surveyObject;
		((CodeAttributeDefinitionFormObject) formObject).setParentCodeAttributeDefinition(parentAttrDefn);
		notifyChange("formObject");
		closeParentAttributeSelector();
	}

}
