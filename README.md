# goProject
Super świetny projekt gry GO w java


## Kompilacja ze źródła

Klient

```bash
mvn compile exec:java -Dexec.mainClass="project.go.App" -Dexec.args="client"
```

Serwer

```bash
mvn compile exec:java -Dexec.mainClass="project.go.App" -Dexec.args="server"
```