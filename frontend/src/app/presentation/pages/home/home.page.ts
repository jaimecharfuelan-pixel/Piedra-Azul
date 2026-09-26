import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="home">
      <h1>PiedraAzul</h1>
      <p>Frontend Angular con arquitectura hexagonal.</p>
      <a routerLink="/calendario">Ir a la agenda (FullCalendar)</a>
    </section>
  `,
  styles: `
    .home {
      max-width: 640px;
      margin: 3rem auto;
      padding: 1.5rem;
      font-family: Georgia, 'Times New Roman', serif;

      h1 { font-size: 2.5rem; margin-bottom: 0.5rem; }
      p { color: #444; }
      a { color: #0b3d91; }
    }
  `,
})
export class HomePageComponent {}
