import { Component } from '@angular/core';
import { GenerationService } from '../generation.service';
import { MockServerService } from '../mock-server.service';
import { HttpClient } from '@angular/common/http';
import {MatStep, MatStepLabel, MatStepper} from '@angular/material/stepper';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';

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
export class GenerationComponent {
  uploadedFile: File | null = null;
  isGeneratedSuccessfully = false;
  fileFormat: string = '';
  diagramType: string = 'Class Diagram';
  serverButtonText = 'Start mock server';
  elementCount: any = {};

  constructor(
    private generationService: GenerationService,
    private mockServerService: MockServerService,
    private http: HttpClient
  ) { }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    this.uploadedFile = input.files[0];
    this.fileFormat = this.uploadedFile.name.split('.').pop()!;
    this.readFile(this.uploadedFile);
  }

  generate(): void {
    if (this.uploadedFile) {
      this.generationService.generateSpec(this.uploadedFile).subscribe({
        next: (response) => {
          console.log('Generation successful', response);
          this.isGeneratedSuccessfully = true;
        },
        error: (error) => console.error('Generation failed', error)
      });
    } else {
      console.error('No file selected');
    }
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

  readFile(file: File): void {
    const reader = new FileReader();
    reader.onload = () => {
      const content = reader.result as string;
      switch (this.fileFormat) {
        case 'xml':
          this.elementCount = this.parseDrawioXML(content);
          break;
        case 'uxf':
          this.elementCount = this.parseUMLetUXF(content);
          break;
        case 'mdj':
          this.elementCount = this.parseMDJ(content);
          break;
        case 'puml':
          this.elementCount = this.parsePUML(content);
          break;
        default:
          this.elementCount = {};
      }
    };
    reader.readAsText(file);
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
