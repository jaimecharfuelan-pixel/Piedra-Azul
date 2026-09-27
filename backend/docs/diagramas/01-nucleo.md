# Núcleo común

Enums, value objects y excepciones compartidas (`com.piedraazul.nucleo`).

Diagrama PlantUML de **implementación** (fuente usada para el código).

Archivo: [`puml/01_nucleo_comun.puml`](puml/01_nucleo_comun.puml)

```plantuml
@startuml 01_nucleo_comun
title Piedrazul - Nucleo Comun (clases y reglas compartidas por todos los modulos)

skinparam classAttributeIconSize 0
hide empty members

enum DiaSemana {
  LUNES
  MARTES
  MIERCOLES
  JUEVES
  VIERNES
  SABADO
  DOMINGO
}

enum RolUsuario {
  ADMINISTRADOR
  AGENDADOR
  MEDICO
  PACIENTE
}

' ATENDIDA = la cita se cumplio -> dispara la creacion de una Consulta
enum EstadoCita {
  PROGRAMADA
  CANCELADA
  ATENDIDA
}

class TimeRange <<ValueObject>> {
  -horaInicio: LocalTime
  -horaFin: LocalTime
  +TimeRange(horaInicio: LocalTime, horaFin: LocalTime)
  +solapaCon(otro: TimeRange): boolean
  +duracionMinutos(): long
  +getHoraInicio(): LocalTime
  +getHoraFin(): LocalTime
}

abstract class DomainException {
  #codigo: String
  #mensaje: String
  +getCodigo(): String
  +getMessage(): String
}
class ReglaDeNegocioException extends DomainException {
  +{static} de(codigo: String, mensaje: String): ReglaDeNegocioException
}
class RecursoNoEncontradoException extends DomainException {
  +{static} de(codigo: String, mensaje: String): RecursoNoEncontradoException
}

note bottom of ReglaDeNegocioException
  Una sola clase para TODAS las reglas de
  negocio violadas (slot ocupado, medico
  inactivo, duracion invalida, cita no
  modificable, credenciales invalidas,
  username repetido, etc). No se crea una
  clase por cada caso: se lanza con un
  codigo distinto, ej:
  ReglaDeNegocioException.de("SLOT_NO_DISPONIBLE", "...")
  El @ControllerAdvice global mapea el
  codigo al HTTP status (409, 400, etc).
end note

note bottom of TimeRange
  Unico Value Object real: protege la regla
  de que una cita dura minimo 30 min y no se
  solapa con otra del mismo medico.
end note

note "Estas clases se repiten\n(copiadas) en cada archivo\nde modulo para que cada\n.puml se pueda pegar solo." as N1

@enduml
```
