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
  @ViewChild('stepper') private stepper!: MatStepper;
  @Input() openApiData: any;
  classNames: string[] = [];
  selectedClassAttributes: string[] = [];
  selectedAttributeExamples: any[] = [];
  currentView: 'classes' | 'attributes' | 'examples' = 'classes';
  selectedClass: string = '';
  selectedAttribute: string = '';

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

  editExample(index: number): void {
    const dialogRef = this.dialog.open(RenameDialogComponent, {
      width: '250px',
      data: { newValue: this.selectedAttributeExamples[index] }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result !== undefined) {
        this.selectedAttributeExamples[index] = result;
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
        console.log('Example updated successfully in the backend.');
      }, error => {
        console.error('Failed to update example in the backend', error);
      });
  }

  saveAndContinue(): void {
    if (this.stepper) {
      this.stepper.next();
    }
  }
}
