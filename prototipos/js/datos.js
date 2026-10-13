/* Datos de demostración — prototipos RF4 (CSV) y RF5 (cédula) */
window.PZ = {
  medicos: [
    { id: "1", nombre: "Dra. Ana Pérez", especialidad: "Medicina general" },
    { id: "2", nombre: "Dr. Carlos Ruiz", especialidad: "Pediatría" },
    { id: "3", nombre: "Lic. Sofía Méndez", especialidad: "Fisioterapia" },
  ],

  /* Agenda del día (base para exportar CSV) */
  citasAgenda: [
    {
      medicoId: "1",
      fecha: "2026-10-13",
      horaInicio: "08:00",
      horaFin: "08:30",
      pacienteNombre: "Juan Ramírez",
      pacienteTelefono: "3001234567",
      pacienteCedula: "1001001001",
      estado: "PROGRAMADA",
    },
    {
      medicoId: "1",
      fecha: "2026-10-13",
      horaInicio: "08:30",
      horaFin: "09:00",
      pacienteNombre: "María Gómez",
      pacienteTelefono: "3007654321",
      pacienteCedula: "1002002002",
      estado: "PROGRAMADA",
    },
    {
      medicoId: "1",
      fecha: "2026-10-13",
      horaInicio: "09:00",
      horaFin: "09:30",
      pacienteNombre: "Pedro Vargas",
      pacienteTelefono: "3011112233",
      pacienteCedula: "1003003003",
      estado: "ATENDIDA",
    },
    {
      medicoId: "1",
      fecha: "2026-10-13",
      horaInicio: "09:30",
      horaFin: "10:00",
      pacienteNombre: "Laura Díaz",
      pacienteTelefono: "3022223344",
      pacienteCedula: "1004004004",
      estado: "CANCELADA",
    },
    {
      medicoId: "2",
      fecha: "2026-10-13",
      horaInicio: "10:00",
      horaFin: "10:30",
      pacienteNombre: "Andrés Niño",
      pacienteTelefono: "3033334455",
      pacienteCedula: "1005005005",
      estado: "PROGRAMADA",
    },
    {
      medicoId: "2",
      fecha: "2026-10-14",
      horaInicio: "10:00",
      horaFin: "10:30",
      pacienteNombre: "Camila Soto",
      pacienteTelefono: "3044445566",
      pacienteCedula: "1006006006",
      estado: "PROGRAMADA",
    },
  ],

  /* Pacientes + historial para consulta por cédula */
  pacientes: {
    "1001001001": {
      nombreCompleto: "Juan Ramírez",
      telefono: "3001234567",
      citas: [
        {
          fecha: "2026-10-13",
          horaInicio: "08:00",
          horaFin: "08:30",
          medicoNombre: "Dra. Ana Pérez",
          estado: "PROGRAMADA",
          duracionMinutos: 30,
        },
        {
          fecha: "2026-09-20",
          horaInicio: "09:00",
          horaFin: "09:30",
          medicoNombre: "Dra. Ana Pérez",
          estado: "ATENDIDA",
          duracionMinutos: 30,
        },
        {
          fecha: "2026-08-05",
          horaInicio: "14:00",
          horaFin: "14:45",
          medicoNombre: "Lic. Sofía Méndez",
          estado: "CANCELADA",
          duracionMinutos: 45,
        },
      ],
    },
    "1002002002": {
      nombreCompleto: "María Gómez",
      telefono: "3007654321",
      citas: [
        {
          fecha: "2026-10-13",
          horaInicio: "08:30",
          horaFin: "09:00",
          medicoNombre: "Dra. Ana Pérez",
          estado: "PROGRAMADA",
          duracionMinutos: 30,
        },
      ],
    },
    "1999000000": {
      nombreCompleto: "Paciente sin citas",
      telefono: "3055556677",
      citas: [],
    },
  },

  medicoPorId(id) {
    return this.medicos.find((m) => m.id === id);
  },

  citasPorMedicoYFecha(medicoId, fecha) {
    return this.citasAgenda.filter((c) => c.medicoId === medicoId && c.fecha === fecha);
  },

  normalizarCedula(valor) {
    return String(valor || "")
      .trim()
      .replace(/[.\s-]/g, "");
  },

  buscarPorCedula(cedula) {
    const key = this.normalizarCedula(cedula);
    const p = this.pacientes[key];
    if (!p) return null;
    return { cedula: key, ...p };
  },

  aCsv(citas) {
    const header = "horaInicio,horaFin,pacienteNombre,pacienteTelefono,pacienteCedula,estado";
    const filas = citas.map((c) =>
      [c.horaInicio, c.horaFin, c.pacienteNombre, c.pacienteTelefono, c.pacienteCedula, c.estado]
        .map((v) => `"${String(v).replace(/"/g, '""')}"`)
        .join(",")
    );
    return [header, ...filas].join("\n");
  },
};
