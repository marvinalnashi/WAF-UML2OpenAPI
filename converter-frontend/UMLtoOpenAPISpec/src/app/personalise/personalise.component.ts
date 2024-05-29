import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-personalise',
  standalone: true,
  imports: [
    NgForOf
  ],
  templateUrl: './personalise.component.html',
  styleUrl: './personalise.component.scss'
})
export class PersonaliseComponent implements OnChanges {
  @Input() openApiData: any;
  examples: { className: string, attrName: string, value: any }[] = [];

  constructor() {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['openApiData'] && changes['openApiData'].currentValue) {
      this.extractExamples(changes['openApiData'].currentValue);
    }
  }

  extractExamples(openApiData: any): void {
    this.examples = [];
    if (openApiData && openApiData.components && openApiData.components.schemas) {
      for (const [className, schema] of Object.entries(openApiData.components.schemas)) {
        if (schema && typeof schema === 'object' && 'examples' in schema) {
          const examples = (schema as { examples: { exampleArray: any[] } }).examples.exampleArray;
          if (examples) {
            for (const example of examples) {
              for (const [attrName, value] of Object.entries(example)) {
                if (attrName !== 'id') {
                  this.examples.push({
                    className,
                    attrName,
                    value
                  });
                }
              }
            }
          }
        }
      }
    }
  }
}
