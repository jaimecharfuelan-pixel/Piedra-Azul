<#
.SYNOPSIS
    Prueba de humo de los RF1, RF2 y RF3 contra el backend en ejecucion.

.DESCRIPTION
    Recorre por HTTP el flujo completo: lee el catalogo de Personas, comprueba la
    configuracion del RF3, agenda sobre franjas reales (RF2), lista con cantidad y
    orden (RF1), cierra y cancela citas, y verifica que cada regla de negocio
    responda con el codigo de error esperado.

    No cambia los horarios ya sembrados: crea su propio medico, asi que se puede
    ejecutar varias veces seguidas sin limpiar la base.

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File scripts/smoke-test.ps1
    powershell -ExecutionPolicy Bypass -File scripts/smoke-test.ps1 -BaseUrl http://localhost:8080
#>
param(
    [string]$BaseUrl = 'http://localhost:8080'
)

$ErrorActionPreference = 'Stop'

$script:Correctas = 0
$script:Fallidas = 0

# Los cuerpos JSON se construyen con hashtables y ConvertTo-Json para no tener
# que escapar comillas a mano dentro del script.
function Invoke-Api {
    param([string]$Method, [string]$Path, $Body)

    $parametros = @{
        Method      = $Method
        Uri         = ($BaseUrl + $Path)
        ContentType = 'application/json; charset=utf-8'
    }
    if ($null -ne $Body) {
        $json = $Body | ConvertTo-Json -Depth 6 -Compress
        $parametros.Body = [System.Text.Encoding]::UTF8.GetBytes($json)
    }
    Invoke-RestMethod @parametros
}

function Test-Exito {
    param([string]$Titulo, [scriptblock]$Accion, [scriptblock]$Comprobacion)

    try {
        $resultado = & $Accion
        if ($Comprobacion) {
            $valido = & $Comprobacion $resultado
            if (-not $valido) {
                Write-Host ('  FALLA  ' + $Titulo + ' -> respuesta inesperada') -ForegroundColor Red
                $script:Fallidas++
                return $resultado
            }
        }
        Write-Host ('  ok     ' + $Titulo) -ForegroundColor Green
        $script:Correctas++
        return $resultado
    }
    catch {
        Write-Host ('  FALLA  ' + $Titulo + ' -> ' + $_.Exception.Message) -ForegroundColor Red
        $script:Fallidas++
        return $null
    }
}

function Test-Error {
    param([string]$Titulo, [string]$CodigoEsperado, [string]$Method, [string]$Path, $Body)

    try {
        Invoke-Api -Method $Method -Path $Path -Body $Body | Out-Null
        Write-Host ('  FALLA  ' + $Titulo + ' -> se esperaba ' + $CodigoEsperado + ' y la peticion tuvo exito') -ForegroundColor Red
        $script:Fallidas++
    }
    catch {
        $respuesta = $_.Exception.Response
        if ($null -eq $respuesta) {
            Write-Host ('  FALLA  ' + $Titulo + ' -> sin respuesta HTTP') -ForegroundColor Red
            $script:Fallidas++
            return
        }
        $estado = [int]$respuesta.StatusCode
        $lector = New-Object System.IO.StreamReader($respuesta.GetResponseStream())
        $problema = $lector.ReadToEnd() | ConvertFrom-Json
        if ($problema.codigo -eq $CodigoEsperado) {
            Write-Host ('  ok     ' + $Titulo + ' (' + $estado + ' ' + $CodigoEsperado + ')') -ForegroundColor Green
            $script:Correctas++
        }
        else {
            Write-Host ('  FALLA  ' + $Titulo + ' -> esperaba ' + $CodigoEsperado + ', llego ' + $problema.codigo + ' (' + $estado + ')') -ForegroundColor Red
            $script:Fallidas++
        }
    }
}

function Cuerpo-Cita {
    param($PacienteId, $MedicoId, $Fecha, $HoraInicio, $HoraFin)
    return @{
        pacienteId = $PacienteId
        medicoId   = $MedicoId
        fecha      = $Fecha
        horaInicio = $HoraInicio
        horaFin    = $HoraFin
    }
}

function Invoke-Psql {
    param([string]$Sql)
    $Sql | docker exec -i piedraazul-db psql -U piedraazul -d piedraazul --set ON_ERROR_STOP=1
}

