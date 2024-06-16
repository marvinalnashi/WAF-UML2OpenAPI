import {Component, EventEmitter, Input, Output} from '@angular/core';
import {GenerationService} from "../generation.service";
import {MatStepper} from "@angular/material/stepper";
import {NgIf} from "@angular/common";
import {NotificationService} from "../notification.service";

/**
 * Component for uploading UML diagrams.
 */
@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [
    NgIf
  ],
  templateUrl: './upload.component.html',
  styleUrl: './upload.component.scss',
  providers: [NotificationService]
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
   * Indicates whether a file has been selected.
   */
  isFileSelected = false;

  /**
   * Creates an instance of UploadComponent.
   * @param generationService The Generation service.
   * @param notificationService The Notification service.
   */
  constructor(
    private generationService: GenerationService,
    private notificationService: NotificationService
  ) { }

  /**
   * Processes the UML diagram file that the user selects to upload.
   * @param event The event in which the user selects a file to upload.
   */
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) {
      return;
    }
    const file = input.files[0];
    const fileExtension = file.name.split('.').pop()?.toLowerCase();
    const supportedFormats = ['uxf', 'xml', 'mdj', 'puml'];
    this.isFileSelected = supportedFormats.includes(fileExtension || '');
    if (this.isFileSelected) {
      this.fileSelected.emit(file);
      this.notificationService.showSuccess('The UML diagram file is in a supported format and has been uploaded successfully.');
    } else {
      console.log("Unsupported file format: " + fileExtension);
      this.notificationService.showError('The UML diagram file could not be uploaded because it is in an unsupported format.');
    }
  }
}
