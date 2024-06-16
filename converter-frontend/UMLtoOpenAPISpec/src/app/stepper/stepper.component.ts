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
import {NotificationService} from "../notification.service";

/**
 * Component for handling the main functionalities of the stepper and interacting with its child components.
 */
@Component({
  selector: 'app-stepper',
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
  templateUrl: './stepper.component.html',
  providers: [GenerationService, MockServerService, StepperSessionService, NotificationService]
})
export class StepperComponent implements AfterViewInit, OnInit {
  /**
   * Instance of the stepper that is used for navigation between steps.
   */
  @ViewChild('stepper') private stepper!: MatStepper;

  /**
   * Output property for emitting the extracted data of the uploaded UML diagram to the Generation service.
   */
  @Output() umlDataEmitter = new EventEmitter<any>();

  /**
   * The UML diagram the user uploaded in the Upload step of the stepper.
   */
  uploadedFile: File | null = null;

  /**
   * Indicates whether an OpenAPI specification was successfully generated.
   */
  isGeneratedSuccessfully = false;

  /**
   * The file format of the UML diagram the user uploaded in the Upload step of the stepper.
   */
  fileFormat: string = '';

  /**
   * The modelling tool that was used to create the UML diagram the user uploaded in the Upload step of the stepper.
   */
  modellingTool: string = '';
  serverButtonText = 'Start mock server';

  /**
   * Form group that is used for the form in the Add Elements tab of the Mapping step of the stepper.
   */
  mappingFormGroup: FormGroup;

  /**
   * The data that is extracted from the UML diagram the user uploaded in the Upload step of the stepper.
   */
  umlData: any;

  /**
   * The data of the uploaded UML diagram and generated OpenAPI specification of the current session that will be saved to the database.
   */
  openApiData: any = null;

  /**
   * The HTTP methods that the user selects for one or more classes in the table of the Manage Elements tab of the Mapping step of the stepper.
   */
  selectedHttpMethods: { [className: string]: { [method: string]: boolean } } = {};

  /**
   * The amounts of elements in the uploaded UML diagram before the modifications the user has done in the Mapping step.
   */
  elementCount = {
    classes: 0,
    attributes: 0,
    methods: 0,
    relationships: 0
  };

  /**
   * The amounts of elements in the uploaded UML diagram after the modifications the user has done in the Mapping step.
   */
  mappedElementCount = {
    classes: 0,
    attributes: 0,
    methods: 0,
    relationships: 0
  };

  /**
   * Indicates whether the Mapping step of the stepper has been successfully completed.
   */
  isMappingStepCompleted = false;

  /**
   * Indicates whether the loader overlay that contains the spinner needs to be activated.
   */
  isLoading = false;

  /**
   * Creates an instance of StepperComponent.
   * @param fb The Form builder service.
   * @param generationService The Generation service.
   * @param mockServerService The Mock server service.
   * @param http The HTTP client service.
   * @param sessionService The Stepper session service.
   * @param notificationService The Notification service.
   */
  constructor(
    private fb: FormBuilder,
    private generationService: GenerationService,
    private mockServerService: MockServerService,
    private http: HttpClient,
    private sessionService: StepperSessionService,
    private notificationService: NotificationService
  ) {
    this.mappingFormGroup = this.fb.group({
      userConfirmation: ['', Validators.required]
    });
  }

  /**
   * Makes the step header of each step in the stepper unclickable after the stepper has been initialised.
   */
  ngAfterViewInit() {
    this.stepper._stepHeader.forEach(header => {
      header._elementRef.nativeElement.style.pointerEvents = 'none';
    });
  }

  /**
   * Sets the default value of the OpenAPI specification data to null when the stepper has been initialised and before an OpenAPI specification is generated.
   */
  ngOnInit(): void {
    this.openApiData = null;
  }