function Quitar-CatalogoTemporal {
    param($MedicoId, $EspecialidadId, $PacienteA, $PacienteB)

    if (-not $MedicoId -or -not $EspecialidadId -or -not $PacienteA -or -not $PacienteB) {
        Write-Host '  AVISO  no hay ids temporales para limpiar' -ForegroundColor Yellow
        return
    }

    $mid = [int]$MedicoId
    $eid = [int]$EspecialidadId
    $pa = [int]$PacienteA
    $pb = [int]$PacienteB
    $sql = @"
DELETE FROM consultas WHERE cita_id IN (
  SELECT id FROM citas WHERE medico_id = $mid OR paciente_id IN ($pa, $pb)
);
DELETE FROM citas WHERE medico_id = $mid OR paciente_id IN ($pa, $pb);
DELETE FROM periodo_dias_atencion WHERE periodo_id IN (
  SELECT id FROM periodos_disponibilidad WHERE medico_id = $mid
);
DELETE FROM periodos_disponibilidad WHERE medico_id = $mid;
DELETE FROM medicos WHERE id = $mid;
DELETE FROM especialidades WHERE id = $eid;
DELETE FROM pacientes WHERE id IN ($pa, $pb);
"@
    try {
        Invoke-Psql -Sql $sql | Out-Null
        if ($LASTEXITCODE -eq 0) {
            Write-Host '  ok     catalogo temporal eliminado' -ForegroundColor Green
            $script:Correctas++
            return
        }
    }
    catch { }

    try {
        Invoke-Api -Method Put -Path ('/api/personas/medicos/' + $mid + '/estado') -Body @{ activo = $false } | Out-Null
        Invoke-Api -Method Put -Path ('/api/personas/especialidades/' + $eid + '/estado') -Body @{ activa = $false } | Out-Null
        Write-Host '  ok     medico y especialidad desactivados (SQL no disponible)' -ForegroundColor Green
        $script:Correctas++
    }
    catch {
        Write-Host '  AVISO  no se pudo limpiar el catalogo temporal' -ForegroundColor Yellow
    }
}

function Quitar-ResiduosDeHumo {
    $sql = @'
DELETE FROM consultas WHERE cita_id IN (
  SELECT id FROM citas WHERE medico_id IN (SELECT id FROM medicos WHERE nombre_completo LIKE 'Dr. Humo %')
     OR paciente_id IN (
          SELECT id FROM pacientes
          WHERE nombre_completo LIKE 'Paciente A %' OR nombre_completo LIKE 'Paciente B %'
     )
);
DELETE FROM citas WHERE medico_id IN (SELECT id FROM medicos WHERE nombre_completo LIKE 'Dr. Humo %')
   OR paciente_id IN (
        SELECT id FROM pacientes
        WHERE nombre_completo LIKE 'Paciente A %' OR nombre_completo LIKE 'Paciente B %'
   );
DELETE FROM periodo_dias_atencion WHERE periodo_id IN (
  SELECT p.id FROM periodos_disponibilidad p
  INNER JOIN medicos m ON m.id = p.medico_id
  WHERE m.nombre_completo LIKE 'Dr. Humo %'
);
DELETE FROM periodos_disponibilidad WHERE medico_id IN (
  SELECT id FROM medicos WHERE nombre_completo LIKE 'Dr. Humo %'
);
DELETE FROM medicos WHERE nombre_completo LIKE 'Dr. Humo %';
DELETE FROM especialidades WHERE nombre LIKE 'Prueba %';
DELETE FROM pacientes
 WHERE nombre_completo LIKE 'Paciente A %' OR nombre_completo LIKE 'Paciente B %';
'@
    try {
        Invoke-Psql -Sql $sql | Out-Null
    }
    catch { }
}

# --- Fechas de trabajo ---------------------------------------------------------
# +2 dias evita el filtro de "franjas que ya pasaron" y cae dentro de la ventana.
$hoy = Get-Date
$hoyIso = $hoy.ToString('yyyy-MM-dd')
$fecha = $hoy.AddDays(2).ToString('yyyy-MM-dd')
$ayer = $hoy.AddDays(-1).ToString('yyyy-MM-dd')
$muyLejos = $hoy.AddDays(400).ToString('yyyy-MM-dd')
$finRango = $hoy.AddDays(5).ToString('yyyy-MM-dd')
$sufijo = [guid]::NewGuid().ToString().Substring(0, 8)
$todosLosDias = @('LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO')

Write-Host ''
Write-Host ('PiedraAzul - prueba de humo contra ' + $BaseUrl) -ForegroundColor Cyan
Write-Host ('Fecha de prueba: ' + $fecha) -ForegroundColor Cyan

