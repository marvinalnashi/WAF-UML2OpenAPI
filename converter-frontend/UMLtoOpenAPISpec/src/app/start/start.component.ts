import {Component, Input} from '@angular/core';
import {MatStepper} from "@angular/material/stepper";

/**
 * Component for displaying the Start step of the stepper.
 */
@Component({
  selector: 'app-start',
  standalone: true,
  imports: [],
  templateUrl: './start.component.html',
  styleUrl: './start.component.scss'
})
export class StartComponent {
  /**
   * Input property for the navigation between steps in the stepper.
   */
  @Input() stepper!: MatStepper;

  constructor() {}
}
