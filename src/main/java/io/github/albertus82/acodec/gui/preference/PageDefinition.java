package io.github.albertus82.acodec.gui.preference;

import java.util.Locale;

import org.eclipse.jface.resource.ImageDescriptor;

import io.github.albertus82.acodec.gui.resources.GuiMessages;
import io.github.albertus82.jface.preference.page.BasePreferencePage;
import io.github.albertus82.jface.preference.page.IPageDefinition;
import io.github.albertus82.jface.preference.page.PageDefinitionDetails;
import io.github.albertus82.jface.preference.page.PageDefinitionDetails.PageDefinitionDetailsBuilder;

public enum PageDefinition implements IPageDefinition {

	GENERAL;

	private static final String LABEL_KEY_PREFIX = "label.preferences.";

	private final PageDefinitionDetails pageDefinitionDetails;

	PageDefinition() {
		this(new PageDefinitionDetailsBuilder().build());
	}

	PageDefinition(final PageDefinitionDetails pageDefinitionDetails) {
		this.pageDefinitionDetails = pageDefinitionDetails;
		if (pageDefinitionDetails.getNodeId() == null) {
			pageDefinitionDetails.setNodeId(name().toLowerCase(Locale.ROOT).replace('_', '.'));
		}
		if (pageDefinitionDetails.getLabel() == null) {
			pageDefinitionDetails.setLabel(() -> GuiMessages.INSTANCE.get(LABEL_KEY_PREFIX + pageDefinitionDetails.getNodeId()));
		}
	}

	@Override
	public String getNodeId() {
		return pageDefinitionDetails.getNodeId();
	}

	@Override
	public String getLabel() {
		return pageDefinitionDetails.getLabel().get();
	}

	@Override
	public Class<? extends BasePreferencePage> getPageClass() {
		return pageDefinitionDetails.getPageClass();
	}

	@Override
	public IPageDefinition getParent() {
		return pageDefinitionDetails.getParent();
	}

	@Override
	public ImageDescriptor getImage() {
		return pageDefinitionDetails.getImage();
	}

}