# --- Preparacion ---------------------------------------------------------------
Write-Host ''
Write-Host 'Preparando catalogo propio (modulo Personas)' -ForegroundColor Yellow

$especialidad = Test-Exito 'crear especialidad' {
    Invoke-Api -Method Post -Path '/api/personas/especialidades' -Body @{ nombre = ('Medicina familiar ' + $sufijo) }
} { param($r) $null -ne $r.id }

$medico = Test-Exito 'crear medico' {
    Invoke-Api -Method Post -Path '/api/personas/medicos' -Body @{
        nombreCompleto = ('Dra. Elena Duarte ' + $sufijo)
        especialidadId = $especialidad.id
    }
} { param($r) $r.activo -eq $true }

$pacienteA = Test-Exito 'registrar paciente A' {
    Invoke-Api -Method Post -Path '/api/personas/pacientes' -Body @{
        nombreCompleto = ('Andres Molina ' + $sufijo)
        telefono       = '3001112233'
    }
} { param($r) $null -ne $r.id }

$pacienteB = Test-Exito 'registrar paciente B' {
    Invoke-Api -Method Post -Path '/api/personas/pacientes' -Body @{
        nombreCompleto = ('Lucia Herrera ' + $sufijo)
        telefono       = '3009876543'
    }
} { param($r) $null -ne $r.id }

Test-Error 'rechaza un registro de paciente sin nombre' 'DATOS_INVALIDOS' 'Post' '/api/personas/pacientes' @{
    nombreCompleto = ''
    telefono       = 'abc'
}

$medicoId = $medico.id
$especialidadId = $especialidad.id
$pacienteA = $pacienteA.id
$pacienteB = $pacienteB.id

# --- RF3 -----------------------------------------------------------------------
Write-Host ''
Write-Host 'RF3 - configuracion de disponibilidad' -ForegroundColor Yellow

Test-Exito 'leer la ventana de agendamiento' {
    Invoke-Api -Method Get -Path '/api/disponibilidad/configuracion'
} { param($r) $r.ventanaSemanas -ge 1 -and $r.agendamientoHasta } | Out-Null

Test-Exito 'registrar horario 08:00-12:00 de 30 min, todos los dias' {
    Invoke-Api -Method Post -Path '/api/disponibilidad/periodos' -Body @{
        medicoId                  = $medicoId
        fechaInicio               = $hoyIso
        diasAtencion              = $todosLosDias
        horaInicio                = '08:00'
        horaFin                   = '12:00'
        duracionCitaMinutos       = 30
        descansoEntreCitasMinutos = 0
    }
} { param($r) $r.duracionCitaMinutos -eq 30 -and $r.diasAtencion.Count -eq 7 -and $r.vigente } | Out-Null

# La duracion minima esta protegida dos veces: Bean Validation en el DTO la para
# antes de llegar al dominio, asi que el codigo que sale es DATOS_INVALIDOS.
Test-Error 'rechaza una cita de 20 minutos (validacion del formulario)' 'DATOS_INVALIDOS' 'Post' '/api/disponibilidad/periodos' @{
    medicoId            = $medicoId
    fechaInicio         = $muyLejos
    diasAtencion        = @('LUNES')
    horaInicio          = '08:00'
    horaFin             = '12:00'
    duracionCitaMinutos = 20
}

# Con 120 min el formulario pasa, y es la regla del dominio la que rechaza que la
# cita no quepa en una franja de una hora.
Test-Error 'rechaza una cita que no cabe en la franja (regla del dominio)' 'DURACION_CITA_INVALIDA' 'Post' '/api/disponibilidad/periodos' @{
    medicoId            = $medicoId
    fechaInicio         = $muyLejos
    diasAtencion        = @('LUNES')
    horaInicio          = '08:00'
    horaFin             = '09:00'
    duracionCitaMinutos = 120
}

Test-Error 'rechaza un horario sin dias de atencion' 'DATOS_INVALIDOS' 'Post' '/api/disponibilidad/periodos' @{
    medicoId            = $medicoId
    fechaInicio         = $muyLejos
    diasAtencion        = @()
    horaInicio          = '08:00'
    horaFin             = '12:00'
    duracionCitaMinutos = 30
}

Test-Error 'rechaza un horario que se cruza con el vigente' 'PERIODO_SOLAPADO' 'Post' '/api/disponibilidad/periodos' @{
    medicoId            = $medicoId
    fechaInicio         = $ayer
    diasAtencion        = @('LUNES')
    horaInicio          = '08:00'
    horaFin             = '12:00'
    duracionCitaMinutos = 30
}

