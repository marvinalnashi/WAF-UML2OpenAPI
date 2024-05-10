import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import { GenerationService } from '../generation.service';
import { MockServerService } from '../mock-server.service';
import { HttpClient } from '@angular/common/http';
import {MatStep, MatStepLabel, MatStepper} from '@angular/material/stepper';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';

interface ElementDetails {
  classes: string[];
  attributes: { [key: string]: string[] };
  methods: { [key: string]: string[] };
}

@Component({
  selector: 'app-generation',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatStepper,
    MatInputModule,
    MatButtonModule,
    MatStep,
    MatStepLabel
  ],
  templateUrl: './generation.component.html',
  providers: [GenerationService, MockServerService]
})
export class GenerationComponent implements AfterViewInit, OnInit {
  @ViewChild('stepper', { static: true }) stepper!: MatStepper;

  uploadedFile: File | null = null;
  isGeneratedSuccessfully = false;
  fileFormat: string = '';
  diagramType: string = 'Class Diagram';
  serverButtonText = 'Start mock server';
  showNextButton = false;
  elementCount: any = {};
  fileError = '';
  generationError = '';
  elementNames: ElementDetails = { classes: [], attributes: {}, methods: {} };
  mappingFormGroup: FormGroup;

  constructor(
    private fb: FormBuilder,
    private generationService: GenerationService,
    private mockServerService: MockServerService,
    private http: HttpClient
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

  ngOnInit(): void {

  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) {
      this.fileError = 'Please select a file to upload.';
      this.uploadedFile = null;
      this.showNextButton = false;
      return;
    }
    this.uploadedFile = input.files[0];
    this.fileFormat = this.uploadedFile.name.split('.').pop()?.toLowerCase() ?? '';
    const validFormats = ['uxf', 'xml', 'mdj', 'puml'];
    if (!validFormats.includes(this.fileFormat)) {
      this.fileError = 'Invalid file format. Please upload a UXF, XML, MDJ, or PUML file.';
      this.uploadedFile = null;
      this.showNextButton = false;
    } else {
      this.fileError = '';
      this.showNextButton = true;
      this.readFile(this.uploadedFile);
      this.generationService.parseDiagramElements(this.uploadedFile).subscribe({
        next: (response) => {
          this.elementNames.classes = response.classes || [];
          this.elementNames.attributes = response.attributes || {};
          this.elementNames.methods = response.methods || {};
          this.stepper.next();
        },
        error: (error) => {
          console.error('Error parsing elements', error);
          this.fileError = 'Failed to parse diagram elements.';
        }
      });
    }
  }


  generate(stepper: MatStepper): void {
    if (!this.uploadedFile) {
      this.generationError = 'No file selected or invalid file format.';
      return;
    }
    this.generationService.generateSpec(this.uploadedFile).subscribe({
      next: (response: any) => {
        console.log('Generation successful', response);
        this.isGeneratedSuccessfully = true;
        this.generationError = '';
        stepper.next();
      },
      error: (error: any) => {
        console.error('Generation failed', error);
        this.generationError = 'Failed to generate OpenAPI spec.';
        this.isGeneratedSuccessfully = false;
      }
    });
  }

  continue(stepper: MatStepper): void {
    if (this.isGeneratedSuccessfully) {
      stepper.next();
    }
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
      this.elementCount = this.parseByFormat(content, this.fileFormat);
    };
    reader.readAsText(file);
  }

  // For counting the individual elements of the uploaded UML diagram
  parseByFormat(content: string, format: string) {
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
}
