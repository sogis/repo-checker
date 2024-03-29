# interlis-repo-checker

## Beschreibung


Validiert die INTERLIS-Modellablagen. Es werden die beiden Dateien _ilisite.xml_ und _ilimodels.xml_ geprüft und sämtliche Modelle im Repository. Geprüft wird mit _ilivalidator_ und mit der Methode der Klasse _CheckReposIlis_ des INTERLIS-Compilers _ili2c_.

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
| `REPOSITORIES` | INTERLIS-Modellablagen, die geprüft werden sollen (kommasepariert). | Sämtlich mir bekannten Repositories. |
| `WORK_DIRECTORY` | Root-Verzeichnis, in für jedes Repo und jeden Prüfdurchgang, temporäre Verzeichnisse erstellt werden. | `/tmp/` |
| `WORK_DIRECTORY_PREFIX` | Prefix für die temporären Verzeichnisse. | `repocheck_` |
| `CHECK_CRON_EXPRESSION` | Spring Boot Cron Expression für die Prüfung. | `0 0 */2 * * *` |
| `HTTP_PROXY_HOST` | Hostname eines http proxy. | `` |
| `HTTP_PROXY_PORT` | Port eines http proxy. | `` |
| `HTTP_PROXY_USER` | Username eines http proxy. | `` |
| `HTTP_PROXY_PASSWORD` | Passwort eines http proxy. | `` |

Http Proxy: Siehe auch https://docs.oracle.com/javase/8/docs/api/java/net/doc-files/net-properties.html

### Java

```
java -jar build/libs/interlis-repo-checker-0.1.XXXX-exec.jar
```

### Native Image

```
./build/native/nativeCompile/interlis-repo-checker
```

### Docker


```
docker run -e TZ=Europe/Zurich -p 8080:8080 sogis/interlis-repo-checker(-jvm)
```


## Externe Abhängigkeiten

Die zu prüfenden INTERLIS-Modellablagen. 


## Konfiguration und Betrieb in der GDI

@AndiS: todo

## Interne Struktur

@Stefan: todo

Stichworte:
- Spring Boot
- ilivalidator
- ili2c
- XML -> XSLT -> HTML
- Reverse Proxy wegen Url
- http proxy (nicht wirklich getestet)

## Entwicklung

### Build

#### JVM
```
./gradlew clean build
```

```
docker build -t sogis/interlis-repo-checker-jvm:latest -f Dockerfile.jvm .
```


#### Native

Wegen String-Funktion in SpEL und XSLT muss der Agent verwendet werden:

```
java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image -jar build/libs/interlis-repo-checker-0.1.LOCALBUILD-exec.jar
```

```
./gradlew clean aotTest nativeCompile -i
```

```
docker build -t sogis/interlis-repo-checker:latest -f Dockerfile.native .
```




