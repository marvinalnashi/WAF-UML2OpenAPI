import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatIcon} from "@angular/material/icon";
import {MatIconButton} from "@angular/material/button";
import {NgIf} from "@angular/common";
import {MatDialog} from "@angular/material/dialog";
import {DownloadDialogComponent} from "../download-dialog/download-dialog.component";
import {HttpClient} from "@angular/common/http";
import * as yaml from 'js-yaml';
import {NotificationService} from "../notification.service";

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
  styleUrl: './manage.component.scss',
  providers: [NotificationService]
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

  constructor(public dialog: MatDialog, private http: HttpClient, private notificationService: NotificationService) {}

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
   * Opens the popup dialog that contains a dropdown menu in which the user can choose between downloading the generated OpenAPI specification in YML or JSON format.
   */
  onDownloadOpenApiSpecification(): void {
    const dialogRef = this.dialog.open(DownloadDialogComponent, {
      width: '250px',
      data: {}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.downloadSpecification(result);
        this.notificationService.showSuccess('The generated OpenAPI specification has been downloaded successfully.');
      }
    });
  }

  /**
   * Downloads the generated OpenAPI specification file in the format the user has selected in the dropdown menu of the download popup dialog.
   */
  downloadSpecification(format: string): void {
    this.http.get('http://localhost:8080/export.yml', { responseType: 'text' }).subscribe({
      next: (ymlContent: string) => {
        if (format === 'YML') {
          this.downloadFile(ymlContent, 'application/x-yaml', 'export.yml');
        } else if (format === 'JSON') {
          const jsonContent = yaml.load(ymlContent);
          this.downloadFile(JSON.stringify(jsonContent, null, 2), 'application/json', 'export.json');
        }
      },
      error: (err: any) => {
        console.error('Error downloading the generated OpenAPI specification file', err);
      }
    });
  }

  /**
   * Prepares the generated OpenAPI specification file in the format the user has selected in the dropdown menu of the download popup dialog.
   */
  downloadFile(content: string, contentType: string, fileName: string): void {
    const blob = new Blob([content], { type: contentType });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }
}
