import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CdkDrag, CdkDragDrop, CdkDropList, moveItemInArray} from "@angular/cdk/drag-drop";
import {FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {GenerationService} from "../generation.service";
import {MatButton} from "@angular/material/button";
import {NgForOf, NgIf} from "@angular/common";
import {MatStepper} from "@angular/material/stepper";
import {MatDialog} from "@angular/material/dialog";
import {RenameDialogComponent} from "../rename-dialog/rename-dialog.component";

@Component({
  selector: 'app-mapping',
  standalone: true,
  imports: [
    MatButton,
    ReactiveFormsModule,
    CdkDropList,
    NgForOf,
    CdkDrag,
    NgIf
  ],
  templateUrl: './mapping.component.html',
  styleUrl: './mapping.component.scss'
})
export class MappingComponent implements OnInit {
  @Input() file: File | undefined;
  @Input() umlData: any;
  @Output() mappingCompleted = new EventEmitter<boolean>();
  mappingsForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private dialog: MatDialog,
    private generationService: GenerationService
  ) {
    this.mappingsForm = this.fb.group({
      mappings: this.fb.array([])
    });
  }

  ngOnInit() {
    this.loadUMLData();
  }

  loadUMLData() {
    if (this.file) {
      this.generationService.parseDiagramElements(this.file).subscribe({
        next: (data) => {
          this.umlData = {
            classes: Object.keys(data),
            attributes: data,
            methods: data
          };
          console.log('UML Data fetched:', this.umlData);
        },
        error: (error) => console.error('Failed to fetch UML data:', error)
      });
    }
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

  maxRowsArray(data: any): number[] {
    let maxRows = 0;
    for (const className of data.classes) {
      const totalRows = (data.attributes[className]?.length || 0) + (data.methods[className]?.length || 0);
      if (totalRows > maxRows) {
        maxRows = totalRows;
      }
    }
    return [...Array(maxRows).keys()];
  }

  deleteElement(type: string, name: string): void {
    this.generationService.deleteElement(type, name).subscribe({
      next: () => {
        console.log('Element deleted');
        this.loadUMLData();
      },
      error: error => console.error('Failed to delete element:', error)
    });
  }

  openRenameDialog(type: string, oldName: string): void {
    let newName = prompt("Enter new name for " + oldName);
    if (newName) {
      this.renameElement(type, oldName, newName);
    }
  }

  renameElement(type: string, oldName: string, newName: string): void {
    this.generationService.renameElement(type, oldName, newName).subscribe({
      next: () => {
        alert('Element renamed');
        this.loadUMLData();
      },
      error: error => console.error('Failed to rename element:', error)
    });
  }
}
