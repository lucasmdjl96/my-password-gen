#
# This file is part of MyPasswordGen.
#
# MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
#
# MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
#

#powershell
Set-Location build\processedResources\jvm\main
$content = (Get-Content -Raw `
html\index.html, `
static\css\style.css, `
static\js\my-password-gen.js) -join ""
$stream = [IO.MemoryStream]::new([byte[]][char[]]$content)
$hash = (Get-FileHash -Algorithm SHA256 -InputStream $stream).Hash.ToLower().substring(0, 16)
(Get-Content -Raw static\js\service-worker.js).replace('$VERSION', $hash) `
| Out-File -Encoding UTF8 static\js\service-worker.js -NoNewLine
Set-Location ..\..\..\..
