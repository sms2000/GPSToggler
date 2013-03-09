@echo off
@echo Native copier.
xcopy ..\..\Native\libs\*  ..\libs /S /I /R /Y /J
@echo Native finished.
