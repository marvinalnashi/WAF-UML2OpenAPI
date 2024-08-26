import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { GenerationService } from '../generation.service';
import { MatDialog } from '@angular/material/dialog';
import { AddElementDialogComponent } from '../add-element-dialog/add-element-dialog.component';
import { NgClass, NgForOf, NgIf } from '@angular/common';
import {NotificationService} from "../notification.service";
import {HttpClient} from "@angular/common/http";

/**
 * Component for processing added UML diagrams and managing UML mappings.
 */
@Component({
  selector: 'app-mapping',
  standalone: true,
  imports: [NgForOf, NgIf, NgClass, ReactiveFormsModule],
  templateUrl: './mapping.component.html',
  styleUrl: './mapping.component.scss',
  providers: [NotificationService]
})
export class MappingComponent implements OnInit {
  /**
   * Input property for processing the uploaded UML diagram file.
   */
  @Input() file: File | undefined;

  /**
   * Input property for storing the identified elements of the uploaded UML diagram file.
   */
  @Input() umlData: any = {
    classes: [],
    attributes: {},
    methods: {},
    relationships: []
  };

  /**
   * Output property for emitting the event to indicate whether the Mapping step of the stepper was completed successfully.
   */
  @Output() mappingCompleted = new EventEmitter<boolean>();

  /**
   * Output property for emitting the event to process which HTTP methods have been selected in the table of the Manage Elements tab of the Mapping step of the stepper.
   */
  @Output() httpMethodsSelected = new EventEmitter<{ [className: string]: { [method: string]: boolean } }>();

  /**
   * Output property for emitting the event to indicate that the counted amounts of elements have been updated.
   */
  @Output() elementCountUpdated = new EventEmitter<any>();

  /**
   * Form group that is used for the form in the Add Elements tab of the Mapping step of the stepper.
   */
  mappingsForm: FormGroup;

  /**
   * The HTTP methods that the user selects for one or more classes in the table of the Manage Elements tab of the Mapping step of the stepper.
   */
  selectedHttpMethods: { [className: string]: { [method: string]: boolean } } = {};

  /**
   * The currently selected tab in the Mapping step of the stepper.
   */
  selectedTab: string = 'add-elements';

  /**
   * Boolean that indicates whether the Apply Additions button is to be displayed for the user, based on the state of the Add Element form in the Add Elements tab of the Mapping step of the stepper.
   */
  showApplyAdditionsButton: boolean = false;

  /**
   * Boolean that manages the visibility of the Add Class button.
   */
  showAddClassButton = true;

  /**
   * Array to store the added elements for displaying in the Add Elements tab.
   */
  addedElements: any[] = [];

  classNames: string[] = [];

  /**
   * Creates an instance of MappingComponent.
   * @param fb The Form builder service.
   * @param generationService The Generation service.
   * @param dialog The Angular Material dialog service.
   * @param notificationService
   */
  constructor(
    private fb: FormBuilder,
    private generationService: GenerationService,
    public dialog: MatDialog,
    private notificationService: NotificationService,
  ) {
    this.mappingsForm = this.fb.group({
      mappings: this.fb.array([])
    });
  }

  /**
   * Executes the function that loads the data of the identified individual elements of the uploaded UML diagram after the Mapping step has been initialised.
   */
  ngOnInit(): void {
    this.loadUMLData();
  }

  /**
   * Loads the data of the identified individual elements of the uploaded UML diagram.
   */
  loadUMLData(): void {
    if (this.file) {
      this.generationService.parseDiagramElements(this.file).subscribe(data => {
        this.umlData = data;
        this.classNames = this.umlData.classes || [];
        this.initialiseHttpMethodSelection();
        this.updateElementCount();
      });
    }
  }

  /**
   * Gets the array of elements added in the Add Elements tab of the Mapping step of the stepper.
   * @returns The form array that contains the added elements. The amount of added elements can vary.
   */
  get elements(): FormArray {
    return this.mappingsForm.get('mappings') as FormArray;
  }

  // get relationships(): FormArray {
  //   return this.relationshipsForm.get('relationships') as FormArray;
  // }

