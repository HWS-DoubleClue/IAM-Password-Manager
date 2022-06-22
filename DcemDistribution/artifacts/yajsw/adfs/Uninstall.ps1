$appName = "DcAdfsMfaProvider"
$version = "1.0.0.0"
$pkToken = "426720f1d3ee9cf9"
$keyPath = ".\dc_adfs_mfa_key.snk"
$configPath = ".\dcAdfsMfaProvider_config.json"

# unregister DLLs from GAC
Get-ChildItem .\libs -Recurse -Filter *.dll |
Foreach-Object {
    .\tools\gacutil.exe /u $_.BaseName
}

# unregister key from Event Log registry
$registryPath = "Registry::HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\services\eventlog\Application\$($appName)"
Remove-Item -Path $registryPath

# unregister Registry Keys
$registryPath = "Registry::HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\services\$($appName)"
Remove-Item -Path $registryPath

# unregister the MFA provider from ADFS
$className = "DcAdapter"
$typeName = "$($appName).$($className), $($appName), Version=$($version), Culture=neutral, PublicKeyToken=$($pkToken), processorArchitecture=MSIL"
Set-AdfsGlobalAuthenticationPolicy -AdditionalAuthenticationProvider @()
Unregister-AdfsAuthenticationProvider –Name $appName -Confirm:$false

# restart ADFS service
net stop adfssrv
net start adfssrv