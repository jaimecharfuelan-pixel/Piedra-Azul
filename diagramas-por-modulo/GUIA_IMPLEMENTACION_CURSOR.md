# Guía para generar el código hueco (skeleton) con Cursor

Pega este archivo completo como prompt en Cursor, junto con los 6 `.puml`
adjuntos en el mismo mensaje/contexto (los lee directo como texto, no
necesitas convertirlos a imagen).

## Stack tecnológico del proyecto

- **Frontend**: Angular (SPA aparte, este documento es solo para el backend)
- **Backend**: Spring Boot
- **Seguridad/JWT**: Spring Security + librería **JJWT** (io.jsonwebtoken)
- **Persistencia**: PostgreSQL + Spring Data JPA

## Prompt para Cursor

```
Tienes 6 diagramas de clases en PlantUML que describen un backend Spring
Boot en arquitectura de monolito modular (modular monolith) para un
sistema de reserva de citas medicas ("Piedrazul").

Stack tecnologico OBLIGATORIO:
- Spring Boot (Web, Validation)
- Spring Security + JJWT (io.jsonwebtoken:jjwt-api, jjwt-impl, jjwt-jackson)
  para generar/validar el token. NO uses OAuth2 resource server ni otra
  libreria de JWT.
- PostgreSQL como base de datos
- Spring Data JPA + Hibernate para persistencia (los *JpaEntity de los
  diagramas son @Entity de JPA, los *RepositoryAdapter usan
  JpaRepository de Spring Data por debajo)
- Maven (o Gradle si ya existe el proyecto, respeta el que haya)

Los 6 diagramas:

- 00_diagrama_general_modulos.puml -> mapa de dependencias entre modulos
- 01_nucleo_comun.puml              -> enums, ValueObjects y excepciones
                                       compartidas entre TODOS los modulos
- 02_modulo_identidad.puml         -> JWT + CRUD de Usuario
- 03_modulo_personas.puml          -> CRUD Medico, Paciente, Especialidad
- 04_modulo_disponibilidad.puml    -> horarios/periodos de atencion + algoritmo de slots
- 05_modulo_citas.puml             -> agendar/cancelar/reagendar/atender cita + historial

Genera el CODIGO HUECO (skeleton) en Java 17 + Spring Boot, siguiendo
EXACTAMENTE las clases, interfaces, atributos y firmas de metodo de los
diagramas. Reglas:

1. Estructura de paquetes por modulo (arquitectura hexagonal/puertos y
   adaptadores), un paquete por modulo, ej:
   co.unicauca.piedrazul.citas.dominio
   co.unicauca.piedrazul.citas.aplicacion.puertos.entrada
   co.unicauca.piedrazul.citas.aplicacion.puertos.salida
   co.unicauca.piedrazul.citas.aplicacion.servicio
   co.unicauca.piedrazul.citas.infraestructura.rest
   co.unicauca.piedrazul.citas.infraestructura.persistencia
   co.unicauca.piedrazul.citas.infraestructura.dto
   (mismo patron para identidad, personas, disponibilidad)

2. Las clases del nucleo comun (enums, TimeRange, DomainException,
   ReglaDeNegocioException, RecursoNoEncontradoException) van en un
   paquete unico co.unicauca.piedrazul.nucleo y NO se duplican por
   modulo aunque el .puml las repita (eso es solo para que cada
   archivo se lea solo).

3. CONVENCION OBLIGATORIA para el codigo hueco: todo metodo que
   represente LOGICA DE NEGOCIO (metodos de las clases *Service, y los
   metodos no triviales de las Entity como reagendar(), cancelar(),
   calcular(), etc.) se implementa con el cuerpo:
       throw new UnsupportedOperationException("No implementado: <NombreClase>.<nombreMetodo>");
   Los getters, setters, constructores simples y los DTOs SI se
   implementan completos (son solo datos, no logica).

4. Las interfaces de puerto (UseCase, Repository, *Port) se generan
   completas, sin cuerpo (son interfaces).

5. Las clases de infraestructura (*Controller, *RepositoryAdapter,
   *JpaEntity) se generan con anotaciones de Spring
   (@RestController, @Repository, @Entity) y los metodos del
   Controller/Adapter tambien lanzan UnsupportedOperationException en el
   cuerpo, EXCEPTO el cableado de dependencias (constructor/inyeccion),
   que si debe quedar funcional.

6. Respeta las relaciones entre modulos: un modulo NUNCA importa el
   Repository ni la Entity de otro modulo directamente, solo su
   interfaz de puerto publico (*Port), tal como esta en
   00_diagrama_general_modulos.puml.

7. Genera los modulos en este orden (es el orden de dependencia, de
   abajo hacia arriba): nucleo comun -> Personas -> Identidad ->
   Disponibilidad -> Citas.

8. JwtTokenPort (modulo Identidad) se implementa con JJWT en
   JwtServiceAdapter: generarToken() firma el JWT con una SecretKey
   (HMAC-SHA256, io.jsonwebtoken.Jwts.builder()), metiendo como claims
   el username y el rol (RolUsuario); extraerUsername() y
   esTokenValido() usan Jwts.parser(). La clave secreta y el tiempo de
   expiracion van en application.properties (no hardcodeados). El
   cuerpo interno de estos 3 metodos SI se implementa completo (es
   infraestructura de seguridad, no logica de negocio del dominio), no
   lleva UnsupportedOperationException.

9. JwtAuthenticationFilter es un OncePerRequestFilter de Spring
   Security que lee el header Authorization: Bearer <token>, usa
   JwtTokenPort para validarlo y carga el usuario en el
   SecurityContext con su rol como authority (ej. ROLE_MEDICO,
   ROLE_AGENDADOR). Este filtro y la clase de configuracion
   SecurityConfig (SecurityFilterChain, quien puede llamar cada
   endpoint segun RolUsuario) SI se implementan completos, son
   infraestructura, no dominio.

10. Todos los *JpaEntity son @Entity de JPA mapeadas a PostgreSQL
    (usa @Table, @Id @GeneratedValue(strategy = IDENTITY), tipos
    columna razonables: LocalDate/LocalTime/LocalDateTime directos,
    enums con @Enumerated(EnumType.STRING)). Los *RepositoryAdapter
    implementan la interfaz *Repository del dominio delegando en una
    interfaz interna que extiende JpaRepository<XxxJpaEntity, Long>;
    el mapeo entidad-de-dominio <-> JpaEntity (metodos toDomain()/
    toJpaEntity()) SI se implementa completo, no es logica de negocio.

Empieza por el nucleo comun y el modulo Personas.
```

## Por qué este orden de implementación

Según `00_diagrama_general_modulos.puml`, la dependencia es:
`Personas <- Identidad`, `Personas <- Citas <- Disponibilidad`.
Si implementas Citas antes que Disponibilidad, te va a faltar el puerto
`ConsultarConfiguracionPort`; hazlo en el orden de arriba y no te vas a
trabar.

## Nota sobre las excepciones

Ya viste que simplificamos todo a `ReglaDeNegocioException.de(codigo, mensaje)`
y `RecursoNoEncontradoException.de(codigo, mensaje)`. Cuando Cursor genere
esas dos clases, dile que el `codigo` es un `String` libre (ej.
`"SLOT_NO_DISPONIBLE"`) que luego un `@ControllerAdvice` global mapea a un
HTTP status — no hace falta que implementes ese `@ControllerAdvice` en
esta primera entrega si no te alcanza el tiempo, con las dos clases de
excepcion ya cumples el criterio de la rúbrica.
