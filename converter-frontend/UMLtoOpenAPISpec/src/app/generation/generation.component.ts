import { AfterViewInit, Component, EventEmitter, OnInit, Output, ViewChild, TemplateRef } from '@angular/core';
import { GenerationService } from '../generation.service';
import { MockServerService } from '../mock-server.service';
import { HttpClient } from '@angular/common/http';
import { MatStep, MatStepLabel, MatStepper } from '@angular/material/stepper';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogActions, MatDialogContent, MatDialogTitle } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { MappingComponent } from '../mapping/mapping.component';
import * as yaml from 'js-yaml';
import { MatIcon } from "@angular/material/icon";
import {PersonaliseComponent} from "../personalise/personalise.component";

interface OpenAPIAttribute {
  name: string;
  examples: string[];
}

interface OpenAPIData {
  classes: string[];
  attributes: { [className: string]: OpenAPIAttribute[] };
}

@Component({
  selector: 'app-generation',
  templateUrl: './generation.component.html',
  styleUrls: ['./generation.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatStepper,
    MatInputModule,
    MatButtonModule,
    MatStep,
    MatStepLabel,
    MappingComponent,
    MatIcon,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    PersonaliseComponent
  ],
  providers: [GenerationService, MockServerService]
})
export class GenerationComponent implements AfterViewInit, OnInit {
  @ViewChild('stepper') private stepper!: MatStepper;
  @ViewChild('editDialog', { static: true }) editDialog!: TemplateRef<any>;
  @Output() umlDataEmitter = new EventEmitter<any>();

  uploadedFile: File | null = null;
  isGeneratedSuccessfully = false;
  fileFormat: string = '';
  diagramType: string = 'Class Diagram';
  serverButtonText = 'Start mock server';
  mappingFormGroup: FormGroup;
  umlData: any;
  selectedHttpMethods: { [className: string]: { [method: string]: boolean } } = {};
  elementCount = {
    classes: 0,
    attributes: 0,
    methods: 0,
    relationships: 0
  };
  openAPISpec: OpenAPIData | null = null;
  exampleValues: any = {};

  constructor(
    private fb: FormBuilder,
    private generationService: GenerationService,
    private mockServerService: MockServerService,
    private http: HttpClient,
    public dialog: MatDialog
  ) {
    this.mappingFormGroup = this.fb.group({
      userConfirmation: ['', Validators.required]
    });
  }

  ngAfterViewInit() {
    this.stepper._stepHeader.forEach(header => {
      header._elementRef.nativeElement.style.pointerEvents = 'none';
    });
  }

