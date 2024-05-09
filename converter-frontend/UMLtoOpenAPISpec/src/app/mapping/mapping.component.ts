import {Component, OnInit} from '@angular/core';
import {CdkDrag, CdkDragDrop, CdkDropList, moveItemInArray, transferArrayItem} from '@angular/cdk/drag-drop';
import { GenerationService } from '../generation.service';
import {FormsModule} from "@angular/forms";
import {NgForOf} from "@angular/common";
import {MatButton} from "@angular/material/button";
import {MatStepper} from "@angular/material/stepper";

@Component({
  selector: 'app-mapping',
  standalone: true,
  imports: [
    FormsModule,
    NgForOf,
    CdkDropList,
    CdkDrag,
    MatButton
  ],
  templateUrl: './mapping.component.html',
  styleUrl: './mapping.component.scss'
})
export class MappingComponent implements OnInit {
  apiElements: any[] = [];

  constructor(private generationService: GenerationService, private stepper: MatStepper) {}

  ngOnInit() {
    this.generationService.fetchApiElements().subscribe({
      next: (elements) => this.apiElements = elements.paths || [],
      error: (err) => console.error('Failed to fetch API elements', err)
    });
  }

  drop(event: CdkDragDrop<string[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      transferArrayItem(event.previousContainer.data, event.container.data,
        event.previousIndex, event.currentIndex);
    }
  }

  applyMappingsAndContinue() {
    const mappings = this.apiElements.reduce((acc, el) => {
      acc[el.url] = { ...el };
      return acc;
    }, {});

    this.generationService.applyMappings(mappings).subscribe({
      next: () => this.stepper.next(),
      error: err => console.error('Failed to apply mappings', err)
    });

    this.stepper.next();
  }
}
