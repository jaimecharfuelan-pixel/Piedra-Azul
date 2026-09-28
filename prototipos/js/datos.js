/* Datos hardcodeados para el prototipo conceptual PiedraAzul */
window.PZ = {
  medicos: [
    { id: "m1", nombre: "Dra. Ana López", rol: "Médico", especialidad: "Medicina general" },
    { id: "m2", nombre: "Dr. Carlos Ruiz", rol: "Médico", especialidad: "Pediatría" },
    { id: "m3", nombre: "Lic. Sofía Méndez", rol: "Terapista", especialidad: "Fisioterapia" }
  ],

  citas: [
    { id: "c1", medicoId: "m1", fecha: "2026-09-28", hora: "08:00", paciente: "María Gómez", documento: "1061", estado: "programada" },
    { id: "c2", medicoId: "m1", fecha: "2026-09-28", hora: "08:30", paciente: "Pedro Vargas", documento: "1062", estado: "programada" },
    { id: "c3", medicoId: "m1", fecha: "2026-09-28", hora: "09:00", paciente: "Laura Díaz", documento: "1063", estado: "atendida" },
    { id: "c4", medicoId: "m1", fecha: "2026-09-29", hora: "08:00", paciente: "Juan Pérez", documento: "1064", estado: "programada" },
    { id: "c5", medicoId: "m2", fecha: "2026-09-28", hora: "10:00", paciente: "Andrés Niño", documento: "1065", estado: "programada" },
    { id: "c6", medicoId: "m2", fecha: "2026-09-28", hora: "10:30", paciente: "Camila Soto", documento: "1066", estado: "cancelada" },
    { id: "c7", medicoId: "m3", fecha: "2026-09-28", hora: "14:00", paciente: "Diego Ríos", documento: "1067", estado: "programada" },
    { id: "c8", medicoId: "m3", fecha: "2026-09-28", hora: "14:45", paciente: "Elena Cruz", documento: "1068", estado: "programada" },
    { id: "c9", medicoId: "m3", fecha: "2026-09-30", hora: "15:00", paciente: "Paula Mora", documento: "1069", estado: "programada" }
  ],

  /* Disponibilidad por profesional (RF3 / RF2) */
  disponibilidad: {
    m1: {
      dias: ["lun", "mar", "mie", "jue", "vie"],
      horaInicio: "08:00",
      horaFin: "12:00",
      intervaloMin: 30
    },
    m2: {
      dias: ["lun", "mie", "vie"],
      horaInicio: "10:00",
      horaFin: "13:00",
      intervaloMin: 30
    },
    m3: {
      dias: ["mar", "jue", "vie"],
      horaInicio: "14:00",
      horaFin: "17:00",
      intervaloMin: 45
    }
  },

  configGlobal: {
    ventanaSemanas: 3
  },

  medicoPorId(id) {
    return this.medicos.find((m) => m.id === id);
  },

  generarFranjas(medicoId, fecha) {
    const cfg = this.disponibilidad[medicoId];
    if (!cfg) return [];

    const diaSemana = this.diaClave(fecha);
    if (!cfg.dias.includes(diaSemana)) return [];

    const slots = [];
    let [h, m] = cfg.horaInicio.split(":").map(Number);
    const [hf, mf] = cfg.horaFin.split(":").map(Number);
    const finMin = hf * 60 + mf;

    while (h * 60 + m + cfg.intervaloMin <= finMin) {
      const hora = `${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}`;
      const ocupada = this.citas.some(
        (c) =>
          c.medicoId === medicoId &&
          c.fecha === fecha &&
          c.hora === hora &&
          c.estado !== "cancelada"
      );
      slots.push({ hora, ocupada });
      m += cfg.intervaloMin;
      h += Math.floor(m / 60);
      m = m % 60;
    }
    return slots;
  },

  diaClave(fechaISO) {
    const d = new Date(fechaISO + "T12:00:00");
    return ["dom", "lun", "mar", "mie", "jue", "vie", "sab"][d.getDay()];
  }
};
