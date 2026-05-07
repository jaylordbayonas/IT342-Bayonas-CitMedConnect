@echo off
REM Phase 4.1.2: Move AUTH Feature files

cd /d "c:\Users\JAYLORD\OneDrive\Desktop\IT342-Bayonas-CitMedConnect\backend\src\main\java\edu\cit\bayonas\citmedconnect"

REM Display progress
echo.
echo ====================================
echo STEP 4.1.2: Migrating AUTH Feature
echo ====================================
echo.

REM This will be handled by individual file copies with package updates
REM The script will use PowerShell for find-replace operations

powershell -Command "
    `$srcBase = 'c:\Users\JAYLORD\OneDrive\Desktop\IT342-Bayonas-CitMedConnect\backend\src\main\java\edu\cit\bayonas\citmedconnect'
    
    # Function to copy file and update package
    function Copy-WithPackageUpdate {
        param([string]`$src, [string]`$dest, [string]`$newPackage, [string]`$oldPackage)
        
        `$content = Get-Content `$src -Raw
        `$content = `$content -replace [regex]::Escape(\"package $oldPackage;\"), \"package $newPackage;\"
        
        # Update imports for auth files
        `$content = `$content -replace 'edu\.cit\.bayonas\.citmedconnect\.controller\.', 'edu.cit.bayonas.citmedconnect.features.auth.controller.'
        `$content = `$content -replace 'edu\.cit\.bayonas\.citmedconnect\.service\.', 'edu.cit.bayonas.citmedconnect.features.auth.service.'
        `$content = `$content -replace 'edu\.cit\.bayonas\.citmedconnect\.repository\.', 'edu.cit.bayonas.citmedconnect.features.auth.repository.'
        `$content = `$content -replace 'edu\.cit\.bayonas\.citmedconnect\.entity\.', 'edu.cit.bayonas.citmedconnect.features.auth.entity.'
        `$content = `$content -replace 'edu\.cit\.bayonas\.citmedconnect\.dto\.', 'edu.cit.bayonas.citmedconnect.features.auth.dto.'
        `$content = `$content -replace 'edu\.cit\.bayonas\.citmedconnect\.mapper\.', 'edu.cit.bayonas.citmedconnect.features.auth.mapper.'
        
        Set-Content `$dest `$content -Encoding UTF8
    }
    
    # Create directories if not exist
    @('features/auth/controller', 'features/auth/service', 'features/auth/repository', 'features/auth/entity', 'features/auth/dto', 'features/auth/mapper') | ForEach-Object {
        `$dir = Join-Path `$srcBase `$_
        if (!(Test-Path `$dir)) { New-Item -ItemType Directory -Path `$dir -Force | Out-Null }
    }
    
    # Move controller files
    Copy-WithPackageUpdate \
        (Join-Path `$srcBase 'controller/AuthController.java') \
        (Join-Path `$srcBase 'features/auth/controller/AuthController.java') \
        'edu.cit.bayonas.citmedconnect.features.auth.controller' \
        'edu.cit.bayonas.citmedconnect.controller'
    
    # Move service files
    Copy-WithPackageUpdate \
        (Join-Path `$srcBase 'service/UserService.java') \
        (Join-Path `$srcBase 'features/auth/service/UserService.java') \
        'edu.cit.bayonas.citmedconnect.features.auth.service' \
        'edu.cit.bayonas.citmedconnect.service'
    
    # Move repository files
    Copy-WithPackageUpdate \
        (Join-Path `$srcBase 'repository/UserRepository.java') \
        (Join-Path `$srcBase 'features/auth/repository/UserRepository.java') \
        'edu.cit.bayonas.citmedconnect.features.auth.repository' \
        'edu.cit.bayonas.citmedconnect.repository'
    
    # Move entity files
    Copy-WithPackageUpdate \
        (Join-Path `$srcBase 'entity/UserEntity.java') \
        (Join-Path `$srcBase 'features/auth/entity/UserEntity.java') \
        'edu.cit.bayonas.citmedconnect.features.auth.entity' \
        'edu.cit.bayonas.citmedconnect.entity'
    
    # Move DTO files
    Copy-WithPackageUpdate \
        (Join-Path `$srcBase 'dto/RegisterRequest.java') \
        (Join-Path `$srcBase 'features/auth/dto/RegisterRequest.java') \
        'edu.cit.bayonas.citmedconnect.features.auth.dto' \
        'edu.cit.bayonas.citmedconnect.dto'
    
    Copy-WithPackageUpdate \
        (Join-Path `$srcBase 'dto/LoginRequest.java') \
        (Join-Path `$srcBase 'features/auth/dto/LoginRequest.java') \
        'edu.cit.bayonas.citmedconnect.features.auth.dto' \
        'edu.cit.bayonas.citmedconnect.dto'
    
    Copy-WithPackageUpdate \
        (Join-Path `$srcBase 'dto/AuthResponse.java') \
        (Join-Path `$srcBase 'features/auth/dto/AuthResponse.java') \
        'edu.cit.bayonas.citmedconnect.features.auth.dto' \
        'edu.cit.bayonas.citmedconnect.dto'
    
    # Move mapper files
    Copy-WithPackageUpdate \
        (Join-Path `$srcBase 'mapper/UserMapper.java') \
        (Join-Path `$srcBase 'features/auth/mapper/UserMapper.java') \
        'edu.cit.bayonas.citmedconnect.features.auth.mapper' \
        'edu.cit.bayonas.citmedconnect.mapper'
    
    Write-Host '✅ Auth feature files migrated successfully'
"