Test-Error 'rechaza una ventana de 0 semanas' 'DATOS_INVALIDOS' 'Put' '/api/disponibilidad/configuracion' @{ semanas = 0 }

# --- RF2 -----------------------------------------------------------------------
Write-Host ''
Write-Host 'RF2 - franjas disponibles y agendamiento' -ForegroundColor Yellow

$rutaSlots = '/api/disponibilidad/slots?medicoId=' + $medicoId + '&fecha=' + $fecha
$rutaListado = '/api/citas?medicoId=' + $medicoId + '&fecha=' + $fecha

Test-Exito 'consultar franjas (8 esperadas)' {
    Invoke-Api -Method Get -Path $rutaSlots
} { param($r) $r.Count -eq 8 } | Out-Null

$cita1 = Test-Exito 'agendar 08:00-08:30' {
    Invoke-Api -Method Post -Path '/api/citas' -Body (Cuerpo-Cita $pacienteA $medicoId $fecha '08:00' '08:30')
} { param($r) $r.estado -eq 'PROGRAMADA' -and $r.duracionMinutos -eq 30 }

Test-Exito 'la franja agendada deja de ofrecerse (7 esperadas)' {
    Invoke-Api -Method Get -Path $rutaSlots
} { param($r) $r.Count -eq 7 -and -not ($r.horaInicio -contains '08:00') } | Out-Null

$cita2 = Test-Exito 'agendar 09:00-09:30 para otro paciente' {
    Invoke-Api -Method Post -Path '/api/citas' -Body (Cuerpo-Cita $pacienteB $medicoId $fecha '09:00' '09:30')
} { param($r) $r.estado -eq 'PROGRAMADA' }

Test-Error 'rechaza una franja ya tomada' 'SLOT_NO_DISPONIBLE' 'Post' '/api/citas' (Cuerpo-Cita $pacienteB $medicoId $fecha '08:00' '08:30')
Test-Error 'rechaza un inicio fuera de la rejilla' 'SLOT_NO_DISPONIBLE' 'Post' '/api/citas' (Cuerpo-Cita $pacienteA $medicoId $fecha '10:15' '10:45')
Test-Error 'rechaza una hora fuera de la franja de atencion' 'SLOT_NO_DISPONIBLE' 'Post' '/api/citas' (Cuerpo-Cita $pacienteA $medicoId $fecha '20:00' '20:30')
Test-Error 'rechaza una fecha fuera de la ventana' 'FUERA_DE_VENTANA_AGENDAMIENTO' 'Post' '/api/citas' (Cuerpo-Cita $pacienteA $medicoId $muyLejos '08:00' '08:30')
Test-Error 'rechaza una fecha pasada' 'FUERA_DE_VENTANA_AGENDAMIENTO' 'Post' '/api/citas' (Cuerpo-Cita $pacienteA $medicoId $ayer '08:00' '08:30')
Test-Error 'rechaza una cita de menos de 30 minutos' 'DURACION_INVALIDA' 'Post' '/api/citas' (Cuerpo-Cita $pacienteA $medicoId $fecha '10:00' '10:15')
Test-Error 'rechaza un paciente no registrado' 'PACIENTE_NO_ENCONTRADO' 'Post' '/api/citas' (Cuerpo-Cita 999999 $medicoId $fecha '10:00' '10:30')
Test-Error 'rechaza un cuerpo sin los campos obligatorios' 'DATOS_INVALIDOS' 'Post' '/api/citas' @{}

# --- RF1 -----------------------------------------------------------------------
Write-Host ''
Write-Host 'RF1 - listado con cantidad y orden' -ForegroundColor Yellow

Test-Exito 'listado por defecto (HORA_ASC)' {
    Invoke-Api -Method Get -Path $rutaListado
} { param($r) $r.cantidad -eq 2 -and $r.cantidadProgramadas -eq 2 -and $r.citas[0].horaInicio -eq '08:00' } | Out-Null

Test-Exito 'orden HORA_DESC invierte la tabla' {
    Invoke-Api -Method Get -Path ($rutaListado + '&orden=HORA_DESC')
} { param($r) $r.citas[0].horaInicio -eq '09:00' } | Out-Null

Test-Exito 'orden PACIENTE_ASC ordena por nombre' {
    Invoke-Api -Method Get -Path ($rutaListado + '&orden=PACIENTE_ASC')
} { param($r) $r.citas[0].pacienteNombre -le $r.citas[1].pacienteNombre } | Out-Null

