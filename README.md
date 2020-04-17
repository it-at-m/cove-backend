# COVe Backend

## Inhaltsverzeichnis

* [Über das Projekt](#über-das-projekt)
  * [Architektur](#architektur)
  * [Verwendete Technologien](#verwendete-technologien)
* [Erste Schritte](#erste-schritte)
  * [Installation](#installation)
  * [Konfiguration](#konfiguration)
    * [Datenbank](#datenbank)
    * [Single-Sign-On](#single-sign-on)
* [Verwendung](#verwendung)
* [Lizenz](#lizenz)


## Über das Projekt

In Zeiten von COVID-19 müssen Gesundheitsämter die gemeldeten COVID-19-Verdachtsfälle und deren 
Kontaktpersonen erfassen, Laborergebnisse und Quarantäne-Zeiträume dokumentieren und 
zeitgleich viele Telefonanrufe mit den Betroffenen führen. 

COVe (COVID-19-Verdachtsfall-Verwaltung) vereint dies innerhalb einer modernen Web-App. Durch sie lassen sich Verdachtsfälle einfach erfassen, die Anrufe leichter organisieren und die Ergebnisse schneller dokumentieren.
Durch den innovativen Ansatz der Telefonlisten haben alle Mitarbeiterinnen und Mitarbeiter eines Telefonservices gleichzeitig Zugriff auf aktuelle Daten.
Das spart erheblich Zeit in der Krisensituation.


![Funktionsweise][functionality-screenshot]

### Architektur

![Architektur][architecture-screenshot]

COVe-Backend: https://github.com/it-at-m/cove-backend

COVe-Frontend: https://github.com/it-at-m/cove-frontend


### Verwendete Technologien

* [Java](https://www.java.com/de/)
* [Maven](https://maven.apache.org/)
* [Spring Boot](https://spring.io/projects/spring-boot)
* [Flyway](https://flywaydb.org/)


## Erste Schritte

Für das erfolgreiche Bauen und Ausführen der Anwendung sollte **Java** und **Maven** bereits installiert und eingerichtet sein.
Desweiteren wird für den Security Modus eine **Single-Sign-On Umgebung** benötigt. In unserem Fall wurde Keycloak verwendet. 
Es kann aber auch jeder andere OAuth2-Provider (OpenID-Connect) wie zum Beispiel AWS Cognito genutzt werden.


### Installation

1. Das Repository clonen
```shell script
git clone https://github.com/it-at-m/cove-backend.git
``` 

2. SSO Umgebung für Security Modus einrichten

   Die benötigten Konfigurationsdateien befinden sich im Ordner *sso-config* und können zur Importierung in KeyCloak verwendet werden.

3. Flyway konfigurieren

   Mithilfe der Datein im Ordner *flywayconf* kann Flyway konfiguriert werden.

### Konfiguration

Vor der Verwendung der Anwendung müssen noch einige Konfigurationen vorgenommen werden.

#### Datenbank

Als Datenbank kommt für die locale Entwicklung eine H2 und für den produktiven Betrieb eine SQL Datenbank z.B. Oracle zum Einsatz. Wobei Oracle auch durch jede andere SQL Datenbank wie beispielsweise Postgres oder MariaDB ersetzt werden kann.

Für die initiale Erstellung der Tabellen wird Flyway verwendet. Die Schemas sind unter `src/main/resources/db.migration` zu finden. 
Für die locale Entwicklung wird Flyway außerdem noch zur initalen Befüllung der Tabellen mit Testdaten eingesetzt.

Für die Datenbank muss in der jeweiligen application.yml folgende Parameter wie database, datasource.url, datasource.username, datasource.password und flyway.enabled definiert werden.
```yaml
 # Spring JPA
  h2.console.enabled: true
  jpa:
    database: 
    hibernate:
      # always drop and create the db should be the best
      # configuration for local (development) mode. this
      # is also the default, that spring offers by convention.
      # but here explicite:
      ddl-auto: none
      naming.physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
    # Logging for database operation
    show-sql: true
    properties:
      hibernate:
        format_sql: false
  datasource:
    url: jdbc:database:thin:@//<host>:<port>/<service>
    username: user
    password: pw
  flyway:
    enabled: false
```


#### Single-Sign-On

In der application.yml der jeweiligen Umgebung muss der Realm, die Key-set-uri, die User-info-uri und die 
Client-id noch gesetzt werden.
```yaml
realm:

security:
  logging.requests: all
  oauth2:
    resource:
      jwk:
        key-set-uri: <host>/auth/realms/${realm}/protocol/openid-connect/certs
      user-info-uri: <host>/auth/realms/${realm}/protocol/openid-connect/userinfo
      prefer-token-info: false
    client:
      client-id: client_name
      scope:
```


## Verwendung

Die Anwendung besitzt folgende Spring Profiles:

- security (defaultmäßig aktiviert)
- no-security

Verwendung von Flyway und einer H2 Datenbank
- local
- dev
- test

Verwendung von einer SQL Datenbank
- kon
- prod

Um die Anwendung local zu starten, können folgende zwei Skripte ausgeführt werden:
```shell script
# Mit Security
./runLocal.sh

# Ohne Security
./runLocalNosecurity.sh
```

Eine weitere Möglichkeit ist es, dass Maven Plugin zu verwenden:
```shell script
# Ausführbare Jar Datei erzeugen
mvn clean install

# Anwendung mit jeweiligen Profil starten (Bsp.: local,no-security)
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local,no-security"
```

### Howto Flyway

```shell script
# via Maven Plugin
mvn -Dflyway.configFiles=flywayconf/flyway.conf.k -Dflyway.password=<PW> flyway:clean flyway:migrate

# via Flyway CLI
flyway clean -configFiles=./flywayconf/flyway.conf.k
```


## Lizenz

COVe ist lizenziert unter der European Union Public Licence (EUPL). Für mehr Informationen siehe `LICENSE`.




[functionality-screenshot]: img/COVe_Grafik.jpg
[architecture-screenshot]: img/COVe_Bausteinsicht.png
