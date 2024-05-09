import { Component } from '@angular/core';
import { GenerationComponent } from './generation/generation.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [GenerationComponent],
  templateUrl: './app.component.html',
})
export class AppComponent {
}
