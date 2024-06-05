import {Component, EventEmitter, Input, Output} from '@angular/core';
import {NgIf} from "@angular/common";
import {MatStepper} from "@angular/material/stepper";

/**
 * Component for generating an OpenAPI specification from a UML diagram.
 */
@Component({
  selector: 'app-generate',
  standalone: true,
  imports: [
    NgIf
  ],
  templateUrl: './generate.component.html',
  styleUrl: './generate.component.scss'
})
export class GenerateComponent {
  /**
   * Input property for processing the uploaded UML diagram file.
   */
  @Input() uploadedFile: File | null = null;

  /**
   * Input property for identifying the file format of the uploaded UML diagram file.
   */
  @Input() fileFormat: string = '';

  /**
   * Input property for identifying the modelling tool that is used to create the uploaded UML diagram.
   */
  @Input() modellingTool: string = '';

  /**
   * Input property for identifying the type of UML diagram that has been uploaded.
   */
  @Input() diagramType: string = 'Class Diagram';

  /**
   * Input property for counting the amounts of elements in the uploaded UML diagram.
   */
  @Input() elementCount: any = { classes: 0, attributes: 0, methods: 0, relationships: 0 };

  /**
   * Input property for counting the amounts of elements after the Mapping step of the stepper
   */
  @Input() mappedElementCount: any = { classes: 0, attributes: 0, methods: 0, relationships: 0 };

  /**
   * Input property that indicates whether the generation was successful.
   */
  @Input() isGeneratedSuccessfully = false;

  /**
   * Input property that indicates whether the loader overlay that contains the spinner needs to be activated.
   */
  @Input() isLoading = false;

  /**
   * Input property for the navigation between steps in the stepper.
   */
  @Input() stepper!: MatStepper;

  /**
   * Output property for emitting the event for generating an OpenAPI specification.
   */
  @Output() generate = new EventEmitter<void>();


  constructor() {}

  /**
   * Emits the event for generating an OpenAPI specification.
   */
  onGenerateClick(): void {
    this.generate.emit();
  }
}
