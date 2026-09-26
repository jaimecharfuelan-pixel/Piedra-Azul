import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: `<router-outlet />`,
  styles: `
    :host {
      display: block;
      min-height: 100vh;
      background: linear-gradient(180deg, #eef3f8 0%, #f7f9fc 40%, #ffffff 100%);
    }
  `,
})
export class AppComponent {}
