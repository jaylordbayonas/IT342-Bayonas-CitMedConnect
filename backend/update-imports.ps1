# Update all UserEntity imports across codebase
$basePath = "c:\Users\JAYLORD\OneDrive\Desktop\IT342-Bayonas-CitMedConnect\backend\src\main\java\edu\cit\bayonas\citmedconnect"

# Get all Java files except those in features/auth
$javaFiles = Get-ChildItem -Path $basePath -Filter "*.java" -Recurse | 
    Where-Object { $_.FullName -notlike "*features\auth*" }

$count = 0
foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw
    $original = $content
    
    # Replace UserEntity import
    $content = $content -replace 'import edu\.cit\.bayonas\.citmedconnect\.entity\.UserEntity;', 'import edu.cit.bayonas.citmedconnect.features.auth.entity.UserEntity;'
    
    # Replace UserService import (not in auth service itself)
    if ($file.FullName -notlike "*service\UserService*") {
        $content = $content -replace 'import edu\.cit\.bayonas\.citmedconnect\.service\.UserService;', 'import edu.cit.bayonas.citmedconnect.features.auth.service.UserService;'
    }
    
    # Write back if changed
    if ($content -ne $original) {
        Set-Content $file.FullName $content -Encoding UTF8
        $count++
    }
}

Write-Host "✅ Updated imports in $count files"
