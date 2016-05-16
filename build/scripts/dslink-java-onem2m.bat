@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  dslink-java-onem2m startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

@rem Add default JVM options here. You can also use JAVA_OPTS and DSLINK_JAVA_ONEM_M_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windowz variants

if not "%OS%" == "Windows_NT" goto win9xME_args
if "%@eval[2+2]" == "4" goto 4NT_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*
goto execute

:4NT_args
@rem Get arguments from the 4NT Shell from JP Software
set CMD_LINE_ARGS=%$

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\dslink-java-onem2m-0.0.1.jar;%APP_HOME%\lib\onem2m-client-0.0.1-SNAPSHOT.jar;%APP_HOME%\lib\commons-0.16.0-SNAPSHOT.jar;%APP_HOME%\lib\dslink-0.16.0-SNAPSHOT.jar;%APP_HOME%\lib\gson-2.2.4.jar;%APP_HOME%\lib\jetty-all-server-8.1.17.v20150415.jar;%APP_HOME%\lib\commons-io-1.3.2.jar;%APP_HOME%\lib\californium-core-1.0.0-M3.jar;%APP_HOME%\lib\joda-time-2.7.jar;%APP_HOME%\lib\jersey-json-1.17.jar;%APP_HOME%\lib\commons-codec-1.9.jar;%APP_HOME%\lib\httpclient-4.3.5.jar;%APP_HOME%\lib\json-20151123.jar;%APP_HOME%\lib\runtime_shared-0.16.0-SNAPSHOT.jar;%APP_HOME%\lib\logging-0.16.0-SNAPSHOT.jar;%APP_HOME%\lib\javax.servlet-3.0.0.v201112011016.jar;%APP_HOME%\lib\javax.security.auth.message-1.0.0.v201108011116.jar;%APP_HOME%\lib\javax.mail.glassfish-1.4.1.v201005082020.jar;%APP_HOME%\lib\javax.activation-1.1.0.v201105071233.jar;%APP_HOME%\lib\javax.annotation-1.1.0.v201108011116.jar;%APP_HOME%\lib\commons-io-1.3.2.jar;%APP_HOME%\lib\element-connector-1.0.0-M3.jar;%APP_HOME%\lib\jettison-1.1.jar;%APP_HOME%\lib\jaxb-impl-2.2.3-1.jar;%APP_HOME%\lib\jackson-core-asl-1.9.2.jar;%APP_HOME%\lib\jackson-mapper-asl-1.9.2.jar;%APP_HOME%\lib\jackson-jaxrs-1.9.2.jar;%APP_HOME%\lib\jackson-xc-1.9.2.jar;%APP_HOME%\lib\jersey-core-1.17.jar;%APP_HOME%\lib\httpcore-4.3.2.jar;%APP_HOME%\lib\commons-logging-1.1.3.jar;%APP_HOME%\lib\jzlib-1.1.3.jar;%APP_HOME%\lib\jcommander-1.48.jar;%APP_HOME%\lib\bcprov-jdk15on-1.51.jar;%APP_HOME%\lib\netty-all-4.1.0.CR2.jar;%APP_HOME%\lib\jackson-dataformat-msgpack-0.7.1.jar;%APP_HOME%\lib\slf4j-api-1.7.12.jar;%APP_HOME%\lib\jaxb-api-2.2.2.jar;%APP_HOME%\lib\msgpack-core-0.7.1.jar;%APP_HOME%\lib\jackson-databind-2.6.3.jar;%APP_HOME%\lib\stax-api-1.0-2.jar;%APP_HOME%\lib\activation-1.1.jar;%APP_HOME%\lib\jackson-annotations-2.6.0.jar;%APP_HOME%\lib\jackson-core-2.6.3.jar

@rem Execute dslink-java-onem2m
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %DSLINK_JAVA_ONEM_M_OPTS%  -classpath "%CLASSPATH%" org.dsa.iot.onem2m.Main %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable DSLINK_JAVA_ONEM_M_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%DSLINK_JAVA_ONEM_M_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
