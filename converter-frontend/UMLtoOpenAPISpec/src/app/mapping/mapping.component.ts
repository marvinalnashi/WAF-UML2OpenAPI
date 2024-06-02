import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {GenerationService} from "../generation.service";
import {MatDialog} from "@angular/material/dialog";
import {AddElementDialogComponent} from "../add-element-dialog/add-element-dialog.component";
import {NgClass, NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-mapping',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    NgClass,
    ReactiveFormsModule
  ],
  templateUrl: './mapping.component.html',
  styleUrl: './mapping.component.scss'
})
export class MappingComponent implements OnInit {
  @Input() file: File | undefined;
  @Input() umlData: any;
  @Output() mappingCompleted = new EventEmitter<boolean>();
  @Output() httpMethodsSelected = new EventEmitter<{ [className: string]: { [method: string]: boolean } }>();
  mappingsForm: FormGroup;
  selectedHttpMethods: { [className: string]: { [method: string]: boolean } } = {};
  selectedTab: string = 'add-elements';
  showApplyAdditionsButton: boolean = false;

  constructor(
    private fb: FormBuilder,
    private generationService: GenerationService,
    public dialog: MatDialog
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
        this.initHttpMethodSelection();
      });
    }
  }

  get mappings(): FormArray {
    return this.mappingsForm.get('mappings') as FormArray;
  }

  addMapping(): void {
    const newMapping = this.fb.group({
      className: ['', Validators.required],
      url: ['', Validators.required],
      methods: this.fb.array([]),
      attributes: this.fb.array([])
    });
    this.mappings.push(newMapping);
    this.showApplyAdditionsButton = true;
  }

  openAddElementDialog(isMethod: boolean, mappingIndex: number): void {
    const dialogRef = this.dialog.open(AddElementDialogComponent, {
      width: '400px',
      data: { isMethod }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const mapping = this.mappings.at(mappingIndex) as FormGroup;
        if (isMethod) {
          (mapping.get('methods') as FormArray).push(this.fb.control(result));
        } else {
          (mapping.get('attributes') as FormArray).push(this.fb.control(result));
        }
      }
    });
  }

  addMethod(mappingIndex: number): void {
    this.openAddElementDialog(true, mappingIndex);
  }

  addAttribute(mappingIndex: number): void {
    this.openAddElementDialog(false, mappingIndex);
  }

  applyMappings(): void {
    if (this.mappingsForm.valid) {
      this.generationService.applyMappings(this.mappingsForm.value.mappings).subscribe(() => {
        this.mappingCompleted.emit(true);
        this.httpMethodsSelected.emit(this.selectedHttpMethods);
      });
    }
  }

  addNewClassToElements(): void {
    const newElements = this.mappingsForm.value.mappings.map((mapping: any) => ({
      className: mapping.className,
      attributes: mapping.attributes,
      methods: mapping.methods
    }));

    newElements.forEach((element: any) => {
      this.generationService.addNewElement(element).subscribe(() => {
        this.umlData.classes.push(element.className);
        this.umlData.attributes[element.className] = element.attributes;
        this.umlData.methods[element.className] = element.methods;
        this.selectedHttpMethods[element.className] = {
          'GET/{id}': false,
          'POST': false,
          'PUT/{id}': false,
          'DELETE/{id}': false
        };
      });
    });

    this.mappingsForm.reset();
    while (this.mappings.length) {
      this.mappings.removeAt(0);
    }
    this.showApplyAdditionsButton = false;
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

  maxHttpMethodRows(data: any): number[] {
    return [0, 1, 2, 3];
  }

  getHttpMethodsForClass(className: string) {
    return [
      { url: `/${className.toLowerCase()}/{id}`, method: 'GET' },
      { url: `/${className.toLowerCase()}`, method: 'POST' },
      { url: `/${className.toLowerCase()}/{id}`, method: 'PUT' },
      { url: `/${className.toLowerCase()}/{id}`, method: 'DELETE' }
    ];
  }

  initHttpMethodSelection() {
    this.umlData.classes.forEach((className: string) => {
      this.selectedHttpMethods[className] = {
        'GET/{id}': false,
        'POST': false,
        'PUT/{id}': false,
        'DELETE/{id}': false
      };
    });
  }

  toggleHttpMethodSelection(className: string, method: string) {
    if (!this.selectedHttpMethods[className]) {
      this.selectedHttpMethods[className] = {};
    }
    this.selectedHttpMethods[className][method] = !this.selectedHttpMethods[className][method];
  }
}
