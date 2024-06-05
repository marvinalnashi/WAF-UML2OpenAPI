import {Component, Inject} from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogActions, MatDialogClose,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from "@angular/material/dialog";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {FormsModule} from "@angular/forms";
import {MatButton} from "@angular/material/button";

/**
 * Component for renaming elements of the uploaded UML diagram and elements added in the Add Elements tab of the Mapping step of the stepper and of the generated example values in the Personalise step of the stepper through the Rename popup dialog.
 */
@Component({
  selector: 'app-rename-dialog',
  standalone: true,
  imports: [
    MatLabel,
    MatFormField,
    MatDialogContent,
    MatDialogTitle,
    MatInput,
    FormsModule,
    MatDialogActions,
    MatButton,
    MatDialogClose
  ],
  templateUrl: './rename-dialog.component.html',
  styleUrl: './rename-dialog.component.scss'
})
export class RenameDialogComponent {
  /**
   * Creates an instance of RenameDialogComponent.
   * @param dialogRef Reference to the activated popup dialog.
   * @param data Data passed to the activated popup dialog.
   */
  constructor(
    public dialogRef: MatDialogRef<RenameDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any) {}

  /**
   * Closes the popup dialog without applying any modifications.
   */
  onNoClick(): void {
    this.dialogRef.close();
  }
}
