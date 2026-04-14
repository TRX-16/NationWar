# NationWar

설정:
- Paper: 1.21.10
- Java: 21
- 빌드: Gradle (Kotlin DSL)
- 저장: Gson(JSON)

빌드:
```bash
./gradlew build
# 또는 (fat JAR)
./gradlew shadowJar
```

생성된 JAR은 `build/libs/NationWar-1.0.0.jar` (혹은 shadowJar 사용 시 `NationWar-1.0.0.jar`) 을 서버의 `plugins/` 폴더에 넣어 사용하세요.
