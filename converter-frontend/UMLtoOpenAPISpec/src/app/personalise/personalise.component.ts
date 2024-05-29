import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-personalise',
  standalone: true,
  imports: [
    NgForOf,
    NgIf
  ],
  templateUrl: './personalise.component.html',
  styleUrl: './personalise.component.scss'
})
export class PersonaliseComponent implements OnChanges {
  @Input() openApiData: any;
  classNames: string[] = [];
  selectedClassAttributes: string[] = [];
  selectedAttributeExamples: any[] = [];
  currentView: 'classes' | 'attributes' | 'examples' = 'classes';
  selectedClass: string = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['openApiData'] && changes['openApiData'].currentValue) {
      this.extractClassNames();
    }
  }

  extractClassNames(): void {
    this.classNames = [];
    if (this.openApiData && this.openApiData.components && this.openApiData.components.schemas) {
      this.classNames = Object.keys(this.openApiData.components.schemas);
    }
    this.currentView = 'classes';
  }

  selectClass(className: string): void {
    this.selectedClass = className;
    this.selectedClassAttributes = [];
    const schema = this.openApiData.components.schemas[className];
    if (schema && schema.properties) {
      this.selectedClassAttributes = Object.keys(schema.properties);
    }
    this.currentView = 'attributes';
  }

  selectAttribute(attribute: string): void {
    this.selectedAttributeExamples = [];
    const schema = this.openApiData.components.schemas[this.selectedClass];
    if (schema && schema.examples && schema.examples.exampleArray) {
      this.selectedAttributeExamples = schema.examples.exampleArray.map((example: { [x: string]: any; }) => example[attribute]);
    }
    this.currentView = 'examples';
  }

  goBack(): void {
    this.currentView = 'classes';
  }

  goBackToAttributes(): void {
    this.currentView = 'attributes';
  }
}
