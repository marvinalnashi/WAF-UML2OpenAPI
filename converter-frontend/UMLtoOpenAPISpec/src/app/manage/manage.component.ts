import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatIcon} from "@angular/material/icon";
import {MatIconButton} from "@angular/material/button";
import {NgIf} from "@angular/common";

/**
 * Component for performing managing tasks with the generated OpenAPI specification.
 */
@Component({
  selector: 'app-manage',
  standalone: true,
  imports: [
    MatIcon,
    MatIconButton,
    NgIf
  ],
  templateUrl: './manage.component.html',
  styleUrl: './manage.component.scss'
})
export class ManageComponent {
  /**
   * Input property that indicates whether an OpenAPI specification was successfully generated.
   */
  @Input() isGeneratedSuccessfully = false;

  /**
   * Output property for emitting the event that starts the Prism mock server.
   */
  @Output() toggleMockServer = new EventEmitter<void>();

  /**
   * Output property for emitting the event that opens Swagger UI and loads the generated OpenAPI specification in it.
   */
  @Output() openSwaggerUI = new EventEmitter<void>();

  /**
   * Output property for emitting the event that saves the data of the current session, consisting of the uploaded UML diagram and the generated OpenAPI specification, and restarts the stepper.
   */
  @Output() restartApplication = new EventEmitter<void>();

  /**
   * Output property for emitting the event that downloads a generated OpenAPI specification.
   */
  @Output() downloadOpenApiSpecification = new EventEmitter<void>();

  /**
   * Emits the event for starting or restarting the Prism mock server.
   */
  onToggleMockServer(): void {
    this.toggleMockServer.emit();
  }

  /**
   * Emits the event for opening Swagger UI in a new tab and automatically loading the generated OpenAPI specification in it.
   */
  onOpenSwaggerUI(): void {
    this.openSwaggerUI.emit();
  }

  /**
   * Emits the event for saving the session data to the database and restarting the stepper.
   */
  onRestartApplication(): void {
    this.restartApplication.emit();
  }

  /**
   * Emits the event for downloading the OpenAPI specification that was generated in the current session.
   */
  onDownloadOpenApiSpecification(): void {
    this.downloadOpenApiSpecification.emit();
  }
}
