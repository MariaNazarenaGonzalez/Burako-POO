# Burako-POO

# Burako - Trabajo Práctico Integrador

## Descripción

Este proyecto implementa una versión digital del juego de cartas **Burako** como Trabajo Práctico Integrador de la asignatura **Programación Orientada a Objetos**.

La aplicación permite disputar partidas entre dos jugadores mediante una arquitectura cliente-servidor, incorporando persistencia de datos, ranking de jugadores e interfaces gráfica y por consola.

El desarrollo fue realizado aplicando los principios de la Programación Orientada a Objetos y los patrones de diseño requeridos por la cátedra.

---

# Características implementadas

* Partidas multijugador en red (2 jugadores).
* Arquitectura cliente-servidor.
* Interfaz gráfica.
* Interfaz por consola.
* Persistencia de usuarios.
* Persistencia de partidas.
* Ranking de jugadores.
* Recuperación de información persistida.
* Identificación de usuarios mediante su nombre.
* Implementación del patrón MVC.
* Implementación del patrón Observer.

---

# Tecnologías utilizadas

* Java 14
* Java Swing
* Java Sockets
* Serialización de objetos
* VS Code

---

# Arquitectura

El proyecto se encuentra organizado siguiendo el patrón **Modelo-Vista-Controlador (MVC)**.

* **Modelo:** contiene la lógica del juego y las reglas de Burako.
* **Vista:** implementa las interfaces gráfica y de consola.
* **Controlador:** coordina la interacción entre las vistas y el modelo.

Para desacoplar la comunicación entre componentes se implementó además el patrón **Observer**.

La comunicación entre clientes se realiza mediante un servidor dedicado.

---


# Flujo de ejecución de una partida

El siguiente diagrama resume el recorrido que realiza una acción del jugador dentro de la aplicación.

```text
                    ┌──────────────────────────┐
                    │        Main.java         │
                    │ Inicio del cliente       │
                    └─────────────┬────────────┘
                                  │
                                  ▼
                    ┌──────────────────────────┐
                    │      Vista (GUI/CLI)     │
                    │ Interacción del usuario  │
                    └─────────────┬────────────┘
                                  │
                                  ▼
                    ┌──────────────────────────┐
                    │      Controladores       │
                    │ Validan la solicitud     │
                    └─────────────┬────────────┘
                                  │
                                  ▼
                    ┌──────────────────────────┐
                    │         Modelo           │
                    │ Reglas del Burako        │
                    └─────────────┬────────────┘
                                  │
              ┌───────────────────┴───────────────────┐
              │                                       │
              ▼                                       ▼
    ┌────────────────────┐                 ┌────────────────────┐
    │ Persistencia       │                 │ Comunicación Red   │
    │ Guarda el estado   │                 │ Envía la jugada    │
    └─────────┬──────────┘                 └─────────┬──────────┘
              │                                      │
              └───────────────────┬──────────────────┘
                                  │
                                  ▼
                    ┌──────────────────────────┐
                    │       Observer           │
                    │ Notifica cambios         │
                    └─────────────┬────────────┘
                                  │
                                  ▼
                    ┌──────────────────────────┐
                    │         Vista            │
                    │ Actualiza la interfaz    │
                    └──────────────────────────┘
```

## Inicio del sistema

1. Se ejecuta `AppService.java`, iniciando el servidor y quedando a la espera de conexiones.
2. Cada jugador ejecuta `Main.java`, que inicia un cliente.
3. El cliente establece la conexión con el servidor.
4. El usuario ingresa su nombre.
5. Si el nombre ya existe en la persistencia, se recupera la información correspondiente; en caso contrario, se crea un nuevo usuario.

## Inicio de una partida

1. Los clientes solicitan participar en una partida.
2. El servidor coordina la creación de la partida.
3. Se inicializa el modelo del juego.
4. Se reparten las cartas iniciales.
5. Se establece el turno del primer jugador.
6. Las vistas muestran el estado inicial del juego.

## Ejecución de una jugada

Cada acción realizada por un jugador sigue el mismo recorrido:

1. El jugador interactúa con la interfaz.
2. La vista comunica la acción al controlador.
3. El controlador valida la operación solicitada.
4. El modelo ejecuta la lógica correspondiente.
5. Si el estado del juego cambia, el modelo notifica a sus observadores.
6. Las vistas reciben la notificación y actualizan automáticamente la información mostrada.
7. Si la acción debe reflejarse en el otro cliente, el servidor transmite la actualización por la red.

## Persistencia

Cuando ocurre un evento que modifica información permanente del sistema, los componentes de persistencia almacenan los cambios correspondientes.

Entre los datos persistidos se encuentran:

* Usuarios.
* Partidas.
* Ranking de jugadores.

Esto permite conservar la información entre distintas ejecuciones de la aplicación.

## Finalización de la partida

Al concluir una partida:

1. Se calcula el puntaje final.
2. Se actualiza el ranking de jugadores.
3. Se guarda el estado necesario en la persistencia.
4. Las vistas muestran el resultado final.
5. Los clientes pueden iniciar una nueva partida o finalizar la aplicación.

# Persistencia

La aplicación almacena de forma persistente la información necesaria para mantener el estado del sistema entre ejecuciones.

Se persisten:

* Usuarios.
* Partidas.
* Ranking de jugadores.

La persistencia forma parte de los requisitos establecidos por la cátedra.

---

# Ejecución

## Iniciar el servidor

Ejecutar:

```
AppService.java
```

Una vez iniciado el servidor permanecerá esperando conexiones de clientes.

---

## Iniciar el cliente

Ejecutar:

```
Main.java
```

Cada cliente se conectará al servidor para participar de una partida.

---

# Organización del proyecto

```
src/
│
├── modelo/
├── vista/
├── controlador/
├── persistencia/
├── red/
├── observador/
└── ...
```

Cada paquete agrupa clases con responsabilidades específicas para reducir el acoplamiento y facilitar el mantenimiento del sistema.

---

# Patrones de diseño implementados

## MVC

Permite separar la lógica del juego de la interfaz de usuario y del control de la aplicación.

## Observer

Permite mantener sincronizadas las vistas respecto de los cambios producidos en el modelo sin generar dependencias fuertes entre ambos componentes.

---

# Limitaciones

La versión actual implementa partidas de **2 jugadores**.

El reglamento original de Burako contempla también partidas de 4 jugadores, funcionalidad que no fue incorporada en esta versión.

---

# Autora

Maria Nazarena Gonzalez legajo 190217

Trabajo realizado para la materia **Programación Orientada a Objetos**.