  /**
   * Handles the event in which the user clicks on the button in the Upload step and uploads a UML diagram file.
   * @param file The selected UML diagram file that is parsed.
   */
  onFileSelected(file: File): void {
    this.uploadedFile = file;
    this.fileFormat = this.uploadedFile.name.split('.').pop()!;
    this.modellingTool = this.getModellingTool(this.fileFormat);
    this.readFile(this.uploadedFile);
    this.generationService.parseDiagramElements(this.uploadedFile).subscribe({
      next: (data) => {
        this.umlData = data;
        this.emitUmlData(data);
        this.notificationService.showSuccess('The uploaded UML diagram file has been processed successfully.');
      },
      error: (error) => {
        console.error('Failed to parse diagram', error);
        this.notificationService.showError('The uploaded UML diagram file could not be processed.');
      }
    });
  }

  /**
   * Determines the modelling tool that is used to create the uploaded UML diagram based on the uploaded file's format.
   * @param format The file format of the uploaded UML diagram file.
   * @returns The name of the modelling tool that is used to create the uploaded UML diagram.
   */
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

  /**
   * Emits the extracted data of the uploaded UML diagram to the Generation service.
   * @param data The extracted data of the uploaded UML diagram.
   */
  emitUmlData(data: any): void {
    this.umlDataEmitter.emit(data);
  }

  /**
   * Processes the HTTP methods that the user selects in the table of the Manage Elements tab of the Mapping step of the stepper, so that endpoints can be created for them in the generated OpenAPI specification.
   * @param selectedMethods The HTTP methods that the user selects in the table of the Manage Elements tab of the Mapping step of the stepper.
   */
  onHttpMethodsSelected(selectedMethods: { [className: string]: { [method: string]: boolean } }) {
    this.selectedHttpMethods = selectedMethods;
  }

  /**
   * Updates the amounts of elements in the uploaded UML diagram after the modifications the user has done in the Mapping step of the stepper.
   * @param count The amounts of elements after the Mapping step of the stepper.
   */
  updateMappedElementCount(count: any): void {
    this.mappedElementCount = count;
  }

  /**
   * Generates an OpenAPI specification based on the uploaded UML diagram and the modifications performed by the user.
   */
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

