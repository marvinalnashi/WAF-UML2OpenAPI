import {Component, OnInit} from '@angular/core';
import { GenerationComponent } from './generation/generation.component';
import {NgxSpinnerComponent, NgxSpinnerService} from "ngx-spinner";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [GenerationComponent, NgxSpinnerComponent],
  templateUrl: './app.component.html',
})
export class AppComponent implements OnInit {
  constructor(private spinner: NgxSpinnerService) {
  }

  ngOnInit() {
    this.showSpinner();
  }

  showSpinner(): void {
    this.spinner.show(undefined, {
      type: 'ball-spin-clockwise-fade-rotating',
      size: 'medium',
      bdColor: 'rgba(0, 0, 0, 0.8)',
      color: '#fff',
      fullScreen: true
    });

    setTimeout(() => {
      this.spinner.hide();
    }, 3000);
  }
}