  /**
   * Adds a new element to the form array that contains the elements added in the Add Elements tab of the Mapping step of the stepper.
   */
  addElement(): void {
    const newMapping = this.fb.group({
      className: ['', Validators.required],
      url: ['', Validators.required],
      methods: this.fb.array([]),
      attributes: this.fb.array([]),
    });
    this.elements.push(newMapping);
    this.showApplyAdditionsButton = true;
    this.showAddClassButton = false;
  }

  // addRelationship(): void {
  //   const newRelationship = this.fb.group({
  //     relationshipName: ['', Validators.required],
  //     relationshipType: ['', Validators.required],
  //     classFrom: ['', Validators.required],
  //     classTo: ['', Validators.required],
  //   });
  //   this.relationships.push(newRelationship);
  // }
  //
  // deleteRelationship(index: number): void {
  //   this.relationships.removeAt(index);
  // }
  //
  // applyRelationshipChanges(): void {
  //   if (this.relationshipsForm.valid) {
  //     this.generationService.applyRelationships(this.relationshipsForm.value.relationships).subscribe(
  //       () => {
  //         this.notificationService.showSuccess('Relationships applied successfully.');
  //       },
  //       (error) => {
  //         this.notificationService.showError('Failed to apply relationships.');
  //         console.error('Failed to apply relationships:', error);
  //       }
  //     );
  //   }
  // }

