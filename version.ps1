#powershell
cd build\processedResources\jvm\main
$content = (Get-Content -Raw `
html\index.html, `
static\css\style.css, `
static\js\my-password-gen.js) -join ""
$stream = [IO.MemoryStream]::new([byte[]][char[]]$content)
$hash = (Get-FileHash -Algorithm SHA256 -InputStream $stream).Hash.ToLower().substring(0, 16)
(Get-Content -Raw static\js\service-worker.js).replace('$VERSION', $hash) `
| Out-File -Encoding UTF8 static\js\service-worker.js -NoNewLine
cd ..\..\..\..