      this.generationService.generateSpecification(formData).subscribe({
        next: (response) => {
          console.log('Generation successful', response);
          this.isGeneratedSuccessfully = true;
          this.openApiData = null;
          this.generateOpenApiData();
          this.isLoading = false;
          this.notificationService.showInfo('The OpenAPI specification has successfully been generated.');
        },
        error: (error) => {
          console.error('Generation failed', error);
          this.notificationService.showError('The OpenAPI specification could not be generated successfully.');
          this.isLoading = false;
        }
      });
    } else {
      console.error('No file selected');
      this.notificationService.showError('No file has been selected to upload.');
      this.isLoading = false;
    }
  }

  /**
   * Generates the data that is used for and included in the generated OpenAPI specification, including the example values of attributes, which are generated using AI.
   */
  generateOpenApiData(): void {
    // This function needs to be moved to the Generation Service, as the other HTTP methods are stored in that file too.
    this.http.get<any>('http://localhost:8080/personalise').subscribe(
      data => {
        this.openApiData = data;
        this.notificationService.showSuccess('The example values for the OpenAPI specification have successfully been generated.');
      },
      error => {
        console.error('Failed to load OpenAPI data', error);
        this.notificationService.showError('The example values for the OpenAPI specification could not be generated.');
      }
    );
  }

  /**
   * Starts or restarts the Prism mock server in the Manage step of the stepper.
   */
  toggleMockServer(): void {
    this.mockServerService.toggleMockServer().subscribe({
      next: (response) => {
        console.log(response.message);
        this.notificationService.showSuccess('The Prism mock server has successfully started.');
        this.serverButtonText = response.message.includes('starting') ? 'Restart mock server' : 'Start mock server';
      },
      error: (error) => {
        console.error('Server toggle failed', error);
        this.notificationService.showError('The Prism mock server could not be started.');
      }
    });
  }

  /**
   * Saves data of the current session, consisting of the uploaded UML diagram and the generated OpenAPI specification, and restarts the stepper.
   */
  restartApplication(): void {
    if (this.uploadedFile && this.openApiData) {
      const umlDiagram = this.uploadedFile.name;
      const openApiSpec = JSON.stringify(this.openApiData);
      const session = { umlDiagram, openApiSpec };

      this.sessionService.saveSession(session).subscribe({
        next: () => {
          console.log('Session saved successfully');
          this.notificationService.showSuccess('The session data has been saved successfully.');
          window.location.reload();
        },
        error: (error) => {
          console.error('Failed to save session', error);
          this.notificationService.showError('The session data could not be saved.');
          window.location.reload();
        }
      });
    } else {
      console.error('No file selected or OpenAPI data missing');
      this.notificationService.showError('There was an error saving the session data.');
      window.location.reload();
    }
  }


  /**
   * Reads and parses the uploaded UML diagram file to identify individual elements.
   * @param file The selected file.
   */
  readFile(file: File): void {
    const reader = new FileReader();
    reader.onload = () => {
      const content = reader.result as string;
      this.elementCount = this.parseContentByFormat(content, this.fileFormat);
    };
    reader.readAsText(file);
  }

  /**
   * Determines which parser function needs to be executed based on the file format of the uploaded UML diagram.
   * @param content The code inside the uploaded UML diagram file.
   * @param format The file format of the uploaded UML diagram file.
   * @returns The parsed content of the uploaded UML diagram file.
   */
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

  /**
   * Parses the content of a UML diagram that was uploaded in the XML file format and identifies individual elements.
   * @param content The parsed content of the uploaded UML diagram file.
   * @returns The identified individual elements of the uploaded UML diagram file.
   */
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

  /**
   * Parses the content of a UML diagram that was uploaded in the UXF file format and identifies individual elements.
   * @param content The parsed content of the uploaded UML diagram file.
   * @returns The identified individual elements of the uploaded UML diagram file.
   */
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

  /**
   * Parses the content of a MDJ diagram that was uploaded in the XML file format and identifies individual elements.
   * @param content The parsed content of the uploaded UML diagram file.
   * @returns The identified individual elements of the uploaded UML diagram file.
   */
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

  /**
   * Parses the content of a UML diagram that was uploaded in the PUML file format and identifies individual elements.
   * @param content The parsed content of the uploaded UML diagram file.
   * @returns The identified individual elements of the uploaded UML diagram file.
   */
  parsePUML(content: string): any {
    const lines = content.split('\n');
    let classes = lines.filter(line => line.trim().startsWith('class ')).length;
    let attributes = lines.filter(line => line.match(/^\s+[+-]\w+\s*:\s*\w+/)).length;
    let methods = lines.filter(line => line.match(/^\s+[+-]\w+\s*\([^)]*\)\s*(?::\s*\w+)?/)).length;
    let relationships = lines.filter(line => line.trim().includes('--')).length;
    return { classes, attributes, methods, relationships };
  }

  /**
   * Determines whether the Mapping step of the stepper was completed successfully and navigates to the Generate step if so.
   * @param success Indicates whether the Mapping step of the stepper was completed successfully.
   */
  onMappingCompleted(success: boolean) {
    if (success) {
      this.isMappingStepCompleted = true;
      this.notificationService.showSuccess('The additions and modifications that were provided in the Mapping step have been applied successfully.');
      this.stepper.next();
    } else {
      console.error("Failed to apply mappings");
      this.notificationService.showError('The additions and modifications that were provided in the Mapping step could not be applied.');
    }
  }

  /**
   * Opens Swagger UI in a new tab and automatically loads the generated OpenAPI specification in the tool.
   */
  openSwaggerUI(): void {
    window.open('http://localhost:8080/swagger-ui/index.html', '_blank');
    this.notificationService.showSuccess('Swagger UI has been opened and the generated OpenAPI specification has been loaded in it successfully.');
  }

  /**
   * Downloads the generated OpenAPI specification of the current session.
   */
  downloadOpenApiSpecification(): void {
    window.open('http://localhost:8080/export.yml', '_blank');
    this.notificationService.showSuccess('The generated OpenAPI specification has been downloaded successfully.');
  }
}
