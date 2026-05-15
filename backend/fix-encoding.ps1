# Fix UTF-8 BOM issues introduced by file operations
$basePath = "c:\Users\JAYLORD\OneDrive\Desktop\IT342-Bayonas-CitMedConnect\backend\src\main\java\edu\cit\bayonas\citmedconnect"

# Read each Java file and remove BOM if present
$javaFiles = Get-ChildItem -Path $basePath -Filter "*.java" -Recurse

foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Encoding UTF8
    
    # Remove BOM character if present  
    if ($content.StartsWith([char]0xFEFF)) {
        $content = $content.Substring(1)
    }
    
    # Write back with UTF8 (no BOM)
    [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.UTF8Encoding]$false)
}

Write-Host "✅ Fixed UTF-8 BOM issues in all Java files"
