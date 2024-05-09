import { Component } from '@angular/core';
import { GenerationComponent } from './generation/generation.component';
import {MappingComponent} from "./mapping/mapping.component";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [GenerationComponent, MappingComponent],
  templateUrl: './app.component.html',
})
export class AppComponent {
}
