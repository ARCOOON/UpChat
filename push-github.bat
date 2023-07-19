@echo off
git add .
set /p commit_message="Commit Message: "
git commit -m "%commit_message%"
git push -f origin main
@echo on
