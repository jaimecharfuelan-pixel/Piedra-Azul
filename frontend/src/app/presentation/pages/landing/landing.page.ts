import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PzMarcaPiedrazulComponent } from '../../components/atoms/pz-marca-piedrazul.component';
import { PzMarcaSystemicmindsComponent } from '../../components/atoms/pz-marca-systemicminds.component';

/**
 * Landing pública de Piedrazul: presentación clínica + CTAs a login.
 */
@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [RouterLink, PzMarcaPiedrazulComponent, PzMarcaSystemicmindsComponent],
  templateUrl: './landing.page.html',
  styleUrl: './landing.page.scss',
})
export class LandingPageComponent {
  readonly anio = new Date().getFullYear();
}
