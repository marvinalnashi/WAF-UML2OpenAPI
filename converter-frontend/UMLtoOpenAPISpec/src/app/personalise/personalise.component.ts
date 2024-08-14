import {Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {JsonPipe, NgForOf, NgIf} from "@angular/common";
import {RenameDialogComponent} from "../rename-dialog/rename-dialog.component";
import {MatDialog} from "@angular/material/dialog";
import {MatIcon} from "@angular/material/icon";
import {MatButton} from "@angular/material/button";
import {MatStepper} from "@angular/material/stepper";

/**
 * Component for modifying the example values that were generated with AI for the generated OpenAPI specification.
 */
@Component({
  selector: 'app-personalise',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    MatIcon,
    MatButton,
    JsonPipe
  ],
  templateUrl: './personalise.component.html',
  styleUrl: './personalise.component.scss'
})
export class PersonaliseComponent implements OnChanges {
  /**
   * Input property for the transmission of the generated OpenAPI specification data.
   */
  @Input() openApiData: any;

  /**
   * List of classnames from the generated OpenAPI specification data, which are displayed in the explorer section.
   */
  classNames: string[] = [];

  /**
   * List of attributes from the generated OpenAPI specification data for the selected classname in the explorer section.
   */
  selectedClassAttributes: string[] = [];

  /**
   * List of example values that were generated with AI from the generated OpenAPI specification data for the selected attribute in the explorer section.
   */
  selectedAttributeExamples: any[] = [];

  /**
   * Current view of the explorer, based on the selected items and the state of the Personalise step in the stepper.
   */
  currentView: 'classes' | 'attributes' | 'examples' | 'linking' = 'classes';

  /**
   * The classname that is currently selected in the explorer by the user.
   */
  selectedClass: string = '';

  /**
   * The attribute that is currently selected in the explorer by the user.
   */
  selectedAttribute: string = '';

  /**
   * Temporary storage for example values of attributes. This ensures that example value updates are successfully processed and displayed in the user interface.
   */
  temporarilyStoredExampleValues: { [key: string]: any[] } = {};

  linkedExamples: { [key: string]: any }[] = [];

  editingLink: { [key: string]: any } | null = null;

  /**
   * Creates an instance of PersonaliseComponent.
   * @param dialog The popup dialog service.
   * @param http The HTTP client service.
   */
  constructor(public dialog: MatDialog, private http: HttpClient) {}

  /**
   * Updates a modified example value, its attribute, and its class.
   */
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['openApiData'] && changes['openApiData'].currentValue) {
      this.extractClassNames();
    }
  }

  /**
   * Extracts classnames from the generated OpenAPI specification data.
   */
  extractClassNames(): void {
    this.classNames = [];
    if (this.openApiData && this.openApiData.components && this.openApiData.components.schemas) {
      this.classNames = Object.keys(this.openApiData.components.schemas);
    }
    this.currentView = 'classes';
  }

  /**
   * Selects a class and extracts its attributes from the generated OpenAPI specification data.
   * @param className The name of the class to select.
   */
  selectClass(className: string): void {
    this.selectedClass = className;
    this.selectedClassAttributes = [];
    const schema = this.openApiData.components.schemas[className];
    if (schema && schema.properties) {
      this.selectedClassAttributes = Object.keys(schema.properties);
    }
    this.initialiseExamples();
    this.currentView = 'attributes';
  }

  initialiseExamples(): void {
    for (const attribute of this.selectedClassAttributes) {
      if (!this.temporarilyStoredExampleValues[`${this.selectedClass}_${attribute}`]) {
        const schema = this.openApiData.components.schemas[this.selectedClass];
        if (schema && schema.examples && schema.examples.exampleArray) {
          this.temporarilyStoredExampleValues[`${this.selectedClass}_${attribute}`] = schema.examples.exampleArray.map(
            (example: { [x: string]: any }) => example[attribute]
          );
        } else {
          this.temporarilyStoredExampleValues[`${this.selectedClass}_${attribute}`] = [];
        }
      }
    }
  }

  /**
   * Selects an attribute and extracts its example values from the generated OpenAPI specification data.
   * @param attribute The name of the attribute to select.
   */
  // selectAttribute(attribute: string): void {
  //   this.selectedAttribute = attribute;
  //   if (this.temporarilyStoredExampleValues[`${this.selectedClass}_${attribute}`]) {
  //     this.selectedAttributeExamples = this.temporarilyStoredExampleValues[`${this.selectedClass}_${attribute}`];
  //   } else {
  //     this.selectedAttributeExamples = [];
  //     const schema = this.openApiData.components.schemas[this.selectedClass];
  //     if (schema && schema.examples && schema.examples.exampleArray) {
  //       this.selectedAttributeExamples = schema.examples.exampleArray.map((example: { [x: string]: any }) => example[attribute]);
  //       this.temporarilyStoredExampleValues[`${this.selectedClass}_${attribute}`] = this.selectedAttributeExamples;
  //     }
  //   }
  //   this.currentView = 'examples';
  // }
  selectAttribute(attribute: string): void {
    this.selectedAttribute = attribute;
    this.selectedAttributeExamples = this.temporarilyStoredExampleValues[`${this.selectedClass}_${attribute}`] || [];
    this.currentView = 'examples';
  }

  /**
   * Navigates back to the main view of the explorer that contains the classnames.
   */
  goBackToClasses(): void {
    this.currentView = 'classes';
  }

  /**
   * Navigates back to the view of the explorer that contains the attributes of a selected class.
   */
  goBackToAttributes(): void {
    this.currentView = 'attributes';
  }

  /**
   * Activates a popup dialog in which a generated example value can be modified.
   * @param index The index of the example value to modify.
   */
  editExampleValue(index: number): void {
    const dialogRef = this.dialog.open(RenameDialogComponent, {
      width: '250px',
      data: { newValue: this.selectedAttributeExamples[index] }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result !== undefined) {
        this.selectedAttributeExamples[index] = result;
        this.temporarilyStoredExampleValues[`${this.selectedClass}_${this.selectedAttribute}`][index] = result;
        this.postExampleValue(index, result);
      }
    });
  }

  /**
   * Updates an example value in the backend by sending a POST request.
   * @param index The index of the example value that will be sent to the backend.
   * @param newValue The newly set example value that will be sent to the backend.
   */
  postExampleValue(index: number, newValue: string): void {
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

  linkExampleValues(): void {
    this.linkedExamples = [];
    this.currentView = 'linking';
  }

  createNewLink(): void {
    this.editingLink = { id: this.linkedExamples.length + 1 };
    this.currentView = 'attributes';
  }

  addLink(attribute: string, example: any): void {
    if (this.editingLink) {
      this.editingLink[attribute] = example;
      this.checkIfLinkComplete();
    }
  }

  checkIfLinkComplete(): void {
    if (this.editingLink && this.selectedClassAttributes.every(attr => this.editingLink![attr] !== undefined)) {
      this.linkedExamples.push(this.editingLink);
      this.editingLink = null;
      this.saveLinkedExamples();
    }
  }

  saveLinkedExamples(): void {
    const linkRequest = {
      className: this.selectedClass,
      links: this.linkedExamples
    };
    this.http.post('http://localhost:8080/linkExamples', linkRequest)
      .subscribe(() => {
        console.log('Linked examples saved successfully in the backend.');
      }, error => {
        console.error('Failed to save linked examples in the backend', error);
      });
  }

  /**
   * Updates the generated OpenAPI specification data with the new example value.
   * @param index The index of the example value.
   * @param newValue The newly set example value.
   */
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
