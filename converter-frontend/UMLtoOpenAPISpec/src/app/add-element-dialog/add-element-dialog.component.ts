import {Component, Inject} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from "@angular/material/dialog";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatOption, MatSelect} from "@angular/material/select";
import {MatInput} from "@angular/material/input";
import {NgForOf, NgIf} from "@angular/common";
import {MatButton} from "@angular/material/button";

/**
 * Component for adding attributes and methods in the Add Elements tab of the Mapping step of the stepper through the Add Element popup dialog.
 */
@Component({
  selector: 'app-add-element-dialog',
  standalone: true,
  imports: [
    MatFormField,
    MatLabel,
    MatSelect,
    MatOption,
    ReactiveFormsModule,
    MatDialogContent,
    MatDialogTitle,
    MatInput,
    NgIf,
    NgForOf,
    MatDialogActions,
    MatButton
  ],
  templateUrl: './add-element-dialog.component.html',
  styleUrl: './add-element-dialog.component.scss'
})
export class AddElementDialogComponent {
  /**
   * Form group that is used for the form in the popup dialog in the Add Elements tab of the Mapping step of the stepper.
   */
  elementForm: FormGroup;

  /**
   * Boolean indicating if the element that is being added is a method.
   */
  isMethod: boolean;

  /**
   * Array of data types that can be selected in the Add Element form.
   */
  dataTypes: string[] = ['int', 'String', 'void', 'long', 'float', 'double', 'boolean', 'char', 'byte', 'short'];

  /**
   * Creates an instance of AddElementDialogComponent.
   * @param dialogRef Reference to the activated popup dialog.
   * @param data Data passed to the activated popup dialog.
   * @param fb The Form builder service.
   */
  constructor(
    public dialogRef: MatDialogRef<AddElementDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: FormBuilder
  ) {
    this.isMethod = data.isMethod;
    this.elementForm = this.fb.group({
      accessModifier: ['public', Validators.required],
      name: ['', Validators.required],
      dataType: ['', Validators.required],
      parameters: ['']
    });
  }

  /**
   * Closes the popup dialog without applying any additions.
   */
  onNoClick(): void {
    this.dialogRef.close();
  }

  /**
   * Validates the input of the Add Element form and submits it to the application.
   */
  onSubmit(): void {
    if (this.elementForm.valid) {
      const { accessModifier, name, dataType, parameters } = this.elementForm.value;
      const symbol = accessModifier === 'public' ? '+' : accessModifier === 'private' ? '-' : '#';
      const element = this.isMethod
        ? `${symbol}${name}(${parameters}) : ${dataType}`
        : `${symbol}${name} : ${dataType}`;
      this.dialogRef.close(element);
    }
  }
}
