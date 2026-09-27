import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AutenticarUseCase } from '../../../application/use-cases/autenticar.use-case';
import { rutaPanelPorRol } from '../../../domain/auth/permisos';
import { aErrorDominio } from '../../../infrastructure/http/error-dominio.mapper';
import { UiAlertComponent } from '../../components/atoms/ui-alert.component';
import { UiButtonComponent } from '../../components/atoms/ui-button.component';
import { UiFieldComponent } from '../../components/molecules/ui-field.component';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    UiButtonComponent,
    UiFieldComponent,
    UiAlertComponent,
  ],
  templateUrl: './login.page.html',
  styleUrl: './login.page.scss',
})
export class LoginPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly autenticar = inject(AutenticarUseCase);
  private readonly router = inject(Router);

  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  enviar(): void {
    this.error.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.cargando.set(true);
    const { username, password } = this.form.getRawValue();
    this.autenticar.ejecutar({ username, password }).subscribe({
      next: (sesion) => {
        this.cargando.set(false);
        void this.router.navigateByUrl(rutaPanelPorRol(sesion.rol));
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(aErrorDominio(err).mensaje);
      },
    });
  }
}
