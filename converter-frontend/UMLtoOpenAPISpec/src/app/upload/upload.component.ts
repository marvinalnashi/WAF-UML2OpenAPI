import {Component, EventEmitter, Input, Output} from '@angular/core';
import {GenerationService} from "../generation.service";
import {MatStepper} from "@angular/material/stepper";

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [],
  templateUrl: './upload.component.html',
  styleUrl: './upload.component.scss'
})
export class UploadComponent {
  @Output() fileSelected = new EventEmitter<File>();
  @Input() stepper!: MatStepper;

  constructor(private generationService: GenerationService) { }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) {
      return;
    }
    this.fileSelected.emit(input.files[0]);
  }
}
