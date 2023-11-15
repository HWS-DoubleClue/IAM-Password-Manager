<#ftl output_format="plainText">
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xmlns:h="http://java.sun.com/jsf/html" xmlns:f="http://java.sun.com/jsf/core"
	xmlns:ui="http://java.sun.com/jsf/facelets" xmlns:p="http://primefaces.org/ui" xmlns:c="http://java.sun.com/jsp/jstl/core">

<ui:include src="/mgt/modules/dialogHead.xhtml" />
<f:view locale="${r"#{operatorSession.locale}"}">

	<h:body class="dialogBody">
		<span id="customheader"> <h:outputLabel class="${r"#{autoView.icon}"} dialogTitle" /> <h:outputLabel value="${r"#{viewNavigator.dialogTitle}"}"
				styleClass="dialogTitle" />
		</span>

		<h:form id="dialogForm" style="padding-bottom: 100px">

			<p:messages showSummary="true" showDetail="false" closable="true">
				<p:autoUpdate />
			</p:messages>

			<h:panelGrid columns="2" cellpadding="4" cellspacing="4">
${dialogTable}	
			</h:panelGrid>
			<p />
			<p:commandButton id="ok" style="width: 8em" icon="fa fa-check" value="${r"#{CoreMsg['OK']}"}" ajax="true" actionListener="${r"#{autoDialog.actionOk}"}" update="@form"	 />
			<p:spacer width="12" />
			<p:commandButton id="closeDialog" value="${r"#{CoreMsg['cancel']}"}" icon="fa fa-close" style="width: 8em" immediate="true"
				action="${r"#{viewNavigator.actionCloseDialog}"}" />
		</h:form>
	</h:body>
</f:view>
</html>
