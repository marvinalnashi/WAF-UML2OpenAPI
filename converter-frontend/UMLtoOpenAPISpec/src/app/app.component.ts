import {Component, OnInit} from '@angular/core';
import { StepperComponent } from './stepper/stepper.component';
import {NgIf} from "@angular/common";
import {LoaderComponent} from "./loader/loader.component";

/**
 * The main component of the application.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [StepperComponent, NgIf, LoaderComponent],
  templateUrl: './app.component.html',
})
export class AppComponent implements OnInit {
  /**
   * Indicates whether the loader overlay that contains the spinner needs to be activated.
   */
  isLoading = true;

  /**
   * The index of the string that is currently being displayed in the loader/spinner overlay.
   */
  ngOnInit() {
    setTimeout(() => {
      this.isLoading = false;
    }, 2000);
  }
}
