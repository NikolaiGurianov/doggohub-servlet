# DoggoHub
#### Приложение о хранении данных владельца и медицинской информации о собаках

## Dog Servlet
Этот сервлет предоставляет конечные точки RESTful API для управления данными о собаках.
## Конечные точки

### GET /dog
Получение информации о собаке.
#### Параметры
- `id`: Идентификатор собаки для получения информации.
- `user_id`: Идентификатор владельца собаки для получения списка всех собак.
#### Ответ
Если предоставлен параметр `id`, возвращает информацию о собаке в формате JSON.
Если предоставлен параметр `user_id`, возвращает список собак, принадлежащих указанному пользователю, в формате JSON.

### POST /dog
Создание новой записи о собаке.
#### Тело запроса
JSON-объект, представляющий информацию о новой собаке.
#### Ответ
Возвращает созданную информацию о собаке в формате JSON.

### PATCH /dog
Частичное бновление существующей записи о собаке. Обновить можно кличку собаки или новый вес питомца.
#### Параметры
- `id`: Идентификатор собаки для обновления.
#### Тело запроса
JSON-объект, содержащий обновленную информацию о собаке.
#### Ответ
Возвращает обновленную информацию о собаке в формате JSON.

### DELETE /dog
Удаление существующей записи о собаке.
#### Параметры
- `id`: Идентификатор собаки для удаления.
#### Ответ
Возвращает сообщение об успешном удалении, если удаление выполнено успешно.
## Обработка Ошибок
В случае недопустимого запроса или ошибки возвращается соответствующий HTTP-статус и сообщение об ошибке.

## User Servlet
Этот сервлет предоставляет конечные точки RESTful API для управления данными о пользователях.
## Конечные точки
### GET /user
Получение информации о пользователях.
#### Параметры
- `id`: Идентификатор пользователя для получения информации.
#### Ответ
Если предоставлен параметр `id`, возвращает информацию о пользователе в формате JSON.
Если параметр `id` не предоставлен, возвращает список всех пользователей в формате JSON.

### POST /user
Создание новой записи о пользователе.
#### Тело запроса
JSON-объект, представляющий информацию о новом пользователе.
#### Ответ
Возвращает созданную информацию о пользователе в формате JSON.

### PATCH /user
Частичное бновление существующей записи о пользователе. Обновить можно имя пользователя и адрес электронной почты.
#### Параметры
- `id`: Идентификатор пользователя для обновления.
#### Тело запроса
JSON-объект, содержащий обновленную информацию о пользователе.
#### Ответ
Возвращает обновленную информацию о пользователе в формате JSON.

### DELETE /user
Удаление существующей записи о пользователе.
#### Параметры
- `id`: Идентификатор пользователя для удаления.
#### Ответ
Возвращает сообщение об успешном удалении, если удаление выполнено успешно.
## Обработка Ошибок
В случае недопустимого запроса или ошибки возвращается соответствующий HTTP-статус и сообщение об ошибке.

# Health History Servlet

Этот сервлет предоставляет конечные точки RESTful API для управления историями здоровья питомцев.

## Конечные точки

### GET /health

Получение информации об истории здоровья питомцев.

#### Параметры

- `id`: Идентификатор истории здоровья для получения информации.
- `dog_id`: Идентификатор питомца для получения списка всех историй здоровья.

#### Ответ

Если предоставлен параметр `id`, возвращает информацию об истории здоровья в формате JSON.
Если предоставлен параметр `dog_id`, возвращает список историй здоровья указанного питомца в формате JSON.

### POST /health

Создание новой записи об истории здоровья.

#### Тело запроса

JSON-объект, представляющий информацию о новой истории здоровья.

#### Ответ

Возвращает созданную информацию об истории здоровья в формате JSON.

### DELETE /health

Удаление существующей записи об истории здоровья.

#### Параметры

- `id`: Идентификатор истории здоровья для удаления.

#### Ответ

Возвращает сообщение об успешном удалении, если удаление выполнено успешно.

## Примечания

- Эти сервлеты использует кодировку UTF-8 для тел запросов и ответов.
- Поддерживаемые HTTP-методы: GET, POST, PATCH, DELETE.

## Автор

Проект создан Гурьяновым Николаем.