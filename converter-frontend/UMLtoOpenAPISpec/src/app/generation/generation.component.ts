import { AfterViewInit, Component, EventEmitter, OnInit, Output, ViewChild } from '@angular/core';
import { GenerationService } from '../generation.service';
import { MockServerService } from '../mock-server.service';
import { HttpClient } from '@angular/common/http';
import {MatStep, MatStepLabel, MatStepper} from '@angular/material/stepper';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MappingComponent } from '../mapping/mapping.component';
import { PersonaliseComponent } from '../personalise/personalise.component';
import { TopBarComponent } from '../top-bar/top-bar.component';
import { MatIcon } from '@angular/material/icon';
import { LoaderComponent } from '../loader/loader.component';
import { StepperSessionService } from '../stepper-session.service';
import {UploadComponent} from "../upload/upload.component";
import {StartComponent} from "../start/start.component";
import { GenerateComponent } from '../generate/generate.component';
import { ManageComponent } from '../manage/manage.component';

@Component({
  selector: 'app-generation',
  standalone: true,
  imports: [
    CommonModule,
    MatStepper,
    MatInputModule,
    MatButtonModule,
    MappingComponent,
    PersonaliseComponent,
    TopBarComponent,
    MatIcon,
    LoaderComponent,
    MatStep,
    UploadComponent,
    StartComponent,
    MatStepLabel,
    UploadComponent,
    GenerateComponent,
    GenerateComponent,
    ManageComponent
  ],
  templateUrl: './generation.component.html',
  providers: [GenerationService, MockServerService, StepperSessionService]
})
export class GenerationComponent implements AfterViewInit, OnInit {
  @ViewChild('stepper') private stepper!: MatStepper;
  @Output() umlDataEmitter = new EventEmitter<any>();

  uploadedFile: File | null = null;
  isGeneratedSuccessfully = false;
  fileFormat: string = '';
  diagramType: string = 'Class Diagram';
  modellingTool: string = '';
  serverButtonText = 'Start mock server';
  mappingFormGroup: FormGroup;
  umlData: any;
  openApiData: any = null;
  selectedHttpMethods: { [className: string]: { [method: string]: boolean } } = {};
  elementCount = {
    classes: 0,
    attributes: 0,
    methods: 0,
    relationships: 0
  };
  mappedElementCount = {
    classes: 0,
    attributes: 0,
    methods: 0,
    relationships: 0
  };

  isMappingStepCompleted = false;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private generationService: GenerationService,
    private mockServerService: MockServerService,
    private http: HttpClient,
    private sessionService: StepperSessionService
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
    this.openApiData = null;
  }

  onFileSelected(file: File): void {
    this.uploadedFile = file;
    this.fileFormat = this.uploadedFile.name.split('.').pop()!;
    this.modellingTool = this.getModellingTool(this.fileFormat);
    this.readFile(this.uploadedFile);
    this.generationService.parseDiagramElements(this.uploadedFile).subscribe({
      next: (data) => {
        this.umlData = data;
        this.emitUmlData(data);
      },
      error: (error) => console.error('Failed to parse diagram', error)
    });
  }

  getModellingTool(format: string): string {
    switch (format) {
      case 'uxf':
        return 'UMLet';
      case 'xml':
        return 'Draw.io';
      case 'mdj':
        return 'StarUML';
      case 'puml':
        return 'PlantUML';
      default:
        return 'Unknown';
    }
  }

  emitUmlData(data: any): void {
    this.umlDataEmitter.emit(data);
  }

  onHttpMethodsSelected(selectedMethods: { [className: string]: { [method: string]: boolean } }) {
    this.selectedHttpMethods = selectedMethods;
  }

  updateMappedElementCount(count: any): void {
    this.mappedElementCount = count;
  }

  generate(): void {
    this.isLoading = true;
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
          this.openApiData = null;
          this.generateOpenApiData();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Generation failed', error);
          this.isLoading = false;
        }
      });
    } else {
      console.error('No file selected');
      this.isLoading = false;
    }
  }

  generateOpenApiData(): void {
    this.http.get<any>('http://localhost:8080/personalise').subscribe(
      data => {
        this.openApiData = data;
      },
      error => console.error('Failed to load OpenAPI data', error)
    );
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
    if (this.uploadedFile && this.openApiData) {
      const umlDiagram = this.uploadedFile.name;
      const openApiSpec = JSON.stringify(this.openApiData);
      const session = { umlDiagram, openApiSpec };

      this.sessionService.saveSession(session).subscribe({
        next: () => {
          console.log('Session saved successfully');
          window.location.reload();
        },
        error: (error) => {
          console.error('Failed to save session', error);
          window.location.reload();
        }
      });
    } else {
      console.error('No file selected or OpenAPI data missing');
      window.location.reload();
    }
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
      this.isMappingStepCompleted = true;
      this.stepper.next();
    } else {
      console.error("Failed to apply mappings");
    }
  }

  openSwaggerUI(): void {
    window.open('http://localhost:8080/swagger-ui/index.html', '_blank');
  }

  downloadOpenAPISpecification(): void {
    window.open('http://localhost:8080/export.yml', '_blank');
  }
}
