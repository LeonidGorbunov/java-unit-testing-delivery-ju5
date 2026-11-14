[![Unit Tests & JaCoCo Coverage](https://github.com)](https://github.com)
[![JaCoCo Coverage](https://img.shields.io/badge/JaCoCo-100%25-brightgreen)](https://leonidgorbunov.github.io/java-unit-testing-delivery-ju5/)
# Java Unit Testing Delivery Service Project

**Контекст и цели проекта**: разработка изолированных модульных тестов для верификации расчёта стоимости доставки, подбора и назначения курьеров, а также формирования SMS-уведомлений в зависимости от типа транспорта.

**ТЗ (вид работ)**: модульное тестирование (Unit Testing), параметризация данных (JUnit 5), Mock- и Spy-изоляция (Mockito), верификация побочных эффектов (`verify`), негативное тестирование и анализ граничных значений (BVA).

**Продукт**: Java-модуль сервиса управления доставкой `DeliveryService` (расчёт тарифов с учётом политики скидок, распределение заказов между курьерами и уведомление клиентов).

**Выполненные задачи**:
1. Моделирование и изоляция (`@Mock` и `@Spy`): обеспечена 100% изоляция класса `DeliveryService` от внешних зависимостей (`DistanceCalculator`, `CourierRepository`, `DiscountPolicy`) через генерацию Mock-объектов, а также обёртывание `NotificationService` в Spy-объект для контроля отправки SMS.
2. Комплексная параметризация (JUnit 5): спроектированы параметризованные наборы тестов через `@CsvSource` и `@MethodSource` для валидации расчёта стоимости доставки, включая проверку граничного нулевого расстояния (`0.0` км), и ветвления логики уведомлений.
3. Верификация полиморфизма и строковых шаблонов: протестировано поведение метода `processDelivery` для разных типов курьеров (`Courier` и наследник `CarCourier`) с проверкой точного текста SMS-уведомлений.
4. Контроль побочных эффектов: с помощью `verify()` подтверждена корректность вызова методов назначения заказа курьеру (`assignOrderToCourier`) и отправки сообщения клиенту (`sendSms`).
5. Негативное тестирование: проверена генерация исключений `IllegalArgumentException` (при отрицательном расстоянии) и `IllegalStateException` (при отсутствии свободных курьеров).
6. Тестовое покрытие (Code Coverage): достигнуто 100% покрытие целевой бизнес-логики класса `DeliveryService` с помощью плагина JaCoCo.

**Область тестирования (Scope)**: корректность расчёта стоимости с учётом скидок, обработка исключительных состояний, полиморфная логика выбора шаблона уведомления, верификация вызовов внешних сервисов.

**Окружение**: Java (JDK 11), JUnit 5 (Jupiter 5.11.0), Mockito (5.11.0), Maven, JaCoCo Plugin (0.8.12).

---

### Архитектура проекта и подход к изоляции

Класс `DeliveryService` получает все зависимости через **Constructor Injection**, что позволяет полностью изолировать его бизнес-логику от смежных модулей в рамках модульного тестирования:

```text
src/
├── main/java/com/springfield/delivery/
│   ├── DeliveryService.java          # Целевой сервис (расчёт стоимости и оформление доставки)
│   ├── DistanceCalculator.java       # Калькулятор расстояний между пунктами (изолирован @Mock)
│   ├── CourierRepository.java        # Репозиторий поиска и назначения курьеров (изолирован @Mock)
│   ├── DiscountPolicy.java           # Интерфейс расчёта скидок (изолирован @Mock)
│   ├── RegularDiscountPolicy.java    # Базовая политика скидок (5%)
│   ├── VipDiscountPolicy.java        # VIP-политика скидок (15%)
│   ├── NotificationService.java      # Сервис отправки SMS-уведомлений (обёрнут в @Spy)
│   ├── Courier.java                  # Базовая модель пешего курьера
│   └── CarCourier.java               # Наследник для автокурьера (полиморфизм транспорта)
└── test/java/com/springfield/delivery/
    └── DeliveryServiceTest.java      # Набор изолированных модульных тестов (12 запусков)
```

### Ключевые инженерные решения в тестах

* **Комбинирование `@Mock` и `@Spy`:** внешние источники данных и правил (`DistanceCalculator`, `CourierRepository`, `DiscountPolicy`) заменены на стабы/моки (`@Mock`) с предсказуемыми ответами, тогда как `NotificationService` обёрнут в шпиона (`@Spy`) для проверки реального вызова и точного текста отправляемого SMS.
* **Проверка побочных эффектов (`verify`):** помимо валидации возвращаемых значений (`assertEquals`) и ожидаемых исключений (`assertThrows`), в отдельных тестах контролируется вызов `courierRepository.assignOrderToCourier(courier, orderId)`.
* **Параметризация и полиморфизм:**
    * `@CsvSource` проверяет расчёт итоговой стоимости доставки для различных маршрутов, включая граничное нулевое расстояние (`0.0` км).
    * `@MethodSource` обеспечивает сквозной прогон сценариев оформления доставки для базового класса `Courier` и подкласса `CarCourier`.
* **Точечный отчет `JaCoCo`:** в `pom.xml` настроен фильтр `<include>com/springfield/delivery/DeliveryService.class</include>` на этапе генерации отчёта, чтобы исключить вспомогательные классы-заглушки и отразить фактическое покрытие целевого сервиса (**100%**).

---

### Покрытые тестовые сценарии (`DeliveryServiceTest`)

| Метод теста | Аннотация | Проверяемая логика и граничные условия |
| :--- | :--- | :--- |
| `shouldCalculatePriceThrowExceptionWhenCalculateDistanceInKmReturnNegative` | `@Test` | Выброс `IllegalArgumentException` при получении отрицательного расстояния (`-1.0` км) |
| `shouldCalculatePriceReturnPrice` | `@ParameterizedTest` (`@CsvSource`) | Корректный расчёт итоговой стоимости доставки с учётом тарифа и скидки (4 набора данных, включая границу `0.0` км) |
| `shouldProcessDeliveryThrowExceptionWhenAvailableCouriersIsEmpty` | `@Test` | Выброс `IllegalStateException` при пустом списке доступных курьеров (`List.of()`) |
| `shouldProcessDeliverySendNotificationServiceSms` | `@ParameterizedTest` (`@MethodSource`) | Проверка отправки SMS через `@Spy` с разным текстом для пешего курьера (`Courier`) и автокурьера (`CarCourier`) |
| `shouldProcessDeliveryReturnCourierName` | `@ParameterizedTest` (`@MethodSource`) | Возврат имени первого доступного курьера при успешном оформлении доставки |
| `shouldProcessDeliveryAssignOrderToCourier` | `@ParameterizedTest` (`@MethodSource`) | Верификация вызова `courierRepository.assignOrderToCourier(courier, orderId)` через `verify()` |

---

### Метрика тестового покрытия JaCoCo (100% Coverage Verification)
![JaCoCo Code Coverage Report](jacoco-report.png)

### 📊 Интерактивный отчёт о покрытии (JaCoCo) и CI/CD

* **Отчёт JaCoCo (100% покрытие класса `DeliveryService`):** [Открыть JaCoCo Report на GitHub Pages](https://leonidgorbunov.github.io/java-unit-testing-delivery-ju5/)
* **Автоматический прогон в облаке:** в репозиторий интегрирован пайплайн [GitHub Actions](.github/workflows/ci.yml), выполняющий `mvn clean test` на JDK 11 с публикацией HTML-отчёта в ветку `gh-pages`.
