::@echo off
for /f "delims=" %%a in (Channelname.txt) do (
ant -Dapk-market=%%a
)