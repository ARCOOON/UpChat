@echo off

git add .
set /p commit_message="Commit Message (default: Synced with local source): "

if "%commit_message%"=="" (
    set commit_message=Synced with local source
)

git commit -m "%commit_message%"
git push -f origin master 2> github-push-error.log

if %errorlevel% equ 0 (
    echo Push successful. Closing in 5 seconds...
    timeout /t 5 /nobreak > nul
) else (
    echo An error occurred during the push. See error.log for details.
    pause
)

@echo on
