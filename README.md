# Chat Keyword Highlighter

Клиентский Fabric-мод для Minecraft `1.21.11`: добавляет меню, где можно вписывать ключевые слова, а мод будет подсвечивать их в чате.

## Что умеет

- Открытие меню по клавише `H`.
- Добавление и удаление ключевых слов прямо в игре.
- Подсветка слов в обычных сообщениях чата и системных сообщениях, которые попадают в ChatHud.
- Переключатели:
  - включить / выключить подсветку;
  - учитывать регистр;
  - подсвечивать только целые слова;
  - выбрать цвет подсветки.
- Настройки сохраняются в `config/chat_highlighter.json`.
- Готовый GitHub Actions workflow собирает `.jar` и кладёт его в artifacts.

## Как собрать через GitHub

1. Создай новый репозиторий на GitHub.
2. Загрузи все файлы из этого проекта в репозиторий.
3. Открой вкладку **Actions**.
4. Запусти workflow **Build Fabric mod** вручную или просто сделай push в `main` / `master`.
5. После завершения сборки скачай artifact `chat-keyword-highlighter-jars`.
6. Нужный `.jar` будет внутри архива artifact.

## Как собрать локально

Нужны JDK 21 и Gradle 9.2+.

```bash
gradle build
```

Готовый мод появится здесь:

```text
build/libs/chat-keyword-highlighter-1.0.0.jar
```

## Как установить

1. Установи Minecraft `1.21.11` с Fabric Loader.
2. Положи Fabric API для `1.21.11` в папку `mods`.
3. Положи собранный `chat-keyword-highlighter-1.0.0.jar` в папку `mods`.
4. Запусти игру и нажми `H`, чтобы открыть меню.

## Где менять версии

Все версии лежат в `gradle.properties`:

```properties
minecraft_version=1.21.11
yarn_mappings=1.21.11+build.4
loader_version=0.19.3
loom_version=1.14.10
fabric_api_version=0.141.4+1.21.11
```

## Структура

```text
.github/workflows/build.yml                         GitHub Actions сборка
src/client/java/ru/laje/chathighlighter             клиентский код мода
src/client/java/ru/laje/chathighlighter/mixin       миксин для подсветки ChatHud
src/main/resources/fabric.mod.json                  метаданные Fabric
src/main/resources/chat_highlighter.client.mixins.json
src/main/resources/assets/chat_highlighter/lang     переводы
```
