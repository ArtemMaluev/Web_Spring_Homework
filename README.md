﻿# Домашнее задание к занятию «1.1. HTTP и современный Web»

В качестве результата пришлите ссылки на ваши GitHub-проект в личном кабинете студента на сайте [netology.ru](https://netology.ru).

**Важно**: ознакомьтесь со ссылками, представленными на главной странице [репозитория с домашними заданиями](../README.md).

**Важно**: если у вас что-то не получилось, то оформляйте Issue [по установленным правилам](../report-requirements.md).

**Важно**: все задачи нужно делать в **одном** репозитории.

## Как сдавать задачи

1. Создайте на вашем компьютере Maven-проект
1. Инициализируйте в нём пустой Git-репозиторий
1. Добавьте в него готовый файл [.gitignore](../.gitignore)
1. Добавьте в этот же каталог остальные необходимые файлы
1. Сделайте необходимые коммиты
1. Создайте публичный репозиторий на GitHub и свяжите свой локальный репозиторий с удалённым
1. Сделайте пуш (удостоверьтесь, что ваш код появился на GitHub)
1. Ссылку на ваш проект отправьте в личном кабинете на сайте [netology.ru](https://netology.ru)

## Refactoring & MultiThreading

### Легенда

Достаточно часто после того, как прототип проверен (мы про то, что было реализовано на лекции), возникает задача привести это в более-менее нормальный вид: выделить классы, методы, обеспечить должную функциональность.

### Задача

Необходимо отрефакторить код, рассмотренный на лекции, и применить все те знания, которые у вас есть:
1. Выделить класс `Server` с методами для
   - запуска
   - обработки конкретного подключения
1. Реализовать обработку подключений с помощью `ThreadPool`'а (выделите фиксированный на 64 потока и каждое подключение обрабатывайте в потоке из пула)

Поскольку вы - главный архитектор и проектировщик данного небольшого класса, то все архитектурные решения принимать вам, но будьте готовы к критике со стороны проверяющих.

### Результат

В качестве результата пришлите ссылку на ваш проект на GitHub в личном кабинете студента на сайте [netology.ru](https://netology.ru).

## Handlers*

**Важно**: это необязательная задача, её выполнение не влияет на получение зачёта.

### Легенда

Сервер, который вы написали в предыдущей задаче, - это, конечно, здорово, но пока он не расширяем и его нельзя переиспользовать, т.к. код обработки зашит прямо внутрь сервера.

Давайте попробуем его сделать немного полезнее.

Что хотим сделать? Мы хотим сделать так, чтобы в сервер можно было добавлять обработчики на определённые шаблоны путей.

Что это значит? Мы хотим, чтобы можно было сделать вот так:

```java
public class Main {
    public static void main(String[] args){
      final var server = new Server();  
      // код инициализации сервера (из вашего предыдущего ДЗ)

      // добавление handler'ов (обработчиков)    
      server.addHandler("GET", "/messages", new Handler() {
        public void handle(Request request, BufferedOutputStream responseStream) {
          // TODO: handlers code
        }
      });
      server.addHandler("POST", "/messages", new Handler() {
        public void handle(Request request, BufferedOutputStream responseStream) {
          // TODO: handlers code
        }
      });

      server.listen(9999);
    }    
}
```

В итоге на запрос типа GET на путь "/messages" будет вызван первый обработчик, на запрос типа POST и путь "/messages" будет вызван второй.

Как вы видите - `Handler` из себя представляет функциональный интерфейс всего с одним методом (может быть заменён на lambda).

`Request` - это класс, который проектируете вы сами, для нас важно, чтобы он содержал:
1. Метод запроса (потому что на разные методы можно назначить один и тот же Handler)
1. Заголовки запроса
1. Тело запроса (если есть)

`BufferedOutputStream` берётся просто путём заворачивания `OutputStream`'а `socket`'а: `new BufferedOutputStream(socket.getOutputStream())`.

### Задача

Реализуйте требования, указанные в легенде.

<details>
<summary>Подсказки по реализации</summary>

1. Вы принимаете запрос, парсите его целиком (как мы сделали на лекции) и собираете объект типа `Request`
1. На основании данных из `Request` вы выбираете handler (он может быть только один), который и будет обрабатывать запрос
1. Все handler'ы должны храниться в полях `Server`'а
1. Самый простой способ хранить handler'ы - это использовать в качестве ключей метод и путь (можно как сделать `Map` внутри `Map`, так и отдельные `Map`'ы на каждый метод)
1. Поиск хендлера заключается в том, что вы выбираете по нужному методу все зарегистрированные handler'ы, а затем перебираете по пути (используйте пока точное соответствие: считайте, что у вас все запросы без Query String)
1. Найдя нужный handler - достаточно вызвать его метод `handle`, передав туда `Request` и `BufferedOutputStream`
1. Поскольку ваш сервер многопоточный - думайте о том, как вы будете безопасно хранить handler'ы
1. В качестве Body достаточно передавать `InputStream` (напоминаем, Body начинается после `\r\n\r\n`

Итого: фактически вы решаете задачу поиска элемента в "коллекции" с вызовом его метода.
</details>

### Результат

Реализуйте новую функциональность в ветке `feature/handlers` вашего репозитория из ДЗ 1 и откройте Pull Request.

Поскольку вы - главный архитектор и проектировщик данного решения (уже более функционального), то все архитектурные решения принимать вам, но будьте готовы к критике со стороны проверяющих.

В качестве результата пришлите ссылку на ваш Pull Request на GitHub в личном кабинете студента на сайте [netology.ru](https://netology.ru).

После того, как ДЗ будет принято, сделайте `merge` для Pull Request'а.

