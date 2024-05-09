import { Component } from '@angular/core';
import {CdkDrag, CdkDragDrop, CdkDropList, moveItemInArray, transferArrayItem} from '@angular/cdk/drag-drop';
import { GenerationService } from '../generation.service';
import {FormsModule} from "@angular/forms";
import {NgForOf} from "@angular/common";

interface ClassItem {
  name: string;
  id: string;
  url?: string;
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
}

@Component({
  selector: 'app-mapping',
  standalone: true,
  imports: [
    FormsModule,
    NgForOf,
    CdkDropList,
    CdkDrag
  ],
  templateUrl: './mapping.component.html',
  styleUrl: './mapping.component.scss'
})
export class MappingComponent {
  availableClasses: ClassItem[] = [
    { name: 'User', id: 'User' },
    { name: 'Order', id: 'Order' },
    { name: 'Product', id: 'Product' }
  ];

  endpoints: ClassItem[] = [];
  uploadedFile: File | null = null;

  constructor(private generationService: GenerationService) {}

  drop(event: CdkDragDrop<ClassItem[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      transferArrayItem(event.previousContainer.data, event.container.data, event.previousIndex, event.currentIndex);
    }
  }

  onFileSelected(event: Event) {
    const element = event.currentTarget as HTMLInputElement;
    let fileList: FileList | null = element.files;
    if (fileList) {
      this.uploadedFile = fileList[0];
    } else {
      this.uploadedFile = null;
    }
  }

  applyMappings() {
    if (!this.uploadedFile) {
      alert('Please upload a file first.');
      return;
    }

    const mappings = this.endpoints.reduce((acc: {[key: string]: any}, endpoint: ClassItem) => {
      acc[endpoint.id] = {
        url: endpoint.url || endpoint.id.toLowerCase(),
        method: endpoint.method || 'GET'
      };
      return acc;
    }, {});

    this.generationService.generateSpecWithMappings(this.uploadedFile, mappings).subscribe({
      next: (response) => alert('OpenAPI Specification generated successfully!'),
      error: (error) => console.error('Error generating OpenAPI spec', error)
    });
  }
}
