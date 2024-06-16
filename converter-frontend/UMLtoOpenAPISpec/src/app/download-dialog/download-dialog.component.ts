import {Component, Inject} from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from "@angular/material/dialog";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatOption, MatSelect} from "@angular/material/select";
import {MatButton} from "@angular/material/button";

/**
 * Component for selecting whether to download the generated OpenAPI specification in YML or JSON format through the Download popup dialog.
 */
@Component({
  selector: 'app-download-dialog',
  standalone: true,
  imports: [
    MatDialogTitle,
    MatDialogContent,
    MatFormField,
    MatSelect,
    MatOption,
    MatDialogActions,
    MatButton,
    MatLabel
  ],
  templateUrl: './download-dialog.component.html',
  styleUrl: './download-dialog.component.scss'
})
export class DownloadDialogComponent {
  selectedFormat: string = 'YML';

  /**
   * Creates an instance of DownloadDialogComponent.
   */
  constructor(
    public dialogRef: MatDialogRef<DownloadDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}

  /**
   * Closes the Download dialog and initiates the download operation for the generated OpenAPI specification in the format selected by the user.
   */
  onDownloadClick(): void {
    this.dialogRef.close(this.selectedFormat);
  }

  /**
   * Closes the Download dialog without initiating any other operations.
   */
  onCancelClick(): void {
    this.dialogRef.close();
  }
}