  /**
   * Activates the Add Element dialog for adding a new attribute or method in the Add Elements tab of the Mapping step of the stepper.
   * @param isMethod Boolean indicating if the element is a method.
   * @param elementIndex The index of the added element in the form array.
   */
  openAddElementDialog(isMethod: boolean, elementIndex: number): void {
    const dialogRef = this.dialog.open(AddElementDialogComponent, {
      width: '400px',
      data: { isMethod },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        const element = this.elements.at(elementIndex) as FormGroup;
        if (isMethod) {
          (element.get('methods') as FormArray).push(this.fb.control(result));
        } else {
          (element.get('attributes') as FormArray).push(this.fb.control(result));
        }
        this.updateElementCount();
        this.notificationService.showSuccess('The element has been added successfully.');
      }
    });
  }

  /**
   * Adds a new method to the added class and its path in the Add Element tab of the Mapping step of the stepper.
   * @param elementIndex The index of the added element in the form array.
   */
  addMethod(elementIndex: number): void {
    this.openAddElementDialog(true, elementIndex);
  }

  /**
   * Adds a new attribute to the added class and its path in the Add Element tab of the Mapping step of the stepper.
   * @param elementIndex The index of the added element in the form array.
   */
  addAttribute(elementIndex: number): void {
    this.openAddElementDialog(false, elementIndex);
  }

  /**
   * Applies the mappings and modifications done by the user in the Mapping step of the stepper.
   */
  applyModifications(): void {
    if (this.mappingsForm.valid) {
      this.generationService.applyMappings(this.mappingsForm.value.mappings).subscribe(() => {
        this.mappingCompleted.emit(true);
        this.httpMethodsSelected.emit(this.selectedHttpMethods);
        this.updateElementCount();
        this.notificationService.showSuccess('The modifications have been applied successfully.');
      });
    }
  }

  /**
   * Adds created elements to the table of the Manage Elements tab of the Mapping step of the stepper.
   */

  addNewClassToElements(): void {
    const newElements = this.mappingsForm.value.mappings.map((mapping: any) => ({
      className: mapping.className,
      url: mapping.url,
      attributes: mapping.attributes,
      methods: mapping.methods,
    }));

    newElements.forEach((element: any) => {
      this.generationService.addNewElement(element).subscribe(() => {
        this.umlData.classes.push(element.className);
        this.umlData.attributes[element.className] = element.attributes;
        this.umlData.methods[element.className] = element.methods;
        this.selectedHttpMethods[element.className] = {
          'GET/{id}': false,
          'POST': false,
          'PUT/{id}': false,
          'DELETE/{id}': false,
        };
        this.updateElementCount();
        this.addedElements.push(element);
        this.classNames = this.umlData.classes || [];
      });
    });

    this.mappingsForm.reset();
    while (this.elements.length) {
      this.elements.removeAt(0);
    }
    this.showApplyAdditionsButton = false;
    this.showAddClassButton = true;
    this.notificationService.showSuccess('The added element has been pushed to the UML element table successfully.');
  }

  /**
   * Removes an element from the table of the Manage Elements tab of the Mapping step of the stepper.
   * @param type The type of the added element. This can be a class, an attribute or a method.
   * @param name The name of the added element.
   * @param index The index of the added element.
   * @param className The name of the class an attribute or method is part of.
   */
  deleteElement(type: string, name: string, index: number, className: string): void {
    this.generationService.deleteElement(type, name).subscribe(() => {
      if (type === 'attribute') {
        this.umlData.attributes[className].splice(index, 1);
      } else if (type === 'method') {
        this.umlData.methods[className].splice(index, 1);
      } else if (type === 'class') {
        this.umlData.classes = this.umlData.classes.filter((c: string) => c !== className);
        delete this.umlData.attributes[className];
        delete this.umlData.methods[className];
      }
      this.updateElementCount();
      this.notificationService.showSuccess('The element has been removed successfully.');
      this.classNames = this.umlData.classes || [];
    });
  }

  /**
   * Activates the rename popup dialog in which a class, attribute or method can be renamed.
   * @param type The type of the added element. This can be a class, an attribute or a method.
   * @param oldName The current name of the added element.
   */
  openRenameDialog(type: string, oldName: string): void {
    let newName = prompt('Enter new name for ' + oldName);
    if (newName) {
      this.renameElement(type, oldName, newName);
    }
  }

  /**
   * Renames an added element by replacing its old name by a new name that the user specifies.
   * @param type The type of the added element. This can be a class, an attribute or a method.
   * @param oldName The current name of the added element.
   * @param newName The new name for the added element.
   */
  renameElement(type: string, oldName: string, newName: string): void {
    this.generationService.renameElement(type, oldName, newName).subscribe({
      next: () => {
        if (type === 'class') {
          const index = this.umlData.classes.indexOf(oldName);
          if (index !== -1) {
            this.umlData.classes[index] = newName;
          }
          this.umlData.attributes[newName] = [...this.umlData.attributes[oldName]];
          delete this.umlData.attributes[oldName];
          this.umlData.methods[newName] = [...this.umlData.methods[oldName]];
          delete this.umlData.methods[oldName];
        } else if (type === 'attribute') {
          const classForAttribute = this.getClassForAttribute(oldName);
          const attrIndex = this.umlData.attributes[classForAttribute].indexOf(oldName);
          if (attrIndex !== -1) {
            this.umlData.attributes[classForAttribute][attrIndex] = newName;
          }
        } else if (type === 'method') {
          const classForMethod = this.getClassForMethod(oldName);
          const methIndex = this.umlData.methods[classForMethod].indexOf(oldName);
          if (methIndex !== -1) {
            this.umlData.methods[classForMethod][methIndex] = newName;
          }
        }
        this.updateElementCount();
        this.notificationService.showSuccess('The element has been renamed successfully.');
        this.classNames = this.umlData.classes || [];
      },
      error: (error) => {
        this.notificationService.showError('The element could not be renamed.');
        console.error('Failed to rename element:', error);
      },
    });
  }

  /**
   * Gets the classname for a renamed attribute so that the renamed attribute's value can be propagated to its corresponding class.
   * @param attributeName The name of the renamed attribute.
   * @returns The name of the class the renamed attribute is part of.
   */
  getClassForAttribute(attributeName: string): string {
    return <string>Object.keys(this.umlData.attributes).find((className) =>
      this.umlData.attributes[className].includes(attributeName)
    );
  }

  /**
   * Gets the classname for a renamed method so that the renamed method's value can be propagated to its corresponding class.
   * @param methodName The name of the renamed method.
   * @returns The name of the class the renamed method is part of.
   */
  getClassForMethod(methodName: string): string {
    return <string>Object.keys(this.umlData.methods).find((className) =>
      this.umlData.methods[className].includes(methodName)
    );
  }

  /**
   * Sets the maximum amount of rows in the table of the Manage Elements tab of the Mapping step of the stepper for the attributes based on the class with the most attributes.
   * @param data The data of the identified individual elements of the uploaded UML diagram.
   * @returns An array representing the maximum number of rows that need to be allocated for the attributes in the table of the Manage Elements tab of the Mapping step of the stepper.
   */
  maxAttributeRows(data: any): number[] {
    let maxRows = 0;
    for (const className of data.classes) {
      const attributeCount = data.attributes[className]?.length || 0;
      if (attributeCount > maxRows) {
        maxRows = attributeCount;
      }
    }
    return [...Array(maxRows).keys()];
  }

  /**
   * Sets the maximum amount of rows in the table of the Manage Elements tab of the Mapping step of the stepper for the methods based on the class with the most methods.
   * @param data The data of the identified individual elements of the uploaded UML diagram.
   * @returns An array representing the maximum number of rows that need to be allocated for the methods in the table of the Manage Elements tab of the Mapping step of the stepper.
   */
  maxMethodRows(data: any): number[] {
    let maxRows = 0;
    for (const className of data.classes) {
      const methodCount = data.methods[className]?.length || 0;
      if (methodCount > maxRows) {
        maxRows = methodCount;
      }
    }
    return [...Array(maxRows).keys()];
  }

  /**
   * Sets the maximum amount of rows in the table of the Manage Elements tab of the Mapping step of the stepper for the HTTP methods to exactly 4.
   * @param data The data of the identified individual elements of the uploaded UML diagram.
   * @returns An array representing the maximum number of rows that need to be allocated for the HTTP methods in the table of the Manage Elements tab of the Mapping step of the stepper.
   */
  maxHttpMethodRows(data: any): number[] {
    return [0, 1, 2, 3];
  }

  /**
   * Gets the HTTP methods for a given class based on its classname.
   * @param className The name of the class in the table of the Manage Elements tab of the Mapping step of the stepper.
   * @returns An array that contains the HTTP methods for the class.
   */
  getHttpMethodsForClass(className: string) {
    return [
      { url: `/${className.toLowerCase()}/{id}`, method: 'GET' },
      { url: `/${className.toLowerCase()}`, method: 'POST' },
      { url: `/${className.toLowerCase()}/{id}`, method: 'PUT' },
      { url: `/${className.toLowerCase()}/{id}`, method: 'DELETE' },
    ];
  }

  /**
   * Initialises the HTTP method checkboxes for each class in the table of the Manage Elements tab of the Mapping step of the stepper.
   */
  initialiseHttpMethodSelection() {
    this.umlData.classes.forEach((className: string) => {
      this.selectedHttpMethods[className] = {
        'GET/{id}': false,
        'POST': false,
        'PUT/{id}': false,
        'DELETE/{id}': false,
      };
    });
  }

  /**
   * Handles the selection of HTTP methods based on whether a checkbox of an HTTP method is checked.
   * @param className The name of the class in the table of the Manage Elements tab of the Mapping step of the stepper.
   * @param method The HTTP method that the user selects for a class by checking its corresponding checkbox.
   */
  toggleHttpMethodSelection(className: string, method: string) {
    if (!this.selectedHttpMethods[className]) {
      this.selectedHttpMethods[className] = {};
    }
    this.selectedHttpMethods[className][method] = !this.selectedHttpMethods[className][method];
  }

  /**
   * Updates the count for the amounts of elements in the data of the identified individual elements of the uploaded UML diagram.
   */
  updateElementCount(): void {
    const count = {
      classes: this.umlData.classes.length,
      attributes: Object.values(this.umlData.attributes).reduce(
        (acc: number, attrs: unknown) => acc + (attrs as string[]).length,
        0
      ),
      methods: Object.values(this.umlData.methods).reduce(
        (acc: number, meths: unknown) => acc + (meths as string[]).length,
        0
      ),
      relationships: this.umlData.relationships ? this.umlData.relationships.length : 0,
    };
    this.elementCountUpdated.emit(count);
  }
}
