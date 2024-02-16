#define MyAppVersion "2.11.0-SNAPSHOT"
#define MyAppName "DoubleClue Enterprise Management"

#include "services.iss"

[Setup]
SignTool=signtool
AppName={#MyAppName}
AppVersion={#MyAppVersion}
DefaultDirName={pf}\{#MyAppName}
OutputDir=..\target
OutputBaseFilename=DCEM-{#MyAppVersion}
SetupIconFile=installer_icon.ico
DisableDirPage=no
AlwaysShowDirOnReadyPage=yes
DefaultGroupName=DoubleClue Management
DisableProgramGroupPage=no
AlwaysShowGroupOnReadyPage=yes
WizardImageStretch=Yes
WizardSmallImageFile=DC_Logo.bmp
WizardImageFile=DC-install-03-01.bmp
ArchitecturesInstallIn64BitMode=x64 ia64
UsePreviousAppDir=Yes
DisableWelcomePage=no

[Icons]
Name: "{group}\Start DCEM Service"; Filename: "{app}\bat\startDcemService.bat"; 
Name: "{group}\Stop DCEM Service"; Filename: "{app}\bat\stopDcemService.bat"; 
Name: "{group}\REST-API Documentation"; Filename: "{app}\doc\REST-WebServices\index.html"
Name: "{group}\Administration Manual"; Filename: "{app}\doc\DCEM_Manual_{code:GetLanguage}.pdf"
Name: "{group}\Documentation"; Filename: "{app}\doc"

[Files]
Source: "..\target\DCEM-Windows\DCEM\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs;


[Run]
;Filename: "{app}\bat\installPortalDemoService.bat"; Parameters: "postinstall";  StatusMsg: "Installing Portal Demo Service..."; 
;Filename: "{app}\bat\startPortalDemoService.bat"; Parameters: "postinstall";  StatusMsg: "Starting Portal Demo Service..."; 


[InstallDelete]
Type: filesandordirs; Name: "{app}\bin";
Type: filesandordirs; Name: "{app}\jvm";

[UninstallRun]
// Filename: "{app}\bat\uninstallPortalDemoService.bat"; Flags: runhidden waituntilterminated;
Filename: "{app}\bat\uninstallDcemService.bat"; Flags: waituntilterminated; RunOnceId: "DelService"; StatusMsg: "Uninstalling DoubleClue Enterprise Management Service...";

[Code]
var
    ExitCode: Integer;
           
function GetLanguage(Param: String): String;
begin
  if (GetUILanguage = $0407) then
  begin
    Result:='DE';
  end
  else begin
    Result:='EN';
  end
end;

procedure DoPreInstall();
var ResultCode: Integer;
begin
  // If our service is already installed, stop it!
  if IsServiceInstalled('DcemApplication') = true then begin
    Exec('net', 'stop DcemApplication', '', SW_HIDE, ewWaitUntilTerminated, ResultCode)
    StopService('DcemApplication');
  end;
  if IsServiceInstalled('PortalDemo') = true then begin
    Exec('net', 'stop PortalDemo', '', SW_HIDE, ewWaitUntilTerminated, ResultCode)
    StopService('PortalDemo');
  end;
end;

procedure DoPostInstall();
begin
  if Exec(ExpandConstant('{app}\bat\runSetup.bat'), '', '', SW_SHOW,
     ewWaitUntilTerminated, ExitCode) then
    begin
      if ExitCode = 0 then
        begin
          if Exec(ExpandConstant('{app}\bat\installDcemService.bat'), '', '', SW_HIDE,
               ewWaitUntilTerminated, ExitCode) then
            begin
              if ExitCode = 0 then
                begin
                  if Exec(ExpandConstant('{app}\bat\startDcemService.bat'), '', '', SW_HIDE,
                     ewWaitUntilTerminated, ExitCode) then
                    begin
                      if ExitCode <> 0 then
                        begin
                          MsgBox('Starting the Service failed with the following Error Code: ' + IntToStr(ExitCode), mbInformation, MB_OK);
                        end
                    end
                  else begin
                    MsgBox('Error Running the script (Start Service).', mbInformation, MB_OK);
                  end
                end
              else begin
                MsgBox('Installing the Service failed with the following Error Code: ' + IntToStr(ExitCode), mbInformation, MB_OK);
              end
            end
          else begin
            MsgBox('Error Running the script (Install Service).', mbInformation, MB_OK);
          end
        end
      else begin
        MsgBox('Running the Setup failed with the following Error Code: ' + IntToStr(ExitCode), mbInformation, MB_OK);
      end
    end
  else begin
    MsgBox('Error Running the script (Run Setup).', mbInformation, MB_OK);
  end;
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssInstall then begin
    DoPreInstall();
  end else if (CurStep = ssPostInstall) then begin
    DoPostInstall();
  end;
end;
