pushd %~dp0
call setenv.bat
%wrapper_bat% -c conf/DcemSetup.conf
popd

