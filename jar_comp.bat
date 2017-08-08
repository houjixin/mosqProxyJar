REM Set jdk bin path
set PATH=%PATH%;C:\Program Files\Java\jdk1.7.0_75\bin
set curdir=%~dp0
cd /d %curdir%

cd bin
jar cf mosq-proxy-client-0.0.1.jar com 
move mosq-proxy-client-0.0.1.jar ../lib
cd ..
pause