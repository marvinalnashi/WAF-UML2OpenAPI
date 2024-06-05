import {Component, EventEmitter, Input, Output} from '@angular/core';
import {GenerationService} from "../generation.service";
import {MatStepper} from "@angular/material/stepper";

/**
 * Component for uploading UML diagrams.
 */
@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [],
  templateUrl: './upload.component.html',
  styleUrl: './upload.component.scss'
})
export class UploadComponent {
  /**
   * Output property for processing the UML diagram file that the user selects to upload.
   */
  @Output() fileSelected = new EventEmitter<File>();

  /**
   * Input property for the navigation between steps in the stepper.
   */
  @Input() stepper!: MatStepper;

  /**
   * Creates an instance of UploadComponent.
   * @param generationService The Generation service.
   */
  constructor(private generationService: GenerationService) { }

  /**
   * Processes the UML diagram file that the user selects to upload.
   * @param event The event in which the user selects a file to upload.
   */
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) {
      return;
    }
    this.fileSelected.emit(input.files[0]);
  }
}
