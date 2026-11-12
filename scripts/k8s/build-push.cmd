@echo off
rem Build image frontend va backend (tag v1, v2) roi day len Docker Hub. Dung cho Windows.
rem Mo Docker Desktop va dang nhap Docker Hub truoc khi chay.
setlocal
cd /d "%~dp0..\.."
set "HUB_USER=%~1"
if "%HUB_USER%"=="" set /p "HUB_USER=Ten dang nhap Docker Hub: "
if "%HUB_USER%"=="" (echo Thieu ten dang nhap. & pause & exit /b 1)

for %%V in (v1 v2) do (
  echo == Build %%V
  docker build --build-arg APP_VERSION=%%V -t %HUB_USER%/qlsv-backend:%%V . || goto :fail
  docker build --build-arg APP_VERSION=%%V -f k8s/frontend/Dockerfile -t %HUB_USER%/qlsv-frontend:%%V . || goto :fail
)
for %%V in (v1 v2) do (
  echo == Push %%V
  docker push %HUB_USER%/qlsv-backend:%%V || goto :fail
  docker push %HUB_USER%/qlsv-frontend:%%V || goto :fail
)
echo.
echo XONG. Da day: %HUB_USER%/qlsv-backend va %HUB_USER%/qlsv-frontend, tag v1 va v2.
pause
exit /b 0

:fail
echo.
echo LOI. Kiem tra: Docker Desktop dang chay chua, da dang nhap Docker Hub chua (docker login).
pause
exit /b 1
