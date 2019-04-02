# Inet stats

## ActDirectoryCTRL
> Удалён Comment Out от 3 марта 2019
### messageToUser
### queryStringExists
>1. new MessageFile заменен на messageToUser.
### adFoto
>1. new MessageCons заменен на messageToUser.
## ADSrv
> Удалён Comment Out от 27 февраля 2019.
### getDetails
>1. В вывод добавлена инет-статистика. Выравнивание по-центру.
## AppComponents
### saveLogsToDB
## AppInfoOnLoad
### schedStarter
>1. saveLogsToDB scheduled с первоначальной задержкой 4 мин и общей ConstantsFor.DELAY
## InetIPUser
## InetUserPCName
## InternetUse
## IntoApplication
### main
>1. Запуск saveLogsToDB при старте.
## NetScanCtr
### pcNameForInfo
>1. Trim аттрибут "ok"
### getUserFromDB
>1. userInputRaw - Trim.
>2. collectedNames - новый лист строк. distinct имён компьютера пользователя.
>3. freqName. Map: Integer, String - частота повторения , имя компьютера.
>4. Определение самого частого имени, и получение инет-статистики.
## NetScannerSvc
> Косметика
## SquidLogsDBSaver
## stats-8.0.1914.jar библиотека
## TForms
### fromArray для ResultSetMetaData