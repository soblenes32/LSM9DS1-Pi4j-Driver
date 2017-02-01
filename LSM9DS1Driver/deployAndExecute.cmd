REM Step #1 FTP Jars to raspi
REM Step #2 Putty to raspi and execute

REM RASPI #1 HOME configuration
REM E:\Dev\putty\psftp pi@192.168.0.23 -b deploy.txt -pw raspberry
REM E:\Dev\putty\plink.exe -ssh pi@192.168.0.23 -m execute.txt -pw raspberry

REM RASPI #1 15D configuration
REM C:\Dev\putty\psftp pi@192.168.1.121 -b deploy.txt -pw raspberry
REM C:\Dev\putty\plink.exe -ssh pi@192.168.1.121 -m execute.txt -pw raspberry

REM RASPI #2 HOME configuration
E:\Dev\putty\psftp pi@192.168.0.24 -b deploy.txt -pw raspberry
E:\Dev\putty\plink.exe -ssh pi@192.168.0.24 -m execute.txt -pw raspberry


