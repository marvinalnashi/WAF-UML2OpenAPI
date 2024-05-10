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
      endpoints: this.fb.array([])
    });
  }

  get endpoints(): FormArray {
    return this.mappingsForm.get('endpoints') as FormArray;
  }

  drop(event: CdkDragDrop<string[]>): void {
    moveItemInArray(this.endpoints.value, event.previousIndex, event.currentIndex);
    this.endpoints.updateValueAndValidity();
  }

  addEndpointMapping(): void {
    this.endpoints.push(this.fb.group({
      className: ['', Validators.required],
      url: ['', Validators.required],
      method: ['', Validators.required]
    }));
  }

  applyMappings(): void {
    this.generationService.applyMappings(this.mappingsForm.value).subscribe(
      response => {
        console.log('Mappings applied successfully', response);
        this.mappingCompleted.emit(true); // Emit success to move to the next step
      },
      error => {
        console.error('Failed to apply mappings', error);
        this.mappingCompleted.emit(false); // Emit failure and handle accordingly
      }
    );
  }
}
