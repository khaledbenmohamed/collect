package org.openforis.collect.io.metadata.collectearth.balloon;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.io.metadata.collectearth.balloon.CEField.CEFieldType;
import org.openforis.idm.metamodel.CodeListItem;

import com.jamesmurty.utils.XMLBuilder;


/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus Diaz
 *
 */
public class CEComponentHTMLFormatter {

	private static final String NOT_AVAILABLE_ITEM_LABEL = "N/A";
	private static final String NOT_AVAILABLE_ITEM_CODE = "-1";

//	public String format(CEComponent comp) {
//		try {
//			if (comp instanceof CEField) {
//				return format((CEField) comp);
//			} else {
//				return format((CETable) comp);
//			}
//		} catch(Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//	
	
	public String format(CETabSet tabSet) {
		try {
			XMLBuilder builder = createBuilder(tabSet, null);
			return writeToString(builder);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private XMLBuilder createBuilder(CETabSet tabSet, XMLBuilder parentBuilder) throws Exception {
		XMLBuilder tabSetBuilder = parentBuilder == null ? XMLBuilder.create("div") : parentBuilder.e("div");
		tabSetBuilder.a("class", "tabset");
		for (CETab tab : tabSet.getTabs()) {
			createBuilder(tab, tabSetBuilder);
		}
		return tabSetBuilder;
	}
	
	private XMLBuilder createBuilder(CETab tab, XMLBuilder parentBuilder) throws Exception {
		XMLBuilder tabBuilder;
		if (parentBuilder == null) {
			tabBuilder = XMLBuilder.create("div");
		} else if (tab.isMain()) {
			//append elements to the root builder
			tabBuilder = parentBuilder;
		} else {
			tabBuilder = parentBuilder.e("div");
		}
		if (! tab.isMain()) {
			tabBuilder.a("class", "tab");
			//TODO add legend/title
			tabBuilder.e("legend").t(tab.getLabel());
		}
		for (CEComponent component : tab.getChildren()) {
			if (component instanceof CEField) {
				createBuilder((CEField) component, true, tabBuilder);
			} else if (component instanceof CEEnumeratedEntityTable) {
				createBuilder((CEEnumeratedEntityTable) component, tabBuilder);
			}
		}
		return tabBuilder;
	}
	
	private XMLBuilder createBuilder(CEEnumeratedEntityTable comp, XMLBuilder parentBuilder) throws Exception {
		XMLBuilder builder =  parentBuilder.e("fieldset");
		builder.e("legend").t(comp.getLabel());
		XMLBuilder tableBuilder = builder.e("table").a("class", "table");
		XMLBuilder headerBuilder = tableBuilder.e("thead").e("tr");
		for (String heading : comp.getHeadings()) {
			headerBuilder.e("th").t(heading);
		}
		XMLBuilder bodyBuilder = tableBuilder.e("tbody");
		List<CETableRow> rows = comp.getRows();
		for (CETableRow row : rows) {
			XMLBuilder rowBuilder = bodyBuilder.e("tr");
			for (CEComponent child : row.getChildren()) {
				XMLBuilder cellBuilder = rowBuilder.e("td");
				if (child instanceof CEEnumeratingCodeField) {
					String elId = child.getHtmlParameterName();
					cellBuilder
						.e("label")
							.a("class", "control-label col-sm-4")
							.t(row.getLabel())
						.up()
						.e("input")
							.a("id", elId)
							.a("name", elId)
							.a("type", "hidden")
							.a("class", "form-control")
							.a("data-field-type", ((CEField) child).getType().name());	
				} else if (child instanceof CEField) {
					createBuilder((CEField) child, false, cellBuilder);
				}
			}
		}
		return builder;
	}
	
	public String format(CEField comp, boolean includeLabel) throws Exception {
		XMLBuilder builder = createBuilder(comp, includeLabel);
		return writeToString(builder);
	}
	
	private XMLBuilder createBuilder(CEField comp, boolean includeLabel) throws Exception {
		return createBuilder(comp, includeLabel, null);
	}
	
	private XMLBuilder createBuilder(CEField comp, boolean includeLabel, XMLBuilder parentBuilder) throws Exception {
		//start of external container
		String elId = comp.getHtmlParameterName();
		
		//external form-group container
		XMLBuilder formGroupBuilder = parentBuilder == null ? XMLBuilder.create("div") : parentBuilder.e("div");
		formGroupBuilder.a("class", "form-group");
		if (includeLabel) {
			//label element
			formGroupBuilder.e("label")
				.a("for", elId)
				.a("class", "control-label col-sm-4")
				.t(comp.getLabel());
		}
		//form control external container (for grid alignment)
		XMLBuilder formControlContainer = formGroupBuilder.e("div")
				.a("class", "col-sm-8");
		
		if (comp instanceof CECodeField) {
			switch(((CEField) comp).getType()) {
			case CODE_BUTTON_GROUP:
				buildCodeButtonGroup(formControlContainer, (CECodeField) comp);
				break;
			case CODE_SELECT:
				buildCodeSelect(formControlContainer, (CECodeField) comp);
				break;
			default:
				break;
			}
		} else if (comp instanceof CEEnumeratingCodeField) {
			// skip it
		} else if (comp instanceof CEField) {
			switch (((CEField) comp).getType()) {
			case SHORT_TEXT:
				formControlContainer.e("input")
					.a("id", elId)
					.a("name", elId)
					.a("type", "text")
					.a("class", "form-control");
				break;
			case LONG_TEXT:
				formControlContainer.e("textarea")
					.a("id", elId)
					.a("rows", "3")
					.a("name", elId)
					.a("class", "form-control")
					.t(" ");
				break;
			case INTEGER:
			case REAL:
				formControlContainer.e("input")
					.a("id", elId)
					.a("name", elId)
					.a("type", "text")
					.a("value", "0")
					.a("class", "form-control numeric");
				break;
			case BOOLEAN:
				formControlContainer.e("input")
					.a("id", elId)
					.a("name", elId)
					.a("type", "checkbox")
					.a("class", "form-control numeric");
				break;
			case COORDINATE:
				break;
			case DATE:
			case TIME:
				formControlContainer.e("div")
					.a("class", "input-group date " + (((CEField) comp).getType() == CEFieldType.DATE ? "datepicker" : "timepicker"))
					.e("input")
						.a("id", elId)
						.a("name", elId)
						.a("class", "form-control")
						.up()
					.e("span")
						.a("class", "input-group-addon")
						.e("span")
							.a("class", "glyphicon glyphicon-time")
					;
				break;
			default:
				break;
			}
		}
		return formGroupBuilder;
	}

	private String writeToString(XMLBuilder builder) {
		try {
			StringWriter writer = new StringWriter();
			@SuppressWarnings("serial")
			Properties outputProperties = new Properties(){{
				put(javax.xml.transform.OutputKeys.INDENT, "yes");
				put(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
				put(javax.xml.transform.OutputKeys.STANDALONE, "yes");
			}};
			builder.toWriter(writer, outputProperties);
			String result = writer.toString();
			return result;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void buildCodeSelect(XMLBuilder builder, CECodeField comp) {
		String elId = comp.getHtmlParameterName();
		
		//build select
		XMLBuilder selectBuilder = builder.e("select")
			.a("id", elId)
			.a("name", elId)
			.a("data-field-type", comp.getType().name())
			.a("class", "form-control selectboxit show-menu-arrow show-tick")
			.a("data-width", "75px");
		if (comp.getParentName() != null) {
			selectBuilder.a("data-parent-code-field-id", comp.getParentName());
		}
		//add empty option
		selectBuilder.e("option")
			.a("value", NOT_AVAILABLE_ITEM_CODE)
			.t(NOT_AVAILABLE_ITEM_LABEL);
		
		//add root items, if any
		Map<String, List<CodeListItem>> itemsByParentCode = ((CECodeField) comp).getCodeItemsByParentCode();
		List<CodeListItem> rootItems = itemsByParentCode.get("");
		if (rootItems != null) {
			for (CodeListItem item : rootItems) {
				selectBuilder.e("option")
					.a("value", item.getCode())
					.t(item.getLabel());
			}
		}
	}

	private void buildCodeButtonGroup(XMLBuilder formControlContainer, CECodeField comp) {
		String elId = comp.getHtmlParameterName();
		
		//button groups external container
		String groupId = elId + "_group";
		
		XMLBuilder itemsGroupExternalContainer = formControlContainer.e("div")
			.a("id", groupId)
			.a("class", "code-items-group");
		
		XMLBuilder hiddenInputField = itemsGroupExternalContainer.e("input")
				.a("id", elId)
				.a("name", elId)
				.a("type", "hidden")
				.a("class", "form-control")
				.a("data-field-type", comp.getType().name());
		
		if (comp.getParentName() != null) {
			hiddenInputField.a("data-parent-code-field-id", comp.getParentName());
		}
		
		Map<String, List<CodeListItem>> itemsByParentCode = ((CECodeField) comp).getCodeItemsByParentCode();
		for (Entry<String, List<CodeListItem>> entry : itemsByParentCode.entrySet()) {
			//one button group for every list of codes by parent code
			String parentCode = entry.getKey();
			String itemsGroupId = groupId + "_" + parentCode;
			XMLBuilder buttonsGroup = itemsGroupExternalContainer.e("div")
				.a("id", itemsGroupId)
				.a("class", "code-items")
				.a("data-toggle", "buttons-radio")
				.a("data-parent-code", parentCode);
			List<CodeListItem> items = entry.getValue();
			if (items == null || items.isEmpty()) {
				buttonsGroup.t(" "); //always use close tag
			} else {
				for (CodeListItem item : items) {
					XMLBuilder buttonItemBuilder = buttonsGroup.e("button")
						.a("type", "button")
						.a("class", "btn btn-info code-item")
						.a("value", item.getCode())
						.t(item.getLabel());
					if (StringUtils.isNotBlank(item.getDescription())) {
						buttonItemBuilder.a("title", item.getDescription());
					}
				}
			}
		}
	}

}
