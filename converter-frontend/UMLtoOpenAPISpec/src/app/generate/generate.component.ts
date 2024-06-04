import {Component, EventEmitter, Input, Output} from '@angular/core';
import {GenerationService} from "../generation.service";
import {HttpClient} from "@angular/common/http";
import {NgIf} from "@angular/common";
import {MatStepper} from "@angular/material/stepper";

@Component({
  selector: 'app-generate',
  standalone: true,
  imports: [
    NgIf
  ],
  templateUrl: './generate.component.html',
  styleUrl: './generate.component.scss'
})
export class GenerateComponent {
  @Input() uploadedFile: File | null = null;
  @Input() fileFormat: string = '';
  @Input() modellingTool: string = '';
  @Input() diagramType: string = 'Class Diagram';
  @Input() elementCount: any = { classes: 0, attributes: 0, methods: 0, relationships: 0 };
  @Input() mappedElementCount: any = { classes: 0, attributes: 0, methods: 0, relationships: 0 };
  @Input() isGeneratedSuccessfully = false;
  @Input() isLoading = false;
  @Input() stepper!: MatStepper;
  @Output() generate = new EventEmitter<void>();

  constructor(
    private generationService: GenerationService,
    private http: HttpClient
  ) { }

  onGenerateClick(): void {
    this.generate.emit();
  }
}
