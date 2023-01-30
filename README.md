# interlis-repo-checker

## Beschreibung


Validiert die INTERLIS-Modellablagen. Es werden die beiden Dateien _ilisite.xml_ und _ilimodels.xml_ geprüft und sämtliche Modelle im Repository. Geprüft wird mit _ilivalidator_ und mit der Methode der Klasse _CheckReposIlis_.

Achtung:
- https://github.com/claeis/ilivalidator/issues/351

## Komponenten

Der INTERLIS repository checker besteht aus einer (dieser) Komponente.

## Konfigurieren und Starten

Die Anwendung kann am einfachsten mittels Env-Variablen gesteuert werden. Es stehen aber auch die normalen Spring Boot Konfigurationsmöglichkeiten zur Verfügung (siehe "Externalized Configuration").

| Name | Beschreibung | Standard |
|-----|-----|-----|
| `LOG_LEVEL_ROOT` | Root Loglevel | `INFO` |
| `LOG_LEVEL_SPRING` | Loglevel für Spring spezifische Klassen. | `INFO` |
| `LOG_LEVEL_APP` | Loglevel für eigene Businesslogik. | `INFO` |
| `CONNECT_TIMEOUT` | Anzahl Millisekunden bis zum Connect Timeout. | `5000` |
| `READ_TIMEOUT` | Anzahl Millisekunden bis zum Read Timeout. | `5000` |
| `REPOSITORIES` | Anzahl Millisekunden bis zum Read Timeout. | `5000` |



| `CONFIG_FILE` | Vollständiger, absoluter Pfad der Themebereitstellungs-Konfigurations-XML-Datei. | `/config/datasearch.xml` |
| `ITEMS_GEOJSON_DIR` | Verzeichnis, in das die GeoJSON-Dateien der Regionen gespeichert werden. Sämtliche JSON-Dateien in diesem Verzeichnis werden öffentlich exponiert. | `#{systemProperties['java.io.tmpdir']}` (= Temp-Verzeichnis des OS) |
| `FILES_SERVER_URL` | Url des Servers, auf dem die Geodaten gespeichert sind. | `https://files.geo.so.ch` |




```
docker run -e TZ=Europe/Zurich -p 8080:8080 sogis/interlis-repo-checker(-jvm)
```



java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image -jar build/libs/interlis-repo-checker-0.1.LOCALBUILD-exec.jar
