package com.example.acceptemptylistinplugin.plugin;

import static org.mybatis.generator.internal.util.StringUtility.*;

import java.util.Iterator;
import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.VisitableElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class AcceptEmptyListInPlugin extends PluginAdapter {

	private static final String UPDATE_REGEX = "(?i).*update.*";

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	@Override
	public boolean sqlMapExampleWhereClauseElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {

		// Check whether the input element is for update.
		List<Attribute> attributes = element.getAttributes();
		String sqlId = attributes.get(0).getValue();
		boolean isUpdate = sqlId.matches(UPDATE_REGEX);

		Iterator<VisitableElement> iterator = element.getElements().iterator();
		while (iterator.hasNext()) {
			VisitableElement ve = iterator.next();
			if (ve instanceof XmlElement) {
				String elementName = ((XmlElement) ve).getName();
				// Remove the child element in case which it is a where element.
				// Then add a new element.
				if (elementName.equals("where")) {
					iterator.remove();
					addWhereElements(element, isUpdate, introspectedTable);
					break;
				}
			}
		}

		return true;
	}

	private void addWhereElements(XmlElement parentElement, boolean isForUpdateByExample,
			IntrospectedTable introspectedTable) {

		XmlElement whereElement = new XmlElement("where"); //$NON-NLS-1$
		parentElement.addElement(whereElement);

		XmlElement outerForEachElement = new XmlElement("foreach"); //$NON-NLS-1$
		if (isForUpdateByExample) {
			outerForEachElement.addAttribute(new Attribute(
					"collection", "example.oredCriteria")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			outerForEachElement.addAttribute(new Attribute(
					"collection", "oredCriteria")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		outerForEachElement.addAttribute(new Attribute("item", "criteria")); //$NON-NLS-1$ //$NON-NLS-2$
		outerForEachElement.addAttribute(new Attribute("separator", "or")); //$NON-NLS-1$ //$NON-NLS-2$
		whereElement.addElement(outerForEachElement);

		XmlElement ifElement = new XmlElement("if"); //$NON-NLS-1$
		ifElement.addAttribute(new Attribute("test", "criteria.valid")); //$NON-NLS-1$ //$NON-NLS-2$
		outerForEachElement.addElement(ifElement);

		XmlElement trimElement = new XmlElement("trim"); //$NON-NLS-1$
		trimElement.addAttribute(new Attribute("prefix", "(")); //$NON-NLS-1$ //$NON-NLS-2$
		trimElement.addAttribute(new Attribute("suffix", ")")); //$NON-NLS-1$ //$NON-NLS-2$
		trimElement.addAttribute(new Attribute("prefixOverrides", "and")); //$NON-NLS-1$ //$NON-NLS-2$

		ifElement.addElement(trimElement);

		trimElement.addElement(getMiddleForEachElement(null));

		for (IntrospectedColumn introspectedColumn : introspectedTable.getNonBLOBColumns()) {
			if (stringHasValue(introspectedColumn.getTypeHandler())) {
				trimElement.addElement(getMiddleForEachElement(introspectedColumn));
			}
		}

	}

	private XmlElement getMiddleForEachElement(IntrospectedColumn introspectedColumn) {
		StringBuilder sb = new StringBuilder();
		String criteriaAttribute;
		boolean typeHandled;
		String typeHandlerString;
		if (introspectedColumn == null) {
			criteriaAttribute = "criteria.criteria"; //$NON-NLS-1$
			typeHandled = false;
			typeHandlerString = null;
		} else {
			sb.setLength(0);
			sb.append("criteria."); //$NON-NLS-1$
			sb.append(introspectedColumn.getJavaProperty());
			sb.append("Criteria"); //$NON-NLS-1$
			criteriaAttribute = sb.toString();

			typeHandled = true;

			sb.setLength(0);
			sb.append(",typeHandler="); //$NON-NLS-1$
			sb.append(introspectedColumn.getTypeHandler());
			typeHandlerString = sb.toString();
		}

		XmlElement middleForEachElement = new XmlElement("foreach"); //$NON-NLS-1$
		middleForEachElement.addAttribute(new Attribute(
				"collection", criteriaAttribute)); //$NON-NLS-1$
		middleForEachElement.addAttribute(new Attribute("item", "criterion")); //$NON-NLS-1$ //$NON-NLS-2$

		XmlElement chooseElement = new XmlElement("choose"); //$NON-NLS-1$
		middleForEachElement.addElement(chooseElement);

		XmlElement when = new XmlElement("when"); //$NON-NLS-1$
		when.addAttribute(new Attribute("test", "criterion.noValue")); //$NON-NLS-1$ //$NON-NLS-2$
		when.addElement(new TextElement("and ${criterion.condition}")); //$NON-NLS-1$
		chooseElement.addElement(when);

		when = new XmlElement("when"); //$NON-NLS-1$
		when.addAttribute(new Attribute("test", "criterion.singleValue")); //$NON-NLS-1$ //$NON-NLS-2$
		sb.setLength(0);
		sb.append("and ${criterion.condition} #{criterion.value"); //$NON-NLS-1$
		if (typeHandled) {
			sb.append(typeHandlerString);
		}
		sb.append('}');
		when.addElement(new TextElement(sb.toString()));
		chooseElement.addElement(when);

		when = new XmlElement("when"); //$NON-NLS-1$
		when.addAttribute(new Attribute("test", "criterion.betweenValue")); //$NON-NLS-1$ //$NON-NLS-2$
		sb.setLength(0);
		sb.append("and ${criterion.condition} #{criterion.value"); //$NON-NLS-1$
		if (typeHandled) {
			sb.append(typeHandlerString);
		}
		sb.append("} and #{criterion.secondValue"); //$NON-NLS-1$
		if (typeHandled) {
			sb.append(typeHandlerString);
		}
		sb.append('}');
		when.addElement(new TextElement(sb.toString()));
		chooseElement.addElement(when);

		when = new XmlElement("when"); //$NON-NLS-1$
		when.addAttribute(new Attribute("test", "criterion.listValue")); //$NON-NLS-1$ //$NON-NLS-2$

		// Insert here an if block to check that the input list has at least one value.
		XmlElement innerIf = new XmlElement("if");
		innerIf.addAttribute(new Attribute("test", "criterion.value.size() > 0"));
		innerIf.addElement(new TextElement("and ${criterion.condition}")); //$NON-NLS-1$
		XmlElement innerForEach = new XmlElement("foreach"); //$NON-NLS-1$
		innerForEach.addAttribute(new Attribute("collection", "criterion.value")); //$NON-NLS-1$ //$NON-NLS-2$
		innerForEach.addAttribute(new Attribute("item", "listItem")); //$NON-NLS-1$ //$NON-NLS-2$
		innerForEach.addAttribute(new Attribute("open", "(")); //$NON-NLS-1$ //$NON-NLS-2$
		innerForEach.addAttribute(new Attribute("close", ")")); //$NON-NLS-1$ //$NON-NLS-2$
		innerForEach.addAttribute(new Attribute("separator", ",")); //$NON-NLS-1$ //$NON-NLS-2$
		sb.setLength(0);
		sb.append("#{listItem"); //$NON-NLS-1$
		if (typeHandled) {
			sb.append(typeHandlerString);
		}
		sb.append('}');
		innerForEach.addElement(new TextElement(sb.toString()));
		innerIf.addElement(innerForEach);
		when.addElement(innerIf);

		// Insert here an if block to check that the input list is empty.
		innerIf = new XmlElement("if");
		innerIf.addAttribute(new Attribute("test", "criterion.value.size() == 0"));
		innerIf.addElement(new TextElement("and false"));
		when.addElement(innerIf);

		chooseElement.addElement(when);

		return middleForEachElement;
	}

}