  ngOnInit(): void {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) {
      return;
    }
    this.uploadedFile = input.files[0];
    this.fileFormat = this.uploadedFile.name.split('.').pop()!;
    this.readFile(this.uploadedFile);
    this.generationService.parseDiagramElements(this.uploadedFile).subscribe({
      next: (data) => {
        this.umlData = data;
        this.emitUmlData(data);
      },
      error: (error) => console.error('Failed to parse diagram', error)
    });
  }

  emitUmlData(data: any): void {
    this.umlDataEmitter.emit(data);
  }

  onHttpMethodsSelected(selectedMethods: { [className: string]: { [method: string]: boolean } }) {
    this.selectedHttpMethods = selectedMethods;
  }

  generate(): void {
    if (this.uploadedFile) {
      const selectedMethods: { [className: string]: string[] } = {};
      for (const className in this.selectedHttpMethods) {
        if (this.selectedHttpMethods.hasOwnProperty(className)) {
          selectedMethods[className] = [];
          for (const method in this.selectedHttpMethods[className]) {
            if (this.selectedHttpMethods[className].hasOwnProperty(method) && this.selectedHttpMethods[className][method]) {
              selectedMethods[className].push(method);
            }
          }
        }
      }

      const formData = new FormData();
      formData.append('file', this.uploadedFile);
      formData.append('selectedHttpMethods', JSON.stringify(selectedMethods));

      this.generationService.generateSpec(formData).subscribe({
        next: (response) => {
          console.log('Generation successful', response);
          this.isGeneratedSuccessfully = true;
          this.fetchOpenAPISpec();
        },
        error: (error) => console.error('Generation failed', error)
      });
    } else {
      console.error('No file selected');
    }
  }

  fetchOpenAPISpec(): void {
    this.http.get('http://localhost:8080/export.yml', { responseType: 'text' }).subscribe((data) => {
      this.openAPISpec = this.parseYAML(data);
      console.log(this.openAPISpec);
    });
  }

  parseYAML(data: string): OpenAPIData | null {
    try {
      const parsedData = yaml.load(data) as any;
      const classes = Object.keys(parsedData.components.schemas);
      const attributes = classes.reduce((acc: any, className: string) => {
        const classAttributes = Object.keys(parsedData.components.schemas[className].properties);
        acc[className] = classAttributes.map(attr => ({
          name: attr,
          examples: parsedData.components.schemas[className].properties[attr].example || []
        }));
        return acc;
      }, {});
      return { classes, attributes };
    } catch (e) {
      console.error('Failed to parse YAML', e);
      return null;
    }
  }

  onExampleValueChange(className: string, attributeName: string, index: number, newValue: string): void {
    if (!this.exampleValues[className]) {
      this.exampleValues[className] = {};
    }
    if (!this.exampleValues[className][attributeName]) {
      this.exampleValues[className][attributeName] = [];
    }
    this.exampleValues[className][attributeName][index] = newValue;
  }

  saveExampleValues(): void {
    this.generationService.updateExampleValues(this.exampleValues).subscribe({
      next: (response) => {
        console.log('Example values updated successfully');
      },
      error: (error) => {
        console.error('Failed to update example values', error);
      }
    });
  }

  openEditDialog(className: string, attributeName: string, index: number, value: string): void {
    const dialogRef = this.dialog.open(this.editDialog, {
      data: { className, attributeName, index, value }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.onExampleValueChange(result.className, result.attributeName, result.index, result.value);
      }
    });
  }

  onDialogCancel(): void {
    this.dialog.closeAll();
  }

  onDialogSave(className: string, attributeName: string, index: number, value: string): void {
    this.dialog.closeAll();
    this.onExampleValueChange(className, attributeName, index, value);
  }

  toggleMockServer(): void {
    this.mockServerService.toggleMockServer().subscribe({
      next: (response) => {
        console.log(response.message);
        this.serverButtonText = response.message.includes('starting') ? 'Restart mock server' : 'Start mock server';
      },
      error: (error) => {
        console.error('Server toggle failed', error);
      }
    });
  }

  restartApplication(): void {
    window.location.reload();
  }

  readFile(file: File): void {
    const reader = new FileReader();
    reader.onload = () => {
      const content = reader.result as string;
      this.elementCount = this.parseContentByFormat(content, this.fileFormat);
    };
    reader.readAsText(file);
  }

  parseContentByFormat(content: string, format: string) {
    switch (format) {
      case 'xml':
        return this.parseDrawioXML(content);
      case 'uxf':
        return this.parseUMLetUXF(content);
      case 'mdj':
        return this.parseMDJ(content);
      case 'puml':
        return this.parsePUML(content);
      default:
        return {};
    }
  }

  parseDrawioXML(content: string): any {
    const parser = new DOMParser();
    const xmlDoc = parser.parseFromString(content, "application/xml");
    let classes = 0, attributes = 0, methods = 0, relationships = 0;

    xmlDoc.querySelectorAll('mxCell').forEach(cell => {
      const value = cell.getAttribute('value');
      const style = cell.getAttribute('style');

      if (style?.includes('swimlane') && value) {
        classes++;
      }

      if (style?.includes('edgeStyle')) {
        relationships++;
      }

      if (style?.includes('text;') && value) {
        const cleanValue = value.replace(/<[^>]*>/g, '');
        if (cleanValue.includes('(') && cleanValue.includes(')')) {
          methods += cleanValue.split('+').filter(line => line.includes('(') && line.includes(')')).length;
        } else {
          attributes += cleanValue.split('+').filter(line => line.includes(':')).length;
        }
      }
    });

    return { classes, attributes, methods, relationships };
  }

  parseUMLetUXF(content: string): any {
    const parser = new DOMParser();
    const xmlDoc = parser.parseFromString(content, "application/xml");
    let classes = 0, attributes = 0, methods = 0, relationships = 0;

    xmlDoc.querySelectorAll('element').forEach(element => {
      if (element.querySelector('id')?.textContent === 'UMLClass') {
        classes++;
        const panelAttributes = element.querySelector('panel_attributes')?.textContent;
        if (panelAttributes) {
          attributes += (panelAttributes.match(/\+\w+\s+:/g) || []).length;
          methods += (panelAttributes.match(/\+\w+\s*\(.*?\)/g) || []).length;
        }
      }
      if (element.querySelector('id')?.textContent === 'Relation') {
        relationships++;
      }
    });

    return { classes, attributes, methods, relationships };
  }

  parseMDJ(content: string): any {
    const json = JSON.parse(content);
    let classes = 0, attributes = 0, methods = 0, relationships = 0;
    const iterateElements = (element: { _type: string; attributes: string | any[]; operations: string | any[]; ownedElements: any[]; }) => {
      if (element._type === 'UMLClass') {
        classes++;
        if (element.attributes) {
          attributes += element.attributes.length;
        }
        if (element.operations) {
          methods += element.operations.length;
        }
      } else if (element._type === 'UMLAssociation') {
        relationships++;
      }
      if (element.ownedElements) {
        element.ownedElements.forEach(iterateElements);
      }
    };

    json.ownedElements.forEach(iterateElements);
    return { classes, attributes, methods, relationships };
  }

  parsePUML(content: string): any {
    const lines = content.split('\n');
    let classes = lines.filter(line => line.trim().startsWith('class ')).length;
    let attributes = lines.filter(line => line.match(/^\s+[+-]\w+\s*:\s*\w+/)).length;
    let methods = lines.filter(line => line.match(/^\s+[+-]\w+\s*\([^)]*\)\s*(?::\s*\w+)?/)).length;
    let relationships = lines.filter(line => line.trim().includes('--')).length;
    return { classes, attributes, methods, relationships };
  }

  onMappingCompleted(success: boolean) {
    if (success) {
      this.stepper.next();
    } else {
      console.error("Failed to apply mappings");
    }
  }
}
