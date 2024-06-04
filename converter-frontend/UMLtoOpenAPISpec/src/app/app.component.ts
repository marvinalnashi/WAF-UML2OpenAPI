import {Component, OnInit} from '@angular/core';
import { StepperComponent } from './stepper/stepper.component';
import {NgIf} from "@angular/common";
import {LoaderComponent} from "./loader/loader.component";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [StepperComponent, NgIf, LoaderComponent],
  templateUrl: './app.component.html',
})
export class AppComponent implements OnInit {
  isLoading = true;

  ngOnInit() {
    setTimeout(() => {
      this.isLoading = false;
    }, 2000);
  }
}
