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
  constructor(
    public dialogRef: MatDialogRef<RenameDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any) {}

  onNoClick(): void {
    this.dialogRef.close();
  }
}
