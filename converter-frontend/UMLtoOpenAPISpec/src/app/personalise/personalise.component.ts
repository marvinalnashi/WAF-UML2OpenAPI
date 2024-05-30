import {Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {NgForOf, NgIf} from "@angular/common";
import {RenameDialogComponent} from "../rename-dialog/rename-dialog.component";
import {MatDialog} from "@angular/material/dialog";
import {MatIcon} from "@angular/material/icon";
import {MatButton} from "@angular/material/button";
import {MatStepper} from "@angular/material/stepper";

@Component({
  selector: 'app-personalise',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    MatIcon,
    MatButton
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
  selectedAttribute: string = '';
  tempExampleValues: { [key: string]: any[] } = {};

  constructor(public dialog: MatDialog, private http: HttpClient) {}

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
    this.selectedAttribute = attribute;
    if (this.tempExampleValues[`${this.selectedClass}_${attribute}`]) {
      this.selectedAttributeExamples = this.tempExampleValues[`${this.selectedClass}_${attribute}`];
    } else {
      this.selectedAttributeExamples = [];
      const schema = this.openApiData.components.schemas[this.selectedClass];
      if (schema && schema.examples && schema.examples.exampleArray) {
        this.selectedAttributeExamples = schema.examples.exampleArray.map((example: { [x: string]: any; }) => example[attribute]);
        this.tempExampleValues[`${this.selectedClass}_${attribute}`] = this.selectedAttributeExamples;
      }
    }
    this.currentView = 'examples';
  }

  goBack(): void {
    this.currentView = 'classes';
  }

  goBackToAttributes(): void {
    this.currentView = 'attributes';
  }

  editExample(index: number): void {
    const dialogRef = this.dialog.open(RenameDialogComponent, {
      width: '250px',
      data: { newValue: this.selectedAttributeExamples[index] }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result !== undefined) {
        this.selectedAttributeExamples[index] = result;
        this.tempExampleValues[`${this.selectedClass}_${this.selectedAttribute}`][index] = result;
        this.updateExampleInBackend(index, result);
      }
    });
  }

  updateExampleInBackend(index: number, newValue: string): void {
    const updateRequest = {
      className: this.selectedClass,
      attributeName: this.selectedAttribute,
      index: index,
      newValue: newValue
    };
    this.http.post('http://localhost:8080/updateExample', updateRequest)
      .subscribe(() => {
        this.updateOpenApiData(index, newValue);
        console.log('Example updated successfully in the backend.');
      }, error => {
        console.error('Failed to update example in the backend', error);
      });
  }

  updateOpenApiData(index: number, newValue: string): void {
    const paths = this.openApiData.paths;
    for (const path in paths) {
      if (paths.hasOwnProperty(path)) {
        const methods = paths[path];
        for (const method in methods) {
          if (methods.hasOwnProperty(method)) {
            const responses = methods[method].responses;
            for (const response in responses) {
              if (responses.hasOwnProperty(response)) {
                const content = responses[response].content;
                for (const contentType in content) {
                  if (content.hasOwnProperty(contentType)) {
                    const examples = content[contentType].examples;
                    if (examples && examples.exampleArray) {
                      examples.exampleArray.value[index][this.selectedAttribute] = newValue;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
