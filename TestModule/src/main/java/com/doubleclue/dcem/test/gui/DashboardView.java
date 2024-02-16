package com.doubleclue.dcem.test.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.doubleclue.dcem.admin.gui.UserDialogBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.test.logic.AbstractTestUnit;
import com.doubleclue.dcem.test.logic.DashboardLogic;
import com.doubleclue.dcem.test.logic.TestStatus;
import com.doubleclue.dcem.test.logic.TestUnitGroupEnum;
import com.doubleclue.dcem.test.subjects.DashboardSubject;

@SuppressWarnings("serial")
@Named("dashboardView")
@SessionScoped
public class DashboardView extends DcemView {

	private TreeNode root;
	private HashMap<TestUnitGroupEnum, TreeNode> testGroupNodes;

	@Inject
	private DashboardSubject dashboardSubject;

	@Inject
	DashboardLogic dashboardLogic;

	@Inject
	private UserDialogBean userDialogBean;

	TreeNode[] selectedTreeNodes;

	@PostConstruct
	private void init() {
		userDialogBean.setParentView(this);
		subject = dashboardSubject;
		testGroupNodes = new HashMap<TestUnitGroupEnum, TreeNode>();
		for (TestUnitGroupEnum testUnitGroup : TestUnitGroupEnum.values()) {
			testGroupNodes.put(testUnitGroup, null);
		}
		selectedTreeNodes = null;
	}

	public void reloadTestUnits() {
		try {
			dashboardLogic.reLoadTestUnits();
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
		}
		selectedTreeNodes = null;
	}

	public void runTestUnits() {
		System.out.println("DashboardView.runTestUnits()");
		if (selectedTreeNodes.length > 0) {
			List<AbstractTestUnit> testUnitList = new ArrayList<AbstractTestUnit>();
			for (TreeNode treeNode : selectedTreeNodes) {
				try {
					AbstractTestUnit currentTestUnit = (AbstractTestUnit) treeNode.getData();
					if (currentTestUnit.isRunnableTest() == true) {
						testUnitList.add(currentTestUnit);
					}
				} catch (Exception e) {
					JsfUtils.addErrorMessage(e.getMessage());
				}
			}
			try {
				dashboardLogic.runTestUnits(testUnitList);
			} catch (Exception e) {
				JsfUtils.addErrorMessage(e.getMessage());
			} finally {
				selectedTreeNodes = null;
			}
		}
	}

	public String getRunningTestUnit() {
		return dashboardLogic.getRunningTestUnit();
	}

	public boolean isTestUnitRunning() {
		if (dashboardLogic.getRunningTestUnit() == null) {
			return false;
		} else {
			return true;
		}
	}

	public void stopTestUnit() {
		System.out.println("DashboardView.stopTestUnit()");
		if (selectedTreeNodes != null) {
			dashboardLogic.cancelTestUnits();
		}
		selectedTreeNodes = null;
	}

	public class TestUnitNode extends AbstractTestUnit {
		String name;
		String description;
		TestStatus testStatus;

		public TestUnitNode(String unitName, String unitDescription) {
			this.name = unitName;
			this.description = unitDescription;
			this.testStatus = null;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public String getAuthor() {
			return null;
		}

		@Override
		public List<String> getDependencies() {
			return null;
		}

		@Override
		public TestUnitGroupEnum getParent() {
			return null;
		}

		@Override
		public boolean isRunnableTest() {
			return false;
		}

		public TestStatus getTestStatus() {
			return testStatus;
		}

		public void setTestStatus(TestStatus testStatus) {
			this.testStatus = testStatus;
		}
	}

	public TreeNode getRootNode() throws Exception {
		root = new DefaultTreeNode(new TestUnitNode(null, null), null);
		try {
			for (TestUnitGroupEnum testUnitGroup : TestUnitGroupEnum.values()) {
				addGroup(testUnitGroup);
			}
		} catch (Exception e) {
			logger.warn("Loading of group nodes failed", e);
			throw e;
		}
		try {
			for (AbstractTestUnit testUnit : dashboardLogic.getTestUnits()) {
				if (testUnit.getParent() == null) {
					new DefaultTreeNode(testUnit, root);
				}
			}
		} catch (Exception e) {
			logger.warn("Loading single nodes failed", e);
			throw e;
		}
		return root;
	}

	public void addGroup(TestUnitGroupEnum testUnitGroup) throws Exception {
		TreeNode targetNode;
		if (testUnitGroup.getParent() == null) {
			targetNode = root;
		} else {
			targetNode = testGroupNodes.get(testUnitGroup.getParent());
		}
		TreeNode groupNode = new DefaultTreeNode(new TestUnitNode(testUnitGroup.toString(), testUnitGroup.getDescription()), targetNode);
		for (AbstractTestUnit testUnit : dashboardLogic.getTestUnits()) {
			if (testUnit.getParent() == testUnitGroup) {
				new DefaultTreeNode(testUnit, groupNode);
			}
		}
		testGroupNodes.put(testUnitGroup, groupNode);
		groupNode.setExpanded(true);
	}

	public String getStatusRowColor(String status) {
		switch (status) {
		case "Passed":
			return "rowColorClassGreen";
		case "Error":
			return "rowColorClassRed";
		case "Running":
			return "rowColorClassBlue";
		default:
			return "";
		}
	}

	public String getGroupRowColor(boolean isRunnableTest) {
		if (isRunnableTest == true) {
			return null;
		} else {
			return "rowColorClassGrey";
		}
	}

	public TreeNode[] getSelectedTreeNodes() {
		return selectedTreeNodes;
	}

	public void setSelectedTreeNodes(TreeNode[] selectedTreeNodes) {
		this.selectedTreeNodes = selectedTreeNodes;
	}

}
