@echo off
rem Tao lai database LOCAL tu du lieu demo trong db/init (Windows).
rem Dung khi khong dang nhap duoc bang tai khoan demo (README muc 5): thuong do volume
rem mysql-db duoc tao tu truoc khi co du lieu demo, nen tai khoan admin trong do mang mat khau cu.
rem CANH BAO: xoa toan bo du lieu local cua MySQL va MinIO.
setlocal
cd /d "%~dp0.."

if not exist .env goto noenv

echo Thao tac nay XOA toan bo du lieu local (MySQL + MinIO) roi nap lai du lieu demo.
set "ANSWER="
set /p "ANSWER=Go YES de tiep tuc: "
if not "%ANSWER%"=="YES" goto cancelled

docker compose down -v
if errorlevel 1 goto dockerfail
docker compose up -d --build
if errorlevel 1 goto dockerfail

set "PORT=8082"
for /f "tokens=1,* delims==" %%a in ('findstr /b "PUBLIC_PORT=" .env') do set "PORT=%%b"

echo Doi app san sang tai http://localhost:%PORT%/health (toi da 3 phut)
set /a TRIES=0
:wait
curl -fsS "http://localhost:%PORT%/health" >nul 2>&1
if not errorlevel 1 goto ready
set /a TRIES+=1
if %TRIES% geq 36 goto timeout
timeout /t 5 /nobreak >nul
goto wait

:ready
echo App da san sang: http://localhost:%PORT%/login.html
echo Dang nhap bang tai khoan demo trong README muc 5: admin, lecturer, student01.
exit /b 0

:timeout
echo App chua san sang sau 3 phut. Xem log: docker compose logs --tail 50 app
exit /b 1

:noenv
echo Chua co file .env. Tao theo README muc 4b: copy .env.example .env roi dien gia tri.
exit /b 1

:cancelled
echo Da huy.
exit /b 1

:dockerfail
echo Lenh docker bi loi. Kiem tra Docker Desktop da mo chua.
exit /b 1
