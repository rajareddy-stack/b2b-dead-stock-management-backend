# Backend

Spring Boot application containing the web and REST controllers, business services, persistence models, security configuration, and tests.

Run from the repository root:

```powershell
mvn -f backend/pom.xml spring-boot:run
mvn -f backend/pom.xml test
```

The build also packages Thymeleaf templates and static assets from `../frontend/src/main/resources`.
