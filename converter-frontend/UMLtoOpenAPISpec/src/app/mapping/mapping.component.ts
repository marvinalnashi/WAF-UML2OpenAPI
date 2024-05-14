import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CdkDrag, CdkDragDrop, CdkDropList, moveItemInArray} from "@angular/cdk/drag-drop";
import {FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {GenerationService} from "../generation.service";
import {MatButton, MatIconButton} from "@angular/material/button";
import {NgForOf, NgIf} from "@angular/common";
import {MatStepper} from "@angular/material/stepper";
import {MatDialog} from "@angular/material/dialog";
import {RenameDialogComponent} from "../rename-dialog/rename-dialog.component";
import {MatIcon} from "@angular/material/icon";

@Component({
  selector: 'app-mapping',
  standalone: true,
  imports: [
    MatButton,
    ReactiveFormsModule,
    CdkDropList,
    NgForOf,
    CdkDrag,
    NgIf,
    MatIcon,
    MatIconButton
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

  ngOnInit(): void {
    this.loadUMLData();
  }

  loadUMLData(): void {
    if (this.file) {
      this.generationService.parseDiagramElements(this.file).subscribe(data => {
        this.umlData = data;
      });
    }
  }

  get mappings(): FormArray {
    return this.mappingsForm.get('mappings') as FormArray;
  }

  addMapping(): void {
    this.mappings.push(this.fb.group({
      className: ['', Validators.required],
      url: ['', Validators.required],
      method: ['', Validators.required]
    }));
  }

  applyMappings(): void {
    if (this.mappingsForm.valid) {
      this.generationService.applyMappings(this.mappingsForm.value.mappings).subscribe(() => {
        this.mappingCompleted.emit(true);
      });
    }
  }

  deleteElement(type: string, name: string, index: number, className: string): void {
    this.generationService.deleteElement(type, name).subscribe(() => {
      if (type === 'attribute') {
        this.umlData.attributes[className].splice(index, 1);
      } else if (type === 'method') {
        this.umlData.methods[className].splice(index, 1);
      } else if (type === 'class') {
        this.umlData.classes = this.umlData.classes.filter((c: string) => c !== className);
        delete this.umlData.attributes[className];
        delete this.umlData.methods[className];
      }
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
        if (type === 'class') {
          const index = this.umlData.classes.indexOf(oldName);
          if (index !== -1) {
            this.umlData.classes[index] = newName;
          }
          this.umlData.attributes[newName] = [...this.umlData.attributes[oldName]];
          delete this.umlData.attributes[oldName];
          this.umlData.methods[newName] = [...this.umlData.methods[oldName]];
          delete this.umlData.methods[oldName];
        } else if (type === 'attribute') {
          const classForAttribute = this.getClassForAttribute(oldName);
          const attrIndex = this.umlData.attributes[classForAttribute].indexOf(oldName);
          if (attrIndex !== -1) {
            this.umlData.attributes[classForAttribute][attrIndex] = newName;
          }
        } else if (type === 'method') {
          const classForMethod = this.getClassForMethod(oldName);
          const methIndex = this.umlData.methods[classForMethod].indexOf(oldName);
          if (methIndex !== -1) {
            this.umlData.methods[classForMethod][methIndex] = newName;
          }
        }
      },
      error: error => console.error('Failed to rename element:', error)
    });
  }

  getClassForAttribute(attributeName: string): string {
    return <string>Object.keys(this.umlData.attributes).find(className =>
      this.umlData.attributes[className].includes(attributeName)
    );
  }

  getClassForMethod(methodName: string): string {
    return <string>Object.keys(this.umlData.methods).find(className =>
      this.umlData.methods[className].includes(methodName)
    );
  }

  maxAttributeRows(data: any): number[] {
    let maxRows = 0;
    for (const className of data.classes) {
      const attributeCount = data.attributes[className]?.length || 0;
      if (attributeCount > maxRows) {
        maxRows = attributeCount;
      }
    }
    return [...Array(maxRows).keys()];
  }

  maxMethodRows(data: any): number[] {
    let maxRows = 0;
    for (const className of data.classes) {
      const methodCount = data.methods[className]?.length || 0;
      if (methodCount > maxRows) {
        maxRows = methodCount;
      }
    }
    return [...Array(maxRows).keys()];
  }
}
