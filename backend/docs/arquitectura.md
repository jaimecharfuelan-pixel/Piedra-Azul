# Arquitectura

Monolito modular. Entre módulos se habla por puertos: el que necesita algo
declara la interfaz y el otro la implementa con una fachada.

Cada módulo trae su `infraestructura` (REST, JPA, DTO). La seguridad no es un
paquete suelto: vive en Identidad.

## Paquetes

```plantuml
@startuml
title Paquetes del monolito (com.piedraazul)
skinparam backgroundColor white
skinparam shadowing false
skinparam packageStyle rectangle

package "com.piedraazul" {
  package "nucleo" as N {
    card "enums, VOs,\nexcepciones" as Nn
  }
  package "identidad" as I {
    card "usuarios, roles,\nKeycloak, SecurityConfig" as Ii
  }
  package "personas" as P {
    card "pacientes, médicos,\nespecialidades" as Pp
  }
  package "disponibilidad" as D {
    card "ventana, periodos,\nslots" as Dd
  }
  package "citas" as C {
    card "agenda y\nciclo de vida" as Cc
  }
}
@enduml
```

## Dependencias

Citas y Disponibilidad se miran entre sí solo por puertos (fachadas), no por
repositorios ajenos. Así no se arma un ciclo raro de beans.

```plantuml
@startuml
title Dependencias entre módulos (por puertos)
skinparam backgroundColor white
skinparam shadowing false
skinparam componentStyle rectangle

component "nucleo" as N
component "identidad" as I
component "personas" as P
component "disponibilidad" as D
component "citas" as C

I --> N
P --> N
D --> N
C --> N

I --> P : RegistrarPersonaPort
D --> P : CatalogoMedicosPort
C --> P : CatalogoMedicosPort\nCatalogoPacientesPort
C --> D : ConsultarConfiguracionPort
D --> C : ConsultarCitasPort

note right of I
  JWT + SecurityConfig
  viven en Identidad
end note
@enduml
```

## Diagramas de clases

- [Núcleo](diagramas/01-nucleo.md)
- [Identidad](diagramas/02-identidad.md)
- [Personas](diagramas/03-personas.md)
- [Disponibilidad](diagramas/04-disponibilidad.md)
- [Citas](diagramas/05-citas.md)

Índice: [Diagramas](diagramas/index.md).
