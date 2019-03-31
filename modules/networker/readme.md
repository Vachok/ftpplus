# Commit **** NO TASK ****

## NetScanCtr
### timeCheck
>1. File & isSystemTimeBigger
### checkMapSizeAndDoAction
>1. else - timeCheck
### timeCheck
>1. Аттрибут модели "newpc" - lastScanLocalTime
## ADSrv
>1. Убран импорт ru.vachok.networker.net.NetScanCtr;
## AppComponents
>1. Убран lastNetScanMap
>2. Убран ConfigurableApplicationContext
>3. Убран lastNetScan
## IntoApplication
### main
>1. Убран AppComponents.setCtx(context);
## LastNetScan
### setTimeLastScan
>1. Актуальный MessageCons.
>2. Убран equals & hashCode.
>3. timeLastScan = new Date(). 
## NetScannerSvc
>1. LastNetScan.getLastNetScan
## SpeedChecker
### messageToUser
>1. Актуальный

