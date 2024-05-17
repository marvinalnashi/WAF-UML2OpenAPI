import {Component, OnInit} from '@angular/core';
import {AbstractControl, FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {GenerationService} from "../generation.service";
import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-data',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgForOf
  ],
  templateUrl: './data.component.html',
  styleUrl: './data.component.scss'
})
export class DataComponent implements OnInit {
  dataForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private generationService: GenerationService
  ) {
    this.dataForm = this.fb.group({
      classes: this.fb.array([])
    });
  }

  ngOnInit(): void {
    this.loadClasses();
  }

  get classes(): FormArray {
    return this.dataForm.get('classes') as FormArray;
  }

  loadClasses(): void {
    this.generationService.getUMLData().subscribe(umlData => {
      const classes = umlData.classes;
      classes.forEach((className: string) => {
        const classGroup = this.fb.group({
          className: [className, Validators.required],
          attributes: this.fb.array([])
        });
        this.classes.push(classGroup);
        this.loadAttributes(classGroup, umlData.attributes[className]);
      });
    });
  }

  loadAttributes(classGroup: FormGroup, attributes: string[]): void {
    const attributesArray = classGroup.get('attributes') as FormArray;
    attributes.forEach(attribute => {
      attributesArray.push(this.fb.group({
        attributeName: [attribute, Validators.required],
        values: this.fb.array([])
      }));
    });
  }

  addValue(attributeGroup: AbstractControl<any>): void {
    const valuesArray = attributeGroup.get('values') as FormArray;
    valuesArray.push(this.fb.control('', Validators.required));
  }

  submitData(): void {
    if (this.dataForm.valid) {
      this.generationService.addDummyData(this.dataForm.value).subscribe(response => {
        alert('Dummy data added successfully');
      });
    }
  }

  getAttributes(classGroup: AbstractControl): FormArray {
    return classGroup.get('attributes') as FormArray;
  }

  getValues(attributeGroup: AbstractControl): FormArray {
    return attributeGroup.get('values') as FormArray;
  }
}
