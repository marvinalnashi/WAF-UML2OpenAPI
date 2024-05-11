import {Component, EventEmitter, Output, ViewChild} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CdkDrag, CdkDragDrop, CdkDropList, moveItemInArray} from "@angular/cdk/drag-drop";
import {FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {GenerationService} from "../generation.service";
import {MatButton} from "@angular/material/button";
import {NgForOf} from "@angular/common";
import {MatStepper} from "@angular/material/stepper";

@Component({
  selector: 'app-mapping',
  standalone: true,
  imports: [
    MatButton,
    ReactiveFormsModule,
    CdkDropList,
    NgForOf,
    CdkDrag
  ],
  templateUrl: './mapping.component.html',
  styleUrl: './mapping.component.scss'
})
export class MappingComponent {
  @Output() mappingCompleted = new EventEmitter<boolean>();
  mappingsForm: FormGroup;

  constructor(private fb: FormBuilder, private generationService: GenerationService) {
    this.mappingsForm = this.fb.group({
      mappings: this.fb.array([])
    });
  }

  get mappings(): FormArray {
    return this.mappingsForm.get('mappings') as FormArray;
  }

  addMapping() {
    this.mappings.push(this.fb.group({
      className: ['', Validators.required],
      url: ['', Validators.required],
      method: ['', Validators.required]
    }));
  }

  applyMappings() {
    if (this.mappingsForm.valid) {
      this.generationService.applyMappings(this.mappingsForm.value.mappings).subscribe({
        next: () => {
          alert('Mappings applied successfully');
          this.mappingCompleted.emit(true);
        },
        error: () => {
          alert('Failed to apply mappings');
          this.mappingCompleted.emit(false);
        }
      });
    } else {
      alert('Please fill in all required fields');
    }
  }

  drop(event: CdkDragDrop<string[]>) {
    moveItemInArray(this.mappings.controls, event.previousIndex, event.currentIndex);
  }
}
