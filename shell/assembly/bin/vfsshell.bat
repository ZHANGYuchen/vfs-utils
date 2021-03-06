@echo off

REM Licensed to the Apache Software Foundation (ASF) under one
REM or more contributor license agreements.  See the NOTICE file
REM distributed with this work for additional information
REM regarding copyright ownership.  The ASF licenses this file
REM to you under the Apache License, Version 2.0 (the
REM "License"); you may not use this file except in compliance
REM with the License.  You may obtain a copy of the License at
REM
REM  http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing,
REM software distributed under the License is distributed on an
REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
REM KIND, either express or implied.  See the License for the
REM specific language governing permissions and limitations
REM under the License.


if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

REM ==== START VALIDATION ====
if not "%JAVA_HOME%" == "" goto OkJHome

echo.
echo ERROR: JAVA_HOME not found in your environment.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto error

:OkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto setVfshHome

echo.
echo ERROR: JAVA_HOME is set to an invalid directory.
echo JAVA_HOME = %JAVA_HOME%
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto error

:setVfshHome

rem ----- use the location of this script to infer $VFSH_HOME -------
if NOT "%OS%"=="Windows_NT" set DEFAULT_VFSH_HOME=..
if "%OS%"=="Windows_NT" set DEFAULT_VFSH_HOME=%~dp0\..
if "%OS%"=="WINNT" set DEFAULT_VFSH_HOME=%~dp0\..
if "%VFSH_HOME%"=="" set VFSH_HOME=%DEFAULT_VFSH_HOME%

rem ----- Create CLASSPATH --------------------------------------------
set VFSH_CLASSPATH=%CLASSPATH%;%VFSH_HOME%\local\classes;%VFSH_HOME%\common\classes

cd /d "%VFSH_HOME%\local\lib"
for %%i in ("*.jar") do call "%VFSH_HOME%\bin\appendcp.bat" "%VFSH_HOME%\local\lib\%%i"
cd /d %VFSH_HOME%

cd /d "%VFSH_HOME%\common\lib"
for %%i in ("*.jar") do call "%VFSH_HOME%\bin\appendcp.bat" "%VFSH_HOME%\common\lib\%%i"
cd /d %VFSH_HOME%

rem ----- call java.. ---------------------------------------------------
set MAIN_CLASS=org.vfsutils.shell.jline.Shell
set JAVA_CMD=%JAVA_HOME%\bin\java

"%JAVA_CMD%" -classpath "%VFSH_CLASSPATH%;%VFSH_EXT_CLASSPATH%" %VFSH_EXT_OPTS% %MAIN_CLASS% %*

