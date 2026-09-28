import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { RegistrarPacienteAuthUseCase } from '../../../application/use-cases/registrar-paciente-auth.use-case';
import { aErrorDominio } from '../../../infrastructure/http/error-dominio.mapper';
import { UiAlertComponent } from '../../components/atoms/ui-alert.component';
import { UiButtonComponent } from '../../components/atoms/ui-button.component';
import { PzMarcaPiedrazulComponent } from '../../components/atoms/pz-marca-piedrazul.component';
import { PzMarcaSystemicmindsComponent } from '../../components/atoms/pz-marca-systemicminds.component';
import { UiFieldComponent } from '../../components/molecules/ui-field.component';

type PasoRegistro = 1 | 2 | 3;

@Component({
  selector: 'app-registro-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    UiButtonComponent,
    UiFieldComponent,
    UiAlertComponent,
    PzMarcaPiedrazulComponent,
    PzMarcaSystemicmindsComponent,
  ],
  templateUrl: './registro.page.html',
  styleUrl: './registro.page.scss',
})
export class RegistroPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly registrar = inject(RegistrarPacienteAuthUseCase);
  private readonly router = inject(Router);

  readonly paso = signal<PasoRegistro>(1);
  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);
  readonly exito = signal(false);
  readonly anio = new Date().getFullYear();

  readonly cuenta = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(80)]],
    password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(100)]],
    confirmar: ['', [Validators.required]],
  });

  readonly datos = this.fb.nonNullable.group({
    nombreCompleto: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(180)]],
    telefono: ['', [Validators.required, Validators.pattern(/^\d{7,15}$/)]],
  });

  readonly progreso = computed(() => (this.paso() / 3) * 100);

  siguienteDesdeCuenta(): void {
    this.error.set(null);
    if (this.cuenta.invalid) {
      this.cuenta.markAllAsTouched();
      return;
    }
    const { password, confirmar } = this.cuenta.getRawValue();
    if (password !== confirmar) {
      this.error.set('Las contraseñas no coinciden.');
      return;
    }
    this.paso.set(2);
  }

  volver(): void {
    this.error.set(null);
    this.paso.update((p) => (p > 1 ? ((p - 1) as PasoRegistro) : p));
  }

  enviar(): void {
    this.error.set(null);
    if (this.datos.invalid) {
      this.datos.markAllAsTouched();
      return;
    }
    this.cargando.set(true);
    const cuenta = this.cuenta.getRawValue();
    const datos = this.datos.getRawValue();
    this.registrar
      .ejecutar({
        username: cuenta.username,
        password: cuenta.password,
        nombreCompleto: datos.nombreCompleto,
        telefono: datos.telefono.replace(/[\s\-()]/g, ''),
      })
      .subscribe({
        next: () => {
          this.cargando.set(false);
          this.exito.set(true);
          this.paso.set(3);
        },
        error: (err) => {
          this.cargando.set(false);
          this.error.set(aErrorDominio(err).mensaje);
        },
      });
  }

  irALogin(): void {
    void this.router.navigateByUrl('/login');
  }
}
