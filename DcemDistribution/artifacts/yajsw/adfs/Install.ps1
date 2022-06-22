$appName = "DcAdfsMfaProvider"
$version = "1.0.0.0"
$pkToken = "426720f1d3ee9cf9"
$keyPath = ".\dc_adfs_mfa_key.snk"
$configPath = ".\dcAdfsMfaProvider_config.json"

# create resource files
$resDir = ".\resources\tmp"
New-Item $resDir -ItemType directory -Force
Get-ChildItem .\resources -Filter *.txt |
Foreach-Object {
    $culture = $_.BaseName
    $resPath = "$($resDir)\$($appName).Resources.$($culture).resources"
    $outDir = ".\libs\$($culture)"
    $outPath = "$($outDir)\$($appName).resources.dll"
    New-Item $outDir -ItemType directory -Force
    .\tools\ResGen.exe $_.FullName $resPath
    .\tools\al.exe /t:lib /embed:$resPath /culture:$culture /keyf:$keyPath /v:$version /out:$outPath
}

# register DLLs in GAC
Get-ChildItem .\libs -Recurse -Filter *.dll |
Foreach-Object {
    .\tools\gacutil.exe /if $_.FullName
}

# register DLL in registry for Event Log
$registryPath = "Registry::HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\services\eventlog\Application\$($appName)"
IF(!(Test-Path $registryPath)) {
    $propertyName = "EventMessageFile"
    $propertyValue = "%windir%\Microsoft.NET\assembly\GAC_MSIL\$($appName)\v4.0_$($version)__$($pkToken)\$($appName).dll"
    New-Item -Path $registryPath
    New-ItemProperty -Path $registryPath -Name $propertyName -Value $propertyValue -PropertyType STRING
}

# add registry key for configuring login attribute
$registryPath = "Registry::HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\services\$($appName)"
IF(!(Test-Path $registryPath)) {
    $propertyName = "LoginAttribute"
    $propertyValue = "http://schemas.microsoft.com/ws/2008/06/identity/claims/windowsaccountname"
    New-Item -Path $registryPath
    New-ItemProperty -Path $registryPath -Name $propertyName -Value $propertyValue -PropertyType STRING
}

# register the MFA provider with ADFS
$className = "DcAdapter"
$typeName = "$($appName).$($className), $($appName), Version=$($version), Culture=neutral, PublicKeyToken=$($pkToken), processorArchitecture=MSIL"
Set-AdfsGlobalAuthenticationPolicy -AdditionalAuthenticationProvider @()
Unregister-AdfsAuthenticationProvider –Name $appName -Confirm:$false
Register-AdfsAuthenticationProvider –TypeName $typeName –Name $appName -ConfigurationFilePath $configPath
Set-AdfsGlobalAuthenticationPolicy -AdditionalAuthenticationProvider $appName

# restart ADFS service
net stop adfssrv
net start adfssrv