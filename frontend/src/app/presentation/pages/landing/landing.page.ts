import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/**
 * Landing pública de Piedrazul: presentación clínica + CTAs a login.
 */
@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './landing.page.html',
  styleUrl: './landing.page.scss',
})
export class LandingPageComponent {}