Test-Exito 'orden PACIENTE_DESC invierte el nombre' {
    Invoke-Api -Method Get -Path ($rutaListado + '&orden=PACIENTE_DESC')
} { param($r) $r.citas[0].pacienteNombre -ge $r.citas[1].pacienteNombre } | Out-Null

Test-Exito 'el listado trae nombre de medico y de paciente' {
    Invoke-Api -Method Get -Path $rutaListado
} { param($r) $r.medicoNombre -and $r.citas[0].pacienteNombre } | Out-Null

Test-Error 'rechaza un orden desconocido' 'ORDEN_INVALIDO' 'Get' ($rutaListado + '&orden=PEPE')

# --- Ciclo de vida -------------------------------------------------------------
Write-Host ''
Write-Host 'Ciclo de vida de la cita e historial' -ForegroundColor Yellow

Test-Exito 'reagendar la cita de las 08:00 a las 11:00' {
    Invoke-Api -Method Put -Path ('/api/citas/' + $cita1.id + '/reagendamiento') -Body @{
        fecha      = $fecha
        horaInicio = '11:00'
        horaFin    = '11:30'
    }
} { param($r) $r.horaInicio -eq '11:00' -and $r.estado -eq 'PROGRAMADA' } | Out-Null

Test-Exito 'cancelar la cita la deja en CANCELADA' {
    Invoke-Api -Method Put -Path ('/api/citas/' + $cita1.id + '/cancelacion')
} { param($r) $r.estado -eq 'CANCELADA' } | Out-Null

Test-Error 'una cita cancelada no se puede volver a cancelar' 'CITA_NO_MODIFICABLE' 'Put' ('/api/citas/' + $cita1.id + '/cancelacion')

Test-Exito 'marcar atendida genera la consulta del historial' {
    Invoke-Api -Method Put -Path ('/api/citas/' + $cita2.id + '/atencion') -Body @{ observaciones = 'Control sin novedades' }
} { param($r) $r.citaId -eq $cita2.id -and $r.observaciones -eq 'Control sin novedades' } | Out-Null

Test-Error 'una cita atendida no se puede reagendar' 'CITA_NO_MODIFICABLE' 'Put' ('/api/citas/' + $cita2.id + '/reagendamiento') @{
    fecha      = $fecha
    horaInicio = '11:00'
    horaFin    = '11:30'
}

Test-Exito 'la consulta aparece en el historial del medico' {
    Invoke-Api -Method Get -Path ('/api/citas/historial/medico/' + $medicoId)
} { param($r) $r.Count -eq 1 -and $r[0].citaId -eq $cita2.id } | Out-Null

Test-Exito 'el listado refleja 1 cancelada y 1 atendida' {
    Invoke-Api -Method Get -Path $rutaListado
} { param($r) $r.cantidad -eq 2 -and $r.cantidadCanceladas -eq 1 -and $r.cantidadAtendidas -eq 1 } | Out-Null

Test-Exito 'franjas por rango para el calendario' {
    Invoke-Api -Method Get -Path ('/api/disponibilidad/slots/rango?medicoId=' + $medicoId + '&desde=' + $fecha + '&hasta=' + $finRango)
} { param($r) $r.Count -gt 8 } | Out-Null

Test-Exito 'citas por rango para el calendario' {
    Invoke-Api -Method Get -Path ('/api/citas/rango?medicoId=' + $medicoId + '&desde=' + $fecha + '&hasta=' + $finRango)
} { param($r) $r.Count -eq 2 } | Out-Null

# --- Limpieza ------------------------------------------------------------------
Write-Host ''
Write-Host 'Limpiando el catalogo temporal de esta prueba' -ForegroundColor Yellow
Quitar-CatalogoTemporal -MedicoId $medicoId -EspecialidadId $especialidadId -PacienteA $pacienteA -PacienteB $pacienteB
Quitar-ResiduosDeHumo

# --- Resultado -----------------------------------------------------------------
Write-Host ''
Write-Host ('-' * 62)
if ($script:Fallidas -eq 0) {
    Write-Host ('TODO CORRECTO: ' + $script:Correctas + ' comprobaciones pasaron') -ForegroundColor Green
    exit 0
}
Write-Host ($script:Correctas.ToString() + ' correctas, ' + $script:Fallidas + ' FALLIDAS') -ForegroundColor Red
exit 1
