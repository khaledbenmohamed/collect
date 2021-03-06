/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.CollectAnnotations.Annotation;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.Orientation;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class NodeDefinitionProxy extends VersionableSurveyObjectProxy {

	protected transient NodeDefinition nodeDefinition;
	protected EntityDefinitionProxy parent;
	
	public NodeDefinitionProxy(EntityDefinitionProxy parent, NodeDefinition nodeDefinition) {
		super(nodeDefinition);
		this.parent = parent;
		this.nodeDefinition = nodeDefinition;
	}

	static List<NodeDefinitionProxy> fromList(EntityDefinitionProxy parent, List<? extends NodeDefinition> list) {
		List<NodeDefinitionProxy> proxies = new ArrayList<NodeDefinitionProxy>();
		if (list != null) {
			for (NodeDefinition n : list) {
				NodeDefinitionProxy p = null;
				CollectSurvey survey = (CollectSurvey) n.getSurvey();
				UIOptions uiOptions = survey.getUIOptions();
				boolean hidden = uiOptions.isHidden(n);
				if ( ! hidden ) {
					if (n instanceof AttributeDefinition) {
						if (n instanceof BooleanAttributeDefinition) {
							p = new BooleanAttributeDefinitionProxy(parent, (BooleanAttributeDefinition) n);
						} else if (n instanceof CodeAttributeDefinition) {
							p = new CodeAttributeDefinitionProxy(parent, (CodeAttributeDefinition) n);
						} else if (n instanceof CoordinateAttributeDefinition) {
							p = new CoordinateAttributeDefinitionProxy(parent, (CoordinateAttributeDefinition) n);
						} else if (n instanceof DateAttributeDefinition) {
							p = new DateAttributeDefinitionProxy(parent, (DateAttributeDefinition) n);
						} else if (n instanceof FileAttributeDefinition) {
							p = new FileAttributeDefinitionProxy(parent, (FileAttributeDefinition) n);
						} else if (n instanceof NumberAttributeDefinition) {
							p = new NumberAttributeDefinitionProxy(parent, (NumberAttributeDefinition) n);
						} else if (n instanceof RangeAttributeDefinition) {
							p = new RangeAttributeDefinitionProxy(parent, (RangeAttributeDefinition) n);
						} else if (n instanceof TaxonAttributeDefinition) {
							p = new TaxonAttributeDefinitionProxy(parent, (TaxonAttributeDefinition) n);
						} else if (n instanceof TextAttributeDefinition) {
							p = new TextAttributeDefinitionProxy(parent, (TextAttributeDefinition) n);
						} else if (n instanceof TimeAttributeDefinition) {
							p = new TimeAttributeDefinitionProxy(parent, (TimeAttributeDefinition) n);
						} else {
							throw new RuntimeException("AttributeDefinition not supported: " + n.getClass().getSimpleName());
						}
					} else if (n instanceof EntityDefinition) {
						p = new EntityDefinitionProxy(parent, (EntityDefinition) n);
					}
					if ( p != null ) {
						proxies.add(p);
					}
				}
			}
		}
		return proxies;
	}

	public Set<QName> getAnnotationNames() {
		return nodeDefinition.getAnnotationNames();
	}

	@ExternalizedProperty
	public String getName() {
		return nodeDefinition.getName();
	}

	@ExternalizedProperty
	public boolean isMultiple() {
		return nodeDefinition.isMultiple();
	}

	@ExternalizedProperty
	public List<NodeLabelProxy> getLabels() {
		return NodeLabelProxy.fromNodeLabelList(nodeDefinition.getLabels());
	}

	@ExternalizedProperty
	public List<PromptProxy> getPrompts() {
		return PromptProxy.fromList(nodeDefinition.getPrompts());
	}

	@ExternalizedProperty
	public List<LanguageSpecificTextProxy> getDescriptions() {
		return LanguageSpecificTextProxy.fromList(nodeDefinition.getDescriptions());
	}

	@ExternalizedProperty
	public String getPath() {
		return nodeDefinition.getPath();
	}

	@ExternalizedProperty
	public String getUiTabName() {
		String tabName = nodeDefinition.getAnnotation(Annotation.TAB_NAME.getQName());
		if ( tabName != null) {
			return tabName;
		} else {
			return parent.getUiTabName();
		}
	}

	@ExternalizedProperty
	public boolean isHideWhenNotRelevant() {
		boolean result = getUIOptions().isHideWhenNotRelevant(nodeDefinition);
		return result;
	}

	@ExternalizedProperty
	public int getColumn() {
		int result = getUIOptions().getColumn(nodeDefinition);
		return result;
	}

	@ExternalizedProperty
	public int getColumnSpan() {
		int result = getUIOptions().getColumnSpan(nodeDefinition);
		return result;
	}
	
	@ExternalizedProperty
	public Integer getWidth() {
		Integer width = getUIOptions().getWidth(nodeDefinition);
		return width;
	}

	@ExternalizedProperty
	public Integer getLabelWidth() {
		Integer width = getUIOptions().getLabelWidth(nodeDefinition);
		return width;
	}
	
	@ExternalizedProperty
	public Orientation getLabelOrientation() {
		Orientation orientation = getUIOptions().getLabelOrientation(nodeDefinition);
		return orientation;
	}
	
	public EntityDefinitionProxy getParent() {
		return parent;
	}

	protected UIOptions getUIOptions() {
		CollectSurvey survey = (CollectSurvey) nodeDefinition.getSurvey();
		return survey.getUIOptions();
	}
	
	protected CollectAnnotations getAnnotations() {
		CollectSurvey survey = (CollectSurvey) nodeDefinition.getSurvey();
		return survey.getAnnotations();
	}
}
