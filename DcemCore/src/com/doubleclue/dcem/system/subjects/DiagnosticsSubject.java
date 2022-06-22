package com.doubleclue.dcem.system.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.DcemNode;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.system.logic.SystemModule;

@ApplicationScoped
public class DiagnosticsSubject extends SubjectAbs {

    public DiagnosticsSubject() {

        RawAction rawAction = new RawAction(DcemConstants.ACTION_RESET_COUNTERS, new String[]{DcemConstants.SYSTEM_ROLE_SUPERADMIN});
        rawAction.setIcon("fa fa-refresh");
        rawAction.setActionType(ActionType.DIALOG);
        rawActions.add(rawAction);

        rawAction = new RawAction(DcemConstants.ACTION_DOWNLOAD_DIAGNOSTIC_FILE, new String[]{DcemConstants.SYSTEM_ROLE_SUPERADMIN});
        rawAction.setIcon("fa fa-download");
        rawAction.setActionType(ActionType.EL_METHOD);
        rawAction.setElMethodExpression("#{diagnosticsView.downloadDiagnosticFile()}");
        rawActions.add(rawAction);

        rawAction = new RawAction(DcemConstants.ACTION_DOWNLOAD_LOG_FILE, new String[]{DcemConstants.SYSTEM_ROLE_SUPERADMIN});
        rawAction.setIcon("fa fa-download");
        rawAction.setActionType(ActionType.EL_METHOD);
        rawAction.setElMethodExpression("#{diagnosticsView.downloadLogFile()}");
        rawActions.add(rawAction);

        rawAction = new RawAction(DcemConstants.ACTION_SHOW_DIAGNOSTIC_CHARTS, new String[]{DcemConstants.SYSTEM_ROLE_SUPERADMIN});
        rawAction.setIcon("fa fa-eye");
        rawAction.setActionType(ActionType.DIALOG);
        rawActions.add(rawAction);
     /*   rawAction = new RawAction(DcemConstants.ACTION_SHOW_DIAGNOSTIC_CHARTS, new String[]{DcemConstants.SYSTEM_ROLE_SUPERADMIN});
        rawAction.setActionType(ActionType.EL_METHOD);
        //rawAction.setElMethodExpression("#{diagnosticsView.showDiagnosticsCharts()}");
        rawActions.add(rawAction);
*/
        rawActions.add(new RawAction(DcemConstants.ACTION_VIEW,
                new String[]{DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}));
        rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[]{DcemConstants.SYSTEM_ROLE_SUPERADMIN}));
        for (RawAction action: rawActions) {
			action.setMasterOnly(true);
		}

    }

    public String getModuleId() {
        return SystemModule.MODULE_ID;
    }

    public int getRank() {
        return 40;
    }

    @Override
    public String getIconName() {
        return "fa fa-line-chart";
    }

    @Override
    public String getPath() {
        return DcemConstants.DIAGNOSTICS_VIEW_PATH;
    }

    @Override
    public Class<?> getKlass() {
        return DcemNode.class;
    }
}