package com.doubleclue.dcem.dm.logic;

import java.awt.Dimension;
import java.time.format.DateTimeFormatter;

public class DmConstants {
	
	public static final String SHOW_DOCUMENT_WITH_TAG = "showDocumentWithTag";
	public static final String DM_EL_METHOD_GOTO_DOCUMENTVIEW_WITH_TAGS = "#{dmTagView.showDocumentsWithTag()}";

	public static final String DM_DOCUMENT_VIEW_PATH = "/modules/dm/dmDocumentView.xhtml";
	public static final String DM_NEW_DOCUMENT_VIEW_PATH = "/modules/dm/dmNewDocumentView.xhtml";
	public static final String DM_SHARED_DOCUMENT_VIEW_PATH = "/modules/dm/dmSharedDocumentView.xhtml";
	public static final String DM_RECYCLE_BIN_VIEW_PATH = "/modules/dm/dmRecycleBinView.xhtml";
	public static final String DM_TAG_DIALOG = "/modules/dm/dmTagDialog.xhtml";

	public static final String SPECIAL_CHARACTERS = "&;<=>[]^`{|}";
	public static final String PDF_EXTENSION = ".pdf";
	public static final String DOUBLE_CLUE_DM = "DoubleClueDM-";

	public static final int MAX_TEXT_EXTRACT = 255;
	public static final String WORKFLOW_TIME_TEMPLATE = DocumentManagementModule.MODULE_ID + ".workFlowTimeTemplate";
	public static final String WORKFLOW_TIME_SUBJECT = DocumentManagementModule.MODULE_ID + ".WorkFlowTimeSubject";
	public static final String DOCUMENT_NAME = "documentName";
	public static final String WORKFLOW_NAME = "workflowName";
	public static final String WORKFLOW_INFORMATION = "workflowInformation";
	public static final String WORKFLOW_TRIGGER = "workflowTrigger";
	public static final String WORKFLOW_USER = "user";
	public static final String ACTION_DOCUMENTS = "documents";
	public static Dimension THUMBNAIL_DIMENSION = new Dimension(240, 240);

	

}
