#define MyAppVersion "2.8.0-SNAPSHOT"
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
//Name: "{group}\Start PortalDemo Service"; Filename: "{app}\bat\startPortalDemoService.bat"; 
//Name: "{group}\Stop PortalDemo Service"; Filename: "{app}\bat\stopPortalDemoService.bat";
Name: "{group}\REST-API Documentation"; Filename: "{app}\doc\REST-WebServices\index.html"
Name: "{group}\Administration Manual"; Filename: "{app}\doc\DCEM_Manual_{code:GetLanguage}.pdf"
Name: "{group}\Documentation"; Filename: "{app}\doc"

[Files]
Source: "..\target\DCEM-Windows\DCEM\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs;
Source: {code:GetDataDir}; DestDir: "{app}\DCEM_HOME\"; Flags: external onlyifdoesntexist; Check: IsDataDir

[Run]
;Filename: "{app}\bat\installPortalDemoService.bat"; Parameters: "postinstall";  StatusMsg: "Installing Portal Demo Service..."; 
;Filename: "{app}\bat\startPortalDemoService.bat"; Parameters: "postinstall";  StatusMsg: "Starting Portal Demo Service..."; 

;[Types]
;Name: "full"; Description: "Full installation"; Flags: iscustom
;Name: "dcem"; Description: "Only DCEM"
;Name: "userportal"; Description: "Only User Portal"
;Name: "portaldemo"; Description: "Only Portal Demo"

; [Components]
;  Name: "dcem"; Description: "DCEM"; Types: full dcem
; Name: "portals"; Description: "DoubleClue Portals";
; Name: "portals\userportal"; Description: "User Portal"; Types: full userportal
; Name: "portals\portaldemo"; Description: "Portal Demo"; Types: full portaldemo


[InstallDelete]
Type: filesandordirs; Name: "{app}\bin";
Type: filesandordirs; Name: "{app}\jvm";

[UninstallRun]
// Filename: "{app}\bat\uninstallPortalDemoService.bat"; Flags: runhidden waituntilterminated;
Filename: "{app}\bat\uninstallDcemService.bat"; Flags: waituntilterminated; StatusMsg: "Uninstalling DCEM Service...";

[Code]
var
    UsagePage: TInputOptionWizardPage;
    DataDirPage: TInputFileWizardPage;
    LightMsgPage: TOutputMsgWizardPage;
    NotepadLocation: string;
    UsageSelection: Integer;
    ExitCode: Integer;

procedure InitializeWizard;
begin
    { Create the pages }
    UsagePage := CreateInputOptionPage(wpSelectComponents,
        'Specify Usage of DoubleClue', ' ',
        'Please specify if this is the first installation of DCEM',
        True, False);
      UsagePage.Add('Install first DCEM cluster node');
      UsagePage.Add('Update current DCEM cluster node');
      UsagePage.Add('Install an additional DCEM cluster node (Configuration file of first cluster node is required!)');

      DataDirPage := CreateInputFilePage(UsagePage.ID,
  'Select Configuration Location', ' ',
  'Select where the configuration should be located, then click "Next".');
    // Add item
  DataDirPage.Add('Location of the "Configuration.xml" file:',         // caption
  'XML files|Configuration.xml|All files|*.*',    // filters
  '.xml');                               // default extension

  LightMsgPage := CreateOutputMsgPage(wpReady,
      'Information', 'Database Configuration with Browser',
      'The setup database configuration will be done using the browser.'  + #13#10 + #13#10
      'Start your browser and go to: https://localhost:8443/setup' + #13#10 + #13#10
      'At the end of the configuration, please click on the button "Close DoubleClue Setup" in order to continue with the installation.' + #13#10 + #13#10
      'Please note: ' + #13#10 + 
      'The setup uses a secured HTTPS connection with a "self-signed" certificate.' + #13#10 +
      'For this reason the browser will show a security alert.' + #13#10 +
      'Please confirm this alert message and proceed with the database configuration.');

  NotepadLocation := DataDirPage.Values[0];
  Log('The Path is: ' + NotepadLocation);
    { Set default values, using settings that were stored last time if possible }

    case GetPreviousData('UsageMode', '') of
      'installation': UsagePage.SelectedValueIndex := 0;
      'updateCluster': UsagePage.SelectedValueIndex := 1;
      'newCluster': UsagePage.SelectedValueIndex := 2;
    else
      UsagePage.SelectedValueIndex := 0;
    end;
end;

procedure RegisterPreviousData(PreviousDataKey: Integer);
var
  UsageMode: String;
begin
  { Store the settings so we can restore them next time }
  case UsagePage.SelectedValueIndex of
    0: UsageMode := 'newInstallation';
    1: UsageMode := 'updateCluster';
    2: UsageMode := 'newCluster';
  end;
  SetPreviousData(PreviousDataKey, 'UsageMode', UsageMode);
  SetPreviousData(PreviousDataKey, 'DataDir', DataDirPage.Values[0]);
end;

function ShouldSkipPage(PageID: Integer): Boolean;
begin
  { Skip pages that shouldn't be shown }
  if (PageID = DataDirPage.ID) and (UsagePage.SelectedValueIndex <> 2) then
    Result := True
  else
    Result := False;
end;

function NextButtonClick(CurPageID: Integer): Boolean;
begin
if (CurPageID = DataDirPage.ID) and (UsagePage.SelectedValueIndex = 2) then
    NotepadLocation := DataDirPage.Values[0];
    Log('Path: ' + NotepadLocation);
    Result := True;
  if (CurPageID = UsagePage.ID) then
    UsageSelection := UsagePage.SelectedValueIndex;
    Result := True;
end;

function GetDataDir(Param: String): String;
begin
  { Return the selected DataDir }
  Result := NotepadLocation;
end;

function IsDataDir: Boolean;
begin
  if (UsageSelection = 2) then
    begin
      Log('This is a Data dir: ' + NotepadLocation);
      Result := True;
    end
  else
  begin
    Log('This is no Data dir: ' + NotepadLocation);
    Result := False;
  end
end;

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

procedure CurPageChanged(CurPageID: Integer);
begin
  if CurPageID = LightMsgPage.ID then
    WizardForm.NextButton.Caption := SetupMessage(msgButtonInstall)
  else begin
    if (CurPageID = wpFinished) then
      WizardForm.NextButton.Caption := SetupMessage(msgButtonFinish)
    else
      WizardForm.NextButton.Caption := SetupMessage(msgButtonNext)
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
