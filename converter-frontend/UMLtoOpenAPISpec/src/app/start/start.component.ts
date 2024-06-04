import {Component, Input} from '@angular/core';
import {MatStepper} from "@angular/material/stepper";

@Component({
  selector: 'app-start',
  standalone: true,
  imports: [],
  templateUrl: './start.component.html',
  styleUrl: './start.component.scss'
})
export class StartComponent {
  @Input() stepper!: MatStepper;

  constructor() {}
}
