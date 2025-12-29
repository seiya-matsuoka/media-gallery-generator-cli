@echo off
setlocal

@REM スクリプトが置かれているプロジェクト直下へ移動して実行する
pushd "%~dp0" >nul

@REM Picocli の引数をそのまま exec-maven-plugin に渡す
mvn -q exec:java -Dexec.args="%*"
set EXITCODE=%ERRORLEVEL%

popd >nul
exit /b %EXITCODE%


